package de.tum.cit.ase.ares.api.jqwik;

import java.lang.reflect.*;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.lifecycle.*;

import de.tum.cit.ase.ares.api.context.*;

@API(status = Status.INTERNAL)
public class JqwikContext extends TestContext {
	private final MethodLifecycleContext lifecycleContext;

	JqwikContext(MethodLifecycleContext lifecycleContext) {
		this.lifecycleContext = lifecycleContext;
	}

	@Override
	public Optional<Method> testMethod() {
		return Optional.ofNullable(lifecycleContext.targetMethod());
	}

	@Override
	public Optional<Class<?>> testClass() {
		return Optional.ofNullable(lifecycleContext.containerClass());
	}

	@Override
	public Optional<Object> testInstance() {
		return Optional.ofNullable(lifecycleContext.testInstance());
	}

	@Override
	public Optional<AnnotatedElement> annotatedElement() {
		return lifecycleContext.optionalElement();
	}

	@Override
	public Optional<String> displayName() {
		return Optional.of(lifecycleContext.label());
	}

	public PropertyLifecycleContext getPropertyLifecycleContext() {
		if (lifecycleContext instanceof PropertyLifecycleContext propertyLifecycleContext) {
			return propertyLifecycleContext;
		}
		throw new IllegalStateException("This context represents a jqwik try, not a property"); //$NON-NLS-1$
	}

	public static JqwikContext of(PropertyLifecycleContext lifecycleContext) {
		return new JqwikContext(lifecycleContext);
	}

	public static JqwikContext of(TryLifecycleContext lifecycleContext) {
		return new JqwikContext(lifecycleContext);
	}

	@Override
	public Optional<TestType> findTestType() {
		return TestContextUtils.findAnnotationIn(this, JqwikAresTest.class).map(JqwikAresTest::value);
	}
}
