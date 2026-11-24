package de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.allowed;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Supervised code that only uses calls a policy without file and thread
 * allowances still permits: stream wrappers over memory, thread getters and a
 * named logger.
 */
public final class StaticAllowedMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private StaticAllowedMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Reads and writes through wrappers over memory, loads and stores properties,
	 * asks the current thread about itself and logs one line.
	 *
	 * @return a summary of what was read, so nothing is optimised away
	 * @throws IOException never, the streams are in memory
	 */
	public static String useWrappers() throws IOException {
		byte[] bytes = "a=b".getBytes(StandardCharsets.UTF_8);
		InputStream input = new BufferedInputStream(new ByteArrayInputStream(bytes));
		int first = input.read();
		byte[] rest = input.readAllBytes();
		Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
		int character = reader.read();
		int fromArray = new CharArrayReader(new char[] { 'x' }).read();
		Writer writer = new BufferedWriter(new OutputStreamWriter(new ByteArrayOutputStream(), StandardCharsets.UTF_8));
		writer.write("hello");
		writer.flush();
		new BufferedOutputStream(new ByteArrayOutputStream()).write(1);
		Properties properties = new Properties();
		properties.load(new StringReader("key=value"));
		properties.store(new StringWriter(), null);
		Thread current = Thread.currentThread();
		Logger.getLogger(StaticAllowedMain.class.getName()).info("wrappers used");
		return first + ":" + rest.length + ":" + character + ":" + fromArray + ":" + properties.getProperty("key") + ":"
				+ current.getName().isEmpty() + ":" + current.isInterrupted() + ":"
				+ (current.getState() == Thread.State.RUNNABLE);
	}
}
