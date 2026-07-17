package de.tum.cit.ase.ares.api.internal;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;

/** Child-process fixture that must be terminated by {@link TimeoutUtils}. */
public final class TimeoutUtilsForkProbe {
	private TimeoutUtilsForkProbe() {
	}

	public static void main(String[] arguments) throws Throwable {
		Method method = TimeoutUtilsForkProbe.class.getDeclaredMethod("strictTimeoutTarget"); //$NON-NLS-1$
		TestContext context = new ProbeContext(method);
		TimeoutUtils.performTimeoutExecution(() -> {
			while (true) {
				Thread.onSpinWait();
			}
		}, context);
	}

	@StrictTimeout(value = 20, unit = TimeUnit.MILLISECONDS)
	private static void strictTimeoutTarget() {
	}

	private static final class ProbeContext extends TestContext {
		private final Method method;

		private ProbeContext(Method method) {
			this.method = method;
		}

		@Override
		public Optional<Method> testMethod() {
			return Optional.of(method);
		}

		@Override
		public Optional<Class<?>> testClass() {
			return Optional.of(TimeoutUtilsForkProbe.class);
		}

		@Override
		public Optional<Object> testInstance() {
			return Optional.empty();
		}

		@Override
		public Optional<String> displayName() {
			return Optional.of("timeout fork probe"); //$NON-NLS-1$
		}

		@Override
		public Optional<AnnotatedElement> annotatedElement() {
			return Optional.of(method);
		}

		@Override
		public Optional<TestType> findTestType() {
			return Optional.empty();
		}
	}
}
