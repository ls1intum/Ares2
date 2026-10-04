package de.tum.cit.ase.ares.api.jupiter;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.ExtensionContext;

import de.tum.cit.ase.ares.api.context.*;
import de.tum.cit.ase.ares.api.internal.ConfigurationUtils;

@API(status = Status.INTERNAL)
public class JupiterContext extends TestContext {
	private final ExtensionContext extensionContext;

	JupiterContext(ExtensionContext extensionContext) {
		this.extensionContext = extensionContext;
	}

	@Override
	public Optional<Method> testMethod() {
		return findOptionalsInHierarchy(ExtensionContext::getTestMethod).findFirst();
	}

	@Override
	public Optional<Class<?>> testClass() {
		return findOptionalsInHierarchy(ExtensionContext::getTestClass).findFirst();
	}

	@Override
	public Optional<Object> testInstance() {
		return findOptionalsInHierarchy(ExtensionContext::getTestInstance).findFirst();
	}

	@Override
	public Optional<AnnotatedElement> annotatedElement() {
		return findOptionalsInHierarchy(ExtensionContext::getElement).findFirst();
	}

	@Override
	public Optional<String> displayName() {
		return Optional.of(extensionContext.getDisplayName());
	}

	/**
	 * The test's type: a method annotation first, then a class one, then the active
	 * policy's list of hidden tests, which only makes a test hidden.
	 *
	 * @return the type, if any level sets one
	 */
	@Override
	public Optional<TestType> findTestType() {
		Optional<TestType> annotated = TestContextUtils.findAnnotationIn(this, JupiterAresTest.class)
				.map(JupiterAresTest::value);
		if (annotated.isPresent()) {
			return annotated;
		}
		Optional<Class<?>> testClass = testClass();
		if (testClass.isEmpty()) {
			return Optional.empty();
		}
		Optional<String> methodName = testMethod().map(Method::getName);
		return ConfigurationUtils.findPolicyHiddenTests(this)
				.filter(hiddenTests -> hiddenTests.covers(testClass.get(), methodName))
				.map(hiddenTests -> TestType.HIDDEN);
	}

	public ExtensionContext getExtensionContext() {
		return extensionContext;
	}

	private <T> Stream<T> findOptionalsInHierarchy(Function<ExtensionContext, Optional<T>> getter) {
		return hierarchy().map(getter).filter(Optional::isPresent).map(Optional::get);
	}

	private Stream<ExtensionContext> hierarchy() {
		return Stream.iterate(extensionContext, Objects::nonNull, ec -> ec.getParent().orElse(null));
	}

	public static JupiterContext of(ExtensionContext extensionContext) {
		return new JupiterContext(extensionContext);
	}
}
