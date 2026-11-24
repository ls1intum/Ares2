package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.ExtendedDeadline;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;

class TestGuardUtilsDeadlineResolutionTest {

	/**
	 * The English prefix of a setup error: caused by the Ares setup, while it is
	 * built.
	 */
	private static final String ENGLISH_PREFIX = "Ares Security Error (Reason: Ares-Code; Stage: Creation): "; //$NON-NLS-1$

	/** The German prefix of a setup error. */
	private static final String GERMAN_PREFIX = "Ares Sicherheitsfehler (Grund: Ares-Code; Phase: Erstellung): "; //$NON-NLS-1$

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
		assertThrows(SecurityException.class,
				() -> TestGuardUtils.checkForHidden(context(PublicFixtures.class, "classOnly", TestType.PUBLIC)));
		assertThrows(SecurityException.class, () -> TestGuardUtils
				.checkForHidden(context(MethodFixtures.class, "publicMethodOnly", TestType.PUBLIC)));
		assertThrows(SecurityException.class,
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
		assertThrows(SecurityException.class,
				() -> TestGuardUtils.checkForHidden(context(MethodFixtures.class, "hiddenNone", TestType.HIDDEN)));
	}

	/**
	 * Each of the seven setup errors of a test guard is a prefixed
	 * {@link SecurityException} that blames the Ares setup while it is built, in
	 * English and German, with the concrete value in its text.
	 */
	@Test
	void everySetupErrorIsAPrefixedSecurityExceptionFromCreation() throws Exception {
		TestContext publicAndHidden = mock(TestContext.class);
		when(publicAndHidden.findTestType()).thenReturn(Optional.of(TestType.HIDDEN), Optional.of(TestType.PUBLIC),
				Optional.of(TestType.HIDDEN), Optional.of(TestType.PUBLIC));
		when(publicAndHidden.displayName()).thenReturn(Optional.of("bothTypes")); //$NON-NLS-1$
		TestContext publicWithDeadline = context(PublicFixtures.class, "classOnly", TestType.PUBLIC); //$NON-NLS-1$
		TestContext hiddenWithoutDeadline = context(MethodFixtures.class, "hiddenNone", TestType.HIDDEN); //$NON-NLS-1$

		assertSetupError(() -> TestGuardUtils.checkForHidden(publicAndHidden),
				"test_guard.test_cannot_be_public_and_hidden", "bothTypes"); //$NON-NLS-1$ //$NON-NLS-2$
		assertSetupError(() -> TestGuardUtils.checkForHidden(publicWithDeadline),
				"test_guard.public_test_cannot_have_deadline", "classOnly"); //$NON-NLS-1$ //$NON-NLS-2$
		assertSetupError(() -> TestGuardUtils.checkForHidden(hiddenWithoutDeadline),
				"test_guard.hidden_test_missing_deadline", "hiddenNone"); //$NON-NLS-1$ //$NON-NLS-2$
		assertSetupError(() -> TestGuardUtils.parseDeadline("not a date"), //$NON-NLS-1$
				"test_guard.invalid_deadline_format", "not a date"); //$NON-NLS-1$ //$NON-NLS-2$
		assertSetupError(() -> TestGuardUtils.parseDuration("tomorrow"), //$NON-NLS-1$
				"test_guard.invalid_extended_deadline_format", "tomorrow"); //$NON-NLS-1$ //$NON-NLS-2$
		assertSetupError(() -> TestGuardUtils.parseDuration("0d"), //$NON-NLS-1$
				"test_guard.extended_deadline_zero_or_negative", "0d"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * The seventh setup text, for a hidden test without deadline in an obfuscated
	 * exercise, is raised by no code on this branch, so only its catalogue values
	 * are checked to carry the setup prefix.
	 */
	@Test
	void obfuscatedHiddenTestWithoutDeadlineTextCarriesTheCreationPrefix() {
		String key = "test_guard.obfuscate_hidden_test_missing_deadline"; //$NON-NLS-1$

		assertThat(inLocale(Locale.ENGLISH, () -> Messages.localized(key, "x"))).startsWith(ENGLISH_PREFIX); //$NON-NLS-1$
		assertThat(inLocale(Locale.GERMAN, () -> Messages.localized(key, "x"))).startsWith(GERMAN_PREFIX); //$NON-NLS-1$
	}

	/**
	 * Checks that a step fails with a plain {@link SecurityException} whose text is
	 * the given catalogue entry, with the setup prefix, in English and German.
	 *
	 * @param step     the step that must fail
	 * @param key      the catalogue key of the expected text
	 * @param argument the value the text names
	 */
	private static void assertSetupError(Executable step, String key, String argument) {
		String english = inLocale(Locale.ENGLISH, () -> assertThrows(SecurityException.class, step).getMessage());
		String german = inLocale(Locale.GERMAN, () -> assertThrows(SecurityException.class, step).getMessage());

		assertThat(english).startsWith(ENGLISH_PREFIX)
				.isEqualTo(inLocale(Locale.ENGLISH, () -> Messages.localized(key, argument)));
		assertThat(german).startsWith(GERMAN_PREFIX)
				.isEqualTo(inLocale(Locale.GERMAN, () -> Messages.localized(key, argument)));
	}

	/**
	 * Runs a step with the given locale as default and returns its text.
	 *
	 * @param locale the locale to use
	 * @param step   the step to run
	 * @return the text of the step
	 */
	private static String inLocale(Locale locale, Supplier<String> step) {
		return AdviceActionWording.inLocale(locale, step);
	}

	private TestContext context(Class<?> type, String methodName, TestType testType) throws Exception {
		return TestContextFixtures.of(type, methodName, testType);
	}

	private TestGuardUtils.ResolvedDeadlineAnnotations resolve(Class<?> type, String methodName) throws Exception {
		Method method = type.getDeclaredMethod(methodName);
		return TestGuardUtils.resolveDeadlineAnnotations(Optional.of(type), Optional.of(method));
	}
}
