package de.tum.cit.ase.ares.testutilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * Compiles a generated test-behaviour settings class and loads it through a
 * fresh {@link URLClassLoader}, isolated from the JVM's default classloader.
 * <p>
 * Description: A class loaded once under a given fully-qualified name stays
 * resident for the rest of that classloader's life, so re-testing
 * {@code ConfigurationUtils}'s reflective lookup with different field values
 * across several test methods - some in the same reused Surefire fork - needs a
 * fresh classloader per scenario rather than the JVM's shared one.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
public final class GeneratedSettingsClassTestSupport {

	private GeneratedSettingsClassTestSupport() {
	}

	/**
	 * Compiles an already-written {@code .java} source file and returns a
	 * classloader that can load its output.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @param javaSourceFile  the source file to compile; must not be null.
	 * @param outputDirectory the directory to compile into; created if absent.
	 * @return a classloader rooted at {@code outputDirectory}, parented to the
	 *         current thread's context classloader.
	 * @throws IOException if the output directory cannot be created.
	 */
	public static ClassLoader compile(Path javaSourceFile, Path outputDirectory) throws IOException {
		Files.createDirectories(outputDirectory);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		int result = compiler.run(null, null, null, "-d", outputDirectory.toString(), javaSourceFile.toString());
		if (result != 0) {
			throw new IllegalStateException("Failed to compile generated test fixture: " + javaSourceFile);
		}
		try {
			return new URLClassLoader(new URL[] { outputDirectory.toUri().toURL() },
					Thread.currentThread().getContextClassLoader());
		} catch (MalformedURLException impossible) {
			throw new IllegalStateException(impossible);
		}
	}

	/**
	 * Writes the given source under {@code fullyQualifiedName}'s package path
	 * inside {@code tempDir}, then compiles and loads it.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @param tempDir            a scratch directory to write and compile into.
	 * @param fullyQualifiedName the class name the source must declare.
	 * @param javaSource         the full source text.
	 * @return a classloader that can load the compiled class.
	 * @throws IOException if writing or compiling the source fails.
	 */
	public static ClassLoader compileSource(Path tempDir, String fullyQualifiedName, String javaSource)
			throws IOException {
		Path sourceFile = tempDir.resolve(fullyQualifiedName.replace('.', '/') + ".java");
		Files.createDirectories(sourceFile.getParent());
		Files.writeString(sourceFile, javaSource);
		return compile(sourceFile, tempDir);
	}

	/**
	 * Builds the source text for a plain, public final class declaring the given
	 * fields as-is, shared by every feature category's own read-back tests rather
	 * than each hand-assembling the same package/class wrapper.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @param fullyQualifiedName the class name to declare, package included.
	 * @param fieldDeclarations  complete field declarations (e.g.
	 *                           {@code "public static final boolean X = true;"}),
	 *                           one per entry.
	 * @return the full, compilable source text.
	 */
	public static String settingsClassSource(String fullyQualifiedName, String... fieldDeclarations) {
		int lastDot = fullyQualifiedName.lastIndexOf('.');
		String packageName = fullyQualifiedName.substring(0, lastDot);
		String simpleClassName = simpleClassNameOf(fullyQualifiedName);
		StringBuilder source = new StringBuilder();
		source.append("package ").append(packageName).append(";\n");
		source.append("public final class ").append(simpleClassName).append(" {\n");
		for (String fieldDeclaration : fieldDeclarations) {
			source.append("    ").append(fieldDeclaration).append('\n');
		}
		source.append("}\n");
		return source.toString();
	}

	/**
	 * Extracts the simple class name from a fully-qualified name, for matching a
	 * generated file's expected {@code <SimpleName>.java} filename.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @param fullyQualifiedName the class name, package included.
	 * @return the final segment after the last {@code .}.
	 */
	public static String simpleClassNameOf(String fullyQualifiedName) {
		return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
	}

	/**
	 * A body that may throw a checked exception, run by
	 * {@link #runWithClassLoader(ClassLoader, ThrowingRunnable)}.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 */
	public interface ThrowingRunnable {
		void run() throws Exception;
	}

	/**
	 * Runs {@code body} with {@code loader} temporarily set as the current thread's
	 * context classloader, restoring the original afterwards even if {@code body}
	 * throws - what {@code ConfigurationUtils}'s generated-settings lookup actually
	 * reads at call time.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @param loader the classloader to install for the duration of {@code body}.
	 * @param body   the code to run under {@code loader}.
	 * @throws Exception whatever {@code body} throws.
	 */
	public static void runWithClassLoader(ClassLoader loader, ThrowingRunnable body) throws Exception {
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(loader);
		try {
			body.run();
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}
}
