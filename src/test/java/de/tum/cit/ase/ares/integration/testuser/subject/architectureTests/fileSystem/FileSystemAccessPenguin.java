package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.fileSystem;

import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.zip.ZipInputStream;

@SuppressWarnings({"resource", "ResultOfMethodCallIgnored"})
public final class FileSystemAccessPenguin {

    //<editor-fold desc="Constructor">
    private FileSystemAccessPenguin() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): FileSystemAccessPenguin is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Other Methods">

    public static void askForFilePermission(String path) {
        //REMOVED: Checking Permission of the system's SecurityManager for "read"
    }
    //</editor-fold>

    //<editor-fold desc="Read Methods">

    // --- Read Methods ---

    /**
     * Access the file system using the {@link RandomAccessFile} class.
     */
    public static void accessFileSystemViaRandomAccessFile() throws IOException {
        RandomAccessFile file = new RandomAccessFile("pom123.xml", "rw");
        file.read();
    }

    /**
     * Access the file system using the {@link File} class for reading.
     */
    public static void accessFileSystemViaFileRead() {
        File file = new File("pom123.xml");
        file.canRead();
        file.exists();
        file.getFreeSpace();
        file.getTotalSpace();
        file.getUsableSpace();
        file.isDirectory();
        file.isFile();
        file.isHidden();
        file.lastModified();
        file.length();
        file.listFiles();
    }

    /**
     * Access the file system using the {@link FileInputStream} class.
     */
    public static void accessFileSystemViaFileInputStream() throws IOException {
        new FileInputStream("pom123.xml").read();
    }

    /**
     * Access the file system using the {@link FileReader} class.
     */
    public static void accessFileSystemViaFileReader() throws IOException {
        FileReader reader = new FileReader("pom123.xml");
        reader.read();
    }

    /**
     * Access the file system using the {@link BufferedReader} class.
     */
    public static void accessFileSystemViaBufferedReader() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("pom123.xml"));
        reader.read();
    }

    /**
     * Access the file system using the {@link Scanner} class.
     */
    public static void accessFileSystemViaScanner() throws FileNotFoundException {
        Scanner scanner;
        scanner = new Scanner(new ZipInputStream(new FileInputStream("pom123.xml")));
        scanner.nextLine();
    }

    /**
     * Access the file system using the {@link DataInputStream} class.
     */
    public static void accessFileSystemViaDataInputStream() throws IOException {
        DataInputStream stream = new DataInputStream(new FileInputStream("pom123.xml"));
        stream.read();
    }

    /**
     * Access the file system using the {@link ObjectInputStream} class.
     */
    public static void accessFileSystemViaObjectInputStream() throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(new FileInputStream("pom123.xml"));
        stream.readObject();
    }

    /**
     * Access the file system using the {@link InputStreamReader} class.
     */
    public static void accessFileSystemViaInputStreamReader() throws IOException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream("pom123.xml"));
        reader.read();
    }

    /**
     * Access the file system using the {@link FileChannel} class for reading.
     */
    public static void accessFileSystemViaFileChannelRead() throws IOException {
        FileChannel fileChannel = FileChannel.open(Path.of("pom123.xml"), StandardOpenOption.READ);
        fileChannel.read(ByteBuffer.allocate(10));
    }

    /**
     * Access the file system using various methods from the {@link Files} class for reading.
     */
    public static void accessFileSystemViaFilesRead() throws IOException {
        Files.readAllBytes(Path.of("pom123.xml"));
    }
    //</editor-fold>

    //<editor-fold desc="Write Methods">
    // --- Write Methods ---

    /**
     * Access the file system using the {@link RandomAccessFile} class for writing.
     */
    public static void accessFileSystemViaRandomAccessFileWrite() throws IOException {
        RandomAccessFile file = new RandomAccessFile("pom123.xml", "rw");
        file.write(0);
    }

    /**
     * Access the file system using the {@link File} class for writing.
     */
    public static void accessFileSystemViaFileWrite() throws IOException {
        File file = new File("pom123.xml");
        file.canWrite();
        file.createNewFile();
        File.createTempFile("temp", ".txt");
        file.setReadable(true);
        file.setWritable(true);
    }

    /**
     * Access the file system using the {@link FileOutputStream} class.
     */
    public static void accessFileSystemViaFileOutputStream() throws IOException {
        new FileOutputStream("pom123.xml").write(0);
    }

    /**
     * Access the file system using the {@link FileWriter} class.
     */
    public static void accessFileSystemViaFileWriter() throws IOException {
        FileWriter writer = new FileWriter("pom123.xml");
        writer.write(0);
        writer.append('c');
        writer.flush();
        writer.close();
    }

    /**
     * Access the file system using the {@link BufferedWriter} class.
     */
    public static void accessFileSystemViaBufferedWriter() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("pom123.xml"));
        writer.write(0);
    }

    /**
     * Access the file system using the {@link PrintWriter} class.
     */
    public static void accessFileSystemViaPrintWriter() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter("pom123.xml");
        writer.write(0);
    }

    /**
     * Access the file system using the {@link DataOutputStream} class.
     */
    public static void accessFileSystemViaDataOutputStream() throws IOException {
        DataOutputStream stream = new DataOutputStream(new FileOutputStream("pom123.xml"));
        stream.write(0);
    }

    /**
     * Access the file system using the {@link OutputStreamWriter} class.
     */
    public static void accessFileSystemViaOutputStreamWriter() throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("pom123.xml"));
        writer.write(0);
    }

    /**
     * Access the file system using the {@link ObjectOutputStream} class.
     */
    public static void accessFileSystemViaObjectOutputStream() throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("pom123.xml"));
        stream.writeObject(new Object());
    }

    /**
     * Access the file system using the {@link PrintStream} class.
     */
    public static void accessFileSystemViaPrintStream() throws FileNotFoundException {
        PrintStream stream = new PrintStream(new FileOutputStream("pom123.xml"));
        stream.write(0);
    }

    /**
     * Access the file system using the {@link FileChannel} class for writing.
     */
    public static void accessFileSystemViaFileChannelWrite() throws IOException {
        FileChannel fileChannel = FileChannel.open(Path.of("pom123.xml"), StandardOpenOption.WRITE);
        fileChannel.write(ByteBuffer.allocate(10));
    }

    /**
     * Access the file system using various methods from the {@link Files} class for writing.
     */
    public static void accessFileSystemViaFilesWrite() throws IOException {
        Files.write(Path.of("pom123.xml"), "data".getBytes());
        Files.createFile(Path.of("pom1234.xml"));
    }

    /**
     * Access the file system using the {@link FileHandler} class.
     */
    public static void accessFileSystemViaFileHandler() throws IOException {
        FileHandler handler = new FileHandler("pom123.xml");
        handler.flush();
        handler.close();
    }
    //</editor-fold>

    //<editor-fold desc="Execute Methods">
    // --- Execute Methods ---

    /**
     * Access the file system using the {@link File} class for execution.
     */
    public static void accessFileSystemViaFileExecute() {
        File file = new File("pom123.xml");
        file.canExecute();
        file.setExecutable(true);
        Files.isExecutable(Path.of("pom123.xml"));
    }

    /**
     * Access the file system using the {@link Desktop} class.
     */
    public static void accessFileSystemViaDesktop() throws IOException {

        Desktop.getDesktop().open(new File("pom123.xml"));
    }
    //</editor-fold>


}
