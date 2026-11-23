package de.tum.cit.ase.ares.api.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Checks that the utility classes of the architecture package refuse to be
 * built.
 */
class ArchitectureUtilityConstructorTest {

	/**
	 * Calls the private constructor of a class and expects the refusal.
	 *
	 * @param binaryName the binary name of the utility class
	 * @throws ReflectiveOperationException if the class or constructor is missing
	 */
	@ParameterizedTest
	@ValueSource(strings = { "de.tum.cit.ase.ares.api.architecture.java.ForbiddenMethodMatcher",
			"de.tum.cit.ase.ares.api.architecture.java.wala.WalaOutcomeCacheStore",
			"de.tum.cit.ase.ares.api.architecture.java.wala.ReachabilityChecker",
			"de.tum.cit.ase.ares.api.architecture.java.wala.WalaPathClassification",
			"de.tum.cit.ase.ares.api.architecture.java.archunit.TypeAnnotationDependencies",
			"de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCaseCollection" })
	void refusesToBeInstantiated(String binaryName) throws ReflectiveOperationException {
		Class<?> utility = Class.forName(binaryName);
		Constructor<?> constructor = utility.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class).satisfies(thrown -> {
			Throwable cause = thrown.getCause();
			assertThat(cause).isInstanceOf(SecurityException.class);
			assertThat(cause)
					.hasMessage(Messages.localized("security.general.utility.initialization", utility.getSimpleName()));
		});
	}
}
