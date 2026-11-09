package de.tum.cit.ase.ares.testutilities;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

/**
 * Compiles the code a precompile run generates and runs it in nested JUnit
 * sessions, shared by every test that proves a generated hook on its real
 * output.
 */
public final class GeneratedCodeRuns {

	/** JUnit's switch for loading extensions through service files. */
	public static final String AUTODETECTION = "junit.jupiter.extensions.autodetection.enabled";

	/** JUnit's filter for which service-file extensions load. */
	public static final String AUTODETECTION_INCLUDE = "junit.jupiter.extensions.autodetection.include";

	/** Not instantiable. */
	private GeneratedCodeRuns() {
	}

	/**
	 * Compiles every Java file below some folders, plus single files, against the
	 * test class path.
	 *
	 * @param output      the class output folder.
	 * @param folders     the source folders.
	 * @param singleFiles further source files.
	 * @throws IOException if compiling fails
	 */
	public static void compile(Path output, List<Path> folders, Path... singleFiles) throws IOException {
		List<String> arguments = new ArrayList<>(
				List.of("-d", output.toString(), "-classpath", System.getProperty("java.class.path"), "-proc:none"));
		for (Path folder : folders) {
			try (Stream<Path> files = Files.walk(folder)) {
				files.filter(file -> file.toString().endsWith(".java")).map(Path::toString).forEach(arguments::add);
			}
		}
		Stream.of(singleFiles).map(Path::toString).forEach(arguments::add);
		JavaCompiler compiler = Optional.ofNullable(ToolProvider.getSystemJavaCompiler())
				.orElseThrow(() -> new IllegalStateException("No Java compiler available"));
		if (compiler.run(null, null, null, arguments.toArray(String[]::new)) != 0) {
			throw new IOException("Compiling the generated code failed");
		}
	}

	/**
	 * Runs one class in a nested JUnit session with the given loader as context
	 * loader and the given display locale, restoring both afterwards.
	 *
	 * @param loader     the loader holding the generated code.
	 * @param className  the class to run.
	 * @param locale     the display locale.
	 * @param parameters the JUnit configuration parameters.
	 * @return the results.
	 * @throws ClassNotFoundException if the class is not on the loader
	 */
	public static EngineExecutionResults runIn(ClassLoader loader, String className, Locale locale,
			Map<String, String> parameters) throws ClassNotFoundException {
		Locale originalLocale = Locale.getDefault(Locale.Category.DISPLAY);
		ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
		try {
			Locale.setDefault(Locale.Category.DISPLAY, locale);
			Thread.currentThread().setContextClassLoader(loader);
			EngineTestKit.Builder builder = EngineTestKit.engine("junit-jupiter");
			parameters.forEach(builder::configurationParameter);
			return builder.selectors(DiscoverySelectors.selectClass(loader.loadClass(className))).execute();
		} finally {
			Thread.currentThread().setContextClassLoader(originalLoader);
			Locale.setDefault(Locale.Category.DISPLAY, originalLocale);
		}
	}

	/**
	 * Runs one class in a nested JUnit session in a fresh loader over the given
	 * class path, with JUnit's extension auto-detection set as asked.
	 *
	 * @param classPath     the class path of the loader.
	 * @param className     the class to run.
	 * @param autodetection whether JUnit loads extensions from service files.
	 * @param include       the auto-detection filter.
	 * @param locale        the display locale.
	 * @return the results.
	 * @throws IOException            if the loader cannot be closed
	 * @throws ClassNotFoundException if the class is not on the class path
	 */
	public static EngineExecutionResults runFresh(URL[] classPath, String className, boolean autodetection,
			String include, Locale locale) throws IOException, ClassNotFoundException {
		try (java.net.URLClassLoader loader = new java.net.URLClassLoader(classPath,
				Thread.currentThread().getContextClassLoader())) {
			return runIn(loader, className, autodetection, include, locale);
		}
	}

	/**
	 * Runs one class in a nested JUnit session with an existing loader, with
	 * JUnit's extension auto-detection set as asked.
	 *
	 * @param loader        the loader holding the generated code.
	 * @param className     the class to run.
	 * @param autodetection whether JUnit loads extensions from service files.
	 * @param include       the auto-detection filter.
	 * @param locale        the display locale.
	 * @return the results.
	 * @throws ClassNotFoundException if the class is not on the loader
	 */
	public static EngineExecutionResults runIn(ClassLoader loader, String className, boolean autodetection,
			String include, Locale locale) throws ClassNotFoundException {
		return runIn(loader, className, locale,
				Map.of(AUTODETECTION, String.valueOf(autodetection), AUTODETECTION_INCLUDE, include));
	}

	/**
	 * The class path of a nested run: the given roots, in order.
	 *
	 * @param roots the class roots and resource folders, first wins.
	 * @return the class path entries
	 * @throws IOException if a path cannot be turned into a URL
	 */
	public static URL[] classPath(List<Path> roots) throws IOException {
		List<URL> urls = new ArrayList<>();
		for (Path root : roots) {
			urls.add(root.toUri().toURL());
		}
		return urls.toArray(URL[]::new);
	}

	/**
	 * The failure messages of every failed event of a run.
	 *
	 * @param results the run's results.
	 * @return the messages, empty text for none.
	 */
	public static List<String> failureMessages(EngineExecutionResults results) {
		return results.allEvents().failed().stream().map(GeneratedCodeRuns::messageOf).toList();
	}

	/**
	 * The failure message of an event.
	 *
	 * @param event the event.
	 * @return the message, empty text for none.
	 */
	public static String messageOf(Event event) {
		return event.getPayload(TestExecutionResult.class).flatMap(TestExecutionResult::getThrowable)
				.map(Throwable::getMessage).map(String::valueOf).orElse("");
	}
}
