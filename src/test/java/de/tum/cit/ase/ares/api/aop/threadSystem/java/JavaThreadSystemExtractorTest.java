package de.tum.cit.ase.ares.api.aop.threadSystem.java;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ThreadPermission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

/**
 * Unit tests for JavaThreadSystemExtractor._
 *
 * <p>Description: Checks extraction of thread numbers and classes from permissions.
 *
 * <p>Design Rationale: Validates policy-driven thread creation limits and class restrictions.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaThreadSystemExtractorTest {

    /**
     * Tests static extractThreadNumbers and extractThreadClasses methods.
     *
     * <p>Description: Supplies ThreadPermission records and asserts correct string lists.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testExtractThreadNumbersAndClasses() {
        List<ThreadPermission> configs = List.of(
                ThreadPermission.builder().createTheFollowingNumberOfThreads(0).ofThisClass("C0").build(),
                ThreadPermission.builder().createTheFollowingNumberOfThreads(1).ofThisClass("C1").build(),
                ThreadPermission.builder().createTheFollowingNumberOfThreads(10).ofThisClass("C2").build()
        );
        Assertions.assertEquals(List.of("0", "1", "10"), JavaThreadSystemExtractor.extractThreadNumbers(configs));
        Assertions.assertEquals(List.of("C0", "C1", "C2"), JavaThreadSystemExtractor.extractThreadClasses(configs));
    }

    /**
     * Tests instance methods getPermittedNumberOfThreads and getPermittedThreadClasses.
     *
     * <p>Description: Uses supplier stub and validates returned integer and string lists.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testGetPermittedThreadMethods() {
        Supplier<List<?>> supplier = () -> List.of(
                ThreadPermission.builder().createTheFollowingNumberOfThreads(0).ofThisClass("C0").build(),
                ThreadPermission.builder().createTheFollowingNumberOfThreads(1).ofThisClass("C1").build(),
                ThreadPermission.builder().createTheFollowingNumberOfThreads(10).ofThisClass("C2").build()
        );
        JavaThreadSystemExtractor extractor = new JavaThreadSystemExtractor(supplier);
        Assertions.assertEquals(List.of(0, 1, 10), extractor.getPermittedNumberOfThreads());
        Assertions.assertEquals(List.of("C0", "C1", "C2"), extractor.getPermittedThreadClasses());
    }
}
