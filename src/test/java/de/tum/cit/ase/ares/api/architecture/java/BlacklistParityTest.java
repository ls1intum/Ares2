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

	@Test
	void supportedJdkFacadeSurfaceIsGeneratedAndCovered() {
		assertPublicSurfaceCovered(java.nio.file.Files.class, FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS,
				Set.of());
		assertPublicSurfaceCovered(java.net.Socket.class, FileHandlerConstants.ARCHUNIT_NETWORK_METHODS, Set.of());
		assertPublicSurfaceCovered(Thread.class, FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS, Set.of());
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

	private static String signatureOf(Executable executable) {
		String name = executable instanceof java.lang.reflect.Constructor<?> ? "<init>" : executable.getName();
		String parameters = Arrays.stream(executable.getParameterTypes()).map(Class::getTypeName)
				.collect(java.util.stream.Collectors.joining(", "));
		return executable.getDeclaringClass().getName() + "." + name + "(" + parameters + ")";
	}
}
