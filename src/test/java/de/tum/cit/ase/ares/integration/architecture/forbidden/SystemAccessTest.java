package de.tum.cit.ase.ares.integration.architecture.forbidden;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;

/**
 * Base class for system access tests providing common constants and helper methods
 * for testing security policies across different system operations (file, thread, command).
 */
public abstract class SystemAccessTest {

    // <editor-fold desc="Constants">
    protected static final String ERROR_MESSAGE = "No Security Exception was thrown.";
    protected static final String ERR_SECURITY_EN = "Ares Security Error";
    protected static final String ERR_SECURITY_DE = "Ares Sicherheitsfehler";
    protected static final String REASON_EN = "(Reason: Student-Code; Stage: Execution)";
    protected static final String REASON_DE = "(Grund: Student-Code; Phase: Ausführung)";
    protected static final String TRIED_EN = "tried";
    protected static final String TRIED_DE = "hat versucht,";
    protected static final String BLOCKED_EN = "was blocked by Ares.";
    protected static final String BLOCKED_DE = "wurde jedoch von Ares blockiert.";
    // </editor-fold>

    //<editor-fold desc="Archunit AspectJ Policy Files">
    /**
     * Base path for ArchUnit AspectJ policy
     */
    protected static final String ARCHUNIT_ASPECTJ_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyEverythingForbidden.yaml";
    //</editor-fold>

    //<editor-fold desc="Archunit Instrumentation Policy Files">
    /**
     * Base path for ArchUnit Instrumentation policy
     */
    protected static final String ARCHUNIT_INSTRUMENTATION_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyEverythingForbidden.yaml";
    //</editor-fold>

    //<editor-fold desc="Wala AspectJ Policy Files">
    /**
     * Base path for Wala AspectJ policy
     */
    protected static final String WALA_ASPECTJ_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyEverythingForbidden.yaml";
    //</editor-fold>

    //<editor-fold desc="Wala Instrumentation Policy Files">
    /**
     * Base path for Wala Instrumentation policy
     */
    protected static final String WALA_INSTRUMENTATION_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyEverythingForbidden.yaml";
    //</editor-fold>

    //<editor-fold desc="General Access Tests">

    /**
     * Common helper that verifies the expected general parts of the error message.
     *
     * @param actualMessage   The message from the thrown exception
     * @param operationTextEN The operation-specific substring in English
     * @param operationTextDE The operation-specific substring in German
     * @param clazz           The class that should be mentioned in the security
     *                        violation
     */
    protected void assertGeneralErrorMessage(
            String actualMessage,
            String operationTextEN,
            String operationTextDE,
            Class<?> clazz) {

        Assertions.assertTrue(
                actualMessage.contains(ERR_SECURITY_EN) || actualMessage.contains(ERR_SECURITY_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        ERR_SECURITY_EN, ERR_SECURITY_DE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(REASON_EN) || actualMessage.contains(REASON_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        REASON_EN, REASON_DE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(TRIED_EN) || actualMessage.contains(TRIED_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        TRIED_EN, TRIED_DE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(clazz.getName()),
                () -> String.format(
                        "Exception message should contain '%s', but was:%n%s",
                        clazz.getName(), actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(operationTextEN) || actualMessage.contains(operationTextDE),
                () -> String.format(
                        "Exception message should indicate the operation by containing '%s' or '%s', but was:%n%s",
                        operationTextEN, operationTextDE, actualMessage));

        Assertions.assertTrue(
                actualMessage.contains(BLOCKED_EN) || actualMessage.contains(BLOCKED_DE),
                () -> String.format(
                        "Exception message should contain '%s' or '%s', but was:%n%s",
                        BLOCKED_EN, BLOCKED_DE, actualMessage));
    }
    //</editor-fold>

    //<editor-fold desc="File System Access Tests">

    /**
     * Common helper that verifies the expected general parts of the error message.
     * This version is used for file system access tests where path validation is
     * required.
     *
     * @param actualMessage   The message from the thrown exception.
     * @param operationTextEN The operation-specific substring in English (e.g.,
     *                        "illegally read from", "illegally write from",
     *                        "illegally execute from").
     * @param operationTextDE The operation-specific substring in German (e.g.,
     *                        "illegal read von", "illegal write von",
     *                        "illegal execute von").
     * @param clazz           The class that should be mentioned in the security
     *                        violation
     */
    protected void assertGeneralErrorMessageWithPath(
            String actualMessage,
            String operationTextEN,
            String operationTextDE,
            Class<?> clazz) {

        Path expectedPath = Paths.get("src", "test", "java", "de", "tum", "cit", "ase", "ares", "integration",
                "aop", "forbidden", "subject", "nottrusted.txt");
        String nativePath = expectedPath.toString();
        String unixPath = nativePath.replace(expectedPath.getFileSystem().getSeparator(), "/");

        // Validate common error message parts
        assertGeneralErrorMessage(actualMessage, operationTextEN, operationTextDE, clazz);

        // Additional path validation for file system operations
        Assertions.assertTrue(
                Stream.of(nativePath, unixPath).anyMatch(actualMessage::contains),
                () -> String.format(
                        "Exception message should contain the path '%s' (or '%s'), but was:%n%s",
                        nativePath, unixPath, actualMessage));
    }

    /**
     * Test that the given executable throws a SecurityException with the expected
     * message for file system read operations.
     *
     * @param executable The executable that should throw a SecurityException
     * @param clazz      The class that performed the read operation
     */
    protected void assertAresSecurityExceptionRead(Executable executable, Class<?> clazz) {
        SecurityException securityException = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessageWithPath(securityException.getMessage(), "illegally read", "illegal read", clazz);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected
     * message for file system overwrite operations.
     *
     * @param executable The executable that should throw a SecurityException
     * @param clazz      The class that performed the overwrite operation
     */
    protected void assertAresSecurityExceptionOverwrite(Executable executable, Class<?> clazz) {
        SecurityException securityException = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessageWithPath(securityException.getMessage(), "illegally overwrite", "illegal overwrite", clazz);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected
     * message for file system execution operations.
     *
     * @param executable The executable that should throw a SecurityException
     * @param clazz      The class that performed the execution operation
     */
    protected void assertAresSecurityExceptionExecution(Executable executable, Class<?> clazz) {
        SecurityException securityException = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessageWithPath(securityException.getMessage(), "illegally execute", "illegal execute", clazz);
    }

    /**
     * Test that the given executable throws a SecurityException with the expected
     * message for file system delete operations.
     *
     * @param executable The executable that should throw a SecurityException
     * @param clazz      The class that performed the delete operation
     */
    protected void assertAresSecurityExceptionDelete(Executable executable, Class<?> clazz) {
        SecurityException securityException = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
        assertGeneralErrorMessageWithPath(securityException.getMessage(), "illegally delete", "illegal delete", clazz);
    }
    //</editor-fold>

    //<editor-fold desc="Command System Access Tests">
    /**
     * Common helper that verifies the expected general parts of the error message.
     * This version is used for command system access tests where command validation
     * is required.
     *
     * @param actualMessage   The message from the thrown exception.
     * @param operationTextEN The operation-specific substring in English (e.g.,
     *                        "illegally execute command", "illegally run command").
     * @param operationTextDE The operation-specific substring in German (e.g.,
     *                        "illegal Befehle ausführen", "illegal Befehle
     *                        ausführen").
     * @param clazz           The class that should be mentioned in the security
     *                        violation
     */
    protected void assertGeneralErrorMessageWithCommand(
            String actualMessage,
            String operationTextEN,
            String operationTextDE,
            Class<?> clazz) {

        // Validate common error message parts
        assertGeneralErrorMessage(actualMessage, operationTextEN, operationTextDE, clazz);

        // Additional command-specific validation
        Assertions.assertTrue(
                actualMessage.contains("command") || actualMessage.contains("Befehl"),
                () -> String.format(
                        "Exception message should contain 'command' or 'Befehl', but was:%n%s",
                        actualMessage));
    }

    /**
     * Test that the given executable DOES throw a SecurityException with the
     * expected
     * message for command execution operations.
     *
     * @param executable    The executable that should throw a SecurityException
     * @param expectedClass The class that should be mentioned in the security
     *                      violation
     */
    protected void assertAresSecurityExceptionCommand(Executable executable, Class<?> expectedClass) {
        SecurityException securityException = Assertions.assertThrows(SecurityException.class, executable,
                ERROR_MESSAGE);
        assertGeneralErrorMessageWithCommand(securityException.getMessage(), "illegally execute",
                "illegal ausgeführt", expectedClass);
    }
    //</editor-fold>

    //<editor-fold desc="Thread System Access Tests">
    /**
     * Common helper that verifies the expected general parts of the error message.
     * This version is used for thread system access tests where thread name
     * validation is required.
     *
     * @param actualMessage   The message from the thrown exception.
     * @param operationTextEN The operation-specific substring in English (e.g.,
     *                        "illegally create threads", "illegally start
     *                        threads").
     * @param operationTextDE The operation-specific substring in German (e.g.,
     *                        "illegal Threads erstellen", "illegal Threads
     *                        starten").
     * @param clazz           The class that should be mentioned in the security
     *                        violation
     */
    protected void assertGeneralErrorMessageWithThread(
            String actualMessage,
            String operationTextEN,
            String operationTextDE,
            Class<?> clazz) {

        // Validate common error message parts
        assertGeneralErrorMessage(actualMessage, operationTextEN, operationTextDE, clazz);

        // Additional thread-specific validation
        Assertions.assertTrue(
                actualMessage.contains("Thread") || actualMessage.contains("thread"),
                () -> String.format(
                        "Exception message should contain 'Thread' or 'thread', but was:%n%s",
                        actualMessage));
    }

    /**
     * Test that the given executable throws a SecurityException with the expected
     * message for thread creation operations.
     *
     * @param executable The executable that should throw a SecurityException
     * @param clazz      The class that performed the thread creation operation
     */
    protected void assertAresSecurityExceptionThread(Executable executable, Class<?> clazz) {
        SecurityException securityException = Assertions.assertThrows(SecurityException.class, executable,
                ERROR_MESSAGE);
        assertGeneralErrorMessageWithThread(securityException.getMessage(), "illegally create",
                "illegal erstellt", clazz);
    }
    //</editor-fold>

    /**
     * Common test helper that executes a test method and verifies it fails with a SecurityException.
     *
     * @param clazz      The test class containing the method
     * @param methodName The name of the test method to execute
     */
    protected void testtest(Class<?> clazz, String methodName) {
        Events testEvents = EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter(
                        "junit.jupiter.conditions.deactivate",
                        "org.junit.*DisabledCondition"
                )
                .selectors(selectMethod(clazz, methodName))
                .execute()
                .testEvents();

        testEvents.assertStatistics(stats -> stats.failed(1).aborted(0).succeeded(0));
        testEvents.assertThatEvents().haveExactly(1, event(test(methodName), finishedWithFailure(instanceOf(SecurityException.class))));
    }
}
