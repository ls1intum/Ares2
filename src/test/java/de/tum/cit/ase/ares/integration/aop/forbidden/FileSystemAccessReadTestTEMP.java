package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.memoryCacheImageInputStream.MemoryCacheImageInputStreamReadMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.objectInputStream.ObjectInputStreamReadMain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

class FileSystemAccessReadTestTEMP {

        private static final String ERROR_MESSAGE = "No Security Exception was thrown. Check if the policy is correctly applied.";
        private static final String ERR_SECURITY_EN = "Ares Security Error";
        private static final String ERR_SECURITY_DE = "Ares Sicherheitsfehler";
        private static final String REASON_EN = "(Reason: Student-Code; Stage: Execution)";
        private static final String REASON_DE = "(Grund: Student-Code; Phase: Ausführung)";
        private static final String TRIED_EN = "tried";
        private static final String TRIED_DE = "hat versucht,";
        private static final String BLOCKED_EN = "was blocked by Ares.";
        private static final String BLOCKED_DE = "wurde jedoch von Ares blockiert.";

        // <editor-fold desc="Helpers">

        /**
         * Common helper that verifies the expected general parts of the error message.
         *
         * @param actualMessage   The message from the thrown exception.
         * @param operationTextEN The operation-specific substring in English (e.g.,
         *                        "illegally read from", "illegally write from",
         *                        "illegally execute from").
         * @param operationTextDE The operation-specific substring in German (e.g.,
         *                        "illegally read from", "illegally write from",
         *                        "illegally execute from").
         */
        private void assertGeneralErrorMessage(
                        String actualMessage,
                        String operationTextEN,
                        String operationTextDE,
                        Class<?> clazz) {
                Path expectedPath = Paths.get(
                                "src", "test", "java", "de", "tum", "cit", "ase",
                                "ares", "integration", "aop", "forbidden", "subject", "nottrusted.txt");
                String nativePath = expectedPath.toString();
                String unixPath = nativePath.replace(expectedPath.getFileSystem().getSeparator(), "/");

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
                                Stream.of(nativePath, unixPath).anyMatch(actualMessage::contains),
                                () -> String.format(
                                                "Exception message should contain the path '%s' (or '%s'), but was:%n%s",
                                                nativePath, unixPath, actualMessage));

                Assertions.assertTrue(
                                actualMessage.contains(BLOCKED_EN) || actualMessage.contains(BLOCKED_DE),
                                () -> String.format(
                                                "Exception message should contain '%s' or '%s', but was:%n%s",
                                                BLOCKED_EN, BLOCKED_DE, actualMessage));
        }

        /**
         * Test that the given executable throws a SecurityException with the expected
         * message.
         *
         * @param executable The executable that should throw a SecurityException
         */
        private void assertAresSecurityExceptionRead(Executable executable, Class<?> clazz) {
                SecurityException se = Assertions.assertThrows(SecurityException.class, executable, ERROR_MESSAGE);
                assertGeneralErrorMessage(se.getMessage(), "illegally read from", "illegal read von", clazz);        }
        // </editor-fold> 
        


}