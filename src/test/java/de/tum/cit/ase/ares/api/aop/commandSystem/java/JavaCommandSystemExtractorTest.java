package de.tum.cit.ase.ares.api.aop.commandSystem.java;

import de.tum.cit.ase.ares.api.policy.policySubComponents.CommandPermission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

/**
 * Unit tests for JavaCommandSystemExtractor.
 *
 * <p>Description: Validates extraction of permitted commands and their arguments.
 *
 * <p>Design Rationale: Ensures command execution permissions map accurately to API responses.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaCommandSystemExtractorTest {

    /**
     * Tests static extractCommands and extractArguments methods.
     *
     * <p>Description: Provides multiple CommandPermission instances and asserts returned lists.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testExtractCommandsAndArguments() {
        List<CommandPermission> configs = List.of(
                CommandPermission.builder().executeTheCommand("cmd0").withTheseArguments(List.of()).build(),
                CommandPermission.builder().executeTheCommand("cmd1").withTheseArguments(List.of("a1")).build(),
                CommandPermission.builder().executeTheCommand("cmd2").withTheseArguments(List.of("a2", "b2")).build()
        );

        List<String> expectedCommands = List.of("cmd0", "cmd1", "cmd2");
        List<String> actualCommands = JavaCommandSystemExtractor.extractCommands(configs);
        Assertions.assertEquals(expectedCommands, actualCommands);

        List<String> expectedArguments = List.of("new String[] {}", "new String[] {\"a1\"}", "new String[] {\"a2\",\"b2\"}");
        List<String> actualArguments = JavaCommandSystemExtractor.extractArguments(configs);
        Assertions.assertEquals(expectedArguments, actualArguments);
    }

    /**
     * Tests instance methods getPermittedCommands and getPermittedArguments.
     *
     * <p>Description: Uses supplier stub and verifies returned commands and argument lists.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testGetPermittedCommandsAndArguments() {
        Supplier<List<?>> supplier = () -> List.of(
                CommandPermission.builder().executeTheCommand("cmd0").withTheseArguments(List.of()).build(),
                CommandPermission.builder().executeTheCommand("cmd1").withTheseArguments(List.of("a1")).build(),
                CommandPermission.builder().executeTheCommand("cmd2").withTheseArguments(List.of("a2", "b2")).build()
        );
        JavaCommandSystemExtractor extractor = new JavaCommandSystemExtractor(supplier);

        List<String> expectedCommands = List.of("cmd0", "cmd1", "cmd2");
        List<String> actualCommands = extractor.getPermittedCommands();
        Assertions.assertEquals(expectedCommands, actualCommands);

        List<List<String>> expectedArguments = List.of(List.of(), List.of("a1"), List.of("a2", "b2"));
        List<List<String>> actualArguments = extractor.getPermittedArguments();
        Assertions.assertEquals(expectedArguments, actualArguments);
    }
}