package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Checks archive receivers before virtual dispatch reaches student overrides.
 */
public final class JavaInstrumentationArchiveCallSite {

	/** Prevents instantiation of this call-site holder. */
	private JavaInstrumentationArchiveCallSite() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
				.localize("security.instrumentation.utility.initialization", "JavaInstrumentationArchiveCallSite"));
	}

	/**
	 * Checks a zip receiver before enumerating entries.
	 *
	 * @param receiver the open archive
	 * @return its entries
	 */
	public static Enumeration<? extends ZipEntry> entries(ZipFile receiver) {
		check(receiver, ZipFile.class.getName(), "entries", "()Ljava/util/Enumeration;", new Object[0]);
		return receiver.entries();
	}

	/**
	 * Checks a jar receiver before enumerating entries.
	 *
	 * @param receiver the open archive
	 * @return its entries
	 */
	public static Enumeration<JarEntry> entries(JarFile receiver) {
		check(receiver, JarFile.class.getName(), "entries", "()Ljava/util/Enumeration;", new Object[0]);
		return receiver.entries();
	}

	/**
	 * Checks a zip receiver before reading an entry.
	 *
	 * @param receiver the open archive
	 * @param entry    the entry to read
	 * @return its stream
	 * @throws IOException if the entry cannot be read
	 */
	public static InputStream getInputStream(ZipFile receiver, ZipEntry entry) throws IOException {
		check(receiver, ZipFile.class.getName(), "getInputStream", "(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;",
				new Object[] { entry });
		return receiver.getInputStream(entry);
	}

	/**
	 * Checks a jar receiver before reading an entry.
	 *
	 * @param receiver the open archive
	 * @param entry    the entry to read
	 * @return its stream
	 * @throws IOException if the entry cannot be read
	 */
	public static InputStream getInputStream(JarFile receiver, ZipEntry entry) throws IOException {
		check(receiver, JarFile.class.getName(), "getInputStream", "(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;",
				new Object[] { entry });
		return receiver.getInputStream(entry);
	}

	/**
	 * Applies the ordinary file policy before virtual archive dispatch.
	 *
	 * @param receiver   the open archive
	 * @param owner      the JDK type named by the call site
	 * @param method     the called method
	 * @param descriptor the method descriptor
	 * @param arguments  the call arguments
	 */
	private static void check(ZipFile receiver, String owner, String method, String descriptor, Object[] arguments) {
		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("read", owner, method, descriptor,
				new Object[0], arguments, receiver);
	}
}
