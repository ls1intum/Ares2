package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.objectOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectOutputStreamWriteMain {

    private ObjectOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    // Sample serializable class for demonstration
    static class Person implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    /**
     * Access the file system using {@link ObjectOutputStream} directly for writing.
     */
    public static void accessFileSystemViaObjectOutputStream() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.dat"))) {
            oos.write(100);
        }
    }

    public static void accessFileSystemViaObjectOutputStreamWithData() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.dat"))) {
            byte[] data = "Hello, world!".getBytes();
            oos.write(data);
        }
    }

    public static void accessFileSystemViaObjectOutputStreamWithDataAndOffset() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.dat"))) {
            byte[] data = "Hello, world!".getBytes();
            oos.write(data, 0, data.length);
        }
    }

    public static void accessFileSystemViaObjectOutputStreamWithObject() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.dat"))) {
            Person person = new Person("John Doe", 30);
            oos.writeObject(person);
        }
    }

    public static void accessFileSystemViaObjectOutputStreamWithPrimitives() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.dat"))) {
            oos.writeInt(42);
            oos.writeDouble(3.14);
            oos.writeBoolean(true);
            oos.writeUTF("Hello ObjectOutputStream");
        }
    }
}
