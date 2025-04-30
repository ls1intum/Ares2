package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

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
        throw new SecurityException(localize("security.general.utility.initialization"));
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
    public static ElementMatcher<TypeDescription> getClassesMatcher(
            Map<String, List<String>> methodsMap
    ) {
        String[] targets = methodsMap
                .keySet()
                .stream()
                .distinct()
                .toArray(String[]::new);

        if (targets.length == 0) {
            return ElementMatchers.none();
        }

        // match the type itself
        ElementMatcher.Junction<TypeDescription> direct = ElementMatchers.namedOneOf(targets);

        // match any subtype (deep in the hierarchy) as well
        ElementMatcher.Junction<TypeDescription> subtypes = ElementMatchers.hasSuperType(direct);

        return direct.or(subtypes);
    }

    static ElementMatcher<MethodDescription> getConstructorsMatcher(
            TypeDescription typeDescription,
            Map<String, List<String>> methodsMap
    ) {
        String className = typeDescription.getName();
        List<String> pointcuts = methodsMap.get(className);

        if (pointcuts == null || pointcuts.isEmpty() || !pointcuts.contains("<init>")) {
            return ElementMatchers.none();
        }

        ElementMatcher.Junction<NamedElement> classNameMatcher = ElementMatchers.named(className);
        ElementMatcher.Junction<TypeDescription> superClassMatcher = ElementMatchers.hasSuperType(classNameMatcher);
        ElementMatcher.Junction<TypeDescription> hierarchyMatcher = classNameMatcher.or(superClassMatcher);

        return ElementMatchers
                .isConstructor()
                .and(ElementMatchers.isDeclaredBy(hierarchyMatcher));
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
        String className = typeDescription.getName();
        List<String> pointcuts = methodsMap.get(className);

        if (pointcuts == null || pointcuts.isEmpty() || pointcuts.stream().allMatch("<init>"::equals)) {
            return ElementMatchers.none();
        }

        // filter out "<init>" and dedupe
        String[] methodNames = pointcuts.stream()
                .filter(name -> !name.equals("<init>"))
                .distinct()
                .toArray(String[]::new);

        if (methodNames.length == 0) {
            return ElementMatchers.none();
        }

        ElementMatcher.Junction<NamedElement> classNameMatcher = ElementMatchers.named(className);
        ElementMatcher.Junction<TypeDescription> superClassMatcher = ElementMatchers.hasSuperType(classNameMatcher);
        ElementMatcher.Junction<TypeDescription> hierarchyMatcher = classNameMatcher.or(superClassMatcher);

        return ElementMatchers
                .namedOneOf(methodNames)
                .and(ElementMatchers.isDeclaredBy(hierarchyMatcher));
    }

    //</editor-fold>

    //<editor-fold desc="Read Path">
    /**
     * This map contains the methods which can read files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file read operations.
     */
    public static final Map<String, List<String>> methodsWhichCanReadFiles = Map.ofEntries(
            // java.io
            Map.entry("java.io.BufferedReader", List.of("lines", "readLine")),
            Map.entry("java.io.FileInputStream", List.of("read")),
            Map.entry("java.io.FileReader", List.of("read", "readLine")),
            Map.entry("java.io.InputStream", List.of("read")),
            Map.entry("java.io.RandomAccessFile", List.of("read", "readBoolean", "readByte", "readChar", "readChars", "readDouble", "readFloat", "readFully", "readInt", "readLong", "readShort")),
            Map.entry("java.io.UnixFileSystem", List.of("canonicalize0", "getBooleanAttributes0", "getSpace")),
            Map.entry("java.io.Win32FileSystem", List.of("canonicalize", "getBooleanAttributes", "getLastModifiedTime", "getSpace")),
            Map.entry("java.io.WinNTFileSystem", List.of("canonicalize", "getBooleanAttributes", "getLastModifiedTime", "getSpace")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("lines", "newBufferedReader", "newByteChannel", "newInputStream", "readAllBytes", "readAllLines", "readString")),
            Map.entry("java.nio.file.spi.FileSystemProvider", List.of("newFileChannel")),
            // sun.nio
            Map.entry("sun.nio.ch.FileChannelImpl", List.of("open", "read", "readDirect", "readFully", "readIntoNativeBuffer"))
    );
    //</editor-fold>

    //<editor-fold desc="Overwrite Path">
    /**
     * This map contains the methods which can overwrite files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file overwrite operations.
     */
    public static final Map<String, List<String>> methodsWhichCanOverwriteFiles = Map.ofEntries(
            // java.io
            Map.entry("java.io.DataOutputStream", List.of("write", "writeBoolean", "writeByte", "writeBytes", "writeChar", "writeChars", "writeDouble", "writeFloat", "writeInt", "writeLong", "writeShort", "writeUTF")),
            Map.entry("java.io.File", List.of("setWritable")),
            Map.entry("java.io.FileSystem", List.of("createFileExclusively", "createDirectory", "rename")),
            Map.entry("java.io.FileOutputStream", List.of("write")),
            Map.entry("java.io.FileWriter", List.of("write")),
            Map.entry("java.io.PrintWriter", List.of("write")),
            Map.entry("java.io.RandomAccessFile", List.of("write", "writeBoolean", "writeByte", "writeBytes", "writeChar", "writeChars", "writeDouble", "writeFloat", "writeInt", "writeLong", "writeShort", "writeUTF")),
            Map.entry("java.io.UnixFileSystem", List.of("createDirectory", "setLastModifiedTime", "createFileExclusively", "delete0")),
            Map.entry("java.io.Win32FileSystem", List.of("createDirectory", "createFileExclusively", "delete", "setLastModifiedTime")),
            Map.entry("java.io.WinNTFileSystem", List.of("createDirectory", "createFileExclusively", "delete", "setLastModifiedTime")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("newOutputStream", "write", "writeBytes", "writeLines", "writeString", "writeAllBytes")),
            // java.print
            Map.entry("java.print.PrintStream", List.of("write")),
            // java.util
            Map.entry("java.util.prefs.FileSystemPreferences", List.of("lockFile0", "unlockFile0")),
            // sun.nio
            Map.entry("sun.nio.ch.FileChannelImpl", List.of("write", "writeDirect", "writeFully", "writeFromNativeBuffer"))
    );
    //</editor-fold>

    //<editor-fold desc="Execute Path">
    /**
     * This map contains the methods which can execute files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file execute operations.
     */
    public static final Map<String, List<String>> methodsWhichCanExecuteFiles = Map.ofEntries(
            // java.awt
            Map.entry("java.awt.Desktop", List.of("browse", "edit", "mail", "open", "print")),
            // java.io
            Map.entry("java.io.File", List.of("setExecutable")),
            Map.entry("java.io.Win32FileSystem", List.of("checkAccess", "setReadOnly")),
            Map.entry("java.io.WinNTFileSystem", List.of("setReadOnly")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("setPosixFilePermissions"))
    );
    //</editor-fold>

    //<editor-fold desc="Delete Path">
    /**
     * This map contains the methods which can delete files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file delete operations.
     */
    public static final Map<String, List<String>> methodsWhichCanDeleteFiles = Map.ofEntries(
            // java.io
            Map.entry("java.io.File", List.of("delete", "deleteOnExit")),
            Map.entry("java.io.FileSystem", List.of("delete")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("delete", "deleteIfExists")),
            Map.entry("java.nio.file.spi.FileSystemProvider", List.of("delete"))
    );
    //</editor-fold>

    //<editor-fold desc="Create Thread">
    /**
     * This map contains the methods which can create threads. The map keys represent class names,
     * and the values are lists of method names that are considered to be thread create operations.
     */
    public static final Map<String, List<String>> methodsWhichCanCreateThreads = Map.ofEntries(
            Map.entry("java.util.concurrent.AbstractExecutorService", List.of("submit")),
            Map.entry("java.util.concurrent.ExecutorService", List.of("submit")),
            Map.entry("java.util.concurrent.ThreadPoolExecutor", List.of("submit"))
    );
    //</editor-fold>

}

