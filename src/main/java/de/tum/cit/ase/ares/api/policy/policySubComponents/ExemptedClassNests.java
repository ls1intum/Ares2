package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Works out which classes an exempted class's exemption covers: the class
 * itself and the classes declared inside it, as its own compiled file lists
 * them in its {@code NestMembers} attribute. A class merely named like a nested
 * class, such as a separate {@code PenguinTest$Evil}, is never covered.
 */
public final class ExemptedClassNests {

	/** Reports class files Ares could not read, so their nest stays unexempted. */
	private static final Logger LOG = LoggerFactory.getLogger(ExemptedClassNests.class);

	/** The largest class file read; a larger one counts as unreadable. */
	static final int MAX_CLASS_FILE_BYTES = 16 * 1024 * 1024;

	/** Prevents instances of this utility. */
	private ExemptedClassNests() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "ExemptedClassNests"));
	}

	/**
	 * Returns the exempted test classes plus the classes each one lists as declared
	 * inside it, read by path from the trusted test output only. A class missing
	 * there, or an unknown root, adds nothing; the exact name stays exempt.
	 *
	 * @param classNames     the exempted test classes' binary names
	 * @param testOutputRoot the trusted compiled test classes, or {@code null}
	 * @return the exempted names and their nest members, without repeats
	 */
	@Nonnull
	public static List<String> expandFromTestOutput(@Nonnull Collection<String> classNames,
			@Nullable Path testOutputRoot) {
		return expand(classNames, className -> readFromOutput(className, testOutputRoot), true);
	}

	/**
	 * Returns the exempted classes plus the classes each one lists as declared
	 * inside it, read through a loader that only sees trusted classes, such as
	 * Ares's own for its essential classes.
	 *
	 * @param classNames the exempted binary class names
	 * @param loader     the trusted loader, or {@code null} for none
	 * @return the exempted names and their nest members, without repeats
	 */
	@Nonnull
	public static List<String> expandThroughLoader(@Nonnull Collection<String> classNames,
			@Nullable ClassLoader loader) {
		return expand(classNames, className -> readThroughLoader(className, loader), false);
	}

	/**
	 * Returns the exempted classes plus their listed nest members, reading each
	 * class file with the given reader.
	 *
	 * @param classNames      the exempted binary class names
	 * @param reader          reads one class file, or yields empty
	 * @param warnWhenMissing whether a missing class is the instructor's to fix,
	 *                        rather than an entry of Ares's own configuration
	 * @return the exempted names and their nest members, without repeats
	 */
	@Nonnull
	private static List<String> expand(@Nonnull Collection<String> classNames,
			@Nonnull Function<String, Optional<byte[]>> reader, boolean warnWhenMissing) {
		Set<String> exempted = new LinkedHashSet<>();
		for (String className : classNames) {
			exempted.add(className);
			Optional<byte[]> classFile = reader.apply(className);
			if (classFile.isEmpty()) {
				reportMissing(className, warnWhenMissing);
				continue;
			}
			exempted.addAll(nestMembersOf(className, classFile.get()));
		}
		return List.copyOf(exempted);
	}

	/**
	 * Reports a class whose file Ares could not read: as a warning for a test
	 * class, which the instructor can compile, and quietly for an essential entry
	 * of Ares's own configuration, some of which name packages.
	 *
	 * @param className       the class that stays exempted by exact name only
	 * @param warnWhenMissing whether the instructor can fix it
	 */
	private static void reportMissing(@Nonnull String className, boolean warnWhenMissing) {
		if (warnWhenMissing) {
			LOG.warn("Ares could not read the compiled class {} from its trusted location, so only that exact "
					+ "class is exempted and the classes declared inside it are not. Compile the tests before "
					+ "generating or running.", className);
		} else {
			LOG.debug("Ares found no class file for the essential entry {}, so it adds no nested classes.", className);
		}
	}

	/**
	 * Returns the classes a class file lists as declared inside the named class,
	 * after checking the whole file. Any defect, a file for another class, or a
	 * class that is itself nested yields an empty list.
	 *
	 * @param className the binary name the file must declare
	 * @param classFile the class file bytes
	 * @return the listed nest members whose names start with {@code className$}
	 */
	@Nonnull
	public static List<String> nestMembersOf(@Nonnull String className, @Nonnull byte[] classFile) {
		try {
			return new ClassFileNest(classFile).membersOf(className);
		} catch (IOException | IllegalArgumentException malformed) {
			LOG.warn("Ares ignores the nest of {} because its class file is not usable: {}", className,
					malformed.getMessage());
			return List.of();
		}
	}

	/**
	 * Refuses a supervised production class named like an exempted class or one of
	 * its nest members, so a student cannot gain an exemption by such a name.
	 *
	 * @param productionOutputRoot the complete compiled production output, or
	 *                             {@code null} if unknown
	 * @param restrictedPackage    the prefix runtime checks treat as supervised
	 * @param exemptedClassNames   the expanded exempted names
	 * @throws SecurityException if a collision exists or the output is unreadable
	 */
	public static void requireNoProductionClassNamedLikeAnExemptedOne(@Nullable Path productionOutputRoot,
			@Nonnull String restrictedPackage, @Nonnull Collection<String> exemptedClassNames) {
		if (productionOutputRoot == null || !Files.isDirectory(productionOutputRoot)) {
			throw new SecurityException(
					Messages.localized("security.policy.exempted.class.output.unreadable", productionOutputRoot));
		}
		Set<String> exempted = Set.copyOf(exemptedClassNames);
		try (Stream<Path> files = Files.walk(productionOutputRoot)) {
			files.peek(file -> requireNoLink(productionOutputRoot, file))
					.filter(file -> file.toString().endsWith(".class"))
					.map(file -> binaryNameOf(productionOutputRoot, file))
					.filter(name -> name.startsWith(restrictedPackage) && exempted.contains(name)).findFirst()
					.ifPresent(name -> {
						throw new SecurityException(
								Messages.localized("security.policy.exempted.class.collision", name));
					});
		} catch (IOException | UncheckedIOException unreadable) {
			throw new SecurityException(
					Messages.localized("security.policy.exempted.class.output.unreadable", productionOutputRoot),
					unreadable);
		}
	}

	/**
	 * Refuses a link inside the production output, since the scan does not follow
	 * links and would otherwise miss the classes behind one.
	 *
	 * @param root the production output root
	 * @param file a path found below it
	 * @throws SecurityException if the path is a link
	 */
	private static void requireNoLink(@Nonnull Path root, @Nonnull Path file) {
		if (Files.isSymbolicLink(file)) {
			throw new SecurityException(Messages.localized("security.policy.exempted.class.output.unreadable", root));
		}
	}

	/**
	 * Turns a class file's path below an output root into the binary class name the
	 * JVM loads it by.
	 *
	 * @param root the output root
	 * @param file a class file below it
	 * @return the binary class name
	 */
	@Nonnull
	private static String binaryNameOf(@Nonnull Path root, @Nonnull Path file) {
		String relative = root.relativize(file).toString().replace('\\', '/');
		return relative.substring(0, relative.length() - ".class".length()).replace('/', '.');
	}

	/**
	 * Reads a class file by path from an output root, refusing anything larger than
	 * {@link #MAX_CLASS_FILE_BYTES}, outside the root, or reached through a link.
	 *
	 * @param className the binary class name
	 * @param root      the output root, or {@code null}
	 * @return the class file bytes, or empty if unavailable
	 */
	@Nonnull
	private static Optional<byte[]> readFromOutput(@Nonnull String className, @Nullable Path root) {
		if (root == null) {
			return Optional.empty();
		}
		Path base = root.toAbsolutePath().normalize();
		Path file = base.resolve(className.replace('.', '/') + ".class").normalize();
		if (!file.startsWith(base) || passesThroughLink(base, file)
				|| !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
			return Optional.empty();
		}
		try (InputStream input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
			return capped(input);
		} catch (IOException unreadable) {
			return Optional.empty();
		}
	}

	/**
	 * Tells whether any directory or file between an output root and a path below
	 * it is a symbolic link, which could lead outside the root.
	 *
	 * @param base the output root
	 * @param file a path below it
	 * @return {@code true} if the path passes through a link
	 */
	private static boolean passesThroughLink(@Nonnull Path base, @Nonnull Path file) {
		Path current = base;
		for (Path part : base.relativize(file)) {
			current = current.resolve(part);
			if (Files.isSymbolicLink(current)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reads a class file through a loader, refusing anything larger than
	 * {@link #MAX_CLASS_FILE_BYTES}.
	 *
	 * @param className the binary class name
	 * @param loader    the loader, or {@code null}
	 * @return the class file bytes, or empty if unavailable
	 */
	@Nonnull
	private static Optional<byte[]> readThroughLoader(@Nonnull String className, @Nullable ClassLoader loader) {
		if (loader == null) {
			return Optional.empty();
		}
		try (InputStream input = loader.getResourceAsStream(className.replace('.', '/') + ".class")) {
			return input == null ? Optional.empty() : capped(input);
		} catch (IOException unreadable) {
			return Optional.empty();
		}
	}

	/**
	 * Reads a stream completely unless it exceeds the size cap.
	 *
	 * @param input the stream to read
	 * @return its bytes, or empty if it is too large
	 * @throws IOException if reading fails
	 */
	@Nonnull
	private static Optional<byte[]> capped(@Nonnull InputStream input) throws IOException {
		byte[] bytes = input.readNBytes(MAX_CLASS_FILE_BYTES + 1);
		return bytes.length > MAX_CLASS_FILE_BYTES ? Optional.empty() : Optional.of(bytes);
	}

	/**
	 * A strict reader of the parts of a class file that name its nest: the constant
	 * pool, the declared class and the class-level attributes. It walks the whole
	 * file before answering, so a defect anywhere yields no members.
	 */
	private static final class ClassFileNest {

		/** The class file being read. */
		private final DataInputStream input;

		/** The class file's major version. */
		private int majorVersion;

		/** The constant-pool tag of each index, 0 for unusable slots. */
		private int[] tags;

		/** The text of each {@code Utf8} constant, by index. */
		private String[] texts;

		/** The name index of each {@code Class} constant, by index. */
		private int[] classNameIndices;

		/** The first index each constant refers to, by index, 0 for none. */
		private int[] firstReferences;

		/** The second index each constant refers to, by index, 0 for none. */
		private int[] secondReferences;

		/**
		 * Prepares to read the given class file.
		 *
		 * @param classFile the class file bytes
		 */
		private ClassFileNest(@Nonnull byte[] classFile) {
			this.input = new DataInputStream(new ByteArrayInputStream(classFile));
		}

		/**
		 * Reads the whole file and returns the nest members of the named class.
		 *
		 * @param className the binary name the file must declare
		 * @return the members starting with {@code className$}, or none
		 * @throws IOException if the file is truncated
		 */
		@Nonnull
		private List<String> membersOf(@Nonnull String className) throws IOException {
			if (input.readInt() != 0xCAFEBABE) {
				throw new IllegalArgumentException("not a class file");
			}
			input.readUnsignedShort();
			majorVersion = input.readUnsignedShort();
			readConstantPool();
			checkConstantReferences();
			input.readUnsignedShort();
			String declared = className(input.readUnsignedShort());
			if (!declared.equals(className)) {
				throw new IllegalArgumentException("the file declares " + declared);
			}
			int superclass = input.readUnsignedShort();
			if (superclass != 0) {
				className(superclass);
			}
			int interfaces = input.readUnsignedShort();
			for (int interfaceIndex = 0; interfaceIndex < interfaces; interfaceIndex++) {
				className(input.readUnsignedShort());
			}
			skipFieldsOrMethods();
			skipFieldsOrMethods();
			List<String> members = readClassAttributes();
			if (input.available() != 0) {
				throw new IllegalArgumentException("trailing bytes");
			}
			String prefix = className + "$";
			return members.stream().filter(member -> member.startsWith(prefix)).distinct().toList();
		}

		/**
		 * Reads and checks every constant-pool entry.
		 *
		 * @throws IOException if the file is truncated
		 */
		private void readConstantPool() throws IOException {
			int count = input.readUnsignedShort();
			if (count == 0) {
				throw new IllegalArgumentException("empty constant pool");
			}
			tags = new int[count];
			texts = new String[count];
			classNameIndices = new int[count];
			firstReferences = new int[count];
			secondReferences = new int[count];
			int index = 1;
			while (index < count) {
				int tag = input.readUnsignedByte();
				tags[index] = tag;
				index = readConstant(tag, index, count) + 1;
			}
		}

		/**
		 * Reads one constant-pool entry and returns the index it ended on, which is one
		 * further for the two-slot {@code Long} and {@code Double}.
		 *
		 * @param tag   the entry's tag
		 * @param index the entry's index
		 * @param count the constant-pool count
		 * @return the last index the entry occupies
		 * @throws IOException if the file is truncated
		 */
		private int readConstant(int tag, int index, int count) throws IOException {
			switch (tag) {
			case 1 -> texts[index] = input.readUTF();
			case 7 -> classNameIndices[index] = input.readUnsignedShort();
			case 8, 16, 19, 20 -> firstReferences[index] = input.readUnsignedShort();
			case 3, 4 -> input.readInt();
			case 9, 10, 11, 12, 17, 18 -> {
				firstReferences[index] = input.readUnsignedShort();
				secondReferences[index] = input.readUnsignedShort();
			}
			case 15 -> {
				firstReferences[index] = input.readUnsignedByte();
				secondReferences[index] = input.readUnsignedShort();
			}
			case 5, 6 -> {
				input.readLong();
				if (index + 1 >= count) {
					throw new IllegalArgumentException("a two-slot constant ends the pool");
				}
				return index + 1;
			}
			default -> throw new IllegalArgumentException("unknown constant tag " + tag);
			}
			return index;
		}

		/**
		 * Checks that every constant refers to constants of the kind it needs.
		 *
		 * @throws IllegalArgumentException if a reference is out of range or of the
		 *                                  wrong kind
		 */
		private void checkConstantReferences() {
			for (int index = 1; index < tags.length; index++) {
				switch (tags[index]) {
				case 7 -> text(classNameIndices[index]);
				case 8, 16, 19, 20 -> text(firstReferences[index]);
				case 9, 10, 11 -> {
					requireTag(firstReferences[index], 7);
					requireTag(secondReferences[index], 12);
				}
				case 12 -> {
					text(firstReferences[index]);
					text(secondReferences[index]);
				}
				case 15 -> checkMethodHandle(index);
				case 17, 18 -> requireTag(secondReferences[index], 12);
				default -> requireKnownOrUnusedSlot(index);
				}
			}
		}

		/**
		 * Checks a {@code MethodHandle} constant: its kind, the kind of member it
		 * refers to, and that member's name, as JVMS 4.4.8 requires.
		 *
		 * @param index the constant-pool index
		 */
		private void checkMethodHandle(int index) {
			int kind = firstReferences[index];
			int target = secondReferences[index];
			if (target <= 0 || target >= tags.length || !methodHandleMayReferTo(kind, tags[target])) {
				throw new IllegalArgumentException("malformed method handle at " + index);
			}
			requireTag(secondReferences[target], 12);
			String name = text(firstReferences[secondReferences[target]]);
			boolean constructor = "<init>".equals(name);
			if (kind == 8 ? !constructor : kind >= 5 && (constructor || "<clinit>".equals(name))) {
				throw new IllegalArgumentException("method handle names the wrong member at " + index);
			}
		}

		/**
		 * Tells whether a method handle of the given kind may refer to a constant with
		 * the given tag: a field for kinds 1 to 4, a class method for 5 and 8, either
		 * method for 6 and 7 from version 52 on, and an interface method for 9.
		 *
		 * @param kind the reference kind
		 * @param tag  the referred constant's tag
		 * @return {@code true} if the pairing is allowed
		 */
		private boolean methodHandleMayReferTo(int kind, int tag) {
			return switch (kind) {
			case 1, 2, 3, 4 -> tag == 9;
			case 5, 8 -> tag == 10;
			case 6, 7 -> tag == 10 || tag == 11 && majorVersion >= 52;
			case 9 -> tag == 11;
			default -> false;
			};
		}

		/**
		 * Accepts the constants that refer to nothing: texts, numbers, and the unused
		 * second slot of a {@code Long} or {@code Double}.
		 *
		 * @param index the constant-pool index
		 */
		private void requireKnownOrUnusedSlot(int index) {
			int tag = tags[index];
			if (tag != 0 && tag != 1 && (tag < 3 || tag > 6)) {
				throw new IllegalArgumentException("unknown constant tag " + tag);
			}
		}

		/**
		 * Requires an index to name a constant with the given tag.
		 *
		 * @param index the constant-pool index
		 * @param tag   the required tag
		 */
		private void requireTag(int index, int tag) {
			if (index <= 0 || index >= tags.length || tags[index] != tag) {
				throw new IllegalArgumentException("index " + index + " is not a constant with tag " + tag);
			}
		}

		/**
		 * Skips the fields or the methods, checking each attribute's name and length.
		 *
		 * @throws IOException if the file is truncated
		 */
		private void skipFieldsOrMethods() throws IOException {
			int count = input.readUnsignedShort();
			for (int member = 0; member < count; member++) {
				input.readUnsignedShort();
				text(input.readUnsignedShort());
				text(input.readUnsignedShort());
				int attributes = input.readUnsignedShort();
				for (int attribute = 0; attribute < attributes; attribute++) {
					text(input.readUnsignedShort());
					input.skipNBytes(attributeLength());
				}
			}
		}

		/**
		 * Reads the class-level attributes and returns the {@code NestMembers} entries,
		 * or none for a class without them or for a nested class.
		 *
		 * @return the binary names the class lists as its nest members
		 * @throws IOException if the file is truncated
		 */
		@Nonnull
		private List<String> readClassAttributes() throws IOException {
			List<String> members = null;
			boolean nested = false;
			int attributes = input.readUnsignedShort();
			for (int attribute = 0; attribute < attributes; attribute++) {
				String name = text(input.readUnsignedShort());
				long length = attributeLength();
				if ("NestMembers".equals(name)) {
					if (members != null) {
						throw new IllegalArgumentException("two NestMembers attributes");
					}
					members = readNestMembers(length);
				} else if ("NestHost".equals(name)) {
					if (nested || length != 2) {
						throw new IllegalArgumentException("malformed NestHost");
					}
					className(input.readUnsignedShort());
					nested = true;
				} else {
					input.skipNBytes(length);
				}
			}
			if (nested && members != null) {
				throw new IllegalArgumentException("both NestHost and NestMembers");
			}
			return nested || members == null ? List.of() : members;
		}

		/**
		 * Reads a {@code NestMembers} attribute body of the given length.
		 *
		 * @param length the attribute length
		 * @return the listed binary class names
		 * @throws IOException if the file is truncated
		 */
		@Nonnull
		private List<String> readNestMembers(long length) throws IOException {
			int count = input.readUnsignedShort();
			if (length != 2L + 2L * count) {
				throw new IllegalArgumentException("NestMembers length does not match its entries");
			}
			List<String> members = new ArrayList<>(count);
			for (int member = 0; member < count; member++) {
				members.add(className(input.readUnsignedShort()));
			}
			return members;
		}

		/**
		 * Reads an attribute length and checks that the file still holds that many
		 * bytes.
		 *
		 * @return the length
		 * @throws IOException if the file is truncated
		 */
		private long attributeLength() throws IOException {
			long length = Integer.toUnsignedLong(input.readInt());
			if (length > input.available()) {
				throw new IllegalArgumentException("an attribute runs past the end of the file");
			}
			return length;
		}

		/**
		 * Returns the binary class name a {@code Class} constant names.
		 *
		 * @param index the constant-pool index
		 * @return the dotted binary name
		 */
		@Nonnull
		private String className(int index) {
			if (index <= 0 || index >= tags.length || tags[index] != 7) {
				throw new IllegalArgumentException("index " + index + " is not a class constant");
			}
			String internal = text(classNameIndices[index]);
			if (internal.isEmpty() || internal.startsWith("[") || internal.indexOf('.') >= 0) {
				throw new IllegalArgumentException("malformed class name " + internal);
			}
			return internal.replace('/', '.');
		}

		/**
		 * Returns the text of a {@code Utf8} constant.
		 *
		 * @param index the constant-pool index
		 * @return the text
		 */
		@Nonnull
		private String text(int index) {
			if (index <= 0 || index >= tags.length || tags[index] != 1) {
				throw new IllegalArgumentException("index " + index + " is not a text constant");
			}
			return texts[index];
		}
	}
}
