package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.writeFlags;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Supervised code that writes text into a file through one of the common Java
 * writing APIs, so a test can check that each API is allowed or refused by the
 * same create and overwrite rules.
 */
public final class FileWriteFlagsMain {

	/**
	 * The writing APIs {@link #write} knows, by name.
	 */
	public static final String[] WRITERS = { "writeString", "filesWrite", "fileWriter", "fileWriterFile", "printWriter",
			"printWriterFile", "printStream", "bufferedWriterNio", "bufferedWriterIo", "newOutputStream",
			"fileOutputStream", "fileOutputStreamFile", "fileChannel" };

	/**
	 * Hidden constructor of a utility class.
	 */
	private FileWriteFlagsMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Writes the text into the file through the named API, replacing what the file
	 * held before.
	 *
	 * @param how  one of {@link #WRITERS}
	 * @param file the file to write
	 * @param text the text to write
	 * @throws IOException if writing fails
	 */
	public static void write(String how, Path file, String text) throws IOException {
		byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
		switch (how) {
		case "writeString" -> Files.writeString(file, text);
		case "filesWrite" -> Files.write(file, bytes);
		case "fileWriter" -> writeAndClose(new FileWriter(file.toString()), text);
		case "fileWriterFile" -> writeAndClose(new FileWriter(file.toFile()), text);
		case "printWriter" -> writeAndClose(new PrintWriter(file.toString()), text);
		case "printWriterFile" -> writeAndClose(new PrintWriter(file.toFile()), text);
		case "printStream" -> writeAndClose(new PrintStream(file.toFile()), bytes);
		case "bufferedWriterNio" -> writeAndClose(Files.newBufferedWriter(file), text);
		case "bufferedWriterIo" -> writeAndClose(new BufferedWriter(new FileWriter(file.toString())), text);
		case "newOutputStream" -> writeAndClose(Files.newOutputStream(file), bytes);
		case "fileOutputStream" -> writeAndClose(new FileOutputStream(file.toString()), bytes);
		case "fileOutputStreamFile" -> writeAndClose(new FileOutputStream(new File(file.toString())), bytes);
		case "fileChannel" -> writeFileChannel(file, bytes);
		default -> throw new IllegalArgumentException(how);
		}
	}

	/**
	 * Writes the text with a writer and closes it.
	 *
	 * @param writer the writer to use
	 * @param text   the text to write
	 * @throws IOException if writing fails
	 */
	private static void writeAndClose(java.io.Writer writer, String text) throws IOException {
		try (writer) {
			writer.write(text);
		}
	}

	/**
	 * Writes the bytes with a stream and closes it.
	 *
	 * @param stream the stream to use
	 * @param bytes  the bytes to write
	 * @throws IOException if writing fails
	 */
	private static void writeAndClose(OutputStream stream, byte[] bytes) throws IOException {
		try (stream) {
			stream.write(bytes);
		}
	}

	/**
	 * Replaces the file's content through a channel opened with create, write and
	 * truncate, writing through the open channel.
	 *
	 * @param file  the file to write
	 * @param bytes the bytes to write
	 * @throws IOException if writing fails
	 */
	private static void writeFileChannel(Path file, byte[] bytes) throws IOException {
		try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			channel.write(ByteBuffer.wrap(bytes));
		}
	}
}
