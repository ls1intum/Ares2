package de.tum.cit.ase.ares.api.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.ExtendedDeadline;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

class TestGuardUtilsDeadlineResolutionTest {
	@Deadline("2030-01-01 00:00 UTC")
	@ExtendedDeadline("10d")
	static class ClassDeadline {
		void classOnly() {
		}

		@Deadline("2040-01-01 00:00 UTC")
		@ExtendedDeadline("2d")
		void methodOverrides() {
		}

		@ExtendedDeadline("3d")
		void methodExtendsClass() {
		}
	}

	static class NoDeadline {
		void test() {
		}
	}

	@Deadline("2999-01-01 00:00 UTC")
	static class PublicFixtures {
		@PublicTest
		void classOnly() {
		}

		@PublicTest
		@Deadline("2998-01-01 00:00 UTC")
		void both() {
		}
	}

	static class MethodFixtures {
		@PublicTest
		@Deadline("2998-01-01 00:00 UTC")
		void publicMethodOnly() {
		}

		@PublicTest
		void publicNone() {
		}

		@HiddenTest
		@Deadline("2998-01-01 00:00 UTC")
		void hiddenMethodOnly() {
		}

		@HiddenTest
		void hiddenNone() {
		}
	}

	@Deadline("2999-01-01 00:00 UTC")
	static class HiddenClassFixtures {
		@HiddenTest
		void classOnly() {
		}

		@HiddenTest
		@Deadline("2998-01-01 00:00 UTC")
		void both() {
		}
	}

	@Test
	void resolvesClassMethodOverrideAugmentationAndAbsenceThroughOneHelper() throws Exception {
		var classOnly = resolve(ClassDeadline.class, "classOnly");
		assertEquals(TestGuardUtils.parseDeadline("2030-01-01 00:00 UTC").plus(Duration.ofDays(10)),
				classOnly.effectiveDeadline().orElseThrow());
		assertFalse(classOnly.methodDeadline());
		assertTrue(classOnly.classDeadline());

		var override = resolve(ClassDeadline.class, "methodOverrides");
		assertEquals(TestGuardUtils.parseDeadline("2040-01-01 00:00 UTC").plus(Duration.ofDays(2)),
				override.effectiveDeadline().orElseThrow());
		assertTrue(override.methodDeadline());
		assertTrue(override.classDeadline());

		var augmented = resolve(ClassDeadline.class, "methodExtendsClass");
		assertEquals(TestGuardUtils.parseDeadline("2030-01-01 00:00 UTC").plus(Duration.ofDays(13)),
				augmented.effectiveDeadline().orElseThrow());
		assertTrue(augmented.hasAnyDeadlineAnnotation());

		var absent = resolve(NoDeadline.class, "test");
		assertTrue(absent.effectiveDeadline().isEmpty());
		assertFalse(absent.hasAnyDeadlineAnnotation());
	}

	@Test
	void publicValidationRejectsMethodClassAndBothDeadlinesButAcceptsNone() throws Exception {
		assertThrows(AnnotationFormatError.class,
				() -> TestGuardUtils.checkForHidden(context(PublicFixtures.class, "classOnly", TestType.PUBLIC)));
		assertThrows(AnnotationFormatError.class, () -> TestGuardUtils
				.checkForHidden(context(MethodFixtures.class, "publicMethodOnly", TestType.PUBLIC)));
		assertThrows(AnnotationFormatError.class,
				() -> TestGuardUtils.checkForHidden(context(PublicFixtures.class, "both", TestType.PUBLIC)));
		assertDoesNotThrow(
				() -> TestGuardUtils.checkForHidden(context(MethodFixtures.class, "publicNone", TestType.PUBLIC)));
	}

	@Test
	void hiddenValidationResolvesMethodClassBothAndNoDeadlineIdentically() throws Exception {
		assertEquals(TestGuardUtils.parseDeadline("2999-01-01 00:00 UTC"),
				TestGuardUtils.extractDeadline(context(HiddenClassFixtures.class, "classOnly", TestType.HIDDEN)));
		assertEquals(TestGuardUtils.parseDeadline("2998-01-01 00:00 UTC"),
				TestGuardUtils.extractDeadline(context(MethodFixtures.class, "hiddenMethodOnly", TestType.HIDDEN)));
		assertEquals(TestGuardUtils.parseDeadline("2998-01-01 00:00 UTC"),
				TestGuardUtils.extractDeadline(context(HiddenClassFixtures.class, "both", TestType.HIDDEN)));
		assertThrows(AnnotationFormatError.class,
				() -> TestGuardUtils.checkForHidden(context(MethodFixtures.class, "hiddenNone", TestType.HIDDEN)));
	}

	private TestContext context(Class<?> type, String methodName, TestType testType) throws Exception {
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
				return Optional.of(testType);
			}
		};
	}

	private TestGuardUtils.ResolvedDeadlineAnnotations resolve(Class<?> type, String methodName) throws Exception {
		Method method = type.getDeclaredMethod(methodName);
		return TestGuardUtils.resolveDeadlineAnnotations(Optional.of(type), Optional.of(method));
	}
}
