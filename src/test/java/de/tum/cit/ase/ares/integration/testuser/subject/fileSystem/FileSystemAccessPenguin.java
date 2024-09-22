package de.tum.cit.ase.ares.integration.testuser.subject.fileSystem;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.Scanner;
import java.util.logging.FileHandler;

@SuppressWarnings({"resource", "ResultOfMethodCallIgnored"})
public final class FileSystemAccessPenguin {

    //<editor-fold desc="Constructor">
    private FileSystemAccessPenguin() {
		throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): FileSystemAccessPenguin is a utility class and should not be instantiated.");
	}
    //</editor-fold>

    //<editor-fold desc="Other Methods">
    public static void accessPath(Path p) throws IOException {
		Files.readString(p);
	}

	public static void askForFilePermission(String path) {
		//REMOVED: Checking Permission of the system's SecurityManager for "read"
	}
    //</editor-fold>

    //<editor-fold desc="Read Methods">

    // --- Read Methods ---

	/**
	 * Access the file system using the {@link RandomAccessFile} class.
	 */
	public static void accessFileSystemViaRandomAccessFile() {
		try {
			RandomAccessFile file = new RandomAccessFile("pom123.xml", "rw");
			file.read();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link File} class for reading.
	 */
	public static void accessFileSystemViaFileRead() {
		try {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link FileInputStream} class.
	 */
	public static void accessFileSystemViaFileInputStream() {
		try {
			new FileInputStream("pom123.xml").read();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link FileReader} class.
	 */
	public static void accessFileSystemViaFileReader() {
		try {
			FileReader reader = new FileReader("pom123.xml");
			reader.read();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link BufferedReader} class.
	 */
	public static void accessFileSystemViaBufferedReader() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("pom123.xml"));
			reader.read();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link Scanner} class.
	 */
	public static void accessFileSystemViaScanner() {
		try {
			Scanner scanner = new Scanner(new File("pom123.xml"));
			scanner.nextLine();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link DataInputStream} class.
	 */
	public static void accessFileSystemViaDataInputStream() {
		try {
			DataInputStream stream = new DataInputStream(new FileInputStream("pom123.xml"));
			stream.read();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link ObjectInputStream} class.
	 */
	public static void accessFileSystemViaObjectInputStream() {
		try {
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream("pom123.xml"));
			stream.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link InputStreamReader} class.
	 */
	public static void accessFileSystemViaInputStreamReader() {
		try {
			InputStreamReader reader = new InputStreamReader(new FileInputStream("pom123.xml"));
			reader.read();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link FileChannel} class for reading.
	 */
	public static void accessFileSystemViaFileChannelRead() {
		try {
			FileChannel fileChannel = FileChannel.open(Path.of("pom123.xml"));
			fileChannel.read(ByteBuffer.allocate(10));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using various methods from the {@link Files} class for reading.
	 */
	public static void accessFileSystemViaFilesRead() {
		try {
			Files.readAllBytes(Path.of("pom123.xml"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    //</editor-fold>

    //<editor-fold desc="Write Methods">
    // --- Write Methods ---

	/**
	 * Access the file system using the {@link RandomAccessFile} class for writing.
	 */
	public static void accessFileSystemViaRandomAccessFileWrite() {
		try {
			RandomAccessFile file = new RandomAccessFile("pom123.xml", "rw");
			file.write(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link File} class for writing.
	 */
	public static void accessFileSystemViaFileWrite() {
		try {
			File file = new File("pom123.xml");
			file.canWrite();
			file.createNewFile();
			File.createTempFile("temp", ".txt");
			file.setReadable(true);
			file.setWritable(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link FileOutputStream} class.
	 */
	public static void accessFileSystemViaFileOutputStream() {
		try {
			new FileOutputStream("pom123.xml").write(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link FileWriter} class.
	 */
	public static void accessFileSystemViaFileWriter() {
		try {
			FileWriter writer = new FileWriter("pom123.xml");
			writer.write(0);
			writer.append('c');
			writer.flush();
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link BufferedWriter} class.
	 */
	public static void accessFileSystemViaBufferedWriter() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("pom123.xml"));
			writer.write(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link PrintWriter} class.
	 */
	public static void accessFileSystemViaPrintWriter() {
		try {
			PrintWriter writer = new PrintWriter("pom123.xml");
			writer.write(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link DataOutputStream} class.
	 */
	public static void accessFileSystemViaDataOutputStream() {
		try {
			DataOutputStream stream = new DataOutputStream(new FileOutputStream("pom123.xml"));
			stream.write(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link OutputStreamWriter} class.
	 */
	public static void accessFileSystemViaOutputStreamWriter() {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("pom123.xml"));
			writer.write(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link ObjectOutputStream} class.
	 */
	public static void accessFileSystemViaObjectOutputStream() {
		try {
			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("pom123.xml"));
			stream.writeObject(new Object());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link PrintStream} class.
	 */
	public static void accessFileSystemViaPrintStream() {
		try {
			PrintStream stream = new PrintStream(new FileOutputStream("pom123.xml"));
			stream.write(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link FileChannel} class for writing.
	 */
	public static void accessFileSystemViaFileChannelWrite() {
		try {
			FileChannel fileChannel = FileChannel.open(Path.of("pom123.xml"));
			fileChannel.write(ByteBuffer.allocate(10));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using various methods from the {@link Files} class for writing.
	 */
	public static void accessFileSystemViaFilesWrite() {
		try {
			Files.write(Path.of("pom123.xml"), "data".getBytes());
			Files.createFile(Path.of("pom1234.xml"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link FileHandler} class.
	 */
	public static void accessFileSystemViaFileHandler() {
		try {
			FileHandler handler = new FileHandler("pom123.xml");
			handler.flush();
			handler.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    //</editor-fold>

    //<editor-fold desc="Execute Methods">
    // --- Execute Methods ---

	/**
	 * Access the file system using the {@link File} class for execution.
	 */
	public static void accessFileSystemViaFileExecute() {
		try {
			File file = new File("pom123.xml");
			file.canExecute();
			file.setExecutable(true);
			Files.isExecutable(Path.of("pom123.xml"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link Desktop} class.
	 */
	public static void accessFileSystemViaDesktop() {
		try {
			Desktop.getDesktop().open(new File("pom123.xml"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    //</editor-fold>

    //<editor-fold desc="Delete Methods">
    // --- Delete Methods ---

	/**
	 * Access the file system using various methods from the {@link Files} class for deletion.
	 */
	public static void accessFileSystemViaFilesDelete() {
		try {
			Files.delete(Path.of("pom123.xml"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link FileSystemProvider} class.
	 */
	public static void accessFileSystemViaFileSystemProvider() {
		try {
			FileSystemProvider provider = FileSystemProvider.installedProviders().getFirst();
			provider.delete(Path.of("file.txt"));
			provider.readAttributes(Path.of("pom123.xml"), "*");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Access the file system using the {@link JFileChooser} class.
	 */
	public static void accessFileSystemViaJFileChooser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.showOpenDialog(null);
	}
    //</editor-fold>
}
