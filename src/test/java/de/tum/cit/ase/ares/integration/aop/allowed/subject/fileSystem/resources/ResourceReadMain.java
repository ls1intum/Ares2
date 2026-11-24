package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Supervised code that reads a resource from the class path, through the class
 * or through its class loader.
 */
public final class ResourceReadMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private ResourceReadMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Reads a resource next to this class through
	 * {@code Class.getResourceAsStream}.
	 *
	 * @param name the resource's name relative to this package
	 * @return its content, or {@code null} if there is no such resource
	 * @throws IOException if reading fails
	 */
	public static String readThroughClass(String name) throws IOException {
		return readAll(ResourceReadMain.class.getResourceAsStream(name));
	}

	/**
	 * Reads a resource next to this class through the class loader's
	 * {@code getResourceAsStream}.
	 *
	 * @param name the resource's name relative to this package
	 * @return its content, or {@code null} if there is no such resource
	 * @throws IOException if reading fails
	 */
	public static String readThroughClassLoader(String name) throws IOException {
		String packagePath = ResourceReadMain.class.getPackageName().replace('.', '/');
		return readAll(ResourceReadMain.class.getClassLoader().getResourceAsStream(packagePath + "/" + name));
	}

	/**
	 * Reads a stream to its end and closes it.
	 *
	 * @param stream the stream, or {@code null}
	 * @return the text read, or {@code null} for no stream
	 * @throws IOException if reading fails
	 */
	private static String readAll(InputStream stream) throws IOException {
		if (stream == null) {
			return null;
		}
		try (stream) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
		}
	}
}
