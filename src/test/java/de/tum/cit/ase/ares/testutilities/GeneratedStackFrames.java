package de.tum.cit.ase.ares.testutilities;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Assertions;

/**
 * Stand-in classes whose names the thread-creation check recognises, compiled
 * into a temporary folder so they never shadow the real generated hook on the
 * test class path. A test runs code "inside" one of their methods, so that
 * method's frame is on the stack the check walks.
 */
public final class GeneratedStackFrames {

	/** The generated precompile timeout's name, which the check exempts. */
	public static final String GENERATED_HOOK = "de.tum.cit.ase.ares.generated.GeneratedStrictTimeout";

	/** The generated timeout's thread factory, a nested class the check skips. */
	public static final String GENERATED_HOOK_FACTORY = GENERATED_HOOK + "$WorkerFactory";

	/** Another class in the reserved package, which the check does not exempt. */
	public static final String OTHER_GENERATED_CLASS = "de.tum.cit.ase.ares.generated.OtherHook";

	/** A student class with a method named like the exempt one. */
	public static final String STUDENT_LOOKALIKE = "com.example.student.StudentTimeout";

	/** A student class running code on the student's behalf. */
	public static final String STUDENT_CODE = "com.example.student.StudentCode";

	/** The student package the student classes live in. */
	public static final String STUDENT_PACKAGE = "com.example.student";

	/** Each stand-in's source, by fully qualified name. */
	private static final Map<String, String> SOURCES = Map.of(GENERATED_HOOK, """
			package de.tum.cit.ase.ares.generated;

			public final class GeneratedStrictTimeout {
				public static Object executeWithTimeout(java.util.concurrent.Callable<?> body) throws Exception {
					return body.call();
				}

				public static Object call(java.util.concurrent.Callable<?> body) throws Exception {
					return body.call();
				}

				public static final class WorkerFactory {
					public static Object newThread(java.util.concurrent.Callable<?> body) throws Exception {
						return body.call();
					}
				}
			}
			""", OTHER_GENERATED_CLASS, """
			package de.tum.cit.ase.ares.generated;

			public final class OtherHook {
				public static Object executeWithTimeout(java.util.concurrent.Callable<?> body) throws Exception {
					return body.call();
				}
			}
			""", STUDENT_LOOKALIKE, """
			package com.example.student;

			public final class StudentTimeout {
				public static Object executeWithTimeout(java.util.concurrent.Callable<?> body) throws Exception {
					return body.call();
				}
			}
			""", STUDENT_CODE, """
			package com.example.student;

			public final class StudentCode {
				public static Object run(java.util.concurrent.Callable<?> body) throws Exception {
					return body.call();
				}
			}
			""");

	/** Loads the compiled stand-ins. */
	private final ClassLoader loader;

	/**
	 * Holds the loader of the compiled stand-ins.
	 *
	 * @param loader the loader.
	 */
	private GeneratedStackFrames(ClassLoader loader) {
		this.loader = loader;
	}

	/**
	 * Compiles the stand-ins into a folder.
	 *
	 * @param folder an empty folder.
	 * @return the stand-ins, ready to run code inside
	 * @throws IOException if a source cannot be written or the compiler fails
	 */
	public static GeneratedStackFrames compile(Path folder) throws IOException {
		List<String> arguments = new ArrayList<>(List.of("-d", folder.toString(), "-proc:none"));
		for (Map.Entry<String, String> source : SOURCES.entrySet()) {
			String name = source.getKey();
			Path file = folder.resolve("src").resolve(name.replace('.', '/') + ".java");
			Files.createDirectories(file.getParent());
			Files.writeString(file, source.getValue());
			arguments.add(file.toString());
		}
		JavaCompiler compiler = Optional.ofNullable(ToolProvider.getSystemJavaCompiler())
				.orElseThrow(() -> new IllegalStateException("No Java compiler available"));
		if (compiler.run(null, null, null, arguments.toArray(String[]::new)) != 0) {
			throw new IOException("Compiling the stand-in classes failed");
		}
		return new GeneratedStackFrames(
				new URLClassLoader(new URL[] { folder.toUri().toURL() }, GeneratedStackFrames.class.getClassLoader()));
	}

	/**
	 * Runs code inside a static method of one of the stand-ins, so that method's
	 * frame lies below the code on the stack.
	 *
	 * @param className  the stand-in's binary name.
	 * @param methodName the method to run inside.
	 * @param body       the code to run.
	 * @return what the code returned
	 * @throws Exception whatever the code threw
	 */
	public Object inside(String className, String methodName, Callable<?> body) throws Exception {
		Method method = loader.loadClass(className).getMethod(methodName, Callable.class);
		try {
			return method.invoke(null, body);
		} catch (InvocationTargetException thrown) {
			if (thrown.getCause() instanceof Exception failure) {
				throw failure;
			}
			throw thrown;
		}
	}

	/**
	 * Checks every rule of the generated timeout's thread exemption against one
	 * backend's check: its own submission is exempt, also through its thread
	 * factory; the student's body inside it is not; nothing else is.
	 *
	 * @param classifier the backend's {@code isThreadCreationFromAresTimeout}.
	 * @throws Exception if a call fails
	 */
	public void verifyTimeoutExemption(Method classifier) throws Exception {
		classifier.setAccessible(true);
		String nothing = "de.tum.cit.matches.nothing";
		Assertions.assertTrue(exempted(classifier, nothing, GENERATED_HOOK, "executeWithTimeout"),
				"the generated timeout's own submission must be exempt");
		Assertions.assertTrue(exempted(classifier, nothing, GENERATED_HOOK, "executeWithTimeout",
				GENERATED_HOOK_FACTORY, "newThread"), "its thread factory's frame must be skipped");
		Assertions.assertFalse(
				exempted(classifier, nothing, GENERATED_HOOK, "executeWithTimeout", GENERATED_HOOK, "call"),
				"the student's body inside the generated timeout must not be exempt");
		Assertions.assertFalse(exempted(classifier, nothing, OTHER_GENERATED_CLASS, "executeWithTimeout"),
				"another class in the reserved package must not be exempt");
		Assertions.assertFalse(exempted(classifier, STUDENT_PACKAGE, STUDENT_LOOKALIKE, "executeWithTimeout"),
				"a student class with that method name must not be exempt");
		Assertions.assertFalse(exempted(classifier, nothing, STUDENT_LOOKALIKE, "executeWithTimeout"),
				"a student look-alike must not be exempt even when the student package matches nothing");
		Assertions.assertFalse(
				exempted(classifier, STUDENT_PACKAGE, GENERATED_HOOK, "executeWithTimeout", STUDENT_CODE, "run"),
				"student code above the generated timeout must not be exempt");
	}

	/**
	 * The check's verdict when called from inside the given stand-in frames.
	 *
	 * @param classifier        the backend's check.
	 * @param restrictedPackage the student package the check is given.
	 * @param path              the stand-in methods to nest, outermost first, as
	 *                          class and method name pairs.
	 * @return whether the check exempts the thread creation
	 * @throws Exception if a call fails
	 */
	private boolean exempted(Method classifier, String restrictedPackage, String... path) throws Exception {
		Callable<?> body = () -> classifier.invoke(null, restrictedPackage);
		for (int index = path.length - 2; index >= 0; index -= 2) {
			Callable<?> inner = body;
			String className = path[index];
			String methodName = path[index + 1];
			body = () -> inside(className, methodName, inner);
		}
		return (Boolean) body.call();
	}
}
