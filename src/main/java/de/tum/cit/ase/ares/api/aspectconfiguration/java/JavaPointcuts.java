package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaPointcuts {

    //<editor-fold desc="Constructor">
    private JavaPointcuts() {
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

    static ElementMatcher<NamedElement> getClassesMatcher(
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
    static final Map<String, List<String>> methodsWhichCanReadFiles = Map.of(
            "java.io.FileInputStream",
            List.of("<init>", "read"),
            "java.io.RandomAccessFile",
            List.of("read", "readFully", "readLine", "readBoolean", "readByte", "readChar", "readDouble",
                    "readFloat", "readInt", "readLong", "readShort", "readUnsignedByte", "readUnsignedShort"),
            "java.io.UnixFileSystem",
            List.of("getLastModifiedTime", "getBooleanAttributes0", "getSpace", "canonicalize0"),
            "java.io.WinNTFileSystem",
            List.of("getBooleanAttributes", "canonicalize", "getLastModifiedTime", "getSpace"),
            "java.io.Win32FileSystem",
            List.of("getBooleanAttributes", "canonicalize", "getLastModifiedTime", "getSpace"),
            "java.nio.file.Files",
            List.of("readAttributes", "readAllBytes", "readAllLines", "readString", "read", "newInputStream", "lines")
    );
    //</editor-fold>

    //<editor-fold desc="Write Path">
    static final Map<String, List<String>> methodsWhichCanWriteFiles = Map.of(
            "java.io.FileOutputStream",
            List.of("<init>","write"),
            "java.io.RandomAccessFile",
            List.of("write", "writeBoolean", "writeByte", "writeBytes",
                    "writeChar", "writeChars", "writeDouble", "writeFloat", "writeInt", "writeLong", "writeShort"),
            "java.io.UnixFileSystem",
            List.of("setLastModifiedTime", "createFileExclusively", "delete0", "createDirectory"),
            "java.io.WinNTFileSystem",
            List.of("createFileExclusively", "delete", "setLastModifiedTime", "createDirectory"),
            "java.io.Win32FileSystem",
            List.of("createFileExclusively", "delete", "setLastModifiedTime", "createDirectory"),
            "java.util.prefs.FileSystemPreferences",
            List.of("lockFile0", "unlockFile0")
    );
    //</editor-fold>

    //<editor-fold desc="Execute Path">
    static final Map<String, List<String>> methodsWhichCanExecuteFiles = Map.of(
            "java.io.UnixFileSystem",
            List.of("checkAccess", "setPermission"),
            "java.io.WinNTFileSystem",
            List.of("checkAccess", "setReadOnly"),
            "java.io.Win32FileSystem",
            List.of("checkAccess", "setReadOnly")
    );
    //</editor-fold>

}

