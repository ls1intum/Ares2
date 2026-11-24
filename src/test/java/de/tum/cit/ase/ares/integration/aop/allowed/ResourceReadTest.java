package de.tum.cit.ase.ares.integration.aop.allowed;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.resources.ResourceReadMain;

/**
 * Checks that a class-path resource is read under the file permission for the
 * place the build copied it to, through the class and through its class loader,
 * and that a resource beside it stays refused. With instrumentation the allowed
 * read is not tested yet: the network check refuses the {@code file:} URL the
 * JDK opens for it.
 */
class ResourceReadTest {

	/**
	 * The folder holding the policies of all four modes.
	 */
	private static final String POLICY_BASE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * Where the supervised subject's classes are.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/resources";

	/**
	 * The policy allowing to read data.txt for ArchunitAspectJ.
	 */
	private static final String POLICY_ARCHUNITASPECTJ = POLICY_BASE + "archunit/aspectj/PolicyResourceRead.yaml";

	/**
	 * The policy allowing to read data.txt for ArchunitInstrumentation.
	 */
	private static final String POLICY_ARCHUNITINSTRUMENTATION = POLICY_BASE
			+ "archunit/instrumentation/PolicyResourceRead.yaml";

	/**
	 * The policy allowing to read data.txt for WalaAspectJ.
	 */
	private static final String POLICY_WALAASPECTJ = POLICY_BASE + "wala/aspectj/PolicyResourceRead.yaml";

	/**
	 * The policy allowing to read data.txt for WalaInstrumentation.
	 */
	private static final String POLICY_WALAINSTRUMENTATION = POLICY_BASE
			+ "wala/instrumentation/PolicyResourceRead.yaml";

	/**
	 * The allowed resource is readable through the class in ArchunitAspectJ.
	 *
	 * @throws IOException if reading fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void allowedResourceThroughClassMavenArchunitAspectJ() throws IOException {
		Assertions.assertEquals("resource content", ResourceReadMain.readThroughClass("data.txt"));
	}

	/**
	 * Another resource beside it is refused through the class in ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void otherResourceRefusedThroughClassMavenArchunitAspectJ() {
		Assertions.assertThrows(SecurityException.class, () -> ResourceReadMain.readThroughClass("other.txt"));
	}

	/**
	 * The allowed resource is readable through the class loader in ArchunitAspectJ.
	 *
	 * @throws IOException if reading fails
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void allowedResourceThroughClassLoaderMavenArchunitAspectJ() throws IOException {
		Assertions.assertEquals("resource content", ResourceReadMain.readThroughClassLoader("data.txt"));
	}

	/**
	 * Another resource beside it is refused through the class loader in
	 * ArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void otherResourceRefusedThroughClassLoaderMavenArchunitAspectJ() {
		Assertions.assertThrows(SecurityException.class, () -> ResourceReadMain.readThroughClassLoader("other.txt"));
	}

	/**
	 * Another resource beside it is refused through the class in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void otherResourceRefusedThroughClassMavenArchunitInstrumentation() {
		Assertions.assertThrows(SecurityException.class, () -> ResourceReadMain.readThroughClass("other.txt"));
	}

	/**
	 * Another resource beside it is refused through the class loader in
	 * ArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void otherResourceRefusedThroughClassLoaderMavenArchunitInstrumentation() {
		Assertions.assertThrows(SecurityException.class, () -> ResourceReadMain.readThroughClassLoader("other.txt"));
	}

	/**
	 * The allowed resource is readable through the class in WalaAspectJ.
	 *
	 * @throws IOException if reading fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void allowedResourceThroughClassMavenWalaAspectJ() throws IOException {
		Assertions.assertEquals("resource content", ResourceReadMain.readThroughClass("data.txt"));
	}

	/**
	 * Another resource beside it is refused through the class in WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void otherResourceRefusedThroughClassMavenWalaAspectJ() {
		Assertions.assertThrows(SecurityException.class, () -> ResourceReadMain.readThroughClass("other.txt"));
	}

	/**
	 * The allowed resource is readable through the class loader in WalaAspectJ.
	 *
	 * @throws IOException if reading fails
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void allowedResourceThroughClassLoaderMavenWalaAspectJ() throws IOException {
		Assertions.assertEquals("resource content", ResourceReadMain.readThroughClassLoader("data.txt"));
	}

	/**
	 * Another resource beside it is refused through the class loader in
	 * WalaAspectJ.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void otherResourceRefusedThroughClassLoaderMavenWalaAspectJ() {
		Assertions.assertThrows(SecurityException.class, () -> ResourceReadMain.readThroughClassLoader("other.txt"));
	}

	/**
	 * Another resource beside it is refused through the class in
	 * WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void otherResourceRefusedThroughClassMavenWalaInstrumentation() {
		Assertions.assertThrows(SecurityException.class, () -> ResourceReadMain.readThroughClass("other.txt"));
	}

	/**
	 * Another resource beside it is refused through the class loader in
	 * WalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void otherResourceRefusedThroughClassLoaderMavenWalaInstrumentation() {
		Assertions.assertThrows(SecurityException.class, () -> ResourceReadMain.readThroughClassLoader("other.txt"));
	}
}
