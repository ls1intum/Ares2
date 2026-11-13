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

	/**
	 * The localisation class a precompile run copies beside the generated hooks.
	 */
	public static final String COPIED_MESSAGES = "com.example.ares.api.localization.Messages";

	/** The package prefix of those copied checks. */
	public static final String COPIED_CHECKS_PREFIX = "com.example.";

	/** Each stand-in's source, by fully qualified name. */
	private static final Map<String, String> SOURCES = Map.of(COPIED_MESSAGES, """
			package com.example.ares.api.localization;

			public final class Messages {
			}
			""", GENERATED_HOOK, """
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

	/** Loads the stand-ins compiled beside the copied checks. */
	private final ClassLoader loader;

	/**
	 * Loads a copy of the generated timeout from a student's output first, with the
	 * copied checks still reachable, as a planted class would be.
	 */
	private final ClassLoader plantedLoader;

	/**
	 * A loader a student made, which defines both the generated timeout and a copy
	 * of the localisation class from one place of its own.
	 */
	private final ClassLoader forgedLoader;

	/**
	 * A second loader on the very folder of the stand-ins: the same name and the
	 * same code source as the generated timeout, but a different class.
	 */
	private final ClassLoader siblingLoader;

	/**
	 * Holds the loaders of the compiled stand-ins.
	 *
	 * @param loader        the loader of the stand-ins beside the copied checks.
	 * @param plantedLoader the loader that finds a planted copy first.
	 * @param forgedLoader  the loader that brings its own copy of both classes.
	 * @param siblingLoader another loader on the stand-ins' own folder.
	 */
	private GeneratedStackFrames(ClassLoader loader, ClassLoader plantedLoader, ClassLoader forgedLoader,
			ClassLoader siblingLoader) {
		this.loader = loader;
		this.plantedLoader = plantedLoader;
		this.forgedLoader = forgedLoader;
		this.siblingLoader = siblingLoader;
	}

	/**
	 * Compiles the stand-ins into a folder, and a second copy of the generated
	 * timeout into a folder of its own, standing for the student's output.
	 *
	 * @param folder an empty folder.
	 * @return the stand-ins, ready to run code inside
	 * @throws IOException if a source cannot be written or the compiler fails
	 */
	public static GeneratedStackFrames compile(Path folder) throws IOException {
		Path trusted = Files.createDirectories(folder.resolve("trusted"));
		Path planted = Files.createDirectories(folder.resolve("planted"));
		Path forged = Files.createDirectories(folder.resolve("forged"));
		compileInto(trusted, SOURCES);
		compileInto(planted, Map.of(GENERATED_HOOK, SOURCES.get(GENERATED_HOOK)));
		compileInto(forged, SOURCES);
		URL trustedUrl = trusted.toUri().toURL();
		ClassLoader parent = GeneratedStackFrames.class.getClassLoader();
		return new GeneratedStackFrames(new URLClassLoader(new URL[] { trustedUrl }, parent),
				new URLClassLoader(new URL[] { planted.toUri().toURL(), trustedUrl }, parent),
				new URLClassLoader(new URL[] { forged.toUri().toURL() }, parent),
				new URLClassLoader(new URL[] { trustedUrl }, parent));
	}

	/**
	 * Compiles sources into a folder.
	 *
	 * @param folder  the output folder.
	 * @param sources the sources, by fully qualified name.
	 * @throws IOException if a source cannot be written or the compiler fails
	 */
	private static void compileInto(Path folder, Map<String, String> sources) throws IOException {
		List<String> arguments = new ArrayList<>(List.of("-d", folder.toString(), "-proc:none"));
		for (Map.Entry<String, String> source : sources.entrySet()) {
			Path file = folder.resolveSibling(folder.getFileName() + "-src")
					.resolve(source.getKey().replace('.', '/') + ".java");
			Files.createDirectories(file.getParent());
			Files.writeString(file, source.getValue());
			arguments.add(file.toString());
		}
		JavaCompiler compiler = Optional.ofNullable(ToolProvider.getSystemJavaCompiler())
				.orElseThrow(() -> new IllegalStateException("No Java compiler available"));
		if (compiler.run(null, null, null, arguments.toArray(String[]::new)) != 0) {
			throw new IOException("Compiling the stand-in classes failed");
		}
	}

	/**
	 * Runs code inside a static method of one of the stand-ins, so that method's
	 * frame lies below the code on the stack.
	 *
	 * @param from       the loader the stand-in comes from.
	 * @param className  the stand-in's binary name.
	 * @param methodName the method to run inside.
	 * @param body       the code to run.
	 * @return what the code returned
	 * @throws Exception whatever the code threw
	 */
	private static Object inside(ClassLoader from, String className, String methodName, Callable<?> body)
			throws Exception {
		Method method = from.loadClass(className).getMethod(methodName, Callable.class);
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
	 * factory, but only when it was loaded beside the copied checks; Ares's own
	 * check never exempts it; the student's body inside it, a planted copy and
	 * everything else are not exempt.
	 *
	 * @param backend the class holding the backend's
	 *                {@code isThreadCreationFromAresTimeout}.
	 * @throws Exception if a call fails
	 */
	public void verifyTimeoutExemption(Class<?> backend) throws Exception {
		Method copied = backend.getDeclaredMethod("isThreadCreationFromAresTimeout", String.class, String.class,
				ClassLoader.class);
		Method own = backend.getDeclaredMethod("isThreadCreationFromAresTimeout", String.class);
		copied.setAccessible(true);
		own.setAccessible(true);
		String nothing = "de.tum.cit.matches.nothing";
		Assertions.assertTrue(exempted(copied, loader, nothing, GENERATED_HOOK, "executeWithTimeout"),
				"the generated timeout's own submission must be exempt");
		Assertions.assertTrue(exempted(copied, loader, nothing, GENERATED_HOOK, "executeWithTimeout",
				GENERATED_HOOK_FACTORY, "newThread"), "its thread factory's frame must be skipped");
		Assertions.assertFalse(exempted(copied, plantedLoader, nothing, GENERATED_HOOK, "executeWithTimeout"),
				"a planted copy loaded from the student's output must not be exempt");
		Assertions.assertFalse(exempted(copied, forgedLoader, nothing, GENERATED_HOOK, "executeWithTimeout"),
				"a loader that brings its own copy of both classes must not be exempt");
		Assertions.assertFalse(exempted(copied, siblingLoader, nothing, GENERATED_HOOK, "executeWithTimeout"),
				"a same-named class from the same place but another loader must not be exempt");
		Assertions.assertFalse(exempted(own, loader, nothing, GENERATED_HOOK, "executeWithTimeout"),
				"Ares's own check must never exempt a generated class");
		Assertions.assertFalse(
				exempted(copied, loader, nothing, GENERATED_HOOK, "executeWithTimeout", GENERATED_HOOK, "call"),
				"the student's body inside the generated timeout must not be exempt");
		Assertions.assertFalse(exempted(copied, loader, nothing, OTHER_GENERATED_CLASS, "executeWithTimeout"),
				"another class in the reserved package must not be exempt");
		Assertions.assertFalse(exempted(copied, loader, STUDENT_PACKAGE, STUDENT_LOOKALIKE, "executeWithTimeout"),
				"a student class with that method name must not be exempt");
		Assertions.assertFalse(exempted(copied, loader, nothing, STUDENT_LOOKALIKE, "executeWithTimeout"),
				"a student look-alike must not be exempt even when the student package matches nothing");
		Assertions.assertFalse(
				exempted(copied, loader, STUDENT_PACKAGE, GENERATED_HOOK, "executeWithTimeout", STUDENT_CODE, "run"),
				"student code above the generated timeout must not be exempt");
	}

	/**
	 * The check's verdict when called from inside the given stand-in frames.
	 *
	 * @param classifier        the backend's check, with one or three parameters.
	 * @param from              the loader the stand-ins come from.
	 * @param restrictedPackage the student package the check is given.
	 * @param path              the stand-in methods to nest, outermost first, as
	 *                          class and method name pairs.
	 * @return whether the check exempts the thread creation
	 * @throws Exception if a call fails
	 */
	private boolean exempted(Method classifier, ClassLoader from, String restrictedPackage, String... path)
			throws Exception {
		Object[] arguments = classifier.getParameterCount() == 3
				? new Object[] { restrictedPackage, COPIED_CHECKS_PREFIX, loader }
				: new Object[] { restrictedPackage };
		Callable<?> body = () -> classifier.invoke(null, arguments);
		for (int index = path.length - 2; index >= 0; index -= 2) {
			Callable<?> inner = body;
			String className = path[index];
			String methodName = path[index + 1];
			body = () -> inside(from, className, methodName, inner);
		}
		return (Boolean) body.call();
	}
}
