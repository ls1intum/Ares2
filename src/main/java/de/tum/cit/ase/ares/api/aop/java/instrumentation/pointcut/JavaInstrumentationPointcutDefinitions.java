package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains the pointcut definitions for the Java instrumentation agent.
 * These pointcuts are used to identify methods that perform file system operations
 * (e.g., read, write, execute, delete) and apply security checks using bytecode modification.
 * The methods in this class define which file operations will be instrumented and monitored to
 * enforce security policies at runtime.
 */
public class JavaInstrumentationPointcutDefinitions {

    //<editor-fold desc="Constructor">

    /**
     * This constructor is private to prevent instantiation of this utility class.
     */
    private JavaInstrumentationPointcutDefinitions() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): JavaInstrumentationPointcutDefinitions is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Tools">

    /**
     * This method returns a matcher that matches classes based on the provided methods map.
     * The map defines the classes whose methods are candidates for instrumentation.
     *
     * @param methodsMap A map containing class names as keys and lists of method names as values. These
     *                   define the classes and their respective methods for instrumentation.
     * @return An element matcher that matches classes based on the method map.
     * If no classes are found in the map, returns {@code ElementMatchers.none()}.
     */
    public static ElementMatcher<NamedElement> getClassesMatcher(
            Map<String, List<String>> methodsMap
    ) {
        Set<String> classes = methodsMap.keySet();
        if (classes.isEmpty()) {
            return ElementMatchers.none();
        }
        return ElementMatchers.namedOneOf(
                classes
                        .stream()
                        .distinct()
                        .toArray(String[]::new)
        );

    }

    static ElementMatcher<MethodDescription> getConstructorsMatcher(
            TypeDescription typeDescription,
            Map<String, List<String>> methodsMap
    ) {
        List<String> methods = methodsMap.get(typeDescription.getName());
        if (
                methods == null
                        || methods.isEmpty()
                        || methods
                        .stream()
                        .filter(method -> method.equals("<init>"))
                        .toList()
                        .isEmpty()
        ) {
            return ElementMatchers.none();
        } else {
            return ElementMatchers.isConstructor();
        }
    }

    /**
     * This method returns a matcher that matches the methods of the provided class (type description)
     * against the specified methods map. The map defines the method signatures to target for instrumentation.
     * Each key in the methods map represents a class name, and the corresponding value is a list of method
     * names that are used as pointcuts for monitoring and modifying their execution.
     *
     * @param typeDescription The description of the class whose methods are to be matched.
     * @param methodsMap      A map containing class names as keys and lists of method names as values. These
     *                        define the methods to be instrumented.
     * @return An element matcher that matches methods based on the provided methods map.
     *         If no methods are found for the class, returns {@code ElementMatchers.none()}.
     */
    static ElementMatcher<MethodDescription> getMethodsMatcher(
            TypeDescription typeDescription,
            Map<String, List<String>> methodsMap
    ) {
        List<String> methods = methodsMap.get(typeDescription.getName());
        if (
                methods == null
                        || methods.isEmpty()
                        || methods
                        .stream()
                        .filter(method -> !method.equals("<init>"))
                        .toList()
                        .isEmpty()
        ) {
            return ElementMatchers.none();
        } else {
            return ElementMatchers.namedOneOf(
                    methods
                            .stream()
                            .distinct()
                            .toArray(String[]::new)
            );
        }
    }

    //</editor-fold>

    //<editor-fold desc="Read Path">
    /**
     * This map contains the methods which can read files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file read operations.
     */
    public static final Map<String, List<String>> methodsWhichCanReadFiles = Map.of(
            // TODO Markus: Currently the methods which can read are not properly instrumented
            "java.io.FileInputStream",
            List.of("<init>", "read", "open"),
            "java.io.RandomAccessFile",
            List.of("<init>", "read", "readFully", "readLine", "readBoolean", "readByte", "readChar", "readDouble",
                    "readFloat", "readInt", "readLong", "readShort", "readUnsignedByte", "readUnsignedShort"),
            "java.io.UnixFileSystem",
            List.of("getBooleanAttributes0", "getSpace", "canonicalize0"),
            "java.io.WinNTFileSystem",
            List.of("getBooleanAttributes", "canonicalize", "getLastModifiedTime", "getSpace"),
            "java.io.Win32FileSystem",
            List.of("getBooleanAttributes", "canonicalize", "getLastModifiedTime", "getSpace"),
            "java.nio.file.Files",
            List.of("readAttributes", "readAllBytes", "readAllLines", "readString", "read", "newInputStream", "lines"),
            "java.io.FileReader",
            List.of("<init>", "read", "readLine"),
            "java.io.BufferedReader",
            List.of("lines"),
            "java.nio.channels.FileChannel",
            List.of("open")
    );
    //</editor-fold>

    //<editor-fold desc="Overwrite Path">
    /**
     * This map contains the methods which can overwrite files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file overwrite operations.
     */
    public static final Map<String, List<String>> methodsWhichCanOverwriteFiles = Map.of(
            "java.io.FileOutputStream",
            List.of("<init>"),
            "java.io.RandomAccessFile",
            List.of("write", "writeBoolean", "writeByte", "writeBytes", "writeChar", "writeChars", "writeDouble", "writeFloat", "writeInt", "writeLong", "writeShort"),
            "java.io.UnixFileSystem",
            List.of("setLastModifiedTime", "createFileExclusively", "delete0", "createDirectory"),
            "java.io.WinNTFileSystem",
            List.of("createFileExclusively", "delete", "setLastModifiedTime", "createDirectory"),
            "java.io.Win32FileSystem",
            List.of("createFileExclusively", "delete", "setLastModifiedTime", "createDirectory"),
            "java.util.prefs.FileSystemPreferences",
            List.of("lockFile0", "unlockFile0"),
            "java.nio.file.Files",
            List.of("write", "writeString", "newOutputStream", "writeBytes", "writeAllBytes", "writeLines"),
            "java.io.File",
            List.of("setWritable"),
            "java.nio.channels.FileChannel",
            List.of("open")

    );
    //</editor-fold>

    //<editor-fold desc="Execute Path">
    /**
     * This map contains the methods which can execute files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file execute operations.
     */
    public static final Map<String, List<String>> methodsWhichCanExecuteFiles = Map.of(
            "java.io.UnixFileSystem",
            List.of("setPermission"),
            "java.io.WinNTFileSystem",
            List.of("setReadOnly"),
            "java.io.Win32FileSystem",
            List.of("checkAccess", "setReadOnly"),
            "java.nio.file.Files",
            List.of("setPosixFilePermissions"),
            "java.awt.Desktop",
            List.of("open", "edit", "print", "browse", "mail")
    );
    //</editor-fold>

    //<editor-fold desc="Delete Path">
    /**
     * This map contains the methods which can delete files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file delete operations.
     */
    public static final Map<String, List<String>> methodsWhichCanDeleteFiles = Map.of(
            "java.io.File",
            List.of("delete", "deleteOnExit"),
            "java.nio.file.Files",
            List.of("delete", "deleteIfExists"),
            "sun.nio.fs.UnixFileSystemProvider",
            List.of("implDelete")
    );
    //</editor-fold>

}

