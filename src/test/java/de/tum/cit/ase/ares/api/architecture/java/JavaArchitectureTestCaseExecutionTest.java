package de.tum.cit.ase.ares.api.architecture.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

/**
 * Runs a Java architecture test case against small fixtures and checks which
 * engine it picks, which call graph it hands on and how it writes permissions.
 */
class JavaArchitectureTestCaseExecutionTest {

	/** A class that touches nothing the file-system rule forbids. */
	static class CleanFixture {

		/** Does nothing. */
		void idle() {
		}
	}

	/** A class that deletes a file, which the file-system rule forbids. */
	static class DeletingFixture {

		/**
		 * Deletes the given path.
		 *
		 * @param path the path to delete
		 * @throws IOException if the deletion fails
		 */
		void remove(Path path) throws IOException {
			Files.delete(path);
		}
	}

	/**
	 * Returns a package permission no other test case has used, so no cached
	 * verdict applies.
	 */
	private static PackagePermission uniquePermission() {
		return new PackagePermission("unique" + UUID.randomUUID().toString().replace("-", ""));
	}

	/**
	 * Builds a file-system test case over the given classes. When
	 * {@code useSupplier} is true, the call graph is handed on through a supplier,
	 * otherwise directly.
	 */
	private static JavaArchitectureTestCase fileSystemCase(JavaClasses classes, Set<PackagePermission> allowed,
			CallGraph graph, boolean useSupplier) {
		JavaArchitectureTestCase.Builder builder = JavaArchitectureTestCase.builder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
				.allowedPackages(allowed).javaClasses(classes);
		if (useSupplier) {
			builder.callGraphSupplier(() -> graph);
		} else {
			builder.callGraph(graph);
		}
		return builder.build();
	}

	/** The ArchUnit engine rejects the deleting class and accepts the clean one. */
	@Test
	void executesTheArchUnitRule() {
		JavaClasses deleting = new ClassFileImporter().importClasses(DeletingFixture.class);
		JavaClasses clean = new ClassFileImporter().importClasses(CleanFixture.class);

		SecurityException thrown = assertThrows(SecurityException.class,
				() -> fileSystemCase(deleting, Set.of(), null, false).executeArchitectureTestCase("ARCHUNIT", ""));

		assertTrue(thrown.getMessage().contains("DeletingFixture"), thrown.getMessage());
		assertDoesNotThrow(
				() -> fileSystemCase(clean, Set.of(), null, false).executeArchitectureTestCase("ARCHUNIT", ""));
	}

	/** The WALA engine reads the call graph from the supplier when one is given. */
	@Test
	void executesTheWalaRuleWithTheCallGraphSupplier() {
		JavaClasses clean = new ClassFileImporter().importClasses(CleanFixture.class);
		CallGraph graph = Mockito.mock(CallGraph.class);

		SecurityException thrown = assertThrows(SecurityException.class,
				() -> fileSystemCase(clean, Set.of(uniquePermission()), graph, true).executeArchitectureTestCase("WALA",
						""));

		assertEquals(Messages.localized("security.architecture.wala.entrypoints.empty",
				Messages.localized("security.architecture.file.system.access")), thrown.getMessage());
	}

	/**
	 * The WALA engine uses the call graph given directly when there is no supplier.
	 */
	@Test
	void executesTheWalaRuleWithTheEagerCallGraph() {
		JavaClasses clean = new ClassFileImporter().importClasses(CleanFixture.class);
		CallGraph graph = Mockito.mock(CallGraph.class);

		SecurityException thrown = assertThrows(SecurityException.class,
				() -> fileSystemCase(clean, Set.of(uniquePermission()), graph, false)
						.executeArchitectureTestCase("WALA", ""));

		assertEquals(Messages.localized("security.architecture.wala.entrypoints.empty",
				Messages.localized("security.architecture.file.system.access")), thrown.getMessage());
	}

	/**
	 * A permission inside Ares's api is also written for the copy below the scope.
	 */
	@Test
	void writesTheCopiedCounterpartOfAnAresPermission() {
		JavaClasses clean = new ClassFileImporter().importClasses(CleanFixture.class);
		JavaArchitectureTestCase testCase = JavaArchitectureTestCase.builder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
				.allowedPackages(Set.of(new PackagePermission("de.tum.cit.ase.ares.api.x"))).javaClasses(clean)
				.supervisedPackage("student.pkg").build();

		String written = testCase.allowedPackagesAsCopiedCode();

		assertTrue(written.contains("\"de.tum.cit.ase.ares.api.x\""), written);
		assertTrue(written.contains("\"student.pkg.ares.api.x\""), written);
	}

	/** Without a scope, only the original permission is written. */
	@Test
	void writesNoCounterpartWithoutASupervisedPackage() {
		JavaClasses clean = new ClassFileImporter().importClasses(CleanFixture.class);
		JavaArchitectureTestCase testCase = JavaArchitectureTestCase.builder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
				.allowedPackages(Set.of(new PackagePermission("de.tum.cit.ase.ares.api.x"))).javaClasses(clean)
				.supervisedPackage(" ").build();

		String written = testCase.allowedPackagesAsCopiedCode();

		assertTrue(written.contains("\"de.tum.cit.ase.ares.api.x\""), written);
		assertEquals(1, written.split("ares\\.api\\.x", -1).length - 1, written);
	}

	/** The call graph given to the builder is the one the test case returns. */
	@Test
	void returnsTheCallGraphItWasGiven() {
		JavaClasses clean = new ClassFileImporter().importClasses(CleanFixture.class);
		CallGraph graph = Mockito.mock(CallGraph.class);

		JavaArchitectureTestCase testCase = fileSystemCase(clean, Set.of(), graph, false);

		assertSame(graph, testCase.getCallGraph());
	}
}
