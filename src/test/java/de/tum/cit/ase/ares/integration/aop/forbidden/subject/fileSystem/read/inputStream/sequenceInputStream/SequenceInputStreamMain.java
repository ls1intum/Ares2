package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.sequenceInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.Vector;

@SuppressWarnings("unused")
public class SequenceInputStreamMain {

    private SequenceInputStreamMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    public static int accessFileSystemViaSequenceInputStreamRead() throws IOException {
        Vector<FileInputStream> streams = new Vector<>();
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        try (SequenceInputStream reader = new SequenceInputStream(Collections.enumeration(streams))) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaSequenceInputStreamReadByteArray() throws IOException {
        Vector<FileInputStream> streams = new Vector<>();
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        try (SequenceInputStream reader = new SequenceInputStream(Collections.enumeration(streams))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaSequenceInputStreamReadByteArrayOffsetLength() throws IOException {
        Vector<FileInputStream> streams = new Vector<>();
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        try (SequenceInputStream reader = new SequenceInputStream(Collections.enumeration(streams))) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaSequenceInputStreamReadAllBytes() throws IOException {
        Vector<FileInputStream> streams = new Vector<>();
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        try (SequenceInputStream reader = new SequenceInputStream(Collections.enumeration(streams))) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaSequenceInputStreamReadNBytes() throws IOException {
        Vector<FileInputStream> streams = new Vector<>();
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        streams.add(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
        try (SequenceInputStream reader = new SequenceInputStream(Collections.enumeration(streams))) {
            return reader.readNBytes(1024);
        }
    }
}
