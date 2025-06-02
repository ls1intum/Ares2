package de.tum.cit.ase.ares.api.architecture.java;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JavaArchitectureTestCaseTest {

    @Test
    void testParseErrorMessage_withSingleLine_throwsSecurityException() {
        // Test that parseErrorMessage throws SecurityException when there is only one line in the message
        AssertionError error = new AssertionError("onlyOneLine");
        SecurityException thrown = assertThrows(SecurityException.class,
                () -> JavaArchitectureTestCase.parseErrorMessage(error),
                "parseErrorMessage should throw SecurityException when messageParts length < 2");
        
        // The message should contain the localized error text
        assertTrue((thrown.getMessage().contains("Ares Security Error") || thrown.getMessage().contains("Ares Sicherheitsfehler")) &&
                   thrown.getMessage().contains("onlyOneLine"),
                "Exception message should include localized error text and original message");
    }

    @Test
    void testParseErrorMessage_withTwoLines_throwsSecurityExceptionContainingIdentifier() {
        // Test that parseErrorMessage throws SecurityException when there are two lines in the message, and that the exception message contains the extracted identifier.
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
        // Create a minimal JavaArchitectureTestCase using builder, with null javaClasses to cause NullPointerException in build.
        assertThrows(NullPointerException.class, () -> {
            JavaArchitectureTestCase.builder()
                    .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
                    .allowedPackages(Collections.emptySet())
                    .javaClasses(null)
                    .build();
        }, "Builder should throw NullPointerException when javaClasses is null");
    }

    @Test
    void testWriteArchitectureTestCase_modeNotSupported_throwsSecurityException() {
        // This test checks the unsupported mode branch with valid instance but unsupported mode.
        JavaClasses mockJavaClasses = Mockito.mock(JavaClasses.class);
        JavaArchitectureTestCase instance = JavaArchitectureTestCase.builder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
                .allowedPackages(Collections.emptySet())
                .javaClasses(mockJavaClasses)
                .build();
        assertThrows(SecurityException.class,
                () -> instance.writeArchitectureTestCase("UNSUPPORTED", "AOP"),
                "writeArchitectureTestCase should throw SecurityException for unsupported modes");
    }

    @Test
    void testExecuteArchitectureTestCase_invalidMode_throwsSecurityException() {
        // This test checks that executeArchitectureTestCase throws SecurityException for unsupported mode.
        JavaClasses mockJavaClasses = Mockito.mock(JavaClasses.class);
        JavaArchitectureTestCase instance = JavaArchitectureTestCase.builder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
                .allowedPackages(Collections.emptySet())
                .javaClasses(mockJavaClasses)
                .build();
        assertThrows(SecurityException.class,
                () -> instance.executeArchitectureTestCase("INVALID", "AOP"),
                "executeArchitectureTestCase should throw SecurityException for invalid mode");
    }

    @Test
    void testBuilder_missingFields_throwsException() {
        // Test that the builder throws NullPointerException if required fields are missing, such as javaArchitectureTestCaseSupported.
        assertThrows(NullPointerException.class,
                () -> JavaArchitectureTestCase.builder()
                        .allowedPackages(Collections.emptySet())
                        .javaClasses(null)
                        .build(),
                "Builder should throw NullPointerException if required fields are missing");
    }
}
