package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.Source;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.AnnotationVisitor;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.ClassWriter;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.FieldVisitor;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.Opcodes;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.RecordComponentVisitor;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.Type;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.TypeReference;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ArrayClassLiteralValueFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ArrayDimensionFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.BothDependenciesFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.CallTypeArgumentFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.CastFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.CatchParameterFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ClassLiteralValueFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ClassRetainedFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ClassTypeParameterFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.CleanEnumFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.CleanFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.CleanRecordFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ConstructorResultFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.EnumFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.EnumValueFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ExtendsFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.FieldTypeFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.GenericArgumentFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ImplementsFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.InstanceofFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.LambdaFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.LocalVariableFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.MethodReferenceFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.MethodTypeParameterFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.NestedAnnotationValueFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.NestedTypeFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.NewFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.OrdinaryDependencyFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.OwnTypeUseFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ParameterFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.PrimitiveClassLiteralFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ReceiverFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.RecordComponentFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.RepeatedFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ResourceVariableFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ReturnTypeFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.ThrowsFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.TypeParameterBoundFixture;
import de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked.WildcardBoundFixture;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.testutilities.CustomConditionEvents;
import de.tum.cit.ase.ares.testutilities.JavacFixtures;

/**
 * Checks that a type named only through a type annotation counts as an import
 * in the package-import rule, in every place the class file can hold one, and
 * that a class whose file cannot be read is rejected rather than passed.
 */
class TypeAnnotationDependenciesTest {

	/**
	 * The package of the checked fixtures, which every test allows.
	 */
	private static final String CHECKED = "de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked";

	/**
	 * The package of the forbidden annotations.
	 */
	private static final String FOREIGN = "org.example.subject.typeuse.foreign";

	/**
	 * The allowed package of the annotation whose values name forbidden types.
	 */
	private static final String PERMITTED = "org.example.subject.typeuse.permitted";

	/**
	 * The package of the types named only inside annotation values.
	 */
	private static final String VALUES = "org.example.subject.typeuse.values";

	/**
	 * Builds the import rule for the given allowed packages.
	 *
	 * @param allowed the allowed packages
	 * @return the rule
	 */
	private static ArchRule rule(String... allowed) {
		return JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(
				Set.of(allowed).stream().map(PackagePermission::new).collect(java.util.stream.Collectors.toSet()),
				Set.of());
	}

	/**
	 * Where {@code javac} writes the fixtures. The test build compiles test sources
	 * with the AspectJ compiler, which stores type annotations differently, so the
	 * fixtures are compiled again the way an exercise compiles student code.
	 */
	@TempDir
	static Path javacOutput;

	/**
	 * Compiles one fixture with {@code javac} and imports only that class.
	 *
	 * @param fixture the fixture
	 * @return the imported classes
	 */
	private static JavaClasses classes(Class<?> fixture) {
		return new ClassFileImporter()
				.importPath(JavacFixtures.compile(fixture, javacOutput.resolve(fixture.getSimpleName())));
	}

	/**
	 * Checks the rule, expects it to fail, and returns the Ares message the error
	 * parser makes of the failure.
	 *
	 * @param rule    the rule
	 * @param classes the classes
	 * @return the message of the resulting security exception
	 */
	private static String rejectionMessage(ArchRule rule, JavaClasses classes) {
		AssertionError failure = assertThrows(AssertionError.class, () -> rule.check(classes));
		SecurityException security = assertThrows(SecurityException.class,
				() -> JavaArchitectureTestCase.parseErrorMessage(failure));
		return security.getMessage();
	}

	/**
	 * Checks that a type annotation from a forbidden package is rejected in every
	 * place a class file can hold one, and that ArchUnit alone misses it.
	 *
	 * @param fixture the fixture holding the annotation
	 */
	@ParameterizedTest
	@ValueSource(classes = { FieldTypeFixture.class, ParameterFixture.class, ReturnTypeFixture.class,
			ReceiverFixture.class, ThrowsFixture.class, ClassTypeParameterFixture.class,
			MethodTypeParameterFixture.class, TypeParameterBoundFixture.class, ExtendsFixture.class,
			ImplementsFixture.class, LocalVariableFixture.class, ResourceVariableFixture.class,
			CatchParameterFixture.class, InstanceofFixture.class, NewFixture.class, CastFixture.class,
			MethodReferenceFixture.class, CallTypeArgumentFixture.class, GenericArgumentFixture.class,
			ArrayDimensionFixture.class, WildcardBoundFixture.class, NestedTypeFixture.class,
			RecordComponentFixture.class, ConstructorResultFixture.class, ClassRetainedFixture.class,
			RepeatedFixture.class, EnumFixture.class, LambdaFixture.class })
	@DisplayName("A foreign type annotation is rejected as an import, wherever it sits")
	void foreignTypeAnnotationIsRejected(Class<?> fixture) {
		JavaClasses classes = classes(fixture);

		assertDoesNotThrow(
				() -> noClasses().should().dependOnClassesThat().resideInAPackage(FOREIGN + "..").check(classes),
				"ArchUnit alone must not see the annotation, or this fixture proves nothing about the new check");
		String message = rejectionMessage(rule("java", CHECKED), classes);
		assertTrue(message.contains(FOREIGN), () -> "the rejection must name " + FOREIGN + ": " + message);
	}

	/**
	 * Checks that a type annotation passes once its package is allowed.
	 *
	 * @param fixture the fixture holding the annotation
	 */
	@ParameterizedTest
	@ValueSource(classes = { FieldTypeFixture.class, LocalVariableFixture.class, RecordComponentFixture.class,
			ClassRetainedFixture.class, RepeatedFixture.class, LambdaFixture.class })
	@DisplayName("A foreign type annotation passes once its package is allowed")
	void allowedTypeAnnotationPasses(Class<?> fixture) {
		assertDoesNotThrow(() -> rule("java", CHECKED, FOREIGN).check(classes(fixture)));
	}

	/**
	 * Checks that a forbidden type named in the values of an allowed annotation is
	 * rejected.
	 *
	 * @param fixture the fixture holding the annotation
	 */
	@ParameterizedTest
	@ValueSource(classes = { ClassLiteralValueFixture.class, ArrayClassLiteralValueFixture.class,
			EnumValueFixture.class, NestedAnnotationValueFixture.class })
	@DisplayName("A forbidden type inside the values of an allowed annotation is rejected")
	void forbiddenValueTypeIsRejected(Class<?> fixture) {
		String message = rejectionMessage(rule("java", CHECKED, PERMITTED), classes(fixture));
		assertTrue(message.contains(VALUES), () -> "the rejection must name " + VALUES + ": " + message);
	}

	/**
	 * Checks that a type named in annotation values passes once its package is
	 * allowed.
	 *
	 * @param fixture the fixture holding the annotation
	 */
	@ParameterizedTest
	@ValueSource(classes = { ClassLiteralValueFixture.class, ArrayClassLiteralValueFixture.class,
			EnumValueFixture.class, NestedAnnotationValueFixture.class })
	@DisplayName("A type inside annotation values passes once its package is allowed")
	void allowedValueTypePasses(Class<?> fixture) {
		assertDoesNotThrow(() -> rule("java", CHECKED, PERMITTED, VALUES).check(classes(fixture)));
	}

	/**
	 * Checks that classes without a forbidden type annotation still pass.
	 *
	 * @param fixture the clean fixture
	 */
	@ParameterizedTest
	@ValueSource(classes = { CleanFixture.class, CleanRecordFixture.class, CleanEnumFixture.class,
			OwnTypeUseFixture.class, PrimitiveClassLiteralFixture.class })
	@DisplayName("Classes without a foreign type annotation still pass")
	void cleanClassesPass(Class<?> fixture) {
		assertDoesNotThrow(() -> rule("java", CHECKED, PERMITTED).check(classes(fixture)));
	}

	/**
	 * Checks that an ordinary dependency and a type annotation are each rejected on
	 * their own and together, and that a clean class passes.
	 */
	@Test
	@DisplayName("Both ways of depending on a package combine into one rule")
	void bothConditionsCombine() {
		ArchRule rule = rule("java", CHECKED);

		assertDoesNotThrow(() -> rule.check(classes(CleanFixture.class)));
		rejectionMessage(rule, classes(OrdinaryDependencyFixture.class));
		assertTrue(rejectionMessage(rule, classes(FieldTypeFixture.class)).contains(FOREIGN));
		String both = rejectionMessage(rule, classes(BothDependenciesFixture.class));
		assertTrue(both.contains(FOREIGN), both);
	}

	/**
	 * Checks that the wildcard package and an exempted class still pass.
	 */
	@Test
	@DisplayName("The wildcard and an exempted class still pass")
	void wildcardAndExemptionPass() {
		assertDoesNotThrow(() -> rule("*").check(classes(FieldTypeFixture.class)));
		ArchRule exempting = JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(
				Set.of(new PackagePermission("java"), new PackagePermission(CHECKED)),
				Set.of(new ClassPermission(FieldTypeFixture.class.getName())));
		assertDoesNotThrow(() -> exempting.check(classes(FieldTypeFixture.class)));
	}

	/**
	 * Checks that a test class stays forbidden when only a type annotation names
	 * it, as the annotation or inside its values, although its package is allowed,
	 * and that the same classes pass when no test class is named.
	 */
	@Test
	@DisplayName("A test class stays forbidden when only a type annotation names it")
	void testClassNamedOnlyByATypeAnnotationIsRejected() {
		Set<PackagePermission> allowed = Set.of(new PackagePermission("java"), new PackagePermission(CHECKED),
				new PackagePermission(FOREIGN), new PackagePermission(PERMITTED), new PackagePermission(VALUES));
		JavaClasses annotated = classes(FieldTypeFixture.class);
		JavaClasses valued = classes(ClassLiteralValueFixture.class);

		assertDoesNotThrow(() -> JavaArchunitTestCaseCollection
				.noClassMustImportForbiddenPackages(allowed, Set.of(), Set.of()).check(annotated));
		assertDoesNotThrow(() -> JavaArchunitTestCaseCollection
				.noClassMustImportForbiddenPackages(allowed, Set.of(), Set.of()).check(valued));
		String annotation = rejectionMessage(JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(allowed,
				Set.of(), Set.of(FOREIGN + ".ForeignTypeUse")), annotated);
		assertTrue(annotation.contains(FOREIGN), annotation);
		String value = rejectionMessage(JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(allowed,
				Set.of(), Set.of(VALUES + ".ValueType")), valued);
		assertTrue(value.contains(VALUES), value);
	}

	/**
	 * Checks that the annotation is found from its descriptor alone, even when its
	 * class does not exist.
	 */
	@Test
	@DisplayName("The annotation is found from its descriptor, without loading its class")
	void descriptorIsEnoughWithoutTheAnnotationClass() {
		ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "org/example/subject/typeuse/Ghostly", null, "java/lang/Object",
				null);
		FieldVisitor field = writer.visitField(Opcodes.ACC_PRIVATE, "name", "Ljava/lang/String;", null, null);
		field.visitTypeAnnotation(TypeReference.newTypeReference(TypeReference.FIELD).getValue(), null,
				"Lnonexistent/pkg/Ghost;", true).visitEnd();
		field.visitEnd();
		writer.visitEnd();

		Set<String> namedTypes = TypeAnnotationDependencies.typesNamedByTypeAnnotations(writer.toByteArray());

		assertTrue(namedTypes.contains("nonexistent.pkg.Ghost"), namedTypes::toString);
	}

	/**
	 * Checks that a class whose file was deleted after import is rejected with its
	 * own message.
	 *
	 * @param tempDir the temporary directory
	 * @throws IOException if a class file cannot be written
	 */
	@Test
	@DisplayName("A deleted class file is rejected with its own message, in English and in German")
	void deletedClassFileIsRejected(@TempDir Path tempDir) throws IOException {
		assertUnreadableInBothLanguages(tempDir, Files::delete);
	}

	/**
	 * Checks that a class whose file was damaged after import is rejected with its
	 * own message.
	 *
	 * @param tempDir the temporary directory
	 * @throws IOException if a class file cannot be written
	 */
	@Test
	@DisplayName("A damaged class file is rejected with its own message")
	void damagedClassFileIsRejected(@TempDir Path tempDir) throws IOException {
		assertUnreadableInBothLanguages(tempDir,
				classFile -> Files.write(classFile, new byte[] { (byte) 0xCA, (byte) 0xFE }));
	}

	/**
	 * Checks that the ArchUnit and the WALA test case both reject a forbidden type
	 * annotation.
	 */
	@Test
	@DisplayName("The ArchUnit and the WALA test case both reject a foreign type annotation")
	void bothArchitectureModesReject() {
		Set<PackagePermission> allowed = Set.of(new PackagePermission("java"), new PackagePermission(CHECKED));
		JavaClasses classes = classes(FieldTypeFixture.class);

		JavaArchunitTestCase archunit = JavaArchunitTestCase.archunitBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(allowed).javaClasses(classes).build();
		JavaWalaTestCase wala = walaTestCase(allowed, classes);

		SecurityException fromArchunit = assertThrows(SecurityException.class,
				() -> archunit.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ"));
		SecurityException fromWala = assertThrows(SecurityException.class,
				() -> wala.executeArchitectureTestCase("WALA", "INSTRUMENTATION"));
		assertTrue(fromArchunit.getMessage().contains(FOREIGN), fromArchunit::getMessage);
		assertTrue(fromWala.getMessage().contains(FOREIGN), fromWala::getMessage);
	}

	/**
	 * Checks that WALA's cached verdict changes when only the annotation of a class
	 * changes.
	 *
	 * @param tempDir the temporary directory
	 * @throws IOException if a class file cannot be written
	 */
	@Test
	@DisplayName("WALA's cached verdict follows a change to the annotation alone")
	void walaVerdictFollowsAnAnnotationOnlyChange(@TempDir Path tempDir) throws IOException {
		Set<PackagePermission> allowed = Set.of(new PackagePermission("java"), new PackagePermission(CHECKED));
		Path classFile = tempDir.resolve(CHECKED.replace('.', '/')).resolve("Mutable.class");
		Files.createDirectories(classFile.getParent());

		Files.write(classFile, annotatedClass(CHECKED.replace('.', '/') + "/CheckedTypeUse"));
		JavaClasses own = new ClassFileImporter().importPath(tempDir);
		assertDoesNotThrow(() -> walaTestCase(allowed, own).executeArchitectureTestCase("WALA", "ASPECTJ"));

		Files.write(classFile, annotatedClass(FOREIGN.replace('.', '/') + "/ForeignTypeUse"));
		JavaClasses foreign = new ClassFileImporter().importPath(tempDir);
		assertThrows(SecurityException.class,
				() -> walaTestCase(allowed, foreign).executeArchitectureTestCase("WALA", "ASPECTJ"));
	}

	/**
	 * Builds a WALA test case for the import rule. The import rule needs no call
	 * graph, so a mock stands in for it.
	 *
	 * @param allowed the allowed packages
	 * @param classes the classes
	 * @return the test case
	 */
	private static JavaWalaTestCase walaTestCase(Set<PackagePermission> allowed, JavaClasses classes) {
		return JavaWalaTestCase.walaBuilder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(allowed).javaClasses(classes).callGraph(Mockito.mock(CallGraph.class)).build();
	}

	/**
	 * Builds a class {@code Mutable} in the checked package with one field whose
	 * type carries the given annotation, so two versions differ in nothing else.
	 *
	 * @param annotationInternalName the annotation in internal form
	 * @return the class file
	 */
	private static byte[] annotatedClass(String annotationInternalName) {
		ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, CHECKED.replace('.', '/') + "/Mutable", null, "java/lang/Object",
				null);
		FieldVisitor field = writer.visitField(Opcodes.ACC_PRIVATE, "name", "Ljava/lang/String;", null, null);
		field.visitTypeAnnotation(TypeReference.newTypeReference(TypeReference.FIELD).getValue(), null,
				"L" + annotationInternalName + ";", true).visitEnd();
		field.visitEnd();
		writer.visitEnd();
		return writer.toByteArray();
	}

	/**
	 * Copies the compiled class file of a fixture into a directory tree below the
	 * given root.
	 *
	 * @param fixture the fixture
	 * @param root    the root directory
	 * @return the copied class file
	 * @throws IOException if the copy fails
	 */
	private static Path copyClassFile(Class<?> fixture, Path root) throws IOException {
		String resource = "/" + fixture.getName().replace('.', '/') + ".class";
		Path target = root.resolve(resource.substring(1));
		Files.createDirectories(target.getParent());
		try (InputStream input = TypeAnnotationDependenciesTest.class.getResourceAsStream(resource)) {
			Files.write(target, input.readAllBytes());
		}
		return target;
	}

	/**
	 * Makes a class file unreadable after it was imported.
	 */
	@FunctionalInterface
	private interface ClassFileSpoiler {

		/**
		 * Deletes or damages the class file.
		 *
		 * @param classFile the class file
		 * @throws IOException if the file cannot be changed
		 */
		void spoil(Path classFile) throws IOException;
	}

	/**
	 * Checks that the rule, the ArchUnit test case and the WALA test case all
	 * reject a class as unreadable, with the English and the German message. Each
	 * language imports its own copy, so no cached verdict of the other language is
	 * replayed.
	 *
	 * @param tempDir the temporary directory
	 * @param spoiler what makes the class file unreadable after the import
	 * @throws IOException if a class file cannot be copied or changed
	 */
	private static void assertUnreadableInBothLanguages(Path tempDir, ClassFileSpoiler spoiler) throws IOException {
		Set<PackagePermission> allowed = Set.of(new PackagePermission("java"), new PackagePermission(CHECKED));
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			for (Locale locale : new Locale[] { Locale.ENGLISH, Locale.GERMAN }) {
				Path root = tempDir.resolve(locale.toLanguageTag());
				Path classFile = copyClassFile(CleanFixture.class, root);
				JavaClasses classes = new ClassFileImporter().importPath(root);
				spoiler.spoil(classFile);
				JavaArchunitTestCase archunit = JavaArchunitTestCase.archunitBuilder()
						.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
						.allowedPackages(allowed).javaClasses(classes).build();
				JavaWalaTestCase wala = walaTestCase(allowed, classes);
				Locale.setDefault(Locale.Category.DISPLAY, locale);
				String expected = Messages.localized("security.archunit.type.annotation.unreadable",
						CleanFixture.class.getName());
				assertMessage(locale + " rule", expected, rejectionMessage(rule("java", CHECKED), classes));
				assertMessage(locale + " ArchUnit", expected, assertThrows(SecurityException.class,
						() -> archunit.executeArchitectureTestCase("ARCHUNIT", "ASPECTJ")).getMessage());
				assertMessage(locale + " WALA", expected, assertThrows(SecurityException.class,
						() -> wala.executeArchitectureTestCase("WALA", "INSTRUMENTATION")).getMessage());
			}
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * Checks that a rejection carries exactly the expected message.
	 *
	 * @param where    which check and language produced the message
	 * @param expected the expected message
	 * @param actual   the actual message
	 */
	private static void assertMessage(String where, String expected, String actual) {
		assertTrue(expected.equals(actual), () -> where + ": expected <" + expected + "> but was <" + actual + ">");
	}

	/**
	 * A type annotation that may sit on any type use and carries a class, a nested
	 * annotation and a number as values.
	 */
	private static final String ALLOWED_ANNOTATION = """
			package p;
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Retention;
			import java.lang.annotation.RetentionPolicy;
			import java.lang.annotation.Target;
			@Retention(RetentionPolicy.RUNTIME)
			@Target(ElementType.TYPE_USE)
			public @interface Allowed {
				Class<?> value() default Object.class;
				Inner inner() default @Inner;
				int number() default 0;
			}
			""";

	/**
	 * The annotation that is nested as a value of {@link #ALLOWED_ANNOTATION}.
	 */
	private static final String INNER_ANNOTATION = """
			package p;
			public @interface Inner {
				Class<?> value() default Object.class;
			}
			""";

	/**
	 * The type that only ever appears inside annotation values.
	 */
	private static final String FORBIDDEN_TYPE = """
			package p;
			public class Forbidden {
			}
			""";

	/**
	 * Names the positions a type annotation can hold a forbidden type in, each with
	 * the source of a class {@code p.Fixture} that puts it there.
	 *
	 * @return the position names and the sources
	 */
	static Stream<Arguments> typeUsePositions() {
		return Stream.of(Arguments.of("extends", """
				package p;
				public class Fixture extends @Allowed(Forbidden.class) Base {
				}
				class Base {
				}
				"""), Arguments.of("return type", """
				package p;
				public class Fixture {
					public @Allowed(Forbidden.class) String give() {
						return "";
					}
				}
				"""), Arguments.of("cast", """
				package p;
				public class Fixture {
					public String cast(Object value) {
						return (@Allowed(Forbidden.class) String) value;
					}
				}
				"""), Arguments.of("catch parameter", """
				package p;
				public class Fixture {
					public int parse(String text) {
						try {
							return Integer.parseInt(text);
						} catch (@Allowed(Forbidden.class) NumberFormatException exception) {
							return 0;
						}
					}
				}
				"""), Arguments.of("local variable", """
				package p;
				public class Fixture {
					public int local() {
						@Allowed(Forbidden.class)
						String value = "local";
						return value.length();
					}
				}
				"""), Arguments.of("nested annotation", """
				package p;
				public class Fixture {
					@Allowed(inner = @Inner(Forbidden.class))
					String name = "";
				}
				"""));
	}

	/**
	 * A class whose only type annotation is allowed and carries a forbidden class
	 * in its value.
	 */
	private static final String FIELD_WITH_VALUE = """
			package p;
			public class Fixture {
				@Allowed(Forbidden.class)
				String name = "";
			}
			""";

	/**
	 * Compiles the annotation fixtures and the given class {@code p.Fixture} with
	 * {@code javac} and imports the class files.
	 *
	 * @param directory the directory to work in
	 * @param fixture   the source of {@code p.Fixture}
	 * @return the imported classes
	 * @throws IOException if a source file cannot be written
	 */
	private static JavaClasses compileFixture(Path directory, String fixture) throws IOException {
		Map<String, String> sources = Map.of("p/Allowed.java", ALLOWED_ANNOTATION, "p/Inner.java", INNER_ANNOTATION,
				"p/Forbidden.java", FORBIDDEN_TYPE, "p/Fixture.java", fixture);
		Path sourceRoot = directory.resolve("src");
		Path classRoot = Files.createDirectories(directory.resolve("classes"));
		List<Path> files = new ArrayList<>();
		for (Map.Entry<String, String> source : sources.entrySet()) {
			Path file = sourceRoot.resolve(source.getKey());
			Files.createDirectories(file.getParent());
			Files.writeString(file, source.getValue());
			files.add(file);
		}
		JavacFixtures.compileSources(JavacFixtures.testClasses().toString(), classRoot, files);
		return new ClassFileImporter().importPath(classRoot);
	}

	/**
	 * A forbidden type that sits only in the value of an allowed type annotation is
	 * found at every position that has its own visitor.
	 *
	 * @param position  the name of the position
	 * @param fixture   the source of {@code p.Fixture}
	 * @param directory the directory the fixtures are compiled in
	 * @throws IOException if a fixture cannot be written
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("typeUsePositions")
	void findsAForbiddenTypeInTheValueOfATypeAnnotation(String position, String fixture, @TempDir Path directory)
			throws IOException {
		compileFixture(directory, fixture);
		byte[] bytes = Files.readAllBytes(directory.resolve("classes").resolve("p").resolve("Fixture.class"));

		Set<String> namedTypes = TypeAnnotationDependencies.typesNamedByTypeAnnotations(bytes);

		assertTrue(namedTypes.contains("p.Allowed"), position + ": " + namedTypes);
		assertTrue(namedTypes.contains("p.Forbidden"), position + ": " + namedTypes);
	}

	/**
	 * A class without a forbidden type annotation is reported as one violated
	 * event.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void reportsACleanClassAsAViolatedEvent(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileFixture(directory, "package p;\npublic class Fixture {\n}\n");
		ConditionEvents events = ConditionEvents.Factory.create();

		TypeAnnotationDependencies.reportTypeAnnotations(classes.get("p.Fixture"), typeName -> true, events);

		List<String> lines = events.getViolating().stream().flatMap(event -> event.getDescriptionLines().stream())
				.toList();
		assertEquals(List.of("Class <p.Fixture> uses no forbidden type annotation"), lines);
	}

	/**
	 * A class that has no class file to read is refused with its own message.
	 */
	@Test
	void refusesAClassWithoutAClassFile() {
		JavaClass withoutSource = Mockito.mock(JavaClass.class);
		Mockito.when(withoutSource.getName()).thenReturn("p.Missing");
		Mockito.when(withoutSource.getSource()).thenReturn(Optional.<Source>empty());

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> TypeAnnotationDependencies.typesNamedByTypeAnnotations(withoutSource));

		assertEquals(Messages.localized("security.architecture.class.file.missing", "p.Missing"), refusal.getMessage());
	}

	/**
	 * The event for a forbidden type names the source file the class was compiled
	 * from.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void namesTheSourceFileInTheEvent(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileFixture(directory, FIELD_WITH_VALUE);
		CustomConditionEvents events = new CustomConditionEvents();

		TypeAnnotationDependencies.reportTypeAnnotations(classes.get("p.Fixture"), "p.Forbidden"::equals, events);

		List<String> lines = events.getViolating().stream().flatMap(event -> event.getDescriptionLines().stream())
				.toList();
		assertEquals(
				List.of("Class <p.Fixture> depends on <p.Forbidden> through a type annotation in (Fixture.java:0)"),
				lines);
	}

	/**
	 * A type annotation on a record component is read, and so is the forbidden
	 * class in its value.
	 */
	@Test
	void readsARecordComponentAnnotationOnItsOwn() {
		ClassWriter writer = new ClassWriter(0);
		writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER, "p/Point", null,
				"java/lang/Record", null);
		RecordComponentVisitor component = writer.visitRecordComponent("name", "Ljava/lang/String;", null);
		AnnotationVisitor annotation = component.visitTypeAnnotation(
				TypeReference.newTypeReference(TypeReference.FIELD).getValue(), null, "Lp/Allowed;", true);
		annotation.visit("value", Type.getType("Lp/Forbidden;"));
		annotation.visitEnd();
		component.visitEnd();
		writer.visitEnd();

		Set<String> namedTypes = TypeAnnotationDependencies.typesNamedByTypeAnnotations(writer.toByteArray());

		assertTrue(namedTypes.contains("p.Allowed"), namedTypes::toString);
		assertTrue(namedTypes.contains("p.Forbidden"), namedTypes::toString);
	}

	/**
	 * A number in an annotation value names no type, so only the annotation itself
	 * is named.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void ignoresAScalarAnnotationValue(@TempDir Path directory) throws IOException {
		compileFixture(directory, """
				package p;
				public class Fixture {
					@Allowed(number = 1)
					String name = "";
				}
				""");
		byte[] bytes = Files.readAllBytes(directory.resolve("classes").resolve("p").resolve("Fixture.class"));

		assertEquals(Set.of("p.Allowed"), TypeAnnotationDependencies.typesNamedByTypeAnnotations(bytes));
	}
}
