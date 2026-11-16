package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import de.tum.cit.ase.AncestorPackageAresImport;
import de.tum.cit.ase.AncestorPackageAspectJImport;
import de.tum.cit.ase.AncestorPackageByteBuddyImport;
import de.tum.cit.ase.AncestorPackageExercise;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

public class JavaArchunitTestCaseCollectionTest {

	private static Path tempMethodsFile;

	@BeforeAll
	static void setUpResources() throws IOException {
		// Create a temporary file under src/test/resources for readMethodsFromGivenPath
		Path resourcesDir = Paths.get("src/test/resources");
		if (!Files.exists(resourcesDir)) {
			Files.createDirectories(resourcesDir);
		}
		tempMethodsFile = resourcesDir.resolve("tempMethods.txt");
		String content = "#commentLine\nmethodA\n\nmethodB\n#anotherComment";
		Files.writeString(tempMethodsFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	@Test
	void noClassMustImportForbiddenPackages_returnsRuleWithExpectedDescription() {
		Set<PackagePermission> allowedPackages = new HashSet<>();
		allowedPackages.add(new PackagePermission("com.allowed"));
		ArchRule rule = JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(allowedPackages);
		String expectedDescription = Messages.localized("security.architecture.package.import");
		assertEquals(expectedDescription, rule.getDescription());
	}

	@Test
	void packageWildcardAllowsEveryImportedPackage() {
		ArchRule rule = JavaArchunitTestCaseCollection
				.noClassMustImportForbiddenPackages(Set.of(new PackagePermission("*")));
		assertDoesNotThrow(
				() -> rule.check(new ClassFileImporter().importClasses(JavaArchunitTestCaseCollectionTest.class)));
	}

	/**
	 * A permission for a package above Ares' API covers the ordinary packages below
	 * it, and the fixture really depends on one of them.
	 */
	@Test
	void ancestorPermissionCoversTheOrdinaryPackagesBelowIt() {
		assertDoesNotThrow(() -> checkImports(AncestorPackageExercise.class, "java", "de.tum.cit.ase"));
		assertThrows(AssertionError.class,
				() -> checkImports(AncestorPackageExercise.class, "java", "de.tum.cit.ase.ares.api"));
	}

	/**
	 * A permission for a package above Ares' API does not reach into that API.
	 */
	@Test
	void ancestorPermissionDoesNotReachAresApi() {
		assertThrows(AssertionError.class,
				() -> checkImports(AncestorPackageAresImport.class, "java", "de.tum.cit.ase"));
		assertThrows(AssertionError.class, () -> checkImports(AncestorPackageAresImport.class, "java", "de"));
	}

	/**
	 * A permission naming Ares' API, or a package inside it, reaches it.
	 */
	@Test
	void explicitPermissionReachesAresApi() {
		assertDoesNotThrow(() -> checkImports(AncestorPackageAresImport.class, "java", "de.tum.cit.ase",
				"de.tum.cit.ase.ares.api.policy"));
		assertDoesNotThrow(() -> checkImports(AncestorPackageAresImport.class, "java", "de.tum.cit.ase.ares.api"));
	}

	/**
	 * A permission above AspectJ or Byte Buddy reaches neither, and one naming them
	 * reaches them.
	 */
	@Test
	void ancestorPermissionDoesNotReachOtherTrustedNamespaces() {
		assertThrows(AssertionError.class, () -> checkImports(AncestorPackageAspectJImport.class, "java", "org"));
		assertDoesNotThrow(() -> checkImports(AncestorPackageAspectJImport.class, "java", "org.aspectj"));
		assertThrows(AssertionError.class, () -> checkImports(AncestorPackageByteBuddyImport.class, "java", "net"));
		assertDoesNotThrow(() -> checkImports(AncestorPackageByteBuddyImport.class, "java", "net.bytebuddy"));
	}

	/**
	 * The wildcard still covers every package, trusted namespaces included.
	 */
	@Test
	void wildcardStillReachesTrustedNamespaces() {
		assertDoesNotThrow(() -> checkImports(AncestorPackageAresImport.class, "*"));
		assertDoesNotThrow(() -> checkImports(AncestorPackageByteBuddyImport.class, "*"));
	}

	/**
	 * Checks the import rule over one fixture class.
	 *
	 * @param fixture           the class whose imports are checked
	 * @param permittedPackages the permitted packages
	 */
	static void checkImports(Class<?> fixture, String... permittedPackages) {
		Set<PackagePermission> permissions = Arrays.stream(permittedPackages).map(PackagePermission::new)
				.collect(Collectors.toSet());
		JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(permissions)
				.check(new ClassFileImporter().importClasses(fixture));
	}

	@Test
	void staticRules_areNotNull() {
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_ACCESS_NETWORK);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_USE_REFLECTION);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_TERMINATE_JVM);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_SERIALIZE);
		assertNotNull(JavaArchunitTestCaseCollection.NO_CLASS_MUST_USE_CLASSLOADERS);
	}
}
