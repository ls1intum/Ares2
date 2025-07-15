package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.properties;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertiesMain {

    private ReadPropertiesMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using Properties.load(InputStream inStream) method.
     */
    public static void accessFileSystemViaPropertiesLoadInputStream() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            props.load(fis);
        }
    }

    /**
     * Access the file system using Properties.load(Reader reader) method.
     */
    public static void accessFileSystemViaPropertiesLoadReader() throws IOException {
        Properties props = new Properties();
        try (FileReader reader = new FileReader("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            props.load(reader);
        }
    }

    /**
     * Access the file system using Properties.loadFromXML(InputStream in) method.
     */
    public static void accessFileSystemViaPropertiesLoadFromXML() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt")) {
            props.loadFromXML(fis);
        }
    }
}
