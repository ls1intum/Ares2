package de.tum.cit.ase.ares.testutilities;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.tools.ToolProvider;

/**
 * Compiles test fixtures with {@code javac}. The test build compiles test
 * sources with the AspectJ compiler, which stores type annotations differently
 * from {@code javac}, so fixtures about type annotations are compiled again the
 * way an exercise compiles student code.
 */
public final class JavacFixtures {

	/**
	 * Prevents instantiation, since this class only offers static helpers.
	 */
	private JavacFixtures() {
	}

	/**
	 * Compiles the source of one fixture into the given directory, against the
	 * compiled test classes, and fails when {@code javac} reports an error.
	 *
	 * @param fixture the fixture, whose source lies below {@code src/test/java}
	 * @param output  the directory the class file is written to
	 * @return the output directory
	 */
	public static Path compile(Class<?> fixture, Path output) {
		Path source = Path.of("src/test/java", fixture.getName().replace('.', '/') + ".java");
		int status = ToolProvider.getSystemJavaCompiler().run(null, null, null, "--release", "17", "-proc:none", "-cp",
				testClasses().toString(), "-d", output.toString(), source.toString());
		if (status != 0) {
			throw new IllegalStateException("javac could not compile " + source);
		}
		return output;
	}

	/**
	 * Returns the directory the test build wrote the test classes to.
	 *
	 * @return the test classes directory
	 */
	public static Path testClasses() {
		try {
			return Path.of(JavacFixtures.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException wrongLocation) {
			throw new IllegalStateException("the test classes have no file location", wrongLocation);
		}
	}

	/**
	 * Compiles several source files into one directory against the given class
	 * path, and fails when {@code javac} reports an error.
	 *
	 * @param classPath the class path
	 * @param output    the directory the class files are written to
	 * @param sources   the source files
	 * @return the output directory
	 */
	public static Path compileSources(String classPath, Path output, Iterable<Path> sources) {
		List<String> arguments = new ArrayList<>(
				List.of("--release", "17", "-proc:none", "-nowarn", "-cp", classPath, "-d", output.toString()));
		for (Path source : sources) {
			arguments.add(source.toString());
		}
		int status = ToolProvider.getSystemJavaCompiler().run(null, null, null, arguments.toArray(String[]::new));
		if (status != 0) {
			throw new IllegalStateException("javac could not compile the generated sources");
		}
		return output;
	}
}
