package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.digestInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestInputStreamReadMain {

    private DigestInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    private static MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256");
    }

    public static int accessFileSystemViaDigestInputStreamRead() throws IOException, NoSuchAlgorithmException {
        try (DigestInputStream reader = new DigestInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getMessageDigest())) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaDigestInputStreamReadByteArray() throws IOException, NoSuchAlgorithmException {
        try (DigestInputStream reader = new DigestInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getMessageDigest())) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaDigestInputStreamReadByteArrayOffsetLength() throws IOException, NoSuchAlgorithmException {
        try (DigestInputStream reader = new DigestInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getMessageDigest())) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaDigestInputStreamReadAllBytes() throws IOException, NoSuchAlgorithmException {
        try (DigestInputStream reader = new DigestInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getMessageDigest())) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaDigestInputStreamReadNBytes() throws IOException, NoSuchAlgorithmException {
        try (DigestInputStream reader = new DigestInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getMessageDigest())) {
            return reader.readNBytes(1024);
        }
    }
}

