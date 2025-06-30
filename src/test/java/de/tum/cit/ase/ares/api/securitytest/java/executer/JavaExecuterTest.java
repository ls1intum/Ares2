package de.tum.cit.ase.ares.api.securitytest.java.executer;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JavaExecuter Tests")
public class JavaExecuterTest {

    private JavaExecuter javaExecuter;
    private BuildMode buildMode;
    private ArchitectureMode architectureMode;
    private AOPMode aopMode;
    private List<String> essentialPackages;
    private List<String> essentialClasses;
    private List<String> testClasses;
    private String packageName;
    private String mainClassInPackageName;
    private List<JavaArchitectureTestCase> javaArchitectureTestCases;
    private List<JavaAOPTestCase> javaAOPTestCases;

    @BeforeEach
    void setUp() {
        javaExecuter = new JavaExecuter();
        buildMode = BuildMode.MAVEN;
        architectureMode = ArchitectureMode.ARCHUNIT;
        aopMode = AOPMode.INSTRUMENTATION;
        essentialPackages = List.of("java.lang", "java.util");
        essentialClasses = List.of("java.lang.String", "java.util.List");
        testClasses = List.of("TestClass1", "TestClass2");
        packageName = "com.example";
        mainClassInPackageName = "MainClass";
        javaArchitectureTestCases = List.of(
                mock(JavaArchitectureTestCase.class),
                mock(JavaArchitectureTestCase.class)
        );
        javaAOPTestCases = List.of(
                mock(JavaAOPTestCase.class),
                mock(JavaAOPTestCase.class)
        );
    }

    @Nested
    @DisplayName("executeTestCases() Tests")
    class ExecuteTestCasesTests {

        @Test
        @DisplayName("Should execute test cases and set Java advice settings")
        void shouldExecuteTestCasesAndSetJavaAdviceSettings() {
            try (MockedStatic<JavaAOPTestCase> mockedJavaAOPTestCase = mockStatic(JavaAOPTestCase.class)) {
                // Arrange - mock setJavaAdviceSettingValue to do nothing
                mockedJavaAOPTestCase.when(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        any(String.class), any(), any(String.class), any(String.class)
                )).thenAnswer(invocation -> null);

                // Act
                javaExecuter.executeTestCases(
                        buildMode, architectureMode, aopMode, essentialPackages,
                        essentialClasses, testClasses, packageName, mainClassInPackageName,
                        javaArchitectureTestCases, javaAOPTestCases
                );

                // Assert - verify JavaAOPTestCase.setJavaAdviceSettingValue was called with correct parameters
                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("buildMode"), eq("MAVEN"), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));
                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("architectureMode"), eq("ARCHUNIT"), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));
                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("aopMode"), eq("INSTRUMENTATION"), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));
                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("allowedListedPackages"), eq(essentialPackages.toArray(String[]::new)), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));
                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("restrictedPackage"), eq("com.example"), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));
                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("mainClass"), eq("MainClass"), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));

                // Verify architecture test cases were executed
                for (JavaArchitectureTestCase testCase : javaArchitectureTestCases) {
                    verify(testCase).executeArchitectureTestCase("ARCHUNIT", "INSTRUMENTATION");
                }

                // Verify AOP test cases were executed
                for (JavaAOPTestCase testCase : javaAOPTestCases) {
                    verify(testCase).executeAOPTestCase("ARCHUNIT", "INSTRUMENTATION");
                }
            }
        }

        @Test
        @DisplayName("Should handle different BuildMode values")
        void shouldHandleDifferentBuildModeValues() {
            try (MockedStatic<JavaAOPTestCase> mockedJavaAOPTestCase = mockStatic(JavaAOPTestCase.class)) {
                // Arrange - mock setJavaAdviceSettingValue to do nothing
                mockedJavaAOPTestCase.when(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        any(String.class), any(), any(String.class), any(String.class)
                )).thenAnswer(invocation -> null);

                // Test with GRADLE
                javaExecuter.executeTestCases(
                        BuildMode.GRADLE, architectureMode, aopMode, essentialPackages,
                        essentialClasses, testClasses, packageName, mainClassInPackageName,
                        javaArchitectureTestCases, javaAOPTestCases
                );

                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("buildMode"), eq("GRADLE"), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));
            }
        }

        @Test
        @DisplayName("Should handle different ArchitectureMode values")
        void shouldHandleDifferentArchitectureModeValues() {
            try (MockedStatic<JavaAOPTestCase> mockedJavaAOPTestCase = mockStatic(JavaAOPTestCase.class)) {
                // Arrange - mock setJavaAdviceSettingValue to do nothing
                mockedJavaAOPTestCase.when(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        any(String.class), any(), any(String.class), any(String.class)
                )).thenAnswer(invocation -> null);

                // Test with WALA
                javaExecuter.executeTestCases(
                        buildMode, ArchitectureMode.WALA, aopMode, essentialPackages,
                        essentialClasses, testClasses, packageName, mainClassInPackageName,
                        javaArchitectureTestCases, javaAOPTestCases
                );

                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("architectureMode"), eq("WALA"), eq("WALA"), eq("INSTRUMENTATION")
                ));
            }
        }

        @Test
        @DisplayName("Should handle different AOPMode values")
        void shouldHandleDifferentAOPModeValues() {
            try (MockedStatic<JavaAOPTestCase> mockedJavaAOPTestCase = mockStatic(JavaAOPTestCase.class)) {
                // Arrange - mock setJavaAdviceSettingValue to do nothing
                mockedJavaAOPTestCase.when(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        any(String.class), any(), any(String.class), any(String.class)
                )).thenAnswer(invocation -> null);

                // Test with ASPECTJ
                javaExecuter.executeTestCases(
                        buildMode, architectureMode, AOPMode.ASPECTJ, essentialPackages,
                        essentialClasses, testClasses, packageName, mainClassInPackageName,
                        javaArchitectureTestCases, javaAOPTestCases
                );

                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("aopMode"), eq("ASPECTJ"), eq("ARCHUNIT"), eq("ASPECTJ")
                ));
            }
        }

        @Test
        @DisplayName("Should merge essential classes and test classes correctly")
        void shouldMergeEssentialClassesAndTestClassesCorrectly() {
            try (MockedStatic<JavaAOPTestCase> mockedJavaAOPTestCase = mockStatic(JavaAOPTestCase.class)) {
                // Arrange - mock setJavaAdviceSettingValue to do nothing
                mockedJavaAOPTestCase.when(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        any(String.class), any(), any(String.class), any(String.class)
                )).thenAnswer(invocation -> null);

                // Act
                javaExecuter.executeTestCases(
                        buildMode, architectureMode, aopMode, essentialPackages,
                        essentialClasses, testClasses, packageName, mainClassInPackageName,
                        javaArchitectureTestCases, javaAOPTestCases
                );

                // Assert - verify allowedListedClasses contains both essential and test classes
                String[] expectedClasses = {"java.lang.String", "java.util.List", "TestClass1", "TestClass2"};
                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("allowedListedClasses"), eq(expectedClasses), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));
            }
        }

        @Test
        @DisplayName("Should handle empty lists")
        void shouldHandleEmptyLists() {
            try (MockedStatic<JavaAOPTestCase> mockedJavaAOPTestCase = mockStatic(JavaAOPTestCase.class)) {
                // Arrange - mock setJavaAdviceSettingValue to do nothing
                mockedJavaAOPTestCase.when(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        any(String.class), any(), any(String.class), any(String.class)
                )).thenAnswer(invocation -> null);

                List<String> emptyPackages = List.of();
                List<String> emptyClasses = List.of();
                List<String> emptyTestClasses = List.of();
                List<JavaArchitectureTestCase> emptyArchTestCases = List.of();
                List<JavaAOPTestCase> emptyAOPTestCases = List.of();

                // Act
                javaExecuter.executeTestCases(
                        buildMode, architectureMode, aopMode, emptyPackages,
                        emptyClasses, emptyTestClasses, packageName, mainClassInPackageName,
                        emptyArchTestCases, emptyAOPTestCases
                );

                // Assert - verify empty arrays were passed
                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("allowedListedPackages"), eq(new String[0]), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));
                mockedJavaAOPTestCase.verify(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        eq("allowedListedClasses"), eq(new String[0]), eq("ARCHUNIT"), eq("INSTRUMENTATION")
                ));
            }
        }

        @Test
        @DisplayName("Should not execute test cases when lists are empty")
        void shouldNotExecuteTestCasesWhenListsAreEmpty() {
            try (MockedStatic<JavaAOPTestCase> mockedJavaAOPTestCase = mockStatic(JavaAOPTestCase.class)) {
                // Arrange - mock setJavaAdviceSettingValue to do nothing
                mockedJavaAOPTestCase.when(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        any(String.class), any(), any(String.class), any(String.class)
                )).thenAnswer(invocation -> null);

                List<JavaArchitectureTestCase> emptyArchTestCases = List.of();
                List<JavaAOPTestCase> emptyAOPTestCases = List.of();

                // Act
                javaExecuter.executeTestCases(
                        buildMode, architectureMode, aopMode, essentialPackages,
                        essentialClasses, testClasses, packageName, mainClassInPackageName,
                        emptyArchTestCases, emptyAOPTestCases
                );

                // Assert - no test case executions should occur
                verifyNoInteractions(javaArchitectureTestCases.toArray());
                verifyNoInteractions(javaAOPTestCases.toArray());
            }
        }
    }

    @Nested
    @DisplayName("Interface Implementation Tests")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Should implement Executer interface")
        void shouldImplementExecuterInterface() {
            // Assert
            assertTrue(javaExecuter instanceof Executer);
        }

        @Test
        @DisplayName("Should override executeTestCases method from Executer interface")
        void shouldOverrideExecuteTestCasesMethodFromExecuterInterface() {
            try (MockedStatic<JavaAOPTestCase> mockedJavaAOPTestCase = mockStatic(JavaAOPTestCase.class)) {
                // Arrange - mock setJavaAdviceSettingValue to do nothing
                mockedJavaAOPTestCase.when(() -> JavaAOPTestCase.setJavaAdviceSettingValue(
                        any(String.class), any(), any(String.class), any(String.class)
                )).thenAnswer(invocation -> null);

                // This test ensures the method signature matches the interface
                assertDoesNotThrow(() -> {
                    javaExecuter.executeTestCases(
                            buildMode, architectureMode, aopMode, essentialPackages,
                            essentialClasses, testClasses, packageName, mainClassInPackageName,
                            javaArchitectureTestCases, javaAOPTestCases
                    );
                });
            }
        }
    }
}
