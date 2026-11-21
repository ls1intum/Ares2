package de.tum.cit.ase.ares.api.architecture.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies the shared, versioned forbidden-method policy used by both engines.
 */
class BlacklistParityTest {
	@TempDir
	Path temporaryDirectory;

	/**
	 * The {@code Thread} members the thread rule deliberately permits, in source
	 * notation. Interrupting stays forbidden, since a stored foreign thread could
	 * be interrupted just like the current one.
	 */
	private static final Set<String> PERMITTED_THREAD_MEMBERS = Set.of("java.lang.Thread.currentThread()");

	private static final List<List<Path>> RESOURCE_PAIRS = List.of(
			List.of(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS, FileHandlerConstants.WALA_FILESYSTEM_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_NETWORK_METHODS, FileHandlerConstants.WALA_NETWORK_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS,
					FileHandlerConstants.WALA_JVM_TERMINATION_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_REFLECTION_METHODS, FileHandlerConstants.WALA_REFLECTION_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS,
					FileHandlerConstants.WALA_COMMAND_EXECUTION_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS,
					FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS,
					FileHandlerConstants.WALA_SERIALIZATION_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_CLASSLOADER_METHODS, FileHandlerConstants.WALA_CLASSLOADER_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_NATIVE_CODE_METHODS, FileHandlerConstants.WALA_NATIVE_CODE_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_AGENT_ATTACH_METHODS, FileHandlerConstants.WALA_AGENT_ATTACH_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_ENVIRONMENT_ACCESS_METHODS,
					FileHandlerConstants.WALA_ENVIRONMENT_ACCESS_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_MODULE_SYSTEM_METHODS,
					FileHandlerConstants.WALA_MODULE_SYSTEM_METHODS),
			List.of(FileHandlerConstants.ARCHUNIT_JNDI_INJECTION_METHODS,
					FileHandlerConstants.WALA_JNDI_INJECTION_METHODS));

	@Test
	void everyCategoryHasExactEffectiveParityAndValidatedResources() {
		assertThat(ForbiddenMethodMatcher.POLICY_SCHEMA_VERSION).isEqualTo("2");
		for (List<Path> pair : RESOURCE_PAIRS) {
			Set<String> archunit = ForbiddenMethodMatcher.effectiveMethods(pair.get(0));
			Set<String> wala = ForbiddenMethodMatcher.effectiveMethods(pair.get(1));
			assertThat(archunit).as("effective policy for %s", pair.get(0).getFileName()).isNotEmpty()
					.containsExactlyInAnyOrderElementsOf(wala);
		}
	}

	@Test
	void matchingUsesExactSignatureAndExplicitBoundaries() {
		assertThat(ForbiddenMethodMatcher.matches("java.lang.System.exit(I)V", "java.lang.System.exit")).isTrue();
		assertThat(ForbiddenMethodMatcher.matches("java.lang.System.exitValue()I", "java.lang.System.exit")).isFalse();
		assertThat(ForbiddenMethodMatcher.matches(
				"java.lang.reflect.Method.invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
				"java.lang.reflect.Method.invoke(java.lang.Object, java.lang.Object[])")).isTrue();
		assertThat(ForbiddenMethodMatcher.matches("java.lang.reflective.Custom.invoke()", "java.lang.reflect"))
				.isFalse();
		assertThat(ForbiddenMethodMatcher.matches("java.lang.reflect.Method.invokeExact()", "java.lang.reflect"))
				.isTrue();
	}

	@Test
	void duplicateAndMalformedEntriesAreRejectedBeforeRuleCreation() throws java.io.IOException {
		Path duplicate = temporaryDirectory.resolve("duplicate-methods.txt");
		Files.writeString(duplicate, "java.lang.System.exit(I)\njava.lang.System.exit(I)\n");
		assertThatThrownBy(() -> ForbiddenMethodMatcher.effectiveMethods(duplicate))
				.isInstanceOf(SecurityException.class).hasMessageContaining("System.exit");

		Path malformed = temporaryDirectory.resolve("malformed-methods.txt");
		Files.writeString(malformed, "java.lang.System.exit(I\n");
		assertThatThrownBy(() -> ForbiddenMethodMatcher.effectiveMethods(malformed))
				.isInstanceOf(SecurityException.class).hasMessageContaining("System.exit");
	}

	/**
	 * Checks that the public members of the sensitive JDK classes are forbidden.
	 * For {@code Thread} the nested classes count too, and only the members in
	 * {@link #PERMITTED_THREAD_MEMBERS} may stay out.
	 */
	@Test
	void supportedJdkFacadeSurfaceIsGeneratedAndCovered() {
		assertPublicSurfaceCovered(java.nio.file.Files.class, FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS,
				Set.of());
		assertPublicSurfaceCovered(java.net.Socket.class, FileHandlerConstants.ARCHUNIT_NETWORK_METHODS, Set.of());
		assertThreadSurfaceCoveredExceptPermittedMembers();
		assertPublicSurfaceCovered(Runtime.class, FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS,
				Set.of("exec"));
		assertPublicSurfaceCovered(Runtime.class, FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS,
				Set.of("exit", "halt"));
		assertPublicSurfaceCovered(Runtime.class, FileHandlerConstants.ARCHUNIT_NATIVE_CODE_METHODS,
				Set.of("load", "loadLibrary"));
	}

	private static void assertPublicSurfaceCovered(Class<?> type, Path resource, Set<String> selectedNames) {
		Set<String> forbidden = ForbiddenMethodMatcher.effectiveMethods(resource);
		Stream<Executable> methods = Arrays.stream(type.getDeclaredMethods()).map(method -> (Executable) method);
		Stream<Executable> constructors = Arrays.stream(type.getDeclaredConstructors())
				.map(constructor -> (Executable) constructor);
		List<String> surface = Stream.concat(methods, constructors)
				.filter(member -> Modifier.isPublic(member.getModifiers()))
				.filter(member -> selectedNames.isEmpty() || selectedNames.contains(member.getName()))
				.map(BlacklistParityTest::signatureOf).sorted().toList();
		assertThat(surface).as("generated JDK %s surface on Java %s", type.getName(), Runtime.version().feature())
				.isNotEmpty().allMatch(
						actual -> forbidden.stream()
								.anyMatch(policyEntry -> ForbiddenMethodMatcher.matches(actual, policyEntry)),
						"every public sensitive member is represented by the versioned policy");
	}

	/**
	 * The thread rule permits reading the current thread and nothing more of
	 * {@code Thread}: interrupting, its nested builders and the members once
	 * covered only by a bare {@code java.lang.Thread} entry stay forbidden, in the
	 * notation of either engine.
	 */
	@Test
	void threadRulePermitsReadingTheCurrentThreadOnly() {
		Set<String> forbidden = ForbiddenMethodMatcher
				.effectiveMethods(FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS);
		assertThat(forbidden).doesNotContain("java.lang.Thread");
		assertThat(List.of("java.lang.Thread.currentThread()", "java.lang.Thread.currentThread()Ljava/lang/Thread;"))
				.noneMatch(signature -> isForbidden(signature, forbidden));
		assertThat(List.of("java.lang.Thread.interrupt()", "java.lang.Thread.interrupt()V",
				"java.lang.VirtualThread.interrupt()V", "java.lang.Thread.ofVirtual()", "java.lang.Thread.ofPlatform()",
				"java.lang.Thread$Builder.unstarted(java.lang.Runnable)", "java.lang.Thread$Builder.factory()",
				"java.lang.Thread$Builder$OfVirtual.start(java.lang.Runnable)",
				"java.lang.Thread$Builder$OfVirtual.unstarted(java.lang.Runnable)",
				"java.lang.Thread$Builder$OfPlatform.daemon(boolean)", "java.lang.Thread.stop()",
				"java.lang.Thread.suspend()", "java.lang.Thread.resume()", "java.lang.Thread.countStackFrames()",
				"java.lang.Thread.checkAccess()", "java.lang.Thread.clone()", "java.lang.Thread.dumpStack()",
				"java.lang.Thread.holdsLock(java.lang.Object)", "java.lang.Thread.onSpinWait()",
				"java.lang.Thread.isVirtual()", "java.lang.Thread$State.values()",
				"java.lang.Thread$UncaughtExceptionHandler.uncaughtException(java.lang.Thread, java.lang.Throwable)"))
						.allMatch(signature -> isForbidden(signature, forbidden));
	}

	/**
	 * Asserts that every public member of {@code Thread} and of its public nested
	 * classes on the running JDK is forbidden, apart from
	 * {@link #PERMITTED_THREAD_MEMBERS}.
	 */
	private static void assertThreadSurfaceCoveredExceptPermittedMembers() {
		Set<String> forbidden = ForbiddenMethodMatcher
				.effectiveMethods(FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS);
		List<String> surface = Stream.concat(Stream.of(Thread.class), nestedClassesOf(Thread.class))
				.flatMap(type -> Stream.concat(Arrays.stream(type.getDeclaredMethods()),
						Arrays.stream(type.getDeclaredConstructors())))
				.filter(member -> Modifier.isPublic(member.getModifiers())).map(BlacklistParityTest::signatureOf)
				.filter(signature -> !PERMITTED_THREAD_MEMBERS.contains(signature)).sorted().toList();
		assertThat(surface).as("generated JDK Thread surface on Java %s", Runtime.version().feature()).isNotEmpty()
				.allMatch(signature -> isForbidden(signature, forbidden),
						"every public Thread member but the permitted ones is forbidden");
		assertThat(PERMITTED_THREAD_MEMBERS).noneMatch(signature -> isForbidden(signature, forbidden));
	}

	/**
	 * Returns the public classes nested in a class, at any depth. Code outside the
	 * JDK cannot name the private ones.
	 *
	 * @param type the outer class
	 * @return the public nested classes
	 */
	private static Stream<Class<?>> nestedClassesOf(Class<?> type) {
		return Arrays.stream(type.getDeclaredClasses()).filter(nested -> Modifier.isPublic(nested.getModifiers()))
				.flatMap(nested -> Stream.concat(Stream.of(nested), nestedClassesOf(nested)));
	}

	/**
	 * Tells whether a signature matches an entry of a forbidden-method set.
	 *
	 * @param signature the signature, in source or descriptor notation
	 * @param forbidden the effective forbidden methods
	 * @return {@code true} if an entry matches
	 */
	private static boolean isForbidden(String signature, Set<String> forbidden) {
		return forbidden.stream().anyMatch(entry -> ForbiddenMethodMatcher.matches(signature, entry));
	}

	private static String signatureOf(Executable executable) {
		String name = executable instanceof java.lang.reflect.Constructor<?> ? "<init>" : executable.getName();
		String parameters = Arrays.stream(executable.getParameterTypes()).map(Class::getTypeName)
				.collect(java.util.stream.Collectors.joining(", "));
		return executable.getDeclaringClass().getName() + "." + name + "(" + parameters + ")";
	}
}
