package %s.api.architecture.java.archunit;

/**
 * Supported architecture test cases in Java programming language.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @since 2.0.0
 */
public enum JavaArchUnitTestCaseSupported {
    /**
     * Architecture test case for the file system interaction.
     */
    FILESYSTEM_INTERACTION,
    /**
     * Architecture test case for the network connection.
     */
    NETWORK_CONNECTION,
    /**
     * Architecture test case for the command execution.
     */
    COMMAND_EXECUTION,
    /**
     * Architecture test case for the thread creation.
     */
    THREAD_CREATION,
    /**
     * Architecture test case for the package import.
     */
    PACKAGE_IMPORT
}
