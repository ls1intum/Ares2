package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.cipherOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;

public class CipherOutputStreamWriteMain {

    private CipherOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link CipherOutputStream} directly for writing.
     */
    public static void accessFileSystemViaCipherOutputStream() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = getCipher();
        try (CipherOutputStream cos = new CipherOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), cipher)) {
            cos.write(100);
        }
    }

    public static void accessFileSystemViaCipherOutputStreamWithData() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = getCipher();
        try (CipherOutputStream cos = new CipherOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), cipher)) {
            byte[] data = "Hello, world!".getBytes();
            cos.write(data);
        }
    }

    public static void accessFileSystemViaCipherOutputStreamWithDataAndOffset() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = getCipher();
        try (CipherOutputStream cos = new CipherOutputStream(new FileOutputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), cipher)) {
            byte[] data = "Hello, world!".getBytes();
            cos.write(data, 0, data.length);
        }
    }

    private static Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Generate a key for AES encryption
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        Key key = keyGen.generateKey();

        // Create and initialize cipher
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher;
    }
}
