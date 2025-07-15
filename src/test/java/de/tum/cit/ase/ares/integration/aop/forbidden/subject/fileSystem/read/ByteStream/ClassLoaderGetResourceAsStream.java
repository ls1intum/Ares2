package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream;

import java.io.IOException;
import java.io.InputStream;

public class ClassLoaderGetResourceAsStream {

    private ClassLoaderGetResourceAsStream() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access a class-path resource using ClassLoader.getResourceAsStream(String).
     * Test Case: ClassLoader.getResourceAsStream(String) - Read a file bundled on the class-path or inside a JAR
     */
    public static void accessFileSystemViaClassLoaderGetResourceAsStream() throws IOException {
        ClassLoader cl = ClassLoaderGetResourceAsStream.class.getClassLoader();
        try (InputStream is = cl.getResourceAsStream("de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            if (is != null) {
                is.read(); // Read a single byte from the resource
            }
        }
    }
}
