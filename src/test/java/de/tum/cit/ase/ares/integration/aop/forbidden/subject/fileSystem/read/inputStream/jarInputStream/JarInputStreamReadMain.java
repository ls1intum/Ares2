package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.jarInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;

public class JarInputStreamReadMain {

    private JarInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaJarInputStreamRead() throws IOException {
        try (JarInputStream reader = new JarInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextJarEntry();
            return reader.read();
        }
    }

    public static int accessFileSystemViaJarInputStreamReadByteArray() throws IOException {
        try (JarInputStream reader = new JarInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextJarEntry();
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaJarInputStreamReadByteArrayOffsetLength() throws IOException {
        try (JarInputStream reader = new JarInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextJarEntry();
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaJarInputStreamReadAllBytes() throws IOException {
        try (JarInputStream reader = new JarInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextJarEntry();
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaJarInputStreamReadNBytes() throws IOException {
        try (JarInputStream reader = new JarInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            reader.getNextJarEntry();
            return reader.readNBytes(1024);
        }
    }
}

