package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaInstrumentationPointcutDefinitions {

    //<editor-fold desc="Constructor">
    private JavaInstrumentationPointcutDefinitions() {
        throw new IllegalStateException("Utility class");
    }
    //</editor-fold>

    //<editor-fold desc="Tools">
    static ElementMatcher<MethodDescription> getMethodsMatcher(
            TypeDescription typeDescription,
            Map<String, List<String>> methodsMap
    ) {
        List<String> methods = methodsMap.get(typeDescription.getName());
        if (methods == null || methods.isEmpty()) {
            return ElementMatchers.none();
        }
        return ElementMatchers.namedOneOf(
                methods
                        .stream()
                        .distinct()
                        .toArray(String[]::new)
        );

    }

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

    //</editor-fold>

    //<editor-fold desc="Read Path">
    public static final Map<String, List<String>> methodsWhichCanReadFiles = Map.of(
            "instrumentation.io.FileInputStream",
            List.of("<init>", "read"),
            "instrumentation.io.RandomAccessFile",
            List.of("read", "readFully", "readLine", "readBoolean", "readByte", "readChar", "readDouble",
                    "readFloat", "readInt", "readLong", "readShort", "readUnsignedByte", "readUnsignedShort"),
            "instrumentation.io.UnixFileSystem",
            List.of("getLastModifiedTime", "getBooleanAttributes0", "getSpace", "canonicalize0"),
            "instrumentation.io.WinNTFileSystem",
            List.of("getBooleanAttributes", "canonicalize", "getLastModifiedTime", "getSpace"),
            "instrumentation.io.Win32FileSystem",
            List.of("getBooleanAttributes", "canonicalize", "getLastModifiedTime", "getSpace"),
            "java.nio.file.Files",
            List.of("readAttributes", "readAllBytes", "readAllLines", "readString", "read", "newInputStream", "lines")
    );
    //</editor-fold>

    //<editor-fold desc="Overwrite Path">
    public static final Map<String, List<String>> methodsWhichCanOverwriteFiles = Map.of(
            "java.io.FileOutputStream",
            List.of("<init>","write"),
            "instrumentation.io.RandomAccessFile",
            List.of("write", "writeBoolean", "writeByte", "writeBytes",
                    "writeChar", "writeChars", "writeDouble", "writeFloat", "writeInt", "writeLong", "writeShort"),
            "instrumentation.io.UnixFileSystem",
            List.of("setLastModifiedTime", "createFileExclusively", "delete0", "createDirectory"),
            "instrumentation.io.WinNTFileSystem",
            List.of("createFileExclusively", "delete", "setLastModifiedTime", "createDirectory"),
            "instrumentation.io.Win32FileSystem",
            List.of("createFileExclusively", "delete", "setLastModifiedTime", "createDirectory"),
            "instrumentation.util.prefs.FileSystemPreferences",
            List.of("lockFile0", "unlockFile0")
    );
    //</editor-fold>

    //<editor-fold desc="Execute Path">
    public static final Map<String, List<String>> methodsWhichCanExecuteFiles = Map.of(
            // TODO why do we have instrumentation I reverted them back to java but still the methods are not intercepted
            "java.io.UnixFileSystem",
            List.of("checkAccess", "setPermission"),
            "java.io.WinNTFileSystem",
            List.of("checkAccess", "setReadOnly"),
            "java.io.Win32FileSystem",
            List.of("checkAccess", "setReadOnly")
    );
    //</editor-fold>

}

