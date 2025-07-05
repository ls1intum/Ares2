package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.ByteStream;

import java.io.*;

public class DataInputStream {

    private DataInputStream() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Access the file system using DataInputStream for typed reads.
     * Test Case: DataInputStream(InputStream in) + readInt() etc. - Add typed reads on top of any byte stream
     */
    public static void accessFileSystemViaDataInputStream() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             java.io.DataInputStream dis = new java.io.DataInputStream(fis)) {
            try {
                dis.readInt(); // Reading typed data - int
                dis.readBoolean(); // Reading typed data - boolean
                dis.readByte(); // Reading typed data - byte
                dis.readChar(); // Reading typed data - char
                dis.readDouble(); // Reading typed data - double
                dis.readFloat(); // Reading typed data - float
                dis.readLong(); // Reading typed data - long
                dis.readShort(); // Reading typed data - short
            } catch (EOFException e) {
                // Handle end of file
            }
        }
    }
}
