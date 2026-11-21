package de.tum.cit.ase.ares.api.aop.java.aspectj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
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

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.MethodManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.utility.JavaConstant;

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

	/** The oldest JDK that has the Java 18+ API the compiled fixtures use. */
	private static final int NEWER_API_FEATURE = 21;

	/** The folder the build writes the woven main classes to. */
	private static final Path MAIN_CLASSES = Path.of("target", "classes");

	/** The name the class file gives every constructor. */
	private static final String CONSTRUCTOR = "<init>";

	/**
	 * Owners of signature-polymorphic methods, such as {@code invokeExact}, which
	 * accept any argument list, so a call to one of those is matched by name only.
	 */
	private static final Set<String> SIGNATURE_POLYMORPHIC_OWNERS = Set.of("java.lang.invoke.MethodHandle",
			"java.lang.invoke.VarHandle");

	/**
	 * The class-file letter of each primitive type and of void, keyed by the type's
	 * Java name.
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
	 * The type operations of the release 17 class library, used to erase types and
	 * find supertypes.
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
		writeListMethod(directory, MethodCall.invoke(listReversed()).onArgument(0).withAssigner(Assigner.DEFAULT,
				Assigner.Typing.DYNAMIC));
		assertEquals(List.of("ListCaller references java.util.List.reversed()Ljava/util/List;"),
				missingFromRelease(directory));
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
		writeListMethod(directory,
				MethodCall.invoke(new MethodDescription.ForLoadedMethod(List.class.getMethod("size"))).onArgument(0)
						.withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));
		assertEquals(List.of(), missingFromRelease(directory));
	}

	/**
	 * Fails when the check misses a method handle constant for
	 * {@code List.reversed()}, which no instruction calls.
	 *
	 * @param directory the folder the generated class is written to
	 * @throws IOException if the generated class cannot be written
	 */
	@Test
	void methodHandleToAMethodAddedAfterJava17IsReported(@TempDir Path directory) throws IOException {
		JavaConstant.MethodHandle handle = new JavaConstant.MethodHandle(
				JavaConstant.MethodHandle.HandleType.INVOKE_INTERFACE, TypeDescription.ForLoadedType.of(List.class),
				"reversed", TypeDescription.ForLoadedType.of(List.class), List.of());
		writeListMethod(directory, FixedValue.value(handle));
		assertTrue(missingFromRelease(directory).stream().anyMatch(line -> line.contains("List.reversed")),
				() -> missingFromRelease(directory).toString());
	}

	/**
	 * Fails when a method of {@code MethodHandle} that is not signature-polymorphic
	 * is matched by name alone: no {@code asType()} without a parameter exists.
	 *
	 * @param directory the folder the fixture is written to
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void nonPolymorphicMethodHandleMethodIsMatchedByDescriptor(@TempDir Path directory) throws IOException {
		JavaConstant.MethodHandle handle = new JavaConstant.MethodHandle(
				JavaConstant.MethodHandle.HandleType.INVOKE_VIRTUAL,
				TypeDescription.ForLoadedType.of(java.lang.invoke.MethodHandle.class), "asType",
				TypeDescription.ForLoadedType.of(java.lang.invoke.MethodHandle.class), List.of());
		writeListMethod(directory, FixedValue.value(handle));
		assertTrue(missingFromRelease(directory).stream().anyMatch(line -> line.contains("MethodHandle.asType")),
				() -> missingFromRelease(directory).toString());
	}

	/**
	 * Fails when a call to the signature-polymorphic {@code invokeExact}, whose
	 * descriptor is the call site's own, is reported.
	 *
	 * @param directory the folder the fixture is compiled to
	 */
	@Test
	void signaturePolymorphicCallIsNotReported(@TempDir Path directory) {
		Path classes = compileWithNewerApi(directory, "Invoker", """
				public class Invoker {
					public int call(java.lang.invoke.MethodHandle handle) throws Throwable {
						return (int) handle.invokeExact(3);
					}
				}
				""");
		assertTrue(missingFromRelease(classes).isEmpty(), () -> missingFromRelease(classes).toString());
	}

	/**
	 * Fails when the check misses that {@code DelayQueue.remove()} returns
	 * {@code Delayed} only after Java 17; in Java 17 it returns {@code Object}.
	 *
	 * @param directory the folder the fixture is compiled to
	 */
	@Test
	void methodWithAReturnTypeAddedAfterJava17IsReported(@TempDir Path directory) {
		Path classes = compileWithNewerApi(directory, "Queue", """
				public class Queue {
					public Object take(java.util.concurrent.DelayQueue<?> queue) {
						return queue.remove();
					}
				}
				""");
		assertTrue(missingFromRelease(classes).stream().anyMatch(line -> line.contains("DelayQueue.remove()")),
				() -> missingFromRelease(classes).toString());
	}

	/**
	 * Fails when the check misses {@code getFirst()}, which Java 21 added to
	 * {@code ArrayList}, called on an application subclass. The subclass's
	 * interface declares a static {@code getFirst()}, which the JVM never finds
	 * through a subclass, so it must not count as the method found.
	 *
	 * @param directory the folder the fixture is compiled to
	 */
	@Test
	void inheritedMethodAddedAfterJava17IsReported(@TempDir Path directory) {
		Path classes = compileWithNewerApi(directory, "OwnList", """
				public class OwnList extends java.util.ArrayList<Object> implements Firsts {
					public Object call() {
						return getFirst();
					}
				}

				interface Firsts {
					static Object getFirst() {
						return null;
					}
				}
				""");
		assertTrue(missingFromRelease(classes).stream().anyMatch(line -> line.contains("OwnList.getFirst()")),
				() -> missingFromRelease(classes).toString());
	}

	/**
	 * Fails when the check reports an application method, a method Java 17 has, a
	 * default method of an application interface or a static one called on that
	 * interface, each called from an application subclass of a JDK class.
	 *
	 * @param directory the folder the fixture is compiled to
	 */
	@Test
	void applicationAndInheritedJava17MethodsAreNotReported(@TempDir Path directory) {
		Path classes = compile(directory, RELEASE, "OwnList", """
				public class OwnList extends java.util.ArrayList<Object> implements Helper {
					public Object own() {
						return null;
					}

					public Object call() {
						return own() == null ? size() + helped() + Helper.make() : 0;
					}
				}

				interface Helper {
					default int helped() {
						return 1;
					}

					static int make() {
						return 2;
					}
				}
				""");
		assertEquals(List.of(), missingFromRelease(classes));
	}

	/**
	 * Fails when the check reports the constructor of a JDK inner class, whose
	 * class-file descriptor carries the enclosing instance as an extra parameter.
	 *
	 * @param directory the folder the fixture is compiled to
	 */
	@Test
	void innerClassConstructorIsNotReported(@TempDir Path directory) {
		Path classes = compile(directory, RELEASE, "Panel", """
				public class Panel extends javax.swing.JPanel {
					class Accessible extends AccessibleJPanel {
					}
				}
				""");
		assertEquals(List.of(), missingFromRelease(classes));
	}

	/**
	 * Fails when the check misses an enum constant Java 17 lacks, used as an
	 * annotation value.
	 *
	 * @param directory the folder the fixture is compiled to
	 */
	@Test
	void enumConstantAddedAfterJava17IsReported(@TempDir Path directory) {
		Path classes = compileWithNewerApi(directory, "Processor", """
				@javax.annotation.processing.SupportedSourceVersion(javax.lang.model.SourceVersion.RELEASE_21)
				public class Processor {
				}
				""");
		assertTrue(missingFromRelease(classes).stream().anyMatch(line -> line.contains("SourceVersion.RELEASE_21")),
				() -> missingFromRelease(classes).toString());
	}

	/**
	 * Fails when the check misses a type Java 17 lacks, named as a class value in a
	 * type annotation.
	 *
	 * @param directory the folder the fixture is compiled to
	 */
	@Test
	void typeInATypeAnnotationIsReported(@TempDir Path directory) {
		Path classes = compileWithNewerApi(directory, "Annotated", """
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;

				public class Annotated {
					@Target(ElementType.TYPE_USE)
					@Retention(RetentionPolicy.RUNTIME)
					@interface Marked {
						Class<?> value();
					}

					java.util.List<@Marked(java.util.SequencedCollection.class) Object> values;
				}
				""");
		assertSequencedCollectionReported(classes);
	}

	/**
	 * Fails when the check misses a type Java 17 lacks, named as an annotation
	 * element's default value.
	 *
	 * @param directory the folder the fixture is compiled to
	 */
	@Test
	void typeInAnAnnotationDefaultIsReported(@TempDir Path directory) {
		Path classes = compileWithNewerApi(directory, "Defaulted", """
				public @interface Defaulted {
					Class<?> value() default java.util.SequencedCollection.class;
				}
				""");
		assertSequencedCollectionReported(classes);
	}

	/**
	 * Fails when the check misses a type Java 17 lacks, used only as the component
	 * of a generic array type argument.
	 *
	 * @param directory the folder the fixture is compiled to
	 */
	@Test
	void typeInAGenericArrayArgumentIsReported(@TempDir Path directory) {
		Path classes = compileWithNewerApi(directory, "Holder", """
				public class Holder {
					java.util.List<java.util.SequencedCollection<?>[]> values;
				}
				""");
		assertSequencedCollectionReported(classes);
	}

	/**
	 * Fails when a type parameter without a class bound, followed by the next type
	 * parameter, hides that parameter's interface bound, or when a bound that is a
	 * type variable is misread. javac never writes the first, but the class-file
	 * format allows it.
	 */
	@Test
	void typeParameterBoundsAreReadWhateverTheNextParameterIsCalled() {
		Set<String> afterEmptyBound = new HashSet<>();
		addTypes("<A:Thing::Ljava/util/SequencedCollection;>Ljava/lang/Object;", afterEmptyBound);
		assertEquals(Set.of("java.util.SequencedCollection", "java.lang.Object"), afterEmptyBound);
		Set<String> typeVariableBound = new HashSet<>();
		addTypes("<T:Ljava/lang/Number;U:TT;>Ljava/lang/Object;", typeVariableBound);
		assertEquals(Set.of("java.lang.Number", "java.lang.Object"), typeVariableBound);
	}

	/**
	 * Fails when a class file cut short in its last attribute is read as complete,
	 * which would let the check pass a class the JVM cannot load. The second file
	 * also states a length of 2^31 bytes for that attribute, which a signed read
	 * would take as negative and skip nothing.
	 *
	 * @param directory the folder the fixture is compiled to
	 * @throws IOException if the compiled fixture cannot be read
	 */
	@Test
	void truncatedClassFileIsRefused(@TempDir Path directory) throws IOException {
		Path classes = compile(directory, RELEASE, "Plain", """
				public class Plain {
				}
				""");
		byte[] complete = Files.readAllBytes(classes.resolve("Plain.class"));
		byte[] truncated = Arrays.copyOf(complete, complete.length - 1);
		assertThrows(EOFException.class, () -> new ClassFileReader(new ByteArrayInputStream(truncated)).read());
		byte[] oversized = Arrays.copyOf(complete, complete.length - 2);
		ByteBuffer.wrap(oversized).putInt(oversized.length - 4, Integer.MIN_VALUE);
		assertThrows(EOFException.class, () -> new ClassFileReader(new ByteArrayInputStream(oversized)).read());
	}

	/**
	 * Asserts that the check reports {@code java.util.SequencedCollection}, which
	 * Java 21 added.
	 *
	 * @param classes the folder holding the compiled fixture
	 */
	private static void assertSequencedCollectionReported(Path classes) {
		assertTrue(
				missingFromRelease(classes).stream()
						.anyMatch(line -> line.endsWith(" uses java.util.SequencedCollection")),
				() -> missingFromRelease(classes).toString());
	}

	/**
	 * Describes {@code List.reversed()} without needing the running JDK to have it.
	 *
	 * @return the method as Java 21 declares it
	 */
	private static MethodDescription listReversed() {
		return new MethodDescription.Latent(TypeDescription.ForLoadedType.of(List.class),
				new MethodDescription.Token("reversed",
						Visibility.PUBLIC.getMask() | MethodManifestation.ABSTRACT.getMask(),
						TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(List.class)));
	}

	/**
	 * Writes a class {@code ListCaller} with one static method that takes a list
	 * and runs the given implementation.
	 *
	 * @param directory      the folder the class file is written to
	 * @param implementation the body of the generated method
	 * @throws IOException if the class file cannot be written
	 */
	private static void writeListMethod(Path directory, Implementation implementation) throws IOException {
		Files.createDirectories(directory);
		new ByteBuddy().subclass(Object.class).name("ListCaller")
				.defineMethod("call", Object.class, Visibility.PUBLIC, Ownership.STATIC).withParameters(List.class)
				.intercept(implementation).make().saveIn(directory.toFile());
	}

	/**
	 * Compiles a fixture against the running JDK's own release, which needs at
	 * least JDK 21 for the API the fixtures use.
	 *
	 * @param directory the folder for the source and the classes
	 * @param className the name of the public class in the source
	 * @param source    the source text
	 * @return the folder holding the compiled classes
	 */
	private static Path compileWithNewerApi(Path directory, String className, String source) {
		int feature = Runtime.version().feature();
		assumeTrue(feature >= NEWER_API_FEATURE, "The fixture needs the Java " + NEWER_API_FEATURE + " API");
		return compile(directory, String.valueOf(feature), className, source);
	}

	/**
	 * Compiles a fixture with javac.
	 *
	 * @param directory the folder for the source and the classes
	 * @param release   the release javac compiles against
	 * @param className the name of the public class in the source
	 * @param source    the source text
	 * @return the folder holding the compiled classes
	 */
	private static Path compile(Path directory, String release, String className, String source) {
		try {
			Path sourceFile = Files.writeString(directory.resolve(className + ".java"), source);
			Path classes = Files.createDirectories(directory.resolve("classes"));
			int exitCode = ToolProvider.getSystemJavaCompiler().run(null, null, null, "--release", release, "-d",
					classes.toString(), sourceFile.toString());
			assertEquals(0, exitCode, "javac failed on " + className);
			return classes;
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	/**
	 * Lists, sorted and without repeats, the JDK types and members that the class
	 * files in a folder name in the places {@link ClassFileReader} reads and that
	 * release 17 lacks.
	 *
	 * @param directory the folder holding the class files
	 * @return one line per missing reference, naming the referencing class
	 */
	private static List<String> missingFromRelease(Path directory) {
		Scan scan = new Scan(directory);
		return classFiles(directory).stream().map(scan::read).flatMap(scan::missing).distinct().sorted().toList();
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
	 * Tells whether a type belongs to the running JDK's class library.
	 *
	 * @param binaryName the type's binary name
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
	 * Tells whether a JDK type of release 17 has a referenced member. Fields and
	 * methods are looked up in the type and all its supertypes by name and full
	 * descriptor, without the JVM's access checks; constructors only in the type
	 * itself.
	 *
	 * @param binaryName the JDK type the lookup starts at
	 * @param reference  the referenced member
	 * @return true if release 17 has the type and the member
	 */
	private static boolean resolvesInRelease(String binaryName, MemberReference reference) {
		TypeElement start = typeInRelease(binaryName);
		if (start == null) {
			return false;
		}
		if (reference.kind() == Kind.ELEMENT || CONSTRUCTOR.equals(reference.name())) {
			return start.getEnclosedElements().stream().anyMatch(member -> matches(member, start, reference));
		}
		Deque<TypeElement> pending = new ArrayDeque<>(List.of(start));
		Set<TypeElement> seen = new HashSet<>();
		while (!pending.isEmpty()) {
			TypeElement type = pending.pop();
			if (seen.add(type)
					&& type.getEnclosedElements().stream().anyMatch(member -> matches(member, type, reference))) {
				return true;
			}
			RELEASE_TYPES.directSupertypes(type.asType()).stream().map(DeclaredType.class::cast)
					.map(supertype -> (TypeElement) supertype.asElement())
					.filter(supertype -> !seen.contains(supertype)).forEach(pending::add);
		}
		return false;
	}

	/**
	 * Tells whether a release 17 member is the referenced one: an annotation
	 * element by name, a signature-polymorphic method by name, anything else by
	 * name and full descriptor.
	 *
	 * @param member    the member of a release 17 type
	 * @param owner     the type that declares the member
	 * @param reference the referenced member
	 * @return true if they match
	 */
	private static boolean matches(Element member, TypeElement owner, MemberReference reference) {
		if (!member.getSimpleName().contentEquals(reference.name())) {
			return false;
		}
		if (reference.kind() == Kind.FIELD) {
			return member.getKind().isField() && descriptor(member.asType()).equals(reference.descriptor());
		}
		if (!(member instanceof ExecutableElement executable)) {
			return false;
		}
		return reference.kind() == Kind.ELEMENT
				|| SIGNATURE_POLYMORPHIC_OWNERS.contains(owner.getQualifiedName().toString())
						&& isSignaturePolymorphic(executable)
				|| methodDescriptor(executable, owner).equals(reference.descriptor());
	}

	/**
	 * Whether a method of a signature-polymorphic owner is itself one, as JVMS
	 * 2.9.3 defines it: native, variable-arity, with one parameter of type
	 * {@code Object[]}. Any other method of that owner needs its full descriptor.
	 *
	 * @param executable the method
	 * @return true if any argument list matches it
	 */
	private static boolean isSignaturePolymorphic(ExecutableElement executable) {
		return executable.isVarArgs() && executable.getModifiers().contains(Modifier.NATIVE)
				&& executable.getParameters().size() == 1
				&& "[Ljava/lang/Object;".equals(descriptor(executable.getParameters().get(0).asType()));
	}

	/**
	 * Gives the class-file descriptor of a release 17 method or constructor. The
	 * constructor of an inner class takes the enclosing instance first, which the
	 * language model does not list as a parameter.
	 *
	 * @param executable the method or constructor
	 * @param owner      the type that declares it
	 * @return its descriptor, such as {@code (I)Ljava/lang/Object;}
	 */
	private static String methodDescriptor(ExecutableElement executable, TypeElement owner) {
		StringBuilder descriptor = new StringBuilder("(");
		if (executable.getKind() == ElementKind.CONSTRUCTOR && isInnerClass(owner)) {
			descriptor.append(descriptor(owner.getEnclosingElement().asType()));
		}
		executable.getParameters().forEach(parameter -> descriptor.append(descriptor(parameter.asType())));
		return descriptor.append(')').append(descriptor(executable.getReturnType())).toString();
	}

	/**
	 * Tells whether a type is an inner class, a class declared in another type
	 * without {@code static}, whose instances belong to an instance of that type.
	 *
	 * @param type the type to classify
	 * @return true for an inner class
	 */
	private static boolean isInnerClass(TypeElement type) {
		return type.getKind() == ElementKind.CLASS && type.getNestingKind() == NestingKind.MEMBER
				&& !type.getModifiers().contains(javax.lang.model.element.Modifier.STATIC);
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
		if (erased.getKind().isPrimitive() || erased.getKind() == TypeKind.VOID) {
			return PRIMITIVE_DESCRIPTORS.get(erased.getKind().name().toLowerCase(Locale.ROOT));
		}
		TypeElement element = (TypeElement) ((DeclaredType) erased).asElement();
		return "L" + RELEASE_ELEMENTS.getBinaryName(element).toString().replace('.', '/') + ";";
	}

	/**
	 * Adds the binary names of the class types in a descriptor or generic
	 * signature, array components and type arguments included.
	 *
	 * @param signature the descriptor or signature, such as
	 *                  {@code <T:Ljava/lang/Object;>(TT;)Ljava/util/List<[TT;>;}
	 * @param types     the set the names are added to
	 */
	private static void addTypes(String signature, Set<String> types) {
		int index = signature.startsWith("<") ? skipTypeParameters(signature, types) : 0;
		while (index < signature.length()) {
			char current = signature.charAt(index);
			index = current == '(' || current == ')' || current == '^' ? index + 1 : parseType(signature, index, types);
		}
	}

	/**
	 * Reads the type parameters at the start of a generic signature, adding the
	 * types of their bounds.
	 *
	 * @param signature the signature starting with {@code <}
	 * @param types     the set the bound types are added to
	 * @return the index just after the closing {@code >}
	 */
	private static int skipTypeParameters(String signature, Set<String> types) {
		int index = 1;
		while (signature.charAt(index) != '>') {
			index = signature.indexOf(':', index);
			while (signature.charAt(index) == ':') {
				index++;
				if (startsBound(signature, index)) {
					index = parseType(signature, index, types);
				}
			}
		}
		return index + 1;
	}

	/**
	 * Tells whether a bound starts just after a colon in the type parameters. The
	 * class bound may be empty, so what follows can be the next parameter's name
	 * instead, such as {@code Thing:}. A name runs to a colon and holds no
	 * semicolon, while a bound, apart from an array, always ends with one.
	 *
	 * @param signature the signature
	 * @param index     the index just after the colon
	 * @return true if a type starts there
	 */
	private static boolean startsBound(String signature, int index) {
		char first = signature.charAt(index);
		if (first == '[') {
			return true;
		}
		int colon = signature.indexOf(':', index);
		int semicolon = signature.indexOf(';', index);
		return (first == 'L' || first == 'T') && (colon < 0 || semicolon >= 0 && semicolon < colon);
	}

	/**
	 * Reads one type of a descriptor or signature, adding the class types it names.
	 *
	 * @param signature the descriptor or signature
	 * @param start     the index the type starts at
	 * @param types     the set the class types are added to
	 * @return the index just after the type
	 */
	private static int parseType(String signature, int start, Set<String> types) {
		char first = signature.charAt(start);
		if (first == '[' || first == '+' || first == '-') {
			return parseType(signature, start + 1, types);
		}
		if (first == 'T') {
			return signature.indexOf(';', start) + 1;
		}
		return first == 'L' ? parseClassType(signature, start + 1, types) : start + 1;
	}

	/**
	 * Reads a class type after its {@code L}, adding it and the types in its type
	 * arguments. An inner type of a generic outer type, written after a dot, is
	 * added under its binary name with {@code $}.
	 *
	 * @param signature the descriptor or signature
	 * @param start     the index just after the {@code L}
	 * @param types     the set the class types are added to
	 * @return the index just after the closing {@code ;}
	 */
	private static int parseClassType(String signature, int start, Set<String> types) {
		StringBuilder name = new StringBuilder();
		int index = start;
		while (signature.charAt(index) != ';') {
			char current = signature.charAt(index);
			if (current == '<') {
				index = parseTypeArguments(signature, index + 1, types);
			} else {
				name.append(current == '.' ? '$' : current);
				index++;
			}
		}
		types.add(name.toString().replace('/', '.'));
		return index + 1;
	}

	/**
	 * Reads type arguments after their {@code <}, adding the class types they name.
	 *
	 * @param signature the signature
	 * @param start     the index just after the {@code <}
	 * @param types     the set the class types are added to
	 * @return the index just after the closing {@code >}
	 */
	private static int parseTypeArguments(String signature, int start, Set<String> types) {
		int index = start;
		while (signature.charAt(index) != '>') {
			index = parseType(signature, index, types);
		}
		return index + 1;
	}

	/** What kind of member a reference names. */
	private enum Kind {
		/** A field or an enum constant, matched by name and field descriptor. */
		FIELD,
		/** A method or constructor, matched by name and method descriptor. */
		METHOD,
		/**
		 * An annotation element, matched by name, since its descriptor is not recorded.
		 */
		ELEMENT
	}

	/**
	 * A member a class file refers to.
	 *
	 * @param owner      the binary name of the type the reference names
	 * @param name       the member name, {@code <init>} for a constructor
	 * @param descriptor the class-file descriptor, null for an annotation element
	 * @param kind       what kind of member it is
	 */
	private record MemberReference(String owner, String name, String descriptor, Kind kind) {

		/**
		 * Describes the reference for a failure message.
		 *
		 * @return the owner, the name and the descriptor, or {@code ()} for an
		 *         annotation element
		 */
		String describe() {
			return owner + "." + name + (descriptor == null ? "()" : descriptor);
		}
	}

	/**
	 * What the check reads from one class file.
	 *
	 * @param name         the class's binary name
	 * @param isInterface  whether it is an interface
	 * @param supertypes   its superclass and interfaces
	 * @param declared     its fields and methods, as name plus descriptor
	 * @param notInherited its static and private methods, written the same way
	 * @param types        the class types it names
	 * @param references   the members it refers to
	 */
	private record ParsedClass(String name, boolean isInterface, List<String> supertypes, Set<String> declared,
			Set<String> notInherited, Set<String> types, Set<MemberReference> references) {

		/**
		 * Tells whether the class itself declares a referenced member.
		 *
		 * @param reference the referenced member
		 * @return true if a field or method with that name and descriptor is declared
		 *         here, for an annotation element a method with that name and no
		 *         parameters
		 */
		boolean declares(MemberReference reference) {
			return reference.kind() == Kind.ELEMENT
					? declared.stream().anyMatch(key -> key.startsWith(reference.name() + "()"))
					: declared.contains(reference.name() + reference.descriptor());
		}

		/**
		 * Tells whether the class declares a referenced member that a subtype's call
		 * can reach. The JVM skips the static and private methods of an interface when
		 * it searches a subtype's interfaces, so those do not count here.
		 *
		 * @param reference the referenced member, owned by a subtype of this class
		 * @return true if it is declared here and reachable from that subtype
		 */
		boolean declaresForSubtype(MemberReference reference) {
			return declares(reference)
					&& !(isInterface && notInherited.contains(reference.name() + reference.descriptor()));
		}
	}

	/**
	 * One run of the check over a folder, caching the class files it reads to
	 * follow application supertypes.
	 */
	private static final class Scan {

		/** The folder whose class files are checked and read first. */
		private final Path directory;

		/** The class files already read, by binary name, empty when none was found. */
		private final Map<String, Optional<ParsedClass>> classes = new HashMap<>();

		/** The members already resolved, with the outcome. */
		private final Map<MemberReference, Boolean> resolved = new HashMap<>();

		/**
		 * Creates a run over a folder.
		 *
		 * @param directory the folder whose class files are checked
		 */
		Scan(Path directory) {
			this.directory = directory;
		}

		/**
		 * Reads one class file of the folder.
		 *
		 * @param classFile the class file
		 * @return what the check needs from it
		 */
		ParsedClass read(Path classFile) {
			try (InputStream input = Files.newInputStream(classFile)) {
				return new ClassFileReader(input).read();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		}

		/**
		 * Lists the JDK types and members a class names that release 17 lacks.
		 *
		 * @param parsed the class
		 * @return one line per missing type or member
		 */
		Stream<String> missing(ParsedClass parsed) {
			Stream<String> types = parsed.types().stream().filter(CompiledClassesJava17LibraryTest::isJdkType)
					.filter(type -> typeInRelease(type) == null).map(type -> parsed.name() + " uses " + type);
			Stream<String> members = parsed.references().stream()
					.filter(reference -> !resolved.computeIfAbsent(reference, this::resolves))
					.map(reference -> parsed.name() + " references " + reference.describe());
			return Stream.concat(types, members).toList().stream();
		}

		/**
		 * Tells whether a referenced member exists for Java 17, walking from its owner
		 * through application supertypes to the JDK; constructors and annotation
		 * elements are looked up in the owner only. A member an application class
		 * declares counts as found, and so does one whose walk meets a class it cannot
		 * find or never reaches the JDK.
		 *
		 * @param reference the referenced member
		 * @return false only when the walk reached the JDK and release 17 lacks the
		 *         member
		 */
		private boolean resolves(MemberReference reference) {
			if (reference.owner().startsWith("[")) {
				return true;
			}
			Deque<String> pending = new ArrayDeque<>(List.of(reference.owner()));
			Set<String> seen = new HashSet<>();
			boolean reachedJdk = false;
			while (!pending.isEmpty()) {
				String type = pending.pop();
				if (!seen.add(type)) {
					continue;
				}
				if (isJdkType(type)) {
					reachedJdk = true;
					if (resolvesInRelease(type, reference)) {
						return true;
					}
					continue;
				}
				Optional<ParsedClass> parsed = load(type);
				if (parsed.isEmpty() || (type.equals(reference.owner()) ? parsed.get().declares(reference)
						: parsed.get().declaresForSubtype(reference))) {
					return true;
				}
				if (!CONSTRUCTOR.equals(reference.name()) && reference.kind() != Kind.ELEMENT) {
					pending.addAll(parsed.get().supertypes());
				}
			}
			return !reachedJdk;
		}

		/**
		 * Reads an application class file, from the checked folder first and then from
		 * the test class path.
		 *
		 * @param binaryName the class's binary name
		 * @return the class, or empty if neither place has it
		 */
		private Optional<ParsedClass> load(String binaryName) {
			return classes.computeIfAbsent(binaryName, name -> {
				String file = name.replace('.', '/') + ".class";
				Path local = directory.resolve(file);
				if (Files.isRegularFile(local)) {
					return Optional.of(read(local));
				}
				try (InputStream input = ClassLoader.getSystemResourceAsStream(file)) {
					return input == null ? Optional.empty() : Optional.of(new ClassFileReader(input).read());
				} catch (IOException exception) {
					throw new UncheckedIOException(exception);
				}
			});
		}
	}

	/**
	 * Reads the parts of a class file that name types and members: the constant
	 * pool, which every instruction, method handle and bootstrap argument refers
	 * into, and the descriptors, signatures, annotations, type annotations,
	 * annotation defaults, record components and local variable tables.
	 */
	private static final class ClassFileReader {

		/** The access flag of a class file that marks an interface. */
		private static final int ACC_INTERFACE = 0x0200;

		/** The access flag of a method that marks it static. */
		private static final int ACC_STATIC = 0x0008;

		/** The access flag of a method that marks it private. */
		private static final int ACC_PRIVATE = 0x0002;

		/** The text entries of the constant pool, by index. */
		private String[] texts;

		/** The tag of each constant-pool entry, by index. */
		private int[] tags;

		/** The first index an entry refers to, by index; 0 if it refers to none. */
		private int[] firstIndexes;

		/** The second index an entry refers to, by index; 0 if it refers to none. */
		private int[] secondIndexes;

		/** The class file being read. */
		private final DataInputStream input;

		/** The class types found so far. */
		private final Set<String> types = new LinkedHashSet<>();

		/** The members referred to so far. */
		private final Set<MemberReference> references = new LinkedHashSet<>();

		/** The fields and methods the class declares, each as name and descriptor. */
		private final Set<String> declared = new HashSet<>();

		/** The static and private methods the class declares, written the same way. */
		private final Set<String> notInherited = new HashSet<>();

		/**
		 * Creates a reader for one class file.
		 *
		 * @param stream the class file's bytes
		 */
		ClassFileReader(InputStream stream) {
			this.input = new DataInputStream(new BufferedInputStream(stream));
		}

		/**
		 * Reads the whole class file.
		 *
		 * @return what the check needs from it
		 * @throws IOException if the file cannot be read or is not a class file this
		 *                     reader knows
		 */
		ParsedClass read() throws IOException {
			input.skipNBytes(8);
			readConstantPool();
			boolean isInterface = (input.readUnsignedShort() & ACC_INTERFACE) != 0;
			String name = className(input.readUnsignedShort());
			List<String> supertypes = new ArrayList<>();
			int superclass = input.readUnsignedShort();
			if (superclass != 0) {
				supertypes.add(className(superclass));
			}
			int interfaceCount = input.readUnsignedShort();
			for (int interfaceIndex = 0; interfaceIndex < interfaceCount; interfaceIndex++) {
				supertypes.add(className(input.readUnsignedShort()));
			}
			readMembers(false);
			readMembers(true);
			readAttributes();
			collectConstantPool();
			return new ParsedClass(name, isInterface, supertypes, declared, notInherited, types, references);
		}

		/**
		 * Reads the constant pool into the entry arrays.
		 *
		 * @throws IOException if the file cannot be read or holds an unknown entry
		 */
		private void readConstantPool() throws IOException {
			int count = input.readUnsignedShort();
			texts = new String[count];
			tags = new int[count];
			firstIndexes = new int[count];
			secondIndexes = new int[count];
			int index = 1;
			while (index < count) {
				index += readConstant(index);
			}
		}

		/**
		 * Reads one constant-pool entry.
		 *
		 * @param index the entry's index
		 * @return the number of slots it takes, two for a long or a double
		 * @throws IOException if the file cannot be read or the tag is unknown
		 */
		private int readConstant(int index) throws IOException {
			int tag = input.readUnsignedByte();
			tags[index] = tag;
			switch (tag) {
			case 1 -> texts[index] = input.readUTF();
			case 7, 8, 16, 19, 20 -> firstIndexes[index] = input.readUnsignedShort();
			case 9, 10, 11, 12, 17, 18 -> {
				firstIndexes[index] = input.readUnsignedShort();
				secondIndexes[index] = input.readUnsignedShort();
			}
			case 15 -> {
				input.readUnsignedByte();
				firstIndexes[index] = input.readUnsignedShort();
			}
			case 3, 4 -> input.skipNBytes(4);
			case 5, 6 -> input.skipNBytes(8);
			default -> throw new IOException("Unknown constant-pool tag " + tag);
			}
			return tag == 5 || tag == 6 ? 2 : 1;
		}

		/**
		 * Collects the types and members the constant pool names: class entries, field
		 * and method references, and the descriptors of name-and-type and method-type
		 * entries. A method handle refers to a method reference entry, so it is covered
		 * by that entry.
		 */
		private void collectConstantPool() {
			for (int index = 1; index < tags.length; index++) {
				switch (tags[index]) {
				case 7 -> addClassEntry(texts[firstIndexes[index]]);
				case 9, 10, 11 -> addMemberReference(index);
				case 12 -> addTypes(texts[secondIndexes[index]], types);
				case 16 -> addTypes(texts[firstIndexes[index]], types);
				default -> {
				}
				}
			}
		}

		/**
		 * Adds the type a class entry names, reading an array as a descriptor.
		 *
		 * @param entry the entry's text, such as {@code java/util/List} or
		 *              {@code [Ljava/util/List;}
		 */
		private void addClassEntry(String entry) {
			if (entry.startsWith("[")) {
				addTypes(entry, types);
			} else {
				types.add(entry.replace('/', '.'));
			}
		}

		/**
		 * Adds a field or method reference entry as a member reference.
		 *
		 * @param index the entry's index
		 */
		private void addMemberReference(int index) {
			int nameAndType = secondIndexes[index];
			Kind kind = tags[index] == 9 ? Kind.FIELD : Kind.METHOD;
			references.add(new MemberReference(className(firstIndexes[index]), texts[firstIndexes[nameAndType]],
					texts[secondIndexes[nameAndType]], kind));
		}

		/**
		 * Gives the binary name a class entry points to.
		 *
		 * @param classIndex the index of the class entry
		 * @return the binary name, or the descriptor for an array
		 */
		private String className(int classIndex) {
			return texts[firstIndexes[classIndex]].replace('/', '.');
		}

		/**
		 * Reads the fields or the methods, recording each as declared, and a static or
		 * private method as not inherited too.
		 *
		 * @param methods whether the methods are read rather than the fields
		 * @throws IOException if the file cannot be read
		 */
		private void readMembers(boolean methods) throws IOException {
			int count = input.readUnsignedShort();
			for (int member = 0; member < count; member++) {
				int flags = input.readUnsignedShort();
				String name = texts[input.readUnsignedShort()];
				String descriptor = texts[input.readUnsignedShort()];
				declared.add(name + descriptor);
				if (methods && (flags & (ACC_STATIC | ACC_PRIVATE)) != 0) {
					notInherited.add(name + descriptor);
				}
				addTypes(descriptor, types);
				readAttributes();
			}
		}

		/**
		 * Reads a list of attributes, looking into those that name types or members and
		 * skipping the rest.
		 *
		 * @throws IOException if the file cannot be read
		 */
		private void readAttributes() throws IOException {
			int count = input.readUnsignedShort();
			for (int attribute = 0; attribute < count; attribute++) {
				String name = texts[input.readUnsignedShort()];
				long length = Integer.toUnsignedLong(input.readInt());
				readAttribute(name, length);
			}
		}

		/**
		 * Reads one attribute's content.
		 *
		 * @param name   the attribute's name
		 * @param length its length in bytes, read as unsigned, used to skip it
		 * @throws IOException if the file cannot be read
		 */
		private void readAttribute(String name, long length) throws IOException {
			switch (name) {
			case "Signature" -> addTypes(texts[input.readUnsignedShort()], types);
			case "RuntimeVisibleAnnotations", "RuntimeInvisibleAnnotations" -> readAnnotations();
			case "RuntimeVisibleParameterAnnotations", "RuntimeInvisibleParameterAnnotations" -> readParameterAnnotations();
			case "RuntimeVisibleTypeAnnotations", "RuntimeInvisibleTypeAnnotations" -> readTypeAnnotations();
			case "AnnotationDefault" -> readElementValue();
			case "Code" -> readCode();
			case "LocalVariableTable", "LocalVariableTypeTable" -> readLocalVariables();
			case "Record" -> readRecordComponents();
			default -> input.skipNBytes(length);
			}
		}

		/**
		 * Reads a method's code, skipping the instructions, whose references are all
		 * constant-pool entries, and reading its attributes.
		 *
		 * @throws IOException if the file cannot be read
		 */
		private void readCode() throws IOException {
			input.skipNBytes(4);
			input.skipNBytes(Integer.toUnsignedLong(input.readInt()));
			input.skipNBytes(8 * input.readUnsignedShort());
			readAttributes();
		}

		/**
		 * Reads a local variable table, adding the type of each variable.
		 *
		 * @throws IOException if the file cannot be read
		 */
		private void readLocalVariables() throws IOException {
			int count = input.readUnsignedShort();
			for (int variable = 0; variable < count; variable++) {
				input.skipNBytes(6);
				addTypes(texts[input.readUnsignedShort()], types);
				input.readUnsignedShort();
			}
		}

		/**
		 * Reads the components of a record, adding their types and reading their
		 * attributes.
		 *
		 * @throws IOException if the file cannot be read
		 */
		private void readRecordComponents() throws IOException {
			int count = input.readUnsignedShort();
			for (int component = 0; component < count; component++) {
				input.readUnsignedShort();
				addTypes(texts[input.readUnsignedShort()], types);
				readAttributes();
			}
		}

		/**
		 * Reads the annotations of each parameter.
		 *
		 * @throws IOException if the file cannot be read
		 */
		private void readParameterAnnotations() throws IOException {
			int parameters = input.readUnsignedByte();
			for (int parameter = 0; parameter < parameters; parameter++) {
				readAnnotations();
			}
		}

		/**
		 * Reads a list of annotations.
		 *
		 * @throws IOException if the file cannot be read
		 */
		private void readAnnotations() throws IOException {
			int count = input.readUnsignedShort();
			for (int annotation = 0; annotation < count; annotation++) {
				readAnnotation();
			}
		}

		/**
		 * Reads a list of type annotations, skipping where each one sits and reading
		 * the annotation itself.
		 *
		 * @throws IOException if the file cannot be read
		 */
		private void readTypeAnnotations() throws IOException {
			int count = input.readUnsignedShort();
			for (int annotation = 0; annotation < count; annotation++) {
				skipTypeAnnotationTarget(input.readUnsignedByte());
				input.skipNBytes(2 * input.readUnsignedByte());
				readAnnotation();
			}
		}

		/**
		 * Skips the part of a type annotation that says which type use it annotates.
		 *
		 * @param targetType the kind of type use, as the class file encodes it
		 * @throws IOException if the file cannot be read or the kind is unknown
		 */
		private void skipTypeAnnotationTarget(int targetType) throws IOException {
			switch (targetType) {
			case 0x00, 0x01, 0x16 -> input.skipNBytes(1);
			case 0x10, 0x11, 0x12, 0x17, 0x42, 0x43, 0x44, 0x45, 0x46 -> input.skipNBytes(2);
			case 0x13, 0x14, 0x15 -> {
			}
			case 0x40, 0x41 -> input.skipNBytes(6 * input.readUnsignedShort());
			case 0x47, 0x48, 0x49, 0x4A, 0x4B -> input.skipNBytes(3);
			default -> throw new IOException("Unknown type annotation target " + targetType);
			}
		}

		/**
		 * Reads one annotation, adding its type and each element it sets.
		 *
		 * @throws IOException if the file cannot be read
		 */
		private void readAnnotation() throws IOException {
			String type = texts[input.readUnsignedShort()];
			addTypes(type, types);
			String owner = type.substring(1, type.length() - 1).replace('/', '.');
			int pairs = input.readUnsignedShort();
			for (int pair = 0; pair < pairs; pair++) {
				references.add(new MemberReference(owner, texts[input.readUnsignedShort()], null, Kind.ELEMENT));
				readElementValue();
			}
		}

		/**
		 * Reads one annotation value, adding the enum constant, class or nested
		 * annotation it names.
		 *
		 * @throws IOException if the file cannot be read or the value kind is unknown
		 */
		private void readElementValue() throws IOException {
			char tag = (char) input.readUnsignedByte();
			switch (tag) {
			case 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 's' -> input.readUnsignedShort();
			case 'e' -> readEnumValue();
			case 'c' -> addTypes(texts[input.readUnsignedShort()], types);
			case '@' -> readAnnotation();
			case '[' -> {
				int count = input.readUnsignedShort();
				for (int value = 0; value < count; value++) {
					readElementValue();
				}
			}
			default -> throw new IOException("Unknown annotation value tag " + tag);
			}
		}

		/**
		 * Reads an enum constant used as an annotation value, adding its type and the
		 * constant as a field reference.
		 *
		 * @throws IOException if the file cannot be read
		 */
		private void readEnumValue() throws IOException {
			String type = texts[input.readUnsignedShort()];
			String constant = texts[input.readUnsignedShort()];
			addTypes(type, types);
			references.add(new MemberReference(type.substring(1, type.length() - 1).replace('/', '.'), constant, type,
					Kind.FIELD));
		}
	}
}
