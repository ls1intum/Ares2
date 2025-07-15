package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.cipherOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;

/**
 * Helper that overwrites a not‑trusted file through {@link CipherOutputStream}
 * variants. All literal paths are centralised in one location so they can be
 * moved easily.
 */
public class CipherOutputStreamWriteMain {

    private static final String NOT_TRUSTED_DIR  =
            "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
    private static final String NOT_TRUSTED_FILE = NOT_TRUSTED_DIR + "/nottrusted.txt";

    private CipherOutputStreamWriteMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using {@link CipherOutputStream} for single‑byte write.
     */
    public static void accessFileSystemViaCipherOutputStream() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = getCipher();
        try (CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(NOT_TRUSTED_FILE), cipher)) {
            cos.write(100);
        }
    }

    public static void accessFileSystemViaCipherOutputStreamWithData() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = getCipher();
        try (CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(NOT_TRUSTED_FILE), cipher)) {
            cos.write("Hello, world!".getBytes());
        }
    }

    public static void accessFileSystemViaCipherOutputStreamWithDataAndOffset() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = getCipher();
        try (CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(NOT_TRUSTED_FILE), cipher)) {
            byte[] data = "Hello, world!".getBytes();
            cos.write(data, 0, data.length);
        }
    }

    /* ------------------------------------------------------------- */
    /*  Utility                                                     */
    /* ------------------------------------------------------------- */

    private static Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        Key key = keyGen.generateKey();

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher;
    }
}
