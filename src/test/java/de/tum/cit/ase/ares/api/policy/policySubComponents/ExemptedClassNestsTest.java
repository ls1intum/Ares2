package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import example.nest.NestHostFixture;

/**
 * Checks that an exemption extends exactly to the classes an exempted class's
 * own compiled file lists as declared inside it, and that a defective or
 * foreign class file extends it to nothing.
 */
class ExemptedClassNestsTest {

	/** The host whose nest the tests read. */
	private static final String HOST = NestHostFixture.class.getName();

	/** The loader that sees the compiled fixtures. */
	private static final ClassLoader LOADER = ExemptedClassNestsTest.class.getClassLoader();

	/**
	 * Every kind of class declared inside the host is listed, at any depth, and a
	 * separate top-level class named like a nested one is not.
	 */
	@Test
	void theNestListsEveryClassDeclaredInsideAndNothingElse() throws Exception {
		Set<String> expanded = Set.copyOf(ExemptedClassNests.expandThroughLoader(List.of(HOST), LOADER));
		Set<String> jvmView = Arrays.stream(NestHostFixture.class.getNestMembers()).map(Class::getName)
				.collect(Collectors.toSet());
		assertEquals(jvmView, expanded);
		for (Class<?> declared : List.of(NestHostFixture.anonymous().getClass(), NestHostFixture.local().getClass(),
				NestHostFixture.IN_INITIALISER.getClass(), NestHostFixture.Member.Deeper.class,
				NestHostFixture.Kind.SPECIAL.getClass(), NestHostFixture.Pair.class)) {
			assertTrue(expanded.contains(declared.getName()), declared.getName() + " in " + expanded);
		}
		assertNotNull(Class.forName("example.nest.NestHostFixture$Evil"));
		assertFalse(expanded.contains("example.nest.NestHostFixture$Evil"), expanded.toString());
	}

	/** The trusted test output is read by path, without any loader. */
	@Test
	void theTestOutputIsReadByPath(@TempDir Path testOutput) throws Exception {
		Path file = testOutput.resolve("example/nest/NestHostFixture.class");
		Files.createDirectories(file.getParent());
		Files.write(file, classFile(HOST));
		assertEquals(ExemptedClassNests.expandThroughLoader(List.of(HOST), LOADER),
				ExemptedClassNests.expandFromTestOutput(List.of(HOST), testOutput));
	}

	/**
	 * A test class missing from the trusted test output is refused, even though a
	 * loader could find a class of that name elsewhere, for example in the
	 * student's output, and so is any test class when the root is unknown.
	 */
	@Test
	void aTestClassMissingFromTheTestOutputIsRefused(@TempDir Path testOutput) {
		SecurityException missing = assertThrows(SecurityException.class,
				() -> ExemptedClassNests.expandFromTestOutput(List.of(HOST), testOutput));
		assertTrue(missing.getMessage().contains(HOST), missing.getMessage());
		assertTrue(missing.getMessage().contains(testOutput.toString()), missing.getMessage());
		SecurityException unknownRoot = assertThrows(SecurityException.class,
				() -> ExemptedClassNests.expandFromTestOutput(List.of(HOST), null));
		assertTrue(unknownRoot.getMessage().contains(HOST), unknownRoot.getMessage());
	}

	/** An empty list verifies nothing, so it passes without any test output. */
	@Test
	void anEmptyListNeedsNoTestOutput() {
		assertEquals(List.of(), ExemptedClassNests.expandFromTestOutput(List.of(), null));
	}

	/**
	 * A class file that declares another class, is cut short or carries trailing
	 * bytes is refused, with the reader's complaint kept as the cause.
	 */
	@Test
	void anUnusableTestClassFileIsRefused(@TempDir Path testOutput) throws Exception {
		byte[] valid = classFile(HOST);
		for (byte[] unusable : List.of(classFile("example.nest.NestHostFixture$Evil"),
				Arrays.copyOf(valid, valid.length - 1), Arrays.copyOf(valid, valid.length + 1))) {
			write(testOutput, HOST, unusable);
			SecurityException refused = assertThrows(SecurityException.class,
					() -> ExemptedClassNests.expandFromTestOutput(List.of(HOST), testOutput));
			assertNotNull(refused.getCause(), refused.getMessage());
		}
	}

	/**
	 * An ordinary test class without nested classes, and an explicitly listed
	 * nested test class, pass and stay exempted by their exact names.
	 */
	@Test
	void classesWithoutNestMembersPass(@TempDir Path testOutput) throws Exception {
		String ordinary = "example.nest.NestHostFixture$Evil";
		String member = NestHostFixture.Member.class.getName();
		write(testOutput, ordinary, classFile(ordinary));
		write(testOutput, member, classFile(member));
		assertEquals(List.of(ordinary, member),
				ExemptedClassNests.expandFromTestOutput(List.of(ordinary, member), testOutput));
	}

	/** The refusal reads in both English and German. */
	@Test
	void theRefusalIsLocalised(@TempDir Path testOutput) {
		Locale previous = Locale.getDefault();
		try {
			Locale.setDefault(Locale.ENGLISH);
			assertTrue(refusalFor(testOutput).startsWith("Ares Security Error"));
			Locale.setDefault(Locale.GERMAN);
			assertTrue(refusalFor(testOutput).startsWith("Ares Sicherheitsfehler"));
		} finally {
			Locale.setDefault(previous);
		}
	}

	/**
	 * A test class reached through a linked package directory or a linked class
	 * file is refused, since the link may lead outside the trusted test output.
	 */
	@Test
	void aLinkInTheTestOutputIsRefused(@TempDir Path testOutput, @TempDir Path elsewhere) throws Exception {
		Path real = elsewhere.resolve("nest/NestHostFixture.class");
		Files.createDirectories(real.getParent());
		Files.write(real, classFile(HOST));
		Path linkedDirectory = testOutput.resolve("directory");
		Path linkedFile = testOutput.resolve("file/example/nest/NestHostFixture.class");
		Files.createDirectories(linkedDirectory.resolve("example"));
		Files.createDirectories(linkedFile.getParent());
		try {
			Files.createSymbolicLink(linkedDirectory.resolve("example/nest"), real.getParent());
			Files.createSymbolicLink(linkedFile, real);
		} catch (UnsupportedOperationException | IOException unsupported) {
			org.junit.jupiter.api.Assumptions.abort("Symbolic links are unavailable here: " + unsupported);
		}
		assertThrows(SecurityException.class,
				() -> ExemptedClassNests.expandFromTestOutput(List.of(HOST), linkedDirectory));
		assertThrows(SecurityException.class,
				() -> ExemptedClassNests.expandFromTestOutput(List.of(HOST), testOutput.resolve("file")));
	}

	/**
	 * A link inside the production output fails the scan, which does not follow
	 * links.
	 */
	@Test
	void aLinkInTheProductionOutputFailsTheScan(@TempDir Path production, @TempDir Path elsewhere) throws Exception {
		Files.createDirectories(production.resolve("example"));
		try {
			Files.createSymbolicLink(production.resolve("example/nest"), elsewhere);
		} catch (UnsupportedOperationException | IOException unsupported) {
			org.junit.jupiter.api.Assumptions.abort("Symbolic links are unavailable here: " + unsupported);
		}
		assertThrows(SecurityException.class, () -> ExemptedClassNests
				.requireNoProductionClassNamedLikeAnExemptedOne(production, "example.nest", List.of(HOST)));
	}

	/**
	 * An exempted class that is itself nested keeps only its exact name; its own
	 * descendants are not added.
	 */
	@Test
	void aNestedExemptedClassAddsNoDescendants() {
		String member = NestHostFixture.Member.class.getName();
		assertEquals(List.of(member), ExemptedClassNests.expandThroughLoader(List.of(member), LOADER));
	}

	/** A class Ares cannot find keeps only its exact name. */
	@Test
	void aMissingClassKeepsOnlyItsExactName() {
		assertEquals(List.of("example.nest.Missing"),
				ExemptedClassNests.expandThroughLoader(List.of("example.nest.Missing"), LOADER));
	}

	/** A file declaring another class grants nothing to the requested name. */
	@Test
	void aFileForAnotherClassGrantsNothing() throws Exception {
		assertEquals(List.of(), ExemptedClassNests.nestMembersOf("example.nest.Other", classFile(HOST)));
	}

	/** A defect anywhere, even after a valid listing, grants nothing. */
	@Test
	void aDefectAfterAValidListingGrantsNothing() throws Exception {
		byte[] valid = classFile(HOST);
		assertFalse(ExemptedClassNests.nestMembersOf(HOST, valid).isEmpty());
		byte[] trailing = Arrays.copyOf(valid, valid.length + 1);
		assertEquals(List.of(), ExemptedClassNests.nestMembersOf(HOST, trailing));
		assertEquals(List.of(), ExemptedClassNests.nestMembersOf(HOST, Arrays.copyOf(valid, valid.length - 1)));
		byte[] wrongMagic = valid.clone();
		wrongMagic[0] = 0;
		assertEquals(List.of(), ExemptedClassNests.nestMembersOf(HOST, wrongMagic));
	}

	/**
	 * A minimal well-formed class file lists its members, the control for the cases
	 * below.
	 */
	@Test
	void aMinimalClassFileListsItsMembers() throws Exception {
		assertEquals(List.of("x.T$A"), ExemptedClassNests.nestMembersOf("x.T", minimal(Variant.VALID)));
	}

	/**
	 * Method handles of a kind that matches the member they refer to are accepted,
	 * the control for the malformed handles below.
	 */
	@Test
	void wellFormedMethodHandlesAreAccepted() throws Exception {
		for (Variant variant : List.of(Variant.FIELD_HANDLE, Variant.CONSTRUCTOR_HANDLE,
				Variant.VIRTUAL_HANDLE_TO_METHOD, Variant.SPECIAL_HANDLE_TO_METHOD,
				Variant.SPECIAL_HANDLE_TO_INTERFACE_METHOD_FROM_52)) {
			assertEquals(List.of("x.T$A"), ExemptedClassNests.nestMembersOf("x.T", minimal(variant)), variant.name());
		}
	}

	/**
	 * Number constants, including the two-slot {@code Long} and {@code Double}, and
	 * a declared interface are accepted, the control for the defects below.
	 */
	@Test
	void wellFormedConstantsAndInterfacesAreAccepted() throws Exception {
		for (Variant variant : List.of(Variant.LONG_CONSTANT, Variant.DOUBLE_CONSTANT, Variant.INTEGER_CONSTANT,
				Variant.WITH_INTERFACE)) {
			assertEquals(List.of("x.T$A"), ExemptedClassNests.nestMembersOf("x.T", minimal(variant)), variant.name());
		}
	}

	/** Malformed class files grant nothing. */
	@Test
	void malformedAttributesGrantNothing() throws Exception {
		for (Variant variant : List.of(Variant.TWO_NEST_MEMBERS, Variant.WRONG_LENGTH, Variant.NEST_HOST_TOO,
				Variant.MEMBER_IS_TEXT, Variant.UNKNOWN_TAG, Variant.DANGLING_REFERENCE, Variant.METHOD_HANDLE_TO_FIELD,
				Variant.VIRTUAL_HANDLE_TO_CONSTRUCTOR, Variant.INTERFACE_HANDLE_TO_CLASS_METHOD, Variant.LONG_ENDS_POOL,
				Variant.FIELDREF_CLASS_IS_TEXT, Variant.FIELDREF_NAME_AND_TYPE_IS_TEXT,
				Variant.INVOKEDYNAMIC_NAME_AND_TYPE_IS_TEXT, Variant.SPECIAL_HANDLE_TO_INTERFACE_METHOD_BEFORE_52,
				Variant.SPECIAL_HANDLE_TO_FIELD, Variant.CLASS_INITIALISER_HANDLE, Variant.SUPERCLASS_IS_TEXT,
				Variant.SUPERCLASS_NAME_STARTS_WITH_DOT, Variant.INDEX_PAST_POOL_FOR_TEXT,
				Variant.INDEX_PAST_POOL_FOR_TAG, Variant.INDEX_PAST_POOL_FOR_HANDLE, Variant.INDEX_PAST_POOL_FOR_CLASS,
				Variant.HANDLE_BEFORE_ITS_TARGET, Variant.EMPTY_CONSTANT_POOL, Variant.HANDLE_KIND_ZERO,
				Variant.HANDLE_KIND_TEN)) {
			assertEquals(List.of(), ExemptedClassNests.nestMembersOf("x.T", minimal(variant)), variant.name());
		}
	}

	/**
	 * A nest listing a class from outside the exempted class, beside a real member,
	 * extends the exemption to the real member only.
	 */
	@Test
	void aNestListingAForeignClassGrantsNothingForIt() throws Exception {
		assertEquals(List.of("x.T$A"), ExemptedClassNests.nestMembersOf("x.T", minimal(Variant.FOREIGN_MEMBER)));
	}

	/** A test class file of exactly the size limit is still read and expanded. */
	@Test
	void aClassFileOfExactlyTheCapIsRead(@TempDir Path testOutput) throws Exception {
		write(testOutput, "x.T", padded(ExemptedClassNests.MAX_CLASS_FILE_BYTES));
		assertEquals(List.of("x.T", "x.T$A"), ExemptedClassNests.expandFromTestOutput(List.of("x.T"), testOutput));
	}

	/**
	 * A test class file one byte over the size limit is refused, although it is
	 * otherwise well formed.
	 */
	@Test
	void aClassFileOneByteOverTheCapIsRefused(@TempDir Path testOutput) throws Exception {
		write(testOutput, "x.T", padded(ExemptedClassNests.MAX_CLASS_FILE_BYTES + 1));
		assertThrows(SecurityException.class,
				() -> ExemptedClassNests.expandFromTestOutput(List.of("x.T"), testOutput));
	}

	/**
	 * A production class named like an exempted one in the supervised scope is
	 * refused.
	 */
	@Test
	void aProductionClassNamedLikeAnExemptedOneIsRefused(@TempDir Path production) throws Exception {
		Path spoof = production.resolve("example/nest/NestHostFixture$1.class");
		Files.createDirectories(spoof.getParent());
		Files.write(spoof, new byte[] { 1 });
		List<String> exempted = ExemptedClassNests.expandThroughLoader(List.of(HOST), LOADER);
		SecurityException refused = assertThrows(SecurityException.class, () -> ExemptedClassNests
				.requireNoProductionClassNamedLikeAnExemptedOne(production, "example.nest", exempted));
		assertTrue(refused.getMessage().contains("example.nest.NestHostFixture$1"), refused.getMessage());
	}

	/**
	 * The scan uses the same raw prefix as the runtime checks, so a sibling package
	 * those checks supervise is covered, and a class outside it is not.
	 */
	@Test
	void theScanCoversTheRuntimeScopeOnly(@TempDir Path production) throws Exception {
		Path sibling = production.resolve("example/nestother/Helper$1.class");
		Files.createDirectories(sibling.getParent());
		Files.write(sibling, new byte[] { 1 });
		List<String> exempted = List.of("example.nestother.Helper$1");
		assertThrows(SecurityException.class, () -> ExemptedClassNests
				.requireNoProductionClassNamedLikeAnExemptedOne(production, "example.nest", exempted));
		ExemptedClassNests.requireNoProductionClassNamedLikeAnExemptedOne(production, "other", exempted);
	}

	/** Unrelated production classes pass, and missing output fails closed. */
	@Test
	void unrelatedClassesPassAndMissingOutputFailsClosed(@TempDir Path production) throws Exception {
		Path ordinary = production.resolve("example/nest/Penguin.class");
		Files.createDirectories(ordinary.getParent());
		Files.write(ordinary, new byte[] { 1 });
		ExemptedClassNests.requireNoProductionClassNamedLikeAnExemptedOne(production, "example.nest",
				ExemptedClassNests.expandThroughLoader(List.of(HOST), LOADER));
		assertThrows(SecurityException.class,
				() -> ExemptedClassNests.requireNoProductionClassNamedLikeAnExemptedOne(production.resolve("absent"),
						"example.nest", List.of(HOST)));
	}

	/** Without a loader, only the exact name of an exempted class is kept. */
	@Test
	void withoutALoaderOnlyTheExactNameIsKept() {
		assertEquals(List.of(HOST), ExemptedClassNests.expandThroughLoader(List.of(HOST), null));
	}

	/**
	 * A class file the loader finds but cannot read keeps only the exact name of
	 * the exempted class, without any exception.
	 */
	@Test
	void aClassFileTheLoaderCannotReadKeepsOnlyItsExactName() {
		assertEquals(List.of(HOST), ExemptedClassNests.expandThroughLoader(List.of(HOST), new UnreadableLoader()));
	}

	/**
	 * A hand-built class that is itself nested passes by its exact name and lists
	 * no members, the control for the malformed nest hosts below.
	 */
	@Test
	void aHandBuiltNestedClassPassesByItsExactName(@TempDir Path testOutput) throws Exception {
		write(testOutput, "x.T$A", minimal(Variant.NESTED));
		assertEquals(List.of("x.T$A"), ExemptedClassNests.expandFromTestOutput(List.of("x.T$A"), testOutput));
		assertEquals(List.of(), ExemptedClassNests.nestMembersOf("x.T$A", minimal(Variant.NESTED)));
	}

	/**
	 * A nested class with two NestHost attributes, or with one whose length is not
	 * two, is refused, with the reader's complaint kept as the cause.
	 */
	@Test
	void aMalformedNestHostIsRefused(@TempDir Path testOutput) throws Exception {
		for (Variant variant : List.of(Variant.TWO_NEST_HOSTS, Variant.NEST_HOST_WRONG_LENGTH)) {
			write(testOutput, "x.T$A", minimal(variant));
			SecurityException refused = assertThrows(SecurityException.class,
					() -> ExemptedClassNests.expandFromTestOutput(List.of("x.T$A"), testOutput), variant.name());
			assertNotNull(refused.getCause(), variant.name());
		}
	}

	/** Reads a compiled fixture's bytes. */
	private static byte[] classFile(String className) throws IOException {
		try (InputStream input = LOADER.getResourceAsStream(className.replace('.', '/') + ".class")) {
			return input.readAllBytes();
		}
	}

	/** Writes class file bytes where the test output keeps the named class. */
	private static void write(Path testOutput, String className, byte[] bytes) throws IOException {
		Path file = testOutput.resolve(className.replace('.', '/') + ".class");
		Files.createDirectories(file.getParent());
		Files.write(file, bytes);
	}

	/** Returns the refusal message for the host missing from the given output. */
	private static String refusalFor(Path testOutput) {
		return assertThrows(SecurityException.class,
				() -> ExemptedClassNests.expandFromTestOutput(List.of(HOST), testOutput)).getMessage();
	}

	/** The well-formed and defective shapes a hand-built class file can take. */
	private enum Variant {
		/** A well-formed file. */
		VALID,
		/** Two NestMembers attributes. */
		TWO_NEST_MEMBERS,
		/** A NestMembers length that does not match its entries. */
		WRONG_LENGTH,
		/** NestMembers together with NestHost. */
		NEST_HOST_TOO,
		/** A member index pointing at a text instead of a class constant. */
		MEMBER_IS_TEXT,
		/** A constant with an unknown tag. */
		UNKNOWN_TAG,
		/** A string constant referring to index 0, beside a valid listing. */
		DANGLING_REFERENCE,
		/** A field-reading handle to a field. */
		FIELD_HANDLE,
		/** A constructor handle to a method named {@code <init>}. */
		CONSTRUCTOR_HANDLE,
		/** A virtual-method handle to a field. */
		METHOD_HANDLE_TO_FIELD,
		/** A virtual-method handle to a method named {@code <init>}. */
		VIRTUAL_HANDLE_TO_CONSTRUCTOR,
		/** An interface-method handle to a class method. */
		INTERFACE_HANDLE_TO_CLASS_METHOD,
		/** A {@code Long} constant, which takes two slots of the constant pool. */
		LONG_CONSTANT,
		/** A {@code Double} constant, which takes two slots of the constant pool. */
		DOUBLE_CONSTANT,
		/** An {@code Integer} constant. */
		INTEGER_CONSTANT,
		/** A class that declares the interface {@code java.lang.Runnable}. */
		WITH_INTERFACE,
		/** A virtual-method handle to an ordinary method. */
		VIRTUAL_HANDLE_TO_METHOD,
		/** A special-method handle to an ordinary class method. */
		SPECIAL_HANDLE_TO_METHOD,
		/**
		 * A special-method handle to the interface method {@code Runnable.run} in a
		 * version 52 file, the first version that allows it.
		 */
		SPECIAL_HANDLE_TO_INTERFACE_METHOD_FROM_52,
		/**
		 * A {@code Long} constant in the last slot, with no room for its second slot.
		 */
		LONG_ENDS_POOL,
		/** A field reference whose class index points at a text. */
		FIELDREF_CLASS_IS_TEXT,
		/** A field reference whose name-and-type index points at a text. */
		FIELDREF_NAME_AND_TYPE_IS_TEXT,
		/** An invokedynamic constant whose name-and-type index points at a text. */
		INVOKEDYNAMIC_NAME_AND_TYPE_IS_TEXT,
		/**
		 * A special-method handle to the interface method {@code Runnable.run} in a
		 * version 51 file, which does not allow it.
		 */
		SPECIAL_HANDLE_TO_INTERFACE_METHOD_BEFORE_52,
		/** A special-method handle to a field. */
		SPECIAL_HANDLE_TO_FIELD,
		/** A virtual-method handle to the class initialiser {@code <clinit>}. */
		CLASS_INITIALISER_HANDLE,
		/** A superclass index pointing at a text instead of a class constant. */
		SUPERCLASS_IS_TEXT,
		/** A superclass whose name starts with a dot. */
		SUPERCLASS_NAME_STARTS_WITH_DOT,
		/** A string constant referring to the index just past the constant pool. */
		INDEX_PAST_POOL_FOR_TEXT,
		/** A field reference whose name-and-type index is just past the pool. */
		INDEX_PAST_POOL_FOR_TAG,
		/** A method handle whose target is just past the constant pool. */
		INDEX_PAST_POOL_FOR_HANDLE,
		/** A superclass index just past the constant pool. */
		INDEX_PAST_POOL_FOR_CLASS,
		/**
		 * A method handle placed before the field reference it targets, whose
		 * name-and-type index is past the constant pool.
		 */
		HANDLE_BEFORE_ITS_TARGET,
		/** A nest listing {@code x.T$A} and the unrelated class {@code y.Other}. */
		FOREIGN_MEMBER,
		/**
		 * A well-formed file ending in an empty extra attribute named {@code Padding}.
		 */
		PADDED,
		/** The {@code VALID} file with its constant-pool count written as 0. */
		EMPTY_CONSTANT_POOL,
		/** A field handle of kind 0, a kind no method handle may have. */
		HANDLE_KIND_ZERO,
		/** A field handle of kind 10, a kind no method handle may have. */
		HANDLE_KIND_TEN,
		/** A class {@code x.T$A} with one NestHost attribute naming {@code x.T}. */
		NESTED,
		/** A class {@code x.T$A} with two NestHost attributes naming {@code x.T}. */
		TWO_NEST_HOSTS,
		/** A class {@code x.T$A} with a NestHost attribute of length 4. */
		NEST_HOST_WRONG_LENGTH
	}

	/**
	 * A loader without a parent that returns, for every name, a stream that fails
	 * when it is read.
	 */
	private static final class UnreadableLoader extends ClassLoader {

		/** Creates the loader without a parent. */
		private UnreadableLoader() {
			super(null);
		}

		/** Returns a stream that fails on every read, whatever the name. */
		@Override
		public InputStream getResourceAsStream(String name) {
			return new FailingStream();
		}
	}

	/** A stream whose reads always fail. */
	private static final class FailingStream extends InputStream {

		/** Always fails with an {@link IOException}. */
		@Override
		public int read() throws IOException {
			throw new IOException("unreadable");
		}
	}

	/**
	 * Builds a minimal class file for {@code x.T} whose nest lists {@code x.T$A},
	 * or, for the three NestHost variants, one for {@code x.T$A} that names
	 * {@code x.T} as its host and lists no members, with the version, superclass,
	 * interface, extra constants and attributes the variant asks for.
	 */
	private static byte[] minimal(Variant variant) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.writeInt(0xCAFEBABE);
		out.writeShort(0);
		out.writeShort(majorVersionOf(variant));
		ByteArrayOutputStream extraBytes = new ByteArrayOutputStream();
		int extraSlots = writeExtraConstants(new DataOutputStream(extraBytes), variant);
		out.writeShort(variant == Variant.EMPTY_CONSTANT_POOL ? 0 : 9 + extraSlots);
		out.writeByte(1);
		out.writeUTF("x/T");
		out.writeByte(7);
		out.writeShort(1);
		out.writeByte(1);
		out.writeUTF("x/T$A");
		out.writeByte(7);
		out.writeShort(3);
		out.writeByte(1);
		out.writeUTF("NestMembers");
		out.writeByte(1);
		out.writeUTF("java/lang/Object");
		out.writeByte(7);
		out.writeShort(6);
		out.writeByte(1);
		out.writeUTF("NestHost");
		out.write(extraBytes.toByteArray());
		out.writeShort(0x21);
		out.writeShort(thisClassOf(variant));
		out.writeShort(superclassOf(variant));
		out.writeShort(variant == Variant.WITH_INTERFACE ? 1 : 0);
		if (variant == Variant.WITH_INTERFACE) {
			out.writeShort(10);
		}
		out.writeShort(0);
		out.writeShort(0);
		int attributes = variant == Variant.TWO_NEST_MEMBERS || variant == Variant.NEST_HOST_TOO
				|| variant == Variant.PADDED || variant == Variant.TWO_NEST_HOSTS ? 2 : 1;
		out.writeShort(attributes);
		if (isNestHostVariant(variant)) {
			writeNestHost(out, variant);
			if (variant == Variant.TWO_NEST_HOSTS) {
				writeNestHost(out, variant);
			}
			out.flush();
			return bytes.toByteArray();
		}
		writeNestMembers(out, variant);
		if (variant == Variant.TWO_NEST_MEMBERS) {
			writeNestMembers(out, Variant.VALID);
		}
		if (variant == Variant.NEST_HOST_TOO) {
			out.writeShort(8);
			out.writeInt(2);
			out.writeShort(2);
		}
		if (variant == Variant.PADDED) {
			out.writeShort(9);
			out.writeInt(0);
		}
		out.flush();
		return bytes.toByteArray();
	}

	/**
	 * Returns the class file version a variant declares: 52 or 51 for the two
	 * variants that test the version rule, 61 otherwise.
	 */
	private static int majorVersionOf(Variant variant) {
		return switch (variant) {
		case SPECIAL_HANDLE_TO_INTERFACE_METHOD_FROM_52 -> 52;
		case SPECIAL_HANDLE_TO_INTERFACE_METHOD_BEFORE_52 -> 51;
		default -> 61;
		};
	}

	/**
	 * Returns the constant-pool index a variant names as its own class: 4, the
	 * class constant for {@code x.T$A}, for the three NestHost variants, and 2, the
	 * class constant for {@code x.T}, otherwise.
	 */
	private static int thisClassOf(Variant variant) {
		return isNestHostVariant(variant) ? 4 : 2;
	}

	/** Tells whether a variant is a nested class with NestHost attributes. */
	private static boolean isNestHostVariant(Variant variant) {
		return variant == Variant.NESTED || variant == Variant.TWO_NEST_HOSTS
				|| variant == Variant.NEST_HOST_WRONG_LENGTH;
	}

	/**
	 * Writes one NestHost attribute naming {@code x.T}, with a length of 4 and two
	 * extra bytes for {@link Variant#NEST_HOST_WRONG_LENGTH}, and of 2 otherwise.
	 */
	private static void writeNestHost(DataOutputStream out, Variant variant) throws IOException {
		out.writeShort(8);
		out.writeInt(variant == Variant.NEST_HOST_WRONG_LENGTH ? 4 : 2);
		out.writeShort(2);
		if (variant == Variant.NEST_HOST_WRONG_LENGTH) {
			out.writeShort(0);
		}
	}

	/**
	 * Returns the constant-pool index a variant names as the superclass, normally
	 * 7, the class constant for {@code java.lang.Object}.
	 */
	private static int superclassOf(Variant variant) {
		return switch (variant) {
		case SUPERCLASS_IS_TEXT -> 1;
		case INDEX_PAST_POOL_FOR_CLASS -> 9;
		case SUPERCLASS_NAME_STARTS_WITH_DOT -> 10;
		default -> 7;
		};
	}

	/**
	 * Builds the {@link Variant#PADDED} file and fills its padding attribute with
	 * zero bytes, so the whole file is exactly the given size.
	 */
	private static byte[] padded(int totalBytes) throws IOException {
		byte[] unpadded = minimal(Variant.PADDED);
		byte[] file = Arrays.copyOf(unpadded, totalBytes);
		ByteBuffer.wrap(file).putInt(unpadded.length - Integer.BYTES, totalBytes - unpadded.length);
		return file;
	}

	/**
	 * Writes the constants a variant adds from index 9 on and returns how many
	 * slots the pool count gives them.
	 */
	private static int writeExtraConstants(DataOutputStream out, Variant variant) throws IOException {
		return switch (variant) {
		case UNKNOWN_TAG -> {
			out.writeByte(99);
			yield 1;
		}
		case DANGLING_REFERENCE -> {
			out.writeByte(8);
			out.writeShort(0);
			yield 1;
		}
		case INDEX_PAST_POOL_FOR_TEXT -> {
			out.writeByte(8);
			out.writeShort(10);
			yield 1;
		}
		case LONG_CONSTANT, LONG_ENDS_POOL -> {
			out.writeByte(5);
			out.writeLong(1);
			yield variant == Variant.LONG_CONSTANT ? 2 : 1;
		}
		case DOUBLE_CONSTANT -> {
			out.writeByte(6);
			out.writeDouble(1);
			yield 2;
		}
		case INTEGER_CONSTANT -> {
			out.writeByte(3);
			out.writeInt(1);
			yield 1;
		}
		case WITH_INTERFACE -> writeNamedClass(out, 9, "java/lang/Runnable");
		case FIELD_HANDLE -> writeHandle(out, 9, "f", "I", 1);
		case HANDLE_KIND_ZERO -> writeHandle(out, 9, "f", "I", 0);
		case HANDLE_KIND_TEN -> writeHandle(out, 9, "f", "I", 10);
		case METHOD_HANDLE_TO_FIELD -> writeHandle(out, 9, "f", "I", 5);
		case SPECIAL_HANDLE_TO_FIELD -> writeHandle(out, 9, "f", "I", 7);
		case CONSTRUCTOR_HANDLE -> writeHandle(out, 10, "<init>", "()V", 8);
		case VIRTUAL_HANDLE_TO_CONSTRUCTOR -> writeHandle(out, 10, "<init>", "()V", 5);
		case CLASS_INITIALISER_HANDLE -> writeHandle(out, 10, "<clinit>", "()V", 5);
		case INTERFACE_HANDLE_TO_CLASS_METHOD -> writeHandle(out, 10, "run", "()V", 9);
		case VIRTUAL_HANDLE_TO_METHOD -> writeHandle(out, 10, "run", "()V", 5);
		case SPECIAL_HANDLE_TO_METHOD -> writeHandle(out, 10, "run", "()V", 7);
		case SPECIAL_HANDLE_TO_INTERFACE_METHOD_FROM_52, SPECIAL_HANDLE_TO_INTERFACE_METHOD_BEFORE_52 -> writeRunnableHandle(
				out);
		case FIELDREF_CLASS_IS_TEXT -> writeMember(out, 9, 1, "f", "I");
		case FIELDREF_NAME_AND_TYPE_IS_TEXT -> writeReferences(out, 9, 2, 1);
		case INVOKEDYNAMIC_NAME_AND_TYPE_IS_TEXT -> writeReferences(out, 18, 0, 1);
		case INDEX_PAST_POOL_FOR_TAG -> writeReferences(out, 9, 2, 10);
		case INDEX_PAST_POOL_FOR_HANDLE -> writeHandleConstant(out, 1, 10);
		case HANDLE_BEFORE_ITS_TARGET -> writeHandleConstant(out, 1, 10) + writeReferences(out, 9, 2, 11);
		case SUPERCLASS_NAME_STARTS_WITH_DOT -> writeNamedClass(out, 9, ".Object");
		case FOREIGN_MEMBER -> writeNamedClass(out, 9, "y/Other");
		case PADDED -> writeText(out, "Padding");
		default -> 0;
		};
	}

	/**
	 * Writes a name, a descriptor, their pairing, a member reference with the given
	 * tag on class {@code x.T}, and a method handle of the given kind to it, at
	 * indices 9 to 13, and returns the five slots they take.
	 */
	private static int writeHandle(DataOutputStream out, int memberTag, String name, String descriptor, int kind)
			throws IOException {
		return writeMember(out, memberTag, 2, name, descriptor) + writeHandleConstant(out, kind, 12);
	}

	/**
	 * Writes the name {@code run}, its descriptor and their pairing at indices 9 to
	 * 11, an interface-method reference to {@code Runnable.run} at 12, a
	 * special-method handle to it at 13, and the text {@code java/lang/Runnable}
	 * with its class constant at 14 and 15, and returns the seven slots they take.
	 */
	private static int writeRunnableHandle(DataOutputStream out) throws IOException {
		return writeMember(out, 11, 15, "run", "()V") + writeHandleConstant(out, 7, 12)
				+ writeNamedClass(out, 14, "java/lang/Runnable");
	}

	/**
	 * Writes a name, a descriptor, their pairing, and a member reference with the
	 * given tag and class index, at indices 9 to 12, and returns the four slots
	 * they take.
	 */
	private static int writeMember(DataOutputStream out, int memberTag, int classIndex, String name, String descriptor)
			throws IOException {
		return writeText(out, name) + writeText(out, descriptor) + writeReferences(out, 12, 9, 10)
				+ writeReferences(out, memberTag, classIndex, 11);
	}

	/** Writes one text constant and returns the one slot it takes. */
	private static int writeText(DataOutputStream out, String text) throws IOException {
		out.writeByte(1);
		out.writeUTF(text);
		return 1;
	}

	/**
	 * Writes a text at the given index and a class constant naming it right after,
	 * and returns the two slots they take.
	 */
	private static int writeNamedClass(DataOutputStream out, int textIndex, String internalName) throws IOException {
		writeText(out, internalName);
		out.writeByte(7);
		out.writeShort(textIndex);
		return 2;
	}

	/**
	 * Writes one constant with the given tag and two two-byte indices, such as a
	 * field reference, and returns the one slot it takes.
	 */
	private static int writeReferences(DataOutputStream out, int tag, int first, int second) throws IOException {
		out.writeByte(tag);
		out.writeShort(first);
		out.writeShort(second);
		return 1;
	}

	/**
	 * Writes one method handle of the given kind to the given index and returns the
	 * one slot it takes.
	 */
	private static int writeHandleConstant(DataOutputStream out, int kind, int target) throws IOException {
		out.writeByte(15);
		out.writeByte(kind);
		out.writeShort(target);
		return 1;
	}

	/**
	 * Writes one NestMembers attribute listing {@code x.T$A}, plus {@code y.Other}
	 * for {@link Variant#FOREIGN_MEMBER}, with the variant's defect.
	 */
	private static void writeNestMembers(DataOutputStream out, Variant variant) throws IOException {
		List<Integer> members = switch (variant) {
		case MEMBER_IS_TEXT -> List.of(3);
		case FOREIGN_MEMBER -> List.of(4, 10);
		default -> List.of(4);
		};
		out.writeShort(5);
		out.writeInt(2 + 2 * members.size() + (variant == Variant.WRONG_LENGTH ? 2 : 0));
		out.writeShort(members.size());
		for (int member : members) {
			out.writeShort(member);
		}
		if (variant == Variant.WRONG_LENGTH) {
			out.writeShort(0);
		}
	}
}
