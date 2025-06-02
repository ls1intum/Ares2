package de.tum.cit.ase.ares.api.architecture.java;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

public class FileHandlerConstantsTest {

    @Test
    void testResolveOnTarget_withMultipleParts() {
        Path base = Paths.get("base", "folder");
        Path resolved = FileHandlerConstants.resolveOnTarget(base, "child", "grandchild");
        Path expected = Paths.get("base", "folder", "child", "grandchild");
        assertEquals(expected, resolved, "resolveOnTarget should correctly resolve multiple path parts");
    }

    @Test
    void testResolveOnTarget_withNoFurtherParts() {
        Path base = Paths.get("onlyBase");
        Path resolved = FileHandlerConstants.resolveOnTarget(base);
        Path expected = Paths.get("onlyBase");
        assertEquals(expected, resolved, "resolveOnTarget with no further parts should return the base path unchanged");
    }

    @Test
    void testResolveOnPackage_withAdditionalParts() {
        Path resolved = FileHandlerConstants.resolveOnPackage("templates", "file.txt");
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "file.txt");
        assertEquals(expected, resolved, "resolveOnPackage should prepend the package path before further path parts");
    }

    @Test
    void testResolveOnPackage_withNoAdditionalParts() {
        Path resolved = FileHandlerConstants.resolveOnPackage();
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api");
        assertEquals(expected, resolved, "resolveOnPackage with no additional parts should return the package base path");
    }

    @Test
    void testArchUnitFilesystemMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "file-system-access-methods.txt");
        assertEquals(expected, FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS, "ARCHUNIT_FILESYSTEM_METHODS constant should point to the correct relative path");
    }

    @Test
    void testArchUnitNetworkMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "network-access-methods.txt");
        assertEquals(expected, FileHandlerConstants.ARCHUNIT_NETWORK_METHODS, "ARCHUNIT_NETWORK_METHODS constant should point to the correct relative path");
    }

    @Test
    void testArchUnitJvmTerminationMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "jvm-termination-methods.txt");
        assertEquals(expected, FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS, "ARCHUNIT_JVM_TERMINATION_METHODS constant should point to the correct relative path");
    }

    @Test
    void testArchUnitReflectionMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "reflection-methods.txt");
        assertEquals(expected, FileHandlerConstants.ARCHUNIT_REFLECTION_METHODS, "ARCHUNIT_REFLECTION_METHODS constant should point to the correct relative path");
    }

    @Test
    void testArchUnitCommandExecutionMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "command-execution-methods.txt");
        assertEquals(expected, FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS, "ARCHUNIT_COMMAND_EXECUTION_METHODS constant should point to the correct relative path");
    }

    @Test
    void testArchUnitThreadManipulationMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "thread-manipulation-methods.txt");
        assertEquals(expected, FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS, "ARCHUNIT_THREAD_MANIPULATION_METHODS constant should point to the correct relative path");
    }

    @Test
    void testArchUnitSerializationMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "serializable-methods.txt");
        assertEquals(expected, FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS, "ARCHUNIT_SERIALIZATION_METHODS constant should point to the correct relative path");
    }

    @Test
    void testArchUnitClassLoaderMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "classloader-methods.txt");
        assertEquals(expected, FileHandlerConstants.ARCHUNIT_CLASSLOADER_METHODS, "ARCHUNIT_CLASSLOADER_METHODS constant should point to the correct relative path");
    }

    @Test
    void testWalaClassLoaderMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "classloader-methods.txt");
        assertEquals(expected, FileHandlerConstants.WALA_CLASSLOADER_METHODS, "WALA_CLASSLOADER_METHODS constant should point to the correct relative path");
    }

    @Test
    void testWalaFilesystemMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "file-system-access-methods.txt");
        assertEquals(expected, FileHandlerConstants.WALA_FILESYSTEM_METHODS, "WALA_FILESYSTEM_METHODS constant should point to the correct relative path");
    }

    @Test
    void testWalaNetworkMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "network-access-methods.txt");
        assertEquals(expected, FileHandlerConstants.WALA_NETWORK_METHODS, "WALA_NETWORK_METHODS constant should point to the correct relative path");
    }

    @Test
    void testWalaJvmMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "jvm-termination-methods.txt");
        assertEquals(expected, FileHandlerConstants.WALA_JVM_METHODS, "WALA_JVM_METHODS constant should point to the correct relative path");
    }

    @Test
    void testWalaReflectionMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "reflection-methods.txt");
        assertEquals(expected, FileHandlerConstants.WALA_REFLECTION_METHODS, "WALA_REFLECTION_METHODS constant should point to the correct relative path");
    }

    @Test
    void testWalaCommandExecutionMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "command-execution-methods.txt");
        assertEquals(expected, FileHandlerConstants.WALA_COMMAND_EXECUTION_METHODS, "WALA_COMMAND_EXECUTION_METHODS constant should point to the correct relative path");
    }

    @Test
    void testWalaSerializationMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "serializable-methods.txt");
        assertEquals(expected, FileHandlerConstants.WALA_SERIALIZATION_METHODS, "WALA_SERIALIZATION_METHODS constant should point to the correct relative path");
    }

    @Test
    void testWalaThreadManipulationMethodsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "thread-manipulation-methods.txt");
        assertEquals(expected, FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS, "WALA_THREAD_MANIPULATION_METHODS constant should point to the correct relative path");
    }

    @Test
    void testFalsePositivesFileSystemInteractionsConstant() {
        Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "false-positives", "false-positives-file.txt");
        assertEquals(expected, FileHandlerConstants.FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS, "FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS constant should point to the correct relative path");
    }
}
