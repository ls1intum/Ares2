package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.openingRoles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.Set;

/**
 * Supervised code that opens, moves and copies files through the APIs whose
 * checks depend on what the call really does to each file: the file system
 * provider, {@code RandomAccessFile} in mode {@code "rw"}, {@code Files.move},
 * and the writers that take a charset name.
 */
public final class FileOpeningRolesMain {

	/**
	 * The charset name the writers are given, which is not a path.
	 */
	private static final String CHARSET = "UTF-8";

	/**
	 * Hidden constructor of a utility class.
	 */
	private FileOpeningRolesMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Returns the provider of the file's file system, as a student reaches it.
	 *
	 * @param file the file
	 * @return its provider
	 */
	private static FileSystemProvider provider(Path file) {
		return file.getFileSystem().provider();
	}

	/**
	 * Reads the first byte through the provider's input stream.
	 *
	 * @param file the file to read
	 * @return the byte
	 * @throws IOException if reading fails
	 */
	public static int providerRead(Path file) throws IOException {
		try (InputStream input = provider(file).newInputStream(file)) {
			return input.read();
		}
	}

	/**
	 * Writes the text through the provider's output stream, replacing the content.
	 *
	 * @param file the file to write
	 * @param text the text
	 * @throws IOException if writing fails
	 */
	public static void providerWrite(Path file, String text) throws IOException {
		try (OutputStream output = provider(file).newOutputStream(file)) {
			output.write(text.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Writes the text through a byte channel the provider opens with create, write
	 * and truncate.
	 *
	 * @param file the file to write
	 * @param text the text
	 * @throws IOException if writing fails
	 */
	public static void providerByteChannelWrite(Path file, String text) throws IOException {
		try (SeekableByteChannel channel = provider(file).newByteChannel(file, EnumSet.of(StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
			channel.write(ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8)));
		}
	}

	/**
	 * Reads the first byte through a byte channel the provider opens without
	 * options, which the JDK opens for reading.
	 *
	 * @param file the file to read
	 * @return the byte
	 * @throws IOException if reading fails
	 */
	public static int providerByteChannelRead(Path file) throws IOException {
		try (SeekableByteChannel channel = provider(file).newByteChannel(file, Set.of())) {
			return readOneByte(channel);
		}
	}

	/**
	 * Reads the first byte through a file channel the provider opens for reading.
	 *
	 * @param file the file to read
	 * @return the byte
	 * @throws IOException if reading fails
	 */
	public static int providerFileChannelRead(Path file) throws IOException {
		try (FileChannel channel = provider(file).newFileChannel(file, Set.of(StandardOpenOption.READ))) {
			return readOneByte(channel);
		}
	}

	/**
	 * Reads one byte from a channel.
	 *
	 * @param channel the channel
	 * @return the byte
	 * @throws IOException if reading fails
	 */
	private static int readOneByte(SeekableByteChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		channel.read(buffer);
		return buffer.get(0);
	}

	/**
	 * Replaces the content through a {@code RandomAccessFile} opened in mode
	 * {@code "rw"}.
	 *
	 * @param file the file to write
	 * @param text the text
	 * @throws IOException if writing fails
	 */
	public static void randomAccessFileWrite(Path file, String text) throws IOException {
		try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toString(), "rw")) {
			randomAccessFile.setLength(0);
			randomAccessFile.writeBytes(text);
		}
	}

	/**
	 * Moves a file with {@code Files.move}.
	 *
	 * @param source  the file to move
	 * @param target  where it goes
	 * @param replace whether an existing target is replaced
	 * @throws IOException if moving fails
	 */
	public static void filesMove(Path source, Path target, boolean replace) throws IOException {
		Files.move(source, target, copyOptions(replace));
	}

	/**
	 * Moves a file through the provider.
	 *
	 * @param source  the file to move
	 * @param target  where it goes
	 * @param replace whether an existing target is replaced
	 * @throws IOException if moving fails
	 */
	public static void providerMove(Path source, Path target, boolean replace) throws IOException {
		provider(source).move(source, target, copyOptions(replace));
	}

	/**
	 * Copies a file through the provider.
	 *
	 * @param source the file to copy
	 * @param target where the copy goes
	 * @throws IOException if copying fails
	 */
	public static void providerCopy(Path source, Path target) throws IOException {
		provider(source).copy(source, target);
	}

	/**
	 * Returns the copy options of a move.
	 *
	 * @param replace whether an existing target is replaced
	 * @return the options
	 */
	private static CopyOption[] copyOptions(boolean replace) {
		return replace ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[0];
	}

	/**
	 * Writes the text with {@code PrintWriter(String, String charset)}.
	 *
	 * @param file the file to write
	 * @param text the text
	 * @throws IOException if writing fails
	 */
	public static void printWriterCharset(Path file, String text) throws IOException {
		try (PrintWriter writer = new PrintWriter(file.toString(), CHARSET)) {
			writer.print(text);
		}
	}

	/**
	 * Writes the text with {@code PrintWriter(File, String charset)}.
	 *
	 * @param file the file to write
	 * @param text the text
	 * @throws IOException if writing fails
	 */
	public static void printWriterFileCharset(Path file, String text) throws IOException {
		try (PrintWriter writer = new PrintWriter(new File(file.toString()), CHARSET)) {
			writer.print(text);
		}
	}

	/**
	 * Writes the text with {@code Formatter(String, String charset)}.
	 *
	 * @param file the file to write
	 * @param text the text
	 * @throws IOException if writing fails
	 */
	public static void formatterCharset(Path file, String text) throws IOException {
		try (Formatter formatter = new Formatter(file.toString(), CHARSET)) {
			formatter.format("%s", text);
		}
	}

	/**
	 * Writes the text with {@code Formatter(File, String charset)}.
	 *
	 * @param file the file to write
	 * @param text the text
	 * @throws IOException if writing fails
	 */
	public static void formatterFileCharset(Path file, String text) throws IOException {
		try (Formatter formatter = new Formatter(new File(file.toString()), CHARSET)) {
			formatter.format("%s", text);
		}
	}

	/**
	 * Formats the text with {@code new Formatter()}, which writes to no file.
	 *
	 * @param text the text
	 * @return the formatted text
	 */
	public static String formatterWithoutFile(String text) {
		try (Formatter formatter = new Formatter()) {
			return formatter.format("%s", text).toString();
		}
	}

	/**
	 * Reads the first byte through {@code Files.newInputStream} with
	 * {@code DELETE_ON_CLOSE}, which still reads the file.
	 *
	 * @param file the file to read
	 * @return the byte
	 * @throws IOException if reading fails
	 */
	public static int newInputStreamDeleteOnClose(Path file) throws IOException {
		try (InputStream input = Files.newInputStream(file, StandardOpenOption.DELETE_ON_CLOSE)) {
			return input.read();
		}
	}

	/**
	 * Creates a symbolic link through the provider.
	 *
	 * @param link   the link to create
	 * @param target what it points to
	 * @throws IOException if creating fails
	 */
	public static void providerSymbolicLink(Path link, Path target) throws IOException {
		provider(link).createSymbolicLink(link, target);
	}

	/**
	 * Creates a hard link through the provider.
	 *
	 * @param link     the link to create
	 * @param existing the file it names
	 * @throws IOException if creating fails
	 */
	public static void providerHardLink(Path link, Path existing) throws IOException {
		provider(link).createLink(link, existing);
	}

	/**
	 * Deletes a file through the provider's {@code deleteIfExists}.
	 *
	 * @param file the file to delete
	 * @return whether it existed
	 * @throws IOException if deleting fails
	 */
	public static boolean providerDeleteIfExists(Path file) throws IOException {
		return provider(file).deleteIfExists(file);
	}

	/**
	 * Opens a folder as a secure directory stream.
	 *
	 * @param folder the folder
	 * @return the stream
	 * @throws IOException if opening fails
	 */
	@SuppressWarnings("unchecked")
	private static SecureDirectoryStream<Path> secureDirectory(Path folder) throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(folder);
		if (stream instanceof SecureDirectoryStream<?> secure) {
			return (SecureDirectoryStream<Path>) secure;
		}
		stream.close();
		throw new UnsupportedOperationException("no secure directory stream");
	}

	/**
	 * Reads the first byte of a file through a secure directory stream on a folder.
	 *
	 * @param folder the folder the stream is opened on
	 * @param file   the file, as given to the stream
	 * @return the byte
	 * @throws IOException if reading fails
	 */
	public static int secureDirectoryRead(Path folder, Path file) throws IOException {
		try (SecureDirectoryStream<Path> directory = secureDirectory(folder);
				SeekableByteChannel channel = directory.newByteChannel(file, Set.of(StandardOpenOption.READ))) {
			return readOneByte(channel);
		}
	}

	/**
	 * Moves a file through a secure directory stream on a folder.
	 *
	 * @param folder the folder the stream is opened on
	 * @param source the file to move
	 * @param target where it goes
	 * @throws IOException if moving fails
	 */
	public static void secureDirectoryMove(Path folder, Path source, Path target) throws IOException {
		try (SecureDirectoryStream<Path> directory = secureDirectory(folder)) {
			directory.move(source, directory, target);
		}
	}

	/**
	 * Counts the entries of a folder listed through a secure directory stream.
	 *
	 * @param folder the folder the stream is opened on
	 * @param listed the folder to list, as given to the stream
	 * @return the number of entries
	 * @throws IOException if listing fails
	 */
	public static int secureDirectoryList(Path folder, Path listed) throws IOException {
		int count = 0;
		try (SecureDirectoryStream<Path> directory = secureDirectory(folder);
				SecureDirectoryStream<Path> inner = directory.newDirectoryStream(listed)) {
			for (Path ignored : inner) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Moves the bytes of one channel to another with {@code transferTo}.
	 *
	 * @param source the file channel read from
	 * @param target the channel written to
	 * @return the number of bytes moved
	 * @throws IOException if moving fails
	 */
	public static long transferTo(FileChannel source, WritableByteChannel target) throws IOException {
		return source.transferTo(0, Long.MAX_VALUE, target);
	}

	/**
	 * Moves the bytes of one channel to another with {@code transferFrom}.
	 *
	 * @param target the file channel written to
	 * @param source the channel read from
	 * @return the number of bytes moved
	 * @throws IOException if moving fails
	 */
	public static long transferFrom(FileChannel target, ReadableByteChannel source) throws IOException {
		return target.transferFrom(source, 0, Long.MAX_VALUE);
	}

	/**
	 * Opens a new file for writing and moves the bytes of a channel into it with
	 * {@code transferFrom}.
	 *
	 * @param target the new file
	 * @param source the channel read from
	 * @return the number of bytes moved
	 * @throws IOException if opening or moving fails
	 */
	public static long transferFromIntoNewFile(Path target, ReadableByteChannel source) throws IOException {
		try (FileChannel channel = FileChannel.open(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			return channel.transferFrom(source, 0, Long.MAX_VALUE);
		}
	}

	/**
	 * Copies a file onto a destination with {@code Files.copy}, replacing what is
	 * there.
	 *
	 * @param source the file copied
	 * @param target the destination
	 * @throws IOException if copying fails
	 */
	public static void filesCopyReplace(Path source, Path target) throws IOException {
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Writes text into a file with {@code Files.writeString}.
	 *
	 * @param file the file
	 * @param text the text
	 * @throws IOException if writing fails
	 */
	public static void writeStringText(Path file, CharSequence text) throws IOException {
		Files.writeString(file, text);
	}

	/**
	 * Writes text into a file through a {@code FileOutputStream}.
	 *
	 * @param file the file
	 * @param text the text
	 * @throws IOException if writing fails
	 */
	public static void fileOutputStreamWrite(File file, String text) throws IOException {
		try (FileOutputStream out = new FileOutputStream(file)) {
			out.write(text.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Opens a file with a {@code PrintWriter} in a named charset, and reports a
	 * charset the JDK does not know instead of throwing.
	 *
	 * @param file    the file
	 * @param charset the charset name
	 * @return whether the writer was opened
	 * @throws IOException if opening fails for another reason
	 */
	public static boolean printWriterWithCharset(File file, String charset) throws IOException {
		try (PrintWriter writer = new PrintWriter(file, charset)) {
			writer.flush();
			return true;
		} catch (UnsupportedEncodingException unknownCharset) {
			return false;
		}
	}

	/**
	 * Writes text through a file channel the caller opened.
	 *
	 * @param channel the channel
	 * @param text    the text
	 * @return the number of bytes written
	 * @throws IOException if writing fails
	 */
	public static int writeTo(FileChannel channel, String text) throws IOException {
		return channel.write(ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Builds a redirect that writes a process's output to a file.
	 *
	 * @param file the file
	 * @return the redirect
	 */
	public static ProcessBuilder.Redirect redirectTo(File file) {
		return ProcessBuilder.Redirect.to(file);
	}

	/**
	 * Builds a redirect that appends a process's output to a file.
	 *
	 * @param file the file
	 * @return the redirect
	 */
	public static ProcessBuilder.Redirect redirectAppendTo(File file) {
		return ProcessBuilder.Redirect.appendTo(file);
	}

	/**
	 * Builds a redirect that feeds a process's input from a file.
	 *
	 * @param file the file
	 * @return the redirect
	 */
	public static ProcessBuilder.Redirect redirectFrom(File file) {
		return ProcessBuilder.Redirect.from(file);
	}

	/**
	 * Sends the output of a process builder made by the caller to a file.
	 *
	 * @param builder the process builder
	 * @param file    the file
	 * @return the builder
	 */
	public static ProcessBuilder redirectOutput(ProcessBuilder builder, File file) {
		return builder.redirectOutput(file);
	}

	/**
	 * Feeds the input of a process builder made by the caller from a file.
	 *
	 * @param builder the process builder
	 * @param file    the file
	 * @return the builder
	 */
	public static ProcessBuilder redirectInput(ProcessBuilder builder, File file) {
		return builder.redirectInput(file);
	}
}
