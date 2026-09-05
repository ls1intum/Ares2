package de.tum.cit.ase.ares.api.internal;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;

import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;

/**
 * A hand-built {@link TestContext} pointing at one real, reflectively looked-up
 * method.
 * <p>
 * {@code ConfigurationUtilsTest} and
 * {@code TestGuardUtilsDeadlineResolutionTest} each built the identical
 * six-method anonymous subclass independently; keeping one copy here means the
 * shape of {@link TestContext} only has to be satisfied in one place when it
 * changes.
 */
final class TestContextFixtures {

	private TestContextFixtures() {
		throw new IllegalStateException("TestContextFixtures is a fixture holder and should not be instantiated");
	}

	/**
	 * Builds a context for a method whose test type is not exercised by the test
	 * using it.
	 *
	 * @param type       the declaring class; must not be null.
	 * @param methodName the method's name; must not be null.
	 * @return a context reporting no {@link TestType}.
	 * @throws NoSuchMethodException if the declaring class has no such method.
	 */
	static TestContext of(Class<?> type, String methodName) throws NoSuchMethodException {
		return of(type, methodName, Optional.empty());
	}

	/**
	 * Builds a context for a method with an explicit test type.
	 *
	 * @param type       the declaring class; must not be null.
	 * @param methodName the method's name; must not be null.
	 * @param testType   the test type the context reports; must not be null.
	 * @return a context reporting the given {@link TestType}.
	 * @throws NoSuchMethodException if the declaring class has no such method.
	 */
	static TestContext of(Class<?> type, String methodName, TestType testType) throws NoSuchMethodException {
		return of(type, methodName, Optional.of(testType));
	}

	private static TestContext of(Class<?> type, String methodName, Optional<TestType> testType)
			throws NoSuchMethodException {
		Method method = type.getDeclaredMethod(methodName);
		return new TestContext() {
			@Override
			public Optional<Method> testMethod() {
				return Optional.of(method);
			}

			@Override
			public Optional<Class<?>> testClass() {
				return Optional.of(type);
			}

			@Override
			public Optional<Object> testInstance() {
				return Optional.empty();
			}

			@Override
			public Optional<String> displayName() {
				return Optional.of(methodName);
			}

			@Override
			public Optional<AnnotatedElement> annotatedElement() {
				return Optional.of(method);
			}

			@Override
			public Optional<TestType> findTestType() {
				return testType;
			}
		};
	}
}
