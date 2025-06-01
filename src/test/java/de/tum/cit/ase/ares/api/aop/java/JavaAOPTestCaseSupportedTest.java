package de.tum.cit.ase.ares.api.aop.java;

import de.tum.cit.ase.ares.api.aop.AOPTestCaseSupported;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Test suite for JavaAOPTestCaseSupported enum.
 *
 * <p>Description: This class contains unit tests that verify the correctness and completeness
 * of the JavaAOPTestCaseSupported enum, ensuring that all constants are present in the declared
 * order and that the getDynamic method returns the expected list.
 *
 * <p>Design Rationale: Centralizing all tests for enum constants and dynamic behavior in a single
 * test class improves maintainability, readability, and ensures consistent 100 percent coverage.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaAOPTestCaseSupportedTest {

    /**
     * Validates that the enum defines all expected constants in the declared order and that each
     * constant implements the AOPTestCaseSupported interface.
     *
     * <p>Description: This test retrieves the values of the enum and confirms the count matches
     * the expected number of constants. It also checks that the sequence of constants aligns with
     * their declaration in the source code, and that each enum constant is an instance of
     * AOPTestCaseSupported.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testEnumConstantsAndInterfaceImplementation() {
        JavaAOPTestCaseSupported[] actual = JavaAOPTestCaseSupported.values();
        JavaAOPTestCaseSupported[] expected =
                new JavaAOPTestCaseSupported[]{
                        JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION,
                        JavaAOPTestCaseSupported.NETWORK_CONNECTION,
                        JavaAOPTestCaseSupported.COMMAND_EXECUTION,
                        JavaAOPTestCaseSupported.THREAD_CREATION
                };

        Assertions.assertAll("Enum constants and interface implementation",
                () -> Assertions.assertNotNull(actual, "actual must not be null"),
                () -> Assertions.assertEquals(4, actual.length, "actual should contain exactly four values"),
                () -> Assertions.assertArrayEquals(expected, actual, "actual must match the order of expected"),
                () -> Assertions.assertTrue(Arrays.stream(actual).allMatch(Objects::nonNull), "All values of actual must not be null")
        );
    }

    /**
     * Ensures that getDynamic returns all enum constants in their declared order and is not null.
     *
     * <p>Description: This test calls getDynamic() on one of the enum constants and verifies
     * that the returned list contains exactly the four enum constants, in the same sequence as
     * they are declared, and that the result is not null.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testGetDynamicReturnsAllConstantsInOrder() {
        List<AOPTestCaseSupported> actual = JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION.getDynamic();
        List<AOPTestCaseSupported> expected =
                List.of(
                        JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION,
                        JavaAOPTestCaseSupported.NETWORK_CONNECTION,
                        JavaAOPTestCaseSupported.COMMAND_EXECUTION,
                        JavaAOPTestCaseSupported.THREAD_CREATION
                );

        Assertions.assertAll("getDynamic result",
                () -> Assertions.assertNotNull(actual, "actual must not be null"),
                () -> Assertions.assertEquals(4, actual.size(), "actual should contain exactly four values"),
                () -> Assertions.assertEquals(expected, actual, "actual must match the order of expected"),
                () -> Assertions.assertTrue(actual.stream().allMatch(Objects::nonNull), "All values of actual must not be null")
        );
    }
}