package de.tum.cit.ase.ares.integration.aop.allowed.subject.networkSystem.localUrl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Student code that reads a file through a {@code file:} URL. */
public final class LocalUrlReadMain {

	/** Prevents creating instances of this utility class. */
	private LocalUrlReadMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Reads a file through the {@code file:} URL of its path.
	 *
	 * @param path the path of the file, relative to the working directory
	 * @return the text of the file
	 * @throws IOException if reading fails
	 */
	public static String readThroughFileUrl(String path) throws IOException {
		URL url = Path.of(path).toAbsolutePath().toUri().toURL();
		try (InputStream stream = url.openStream()) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Reads whatever a URL given as text opens.
	 *
	 * @param url the URL
	 * @return the text it opens
	 * @throws IOException        if reading fails
	 * @throws URISyntaxException if the URL is malformed
	 */
	public static String readThroughUrl(String url) throws IOException, URISyntaxException {
		try (InputStream stream = new URI(url).toURL().openStream()) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
