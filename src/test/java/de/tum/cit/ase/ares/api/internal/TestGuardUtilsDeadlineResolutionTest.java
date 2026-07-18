package de.tum.cit.ase.ares.api.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.ExtendedDeadline;

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

	private TestGuardUtils.ResolvedDeadlineAnnotations resolve(Class<?> type, String methodName) throws Exception {
		Method method = type.getDeclaredMethod(methodName);
		return TestGuardUtils.resolveDeadlineAnnotations(Optional.of(type), Optional.of(method));
	}
}
