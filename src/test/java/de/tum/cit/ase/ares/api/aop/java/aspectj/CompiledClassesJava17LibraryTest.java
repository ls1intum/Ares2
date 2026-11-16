package de.tum.cit.ase.ares.api.aop.java.aspectj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sun.source.util.JavacTask;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * Checks that the compiled Ares classes use only the Java 17 class library. The
 * AspectJ compiler writes every main class and, built on a newer JDK, resolves
 * against that JDK's library although it is given release 17, so an aspect
 * could call a method Java 17 lacks. javac rejects that in a Java file; this
 * test reports it for an aspect.
 */
class CompiledClassesJava17LibraryTest {

	/** The release whose class library the compiled classes must stay within. */
	private static final String RELEASE = "17";

	/** The folder the build writes the woven main classes to. */
	private static final Path MAIN_CLASSES = Path.of("target", "classes");

	/** The name the class library gives every constructor. */
	private static final String CONSTRUCTOR = "<init>";

	/**
	 * Owners whose methods accept any argument list, so a call to them is matched
	 * by name only.
	 */
	private static final Set<String> SIGNATURE_POLYMORPHIC_OWNERS = Set.of("java.lang.invoke.MethodHandle",
			"java.lang.invoke.VarHandle");

	/**
	 * The size in bytes of each constant-pool entry that is skipped, keyed by its
	 * tag; long and double take two slots.
	 */
	private static final Map<Integer, Integer> SKIPPED_CONSTANT_SIZES = Map.ofEntries(Map.entry(3, 4), Map.entry(4, 4),
			Map.entry(5, 8), Map.entry(6, 8), Map.entry(8, 2), Map.entry(9, 4), Map.entry(10, 4), Map.entry(11, 4),
			Map.entry(12, 4), Map.entry(15, 3), Map.entry(16, 2), Map.entry(17, 4), Map.entry(18, 4), Map.entry(19, 2),
			Map.entry(20, 2));

	/** The constant-pool tag of a text entry. */
	private static final int UTF8_TAG = 1;

	/**
	 * The constant-pool tag of a class entry, which names a type an instruction
	 * refers to.
	 */
	private static final int CLASS_TAG = 7;

	/** The constant-pool tag of a long entry, which takes two slots. */
	private static final int LONG_TAG = 5;

	/** The constant-pool tag of a double entry, which takes two slots. */
	private static final int DOUBLE_TAG = 6;

	/**
	 * The class-file letter of each primitive type, keyed by the type's Java name.
	 */
	private static final Map<String, String> PRIMITIVE_DESCRIPTORS = Map.of("boolean", "Z", "byte", "B", "char", "C",
			"short", "S", "int", "I", "long", "J", "float", "F", "double", "D", "void", "V");

	/**
	 * Every package of the running JDK, which decides whether a referenced type
	 * belongs to the class library.
	 */
	private static final Set<String> JDK_PACKAGES = ModuleFinder.ofSystem().findAll().stream()
			.map(ModuleReference::descriptor).map(ModuleDescriptor::packages).flatMap(Set::stream)
			.collect(Collectors.toUnmodifiableSet());

	/** The compiler task whose view of the class library is that of release 17. */
	private static final JavacTask RELEASE_TASK = (JavacTask) ToolProvider.getSystemJavaCompiler().getTask(null, null,
			null, List.of("--release", RELEASE), null, null);

	/** The types and members of the release 17 class library. */
	private static final Elements RELEASE_ELEMENTS = RELEASE_TASK.getElements();

	/**
	 * The type operations of the release 17 class library, used to erase parameter
	 * types.
	 */
	private static final Types RELEASE_TYPES = RELEASE_TASK.getTypes();

	/**
	 * Fails when a class the build wrote references a type or member of the JDK
	 * that Java 17 does not have.
	 */
	@Test
	void mainClassesUseOnlyTheJava17ClassLibrary() {
		assertFalse(classFiles(MAIN_CLASSES).isEmpty(),
				"No compiled classes found in " + MAIN_CLASSES.toAbsolutePath());
		assertEquals(List.of(), missingFromRelease(MAIN_CLASSES));
	}

	/**
	 * Fails when the check misses a call to {@code List.reversed()}, which Java 21
	 * added.
	 *
	 * @param directory the folder the generated caller is written to
	 * @throws IOException if the generated caller cannot be written
	 */
	@Test
	void callToAMethodAddedAfterJava17IsReported(@TempDir Path directory) throws IOException {
		MethodDescription reversed = new MethodDescription.Latent(TypeDescription.ForLoadedType.of(List.class),
				new MethodDescription.Token("reversed", Modifier.PUBLIC | Modifier.ABSTRACT,
						TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(List.class)));
		writeCaller(directory, reversed);
		assertEquals(List.of("ListCaller calls java.util.List.reversed()"), missingFromRelease(directory));
	}

	/**
	 * Fails when the check reports a call to {@code List.size()}, which Java 17
	 * has.
	 *
	 * @param directory the folder the generated caller is written to
	 * @throws IOException           if the generated caller cannot be written
	 * @throws NoSuchMethodException if the running JDK lacks {@code List.size()}
	 */
	@Test
	void callToAMethodJava17HasIsNotReported(@TempDir Path directory) throws IOException, NoSuchMethodException {
		writeCaller(directory, new MethodDescription.ForLoadedMethod(List.class.getMethod("size")));
		assertEquals(List.of(), missingFromRelease(directory));
	}

	/**
	 * Writes a class {@code ListCaller} whose one method calls the given method on
	 * its list argument.
	 *
	 * @param directory the folder the class file is written to
	 * @param called    the list method the generated class calls
	 * @throws IOException if the class file cannot be written
	 */
	private static void writeCaller(Path directory, MethodDescription called) throws IOException {
		Files.createDirectories(directory);
		new ByteBuddy().subclass(Object.class).name("ListCaller")
				.defineMethod("call", Object.class, Visibility.PUBLIC, Ownership.STATIC).withParameters(List.class)
				.intercept(
						MethodCall.invoke(called).onArgument(0).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
				.make().saveIn(directory.toFile());
	}

	/**
	 * Lists, sorted and without repeats, every JDK type and member the class files
	 * in a folder reference that release 17 lacks. The constant pool is read as
	 * well because ArchUnit does not record the type of a cast or of a new array.
	 *
	 * @param directory the folder holding the class files
	 * @return one line per missing reference, naming the referencing class
	 */
	private static List<String> missingFromRelease(Path directory) {
		Stream<String> declaredAndAccessed = new ClassFileImporter().importPath(directory).stream()
				.flatMap(javaClass -> Stream.concat(missingTypes(javaClass), missingMembers(javaClass)));
		Stream<String> namedByInstructions = classFiles(directory).stream()
				.flatMap(CompiledClassesJava17LibraryTest::missingConstantPoolTypes);
		return Stream.concat(declaredAndAccessed, namedByInstructions).distinct().sorted().toList();
	}

	/**
	 * Lists the class files below a folder.
	 *
	 * @param directory the folder to search
	 * @return the paths of the class files, empty if the folder does not exist
	 */
	private static List<Path> classFiles(Path directory) {
		if (!Files.isDirectory(directory)) {
			return List.of();
		}
		try (Stream<Path> paths = Files.walk(directory)) {
			return paths.filter(path -> path.toString().endsWith(".class")).toList();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	/**
	 * Lists the JDK types a class file's constant pool names that release 17 lacks.
	 *
	 * @param classFile the class file to read
	 * @return one line per missing type
	 */
	private static Stream<String> missingConstantPoolTypes(Path classFile) {
		try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(classFile)))) {
			input.readNBytes(8);
			String[] texts = new String[input.readUnsignedShort()];
			int[] classEntries = readConstantPool(input, texts);
			input.readUnsignedShort();
			String owner = texts[classEntries[input.readUnsignedShort()]].replace('/', '.');
			return Arrays.stream(classEntries).filter(entry -> entry != 0)
					.mapToObj(entry -> elementTypeName(texts[entry]))
					.filter(CompiledClassesJava17LibraryTest::isJdkType).filter(type -> typeInRelease(type) == null)
					.map(type -> owner + " uses " + type).toList().stream();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	/**
	 * Reads a constant pool, storing its text entries and returning where its class
	 * entries point.
	 *
	 * @param input the class file, positioned at the first constant-pool entry
	 * @param texts the array the text entries are stored in by their index
	 * @return for each index, the text index a class entry there points to, or 0
	 *         for any other entry
	 * @throws IOException if the class file cannot be read or holds an unknown
	 *                     entry
	 */
	private static int[] readConstantPool(DataInputStream input, String[] texts) throws IOException {
		int[] classEntries = new int[texts.length];
		int index = 1;
		while (index < texts.length) {
			int tag = input.readUnsignedByte();
			if (tag == UTF8_TAG) {
				texts[index] = input.readUTF();
			} else if (tag == CLASS_TAG) {
				classEntries[index] = input.readUnsignedShort();
			} else if (SKIPPED_CONSTANT_SIZES.containsKey(tag)) {
				input.readNBytes(SKIPPED_CONSTANT_SIZES.get(tag));
			} else {
				throw new IOException("Unknown constant-pool tag " + tag);
			}
			index += tag == LONG_TAG || tag == DOUBLE_TAG ? 2 : 1;
		}
		return classEntries;
	}

	/**
	 * Gives the binary name of the type a class entry names, the element type for
	 * an array.
	 *
	 * @param classEntry the entry's text, such as {@code java/util/List} or
	 *                   {@code [Ljava/util/List;}
	 * @return the binary name, such as {@code java.util.List}, or a primitive name
	 *         for a primitive array
	 */
	private static String elementTypeName(String classEntry) {
		String element = classEntry.replaceFirst("^\\[+", "");
		String name = element.startsWith("L") && element.endsWith(";") ? element.substring(1, element.length() - 1)
				: element;
		return name.replace('/', '.');
	}

	/**
	 * Lists the JDK types a class depends on that release 17 lacks.
	 *
	 * @param javaClass the class whose dependencies are checked
	 * @return one line per missing type
	 */
	private static Stream<String> missingTypes(JavaClass javaClass) {
		return javaClass.getDirectDependenciesFromSelf().stream().map(Dependency::getTargetClass)
				.map(JavaClass::getBaseComponentType).filter(type -> !type.isPrimitive()).map(JavaClass::getName)
				.filter(CompiledClassesJava17LibraryTest::isJdkType).filter(type -> typeInRelease(type) == null)
				.map(type -> javaClass.getName() + " uses " + type);
	}

	/**
	 * Lists the JDK fields, methods and constructors a class accesses that release
	 * 17 lacks.
	 *
	 * @param javaClass the class whose accesses are checked
	 * @return one line per missing member
	 */
	private static Stream<String> missingMembers(JavaClass javaClass) {
		return javaClass.getAccessesFromSelf().stream().map(JavaAccess::getTarget)
				.filter(target -> !target.getOwner().isArray() && isJdkType(target.getOwner().getName()))
				.filter(target -> !memberInRelease(target))
				.map(target -> javaClass.getName() + " calls " + describe(target));
	}

	/**
	 * Tells whether a type belongs to the running JDK's class library.
	 *
	 * @param binaryName the type's binary name; a primitive name has no package and
	 *                   is never one
	 * @return true for a type in a package of the running JDK
	 */
	private static boolean isJdkType(String binaryName) {
		int lastDot = binaryName.lastIndexOf('.');
		return lastDot > 0 && JDK_PACKAGES.contains(binaryName.substring(0, lastDot));
	}

	/**
	 * Finds a type in release 17 by its binary name.
	 *
	 * @param binaryName the class-file name, nested types separated by {@code $}
	 * @return the type, or null if release 17 has no such type
	 */
	private static TypeElement typeInRelease(String binaryName) {
		return RELEASE_ELEMENTS.getTypeElement(binaryName.replace('$', '.'));
	}

	/**
	 * Tells whether release 17 has the member an access targets, matching methods
	 * and constructors by name and erased parameter types and fields by name.
	 *
	 * @param target the accessed member
	 * @return true if the owner type of release 17 declares or inherits it
	 */
	private static boolean memberInRelease(AccessTarget target) {
		TypeElement owner = typeInRelease(target.getOwner().getName());
		if (owner == null) {
			return false;
		}
		if (!(target instanceof AccessTarget.CodeUnitAccessTarget codeUnit)) {
			return candidates(owner, target.getName()).anyMatch(member -> member.getKind().isField());
		}
		List<String> parameters = codeUnit.getRawParameterTypes().stream()
				.map(CompiledClassesJava17LibraryTest::descriptor).toList();
		boolean anyArguments = SIGNATURE_POLYMORPHIC_OWNERS.contains(owner.getQualifiedName().toString());
		return candidates(owner, target.getName()).filter(ExecutableElement.class::isInstance)
				.map(ExecutableElement.class::cast)
				.anyMatch(member -> anyArguments || parameters.equals(parameterDescriptors(member)));
	}

	/**
	 * Lists the members of a release 17 type that have a given name, inherited ones
	 * and those of {@code Object} included, since an interface type does not list
	 * the latter.
	 *
	 * @param owner the type the member is looked up on
	 * @param name  the member name, {@code <init>} for a constructor
	 * @return the fields, methods or constructors with that name
	 */
	private static Stream<Element> candidates(TypeElement owner, String name) {
		Stream<? extends Element> members = CONSTRUCTOR.equals(name) ? owner.getEnclosedElements().stream()
				: Stream.concat(RELEASE_ELEMENTS.getAllMembers(owner).stream(),
						RELEASE_ELEMENTS.getAllMembers(typeInRelease(Object.class.getName())).stream());
		return members.filter(member -> member.getSimpleName().contentEquals(name)).map(Element.class::cast);
	}

	/**
	 * Gives the erased parameter types of a release 17 method or constructor as
	 * class-file descriptors.
	 *
	 * @param member the method or constructor
	 * @return the descriptors in order
	 */
	private static List<String> parameterDescriptors(ExecutableElement member) {
		return member.getParameters().stream().map(Element::asType).map(CompiledClassesJava17LibraryTest::descriptor)
				.toList();
	}

	/**
	 * Gives the class-file descriptor of an erased release 17 type, such as
	 * {@code [Ljava/lang/String;}.
	 *
	 * @param type the type to describe
	 * @return its descriptor
	 */
	private static String descriptor(TypeMirror type) {
		TypeMirror erased = RELEASE_TYPES.erasure(type);
		if (erased.getKind() == TypeKind.ARRAY) {
			return "[" + descriptor(((ArrayType) erased).getComponentType());
		}
		if (erased.getKind().isPrimitive()) {
			return PRIMITIVE_DESCRIPTORS.get(erased.getKind().name().toLowerCase(Locale.ROOT));
		}
		TypeElement element = (TypeElement) ((DeclaredType) erased).asElement();
		return "L" + RELEASE_ELEMENTS.getBinaryName(element).toString().replace('.', '/') + ";";
	}

	/**
	 * Gives the class-file descriptor of a type a compiled class refers to. An
	 * array is read from its name, such as {@code [Ljava.lang.String;}, because its
	 * component type is not imported.
	 *
	 * @param type the referenced type
	 * @return its descriptor
	 */
	private static String descriptor(JavaClass type) {
		String name = type.getName();
		if (type.isArray()) {
			return name.replace('.', '/');
		}
		if (type.isPrimitive()) {
			return PRIMITIVE_DESCRIPTORS.get(name);
		}
		return "L" + name.replace('.', '/') + ";";
	}

	/**
	 * Describes an accessed member for the failure message.
	 *
	 * @param target the accessed member
	 * @return the owner, the name and, for a method or constructor, the parameter
	 *         types
	 */
	private static String describe(AccessTarget target) {
		String member = target.getOwner().getName() + "." + target.getName();
		return target instanceof AccessTarget.CodeUnitAccessTarget codeUnit ? member + codeUnit.getRawParameterTypes()
				.stream().map(JavaClass::getName).collect(Collectors.joining(", ", "(", ")")) : member;
	}
}
