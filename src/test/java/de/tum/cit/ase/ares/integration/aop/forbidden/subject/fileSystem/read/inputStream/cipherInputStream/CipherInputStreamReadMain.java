package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.inputStream.cipherInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class CipherInputStreamReadMain {

    private CipherInputStreamReadMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    private static Cipher getCipher() throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec("1234567890123456".getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher;
    }

    public static int accessFileSystemViaCipherInputStreamRead() throws IOException, GeneralSecurityException {
        try (CipherInputStream reader = new CipherInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getCipher())) {
            return reader.read();
        }
    }

    public static int accessFileSystemViaCipherInputStreamReadByteArray() throws IOException, GeneralSecurityException {
        try (CipherInputStream reader = new CipherInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getCipher())) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer);
        }
    }

    public static int accessFileSystemViaCipherInputStreamReadByteArrayOffsetLength() throws IOException, GeneralSecurityException {
        try (CipherInputStream reader = new CipherInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getCipher())) {
            byte[] buffer = new byte[1024];
            return reader.read(buffer, 0, buffer.length);
        }
    }

    public static byte[] accessFileSystemViaCipherInputStreamReadAllBytes() throws IOException, GeneralSecurityException {
        try (CipherInputStream reader = new CipherInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getCipher())) {
            return reader.readAllBytes();
        }
    }

    public static byte[] accessFileSystemViaCipherInputStreamReadNBytes() throws IOException, GeneralSecurityException {
        try (CipherInputStream reader = new CipherInputStream(new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), getCipher())) {
            return reader.readNBytes(1024);
        }
    }
}
