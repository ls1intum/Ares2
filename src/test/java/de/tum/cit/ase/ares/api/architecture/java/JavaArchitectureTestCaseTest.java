package de.tum.cit.ase.ares.api.architecture.java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.tngtech.archunit.core.domain.JavaClasses;

import de.tum.cit.ase.ares.api.localization.Messages;

public class JavaArchitectureTestCaseTest {

	@Test
	void testParseErrorMessage_withSingleLine_throwsSecurityException() {
		// Test that parseErrorMessage throws SecurityException when there is only one
		// line in the message
		AssertionError error = new AssertionError("onlyOneLine");
		SecurityException thrown = assertThrows(SecurityException.class,
				() -> JavaArchitectureTestCase.parseErrorMessage(error),
				"parseErrorMessage should throw SecurityException when messageParts length < 2");

		// The message should contain the localized error text
		assertTrue(
				(thrown.getMessage().contains("Ares Security Error")
						|| thrown.getMessage().contains("Ares Sicherheitsfehler"))
						&& thrown.getMessage().contains("onlyOneLine"),
				"Exception message should include localized error text and original message");
	}

	@Test
	void testParseErrorMessage_withTwoLines_throwsSecurityExceptionContainingIdentifier() {
		// Test that parseErrorMessage throws SecurityException when there are two lines
		// in the message, and that the exception message contains the extracted
		// identifier.
		String message = "Rule violated 'TestIdentifier' details\nSecond line explanation";
		AssertionError error = new AssertionError(message);
		SecurityException thrown = assertThrows(SecurityException.class,
				() -> JavaArchitectureTestCase.parseErrorMessage(error),
				"parseErrorMessage should throw SecurityException when messageParts length >= 2");
		assertTrue(thrown.getMessage().contains("TestIdentifier"),
				"Exception message should include extracted identifier from the first line");
	}

	@Test
	void testWriteArchitectureTestCase_invalidMode_throwsSecurityException() {
		// Create a minimal JavaArchitectureTestCase using builder, with null
		// javaClasses to cause NullPointerException in build.
		assertThrows(NullPointerException.class, () -> {
			JavaArchitectureTestCase.builder()
					.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
					.allowedPackages(Collections.emptySet()).javaClasses(null).build();
		}, "Builder should throw NullPointerException when javaClasses is null");
	}

	@Test
	void testWriteArchitectureTestCase_modeNotSupported_throwsSecurityException() {
		// This test checks the unsupported mode branch with valid instance but
		// unsupported mode.
		JavaClasses mockJavaClasses = Mockito.mock(JavaClasses.class);
		JavaArchitectureTestCase instance = JavaArchitectureTestCase.builder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(Collections.emptySet()).javaClasses(mockJavaClasses).build();
		assertThrows(SecurityException.class, () -> instance.writeArchitectureTestCase("UNSUPPORTED", "AOP"),
				"writeArchitectureTestCase should throw SecurityException for unsupported modes");
	}

	@Test
	void testExecuteArchitectureTestCase_invalidMode_throwsSecurityException() {
		// This test checks that executeArchitectureTestCase throws SecurityException
		// for unsupported mode.
		JavaClasses mockJavaClasses = Mockito.mock(JavaClasses.class);
		JavaArchitectureTestCase instance = JavaArchitectureTestCase.builder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(Collections.emptySet()).javaClasses(mockJavaClasses).build();
		assertThrows(SecurityException.class, () -> instance.executeArchitectureTestCase("INVALID", "AOP"),
				"executeArchitectureTestCase should throw SecurityException for invalid mode");
	}

	@Test
	void testBuilder_missingFields_throwsException() {
		// Test that the builder throws NullPointerException if required fields are
		// missing, such as javaArchitectureTestCaseSupported.
		assertThrows(
				NullPointerException.class, () -> JavaArchitectureTestCase.builder()
						.allowedPackages(Collections.emptySet()).javaClasses(null).build(),
				"Builder should throw NullPointerException if required fields are missing");
	}

	@Test
	void testParseErrorMessage_withPackageImportViolation_extractsForbiddenPackages() {
		// Test that parseErrorMessage correctly extracts forbidden packages from
		// package import violation messages
		String message = "Architecture Violation [Priority: MEDIUM] - Rule 'Imports forbidden packages' was violated (2 times):\n"
				+ "Class <com.example.Test> depends on <java.io.File> in (Test.java:0)\n"
				+ "Class <com.example.Test> depends on <java.nio.file.Path> in (Test.java:0)";
		AssertionError error = new AssertionError(message);
		SecurityException thrown = assertThrows(SecurityException.class,
				() -> JavaArchitectureTestCase.parseErrorMessage(error),
				"parseErrorMessage should throw SecurityException for package import violations");
		// The message should contain the forbidden packages
		assertTrue(thrown.getMessage().contains("java.io") || thrown.getMessage().contains("java.nio.file"),
				"Exception message should include the forbidden packages from the violation");
		assertTrue(
				thrown.getMessage().contains("Ares Security Error")
						|| thrown.getMessage().contains("Ares Sicherheitsfehler"),
				"Exception message should contain Ares Security Error prefix");
	}

	@Test
	void testParseErrorMessage_withBritishSerializationRule_normalisesAction() {
		String message = "Architecture Violation [Priority: MEDIUM] - Rule 'Serialises objects' was violated (1 time):\n"
				+ "Method <com.example.Test.serialize()> calls method <java.io.ObjectOutputStream.writeObject(java.lang.Object)>";
		AssertionError error = new AssertionError(message);

		SecurityException thrown = assertThrows(SecurityException.class,
				() -> JavaArchitectureTestCase.parseErrorMessage(error));

		assertTrue(thrown.getMessage().contains("serialise objects"),
				() -> "Exception message should use the normalised serialisation action: " + thrown.getMessage());
		assertFalse(thrown.getMessage().contains("Serialises objects"),
				() -> "Exception message must not leak the original capitalised rule label: " + thrown.getMessage());
	}

	@Test
	void testParseErrorMessage_callerWithQualifiedParameterType_namesTheDeclaringType() {
		// A caller rendered with a fully qualified parameter type ends in a dot of its
		// own. The declaring type must be taken from the part before the parameter
		// list, otherwise the reported caller is cut inside that list.
		String message = "Architecture Violation [Priority: MEDIUM] - Rule 'Accesses file system' was violated (1 times):\n"
				+ "Method <com.example.Sender.sendWithDataOutputStream(java.lang.String)> "
				+ "calls method <java.io.DataOutputStream.writeUTF(java.lang.String)> in (Sender.java:1)";
		AssertionError error = new AssertionError(message);
		SecurityException thrown = assertThrows(SecurityException.class,
				() -> JavaArchitectureTestCase.parseErrorMessage(error),
				"parseErrorMessage should throw SecurityException for a file system violation");
		// Compared against the localized message for the whole expected parse, so the
		// assertion holds in any locale and pins every argument rather than a suffix.
		String expected = Messages.localized("security.archunit.violation.error",
				"com.example.Sender.sendWithDataOutputStream(java.lang.String)", "access the file system",
				"java.io.DataOutputStream.writeUTF(java.lang.String)", "com.example.Sender");
		assertEquals(expected, thrown.getMessage(),
				"The declaring type should be reported without cutting inside the parameter list");
	}

	@Test
	void testParseErrorMessage_callerWithEmptyParameterList_namesTheDeclaringType() {
		// The shape the ArchUnit path produces, kept as a regression guard: an empty
		// parameter list carries no dot, so this spelling was already handled and must
		// keep reporting the same declaring type.
		String message = "Architecture Violation [Priority: MEDIUM] - Rule 'Accesses network' was violated (1 times):\n"
				+ "Method <com.example.Sender.send()> calls method <java.net.Socket.connect()> in (Sender.java:1)";
		AssertionError error = new AssertionError(message);
		SecurityException thrown = assertThrows(SecurityException.class,
				() -> JavaArchitectureTestCase.parseErrorMessage(error),
				"parseErrorMessage should throw SecurityException for a network violation");
		String expected = Messages.localized("security.archunit.violation.error", "com.example.Sender.send()",
				"access the network", "java.net.Socket.connect()", "com.example.Sender");
		assertEquals(expected, thrown.getMessage(), "The declaring type should be reported unchanged");
	}

	/**
	 * The base class writes a file by building a subclass and asking it. Both
	 * branches used to build that subclass without the supervised scope, and a
	 * generated file carrying no scope refuses to be written at all, so both
	 * supported modes threw here. Nothing noticed, because the production path
	 * converts through {@code ArchitectureMode} and never reaches these branches.
	 * These two pin that the scope reaches the delegate.
	 */
	@Test
	void testWriteArchitectureTestCase_archunit_carriesTheSupervisedScopeIntoTheDelegate() {
		JavaClasses mockJavaClasses = Mockito.mock(JavaClasses.class);
		Mockito.when(mockJavaClasses.iterator()).thenReturn(Collections.emptyIterator());
		JavaArchitectureTestCase instance = JavaArchitectureTestCase.builder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(Collections.emptySet()).javaClasses(mockJavaClasses)
				.supervisedPackage("de.tum.cit.aet").supervisedScopeWasDerived(true).build();

		String written = instance.writeArchitectureTestCase("ARCHUNIT", "");

		assertTrue(written.contains("JavaArchunitSupervisedClasses.validated(\"de.tum.cit.aet\")"),
				"the delegate must be given the scope, or the generated file analyses nothing: " + written);
	}

	@Test
	void testWriteArchitectureTestCase_wala_carriesTheSupervisedScopeIntoTheDelegate() {
		JavaClasses mockJavaClasses = Mockito.mock(JavaClasses.class);
		Mockito.when(mockJavaClasses.iterator()).thenReturn(Collections.emptyIterator());
		// The emitted call graph is a literal expression built at the generated test's
		// own runtime, so nothing here reads this object.
		JavaArchitectureTestCase instance = JavaArchitectureTestCase.builder()
				.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
				.allowedPackages(Collections.emptySet()).javaClasses(mockJavaClasses)
				.callGraph(Mockito.mock(com.ibm.wala.ipa.callgraph.CallGraph.class)).supervisedPackage("de.tum.cit.aet")
				.supervisedScopeWasDerived(false).build();

		String written = instance.writeArchitectureTestCase("WALA", "");

		assertTrue(written.contains("JavaArchunitSupervisedClasses.pinned(\"de.tum.cit.aet\")"),
				"the delegate must be given the scope, or the generated file analyses nothing: " + written);
	}
}
