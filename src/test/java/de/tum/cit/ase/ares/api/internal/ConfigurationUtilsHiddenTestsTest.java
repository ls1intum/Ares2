package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import de.tum.cit.ase.ares.api.ActivateHiddenBefore;
import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.ExtendedDeadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.jupiter.JupiterContext;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/**
 * Checks how a hidden test's schedule and type are resolved: the method
 * annotation first, then the class annotation, then the active policy. Uses
 * real policy files whose list names this class's own fixtures.
 */
class ConfigurationUtilsHiddenTestsTest {

	/** Where the hidden-test policy fixtures live. */
	private static final String FIXTURES = "src/test/resources/de/tum/cit/ase/ares/api/internal/hiddenTests/";

	/** The policy with every field set and a list naming the fixtures. */
	private static final String FULL = FIXTURES + "PolicyHiddenTestsFull.yaml";

	/** The policy's deadline plus its one-day extension. */
	private static final ZonedDateTime POLICY_DEADLINE = ZonedDateTime.parse("2000-01-02T00:00Z[UTC]");

	/** Without annotations, the policy's deadline and extension apply. */
	@Test
	void thePolicyDeadlineAppliesWithoutAnnotations() throws Exception {
		assertThat(TestGuardUtils.extractDeadline(context(PolicyOnly.class, "test"))).isEqualTo(POLICY_DEADLINE);
	}

	/** Class and method extensions add to the policy's deadline. */
	@Test
	void extensionsAddToThePolicyDeadline() throws Exception {
		assertThat(TestGuardUtils.extractDeadline(context(ClassExtension.class, "test")))
				.isEqualTo(POLICY_DEADLINE.plusDays(2));
	}

	/** A class deadline replaces the policy's, extension included. */
	@Test
	void aClassDeadlineWinsOverThePolicy() throws Exception {
		assertThat(TestGuardUtils.extractDeadline(context(ClassDeadline.class, "test")))
				.isEqualTo(ZonedDateTime.parse("2010-01-01T00:00Z[UTC]"));
	}

	/** A method deadline wins over the class and the policy. */
	@Test
	void aMethodDeadlineWinsOverEverything() throws Exception {
		assertThat(TestGuardUtils.extractDeadline(context(ClassDeadline.class, "methodDeadline")))
				.isEqualTo(ZonedDateTime.parse("2020-01-01T00:00Z[UTC]"));
	}

	/** Without annotations, the policy's always-run-before date applies. */
	@Test
	void thePolicyAlwaysRunBeforeApplies() throws Exception {
		assertThat(TestGuardUtils.extractActivationBefore(context(PolicyOnly.class, "test")))
				.contains(ZonedDateTime.parse("1990-01-01T00:00Z[UTC]"));
	}

	/** A class {@code @ActivateHiddenBefore} wins over the policy's date. */
	@Test
	void aClassActivationWinsOverThePolicy() throws Exception {
		assertThat(TestGuardUtils.extractActivationBefore(context(ClassActivation.class, "test")))
				.contains(ZonedDateTime.parse("1995-01-01T00:00Z[UTC]"));
	}

	/** Without a policy, only annotations count, so a bare hidden test has none. */
	@Test
	void noPolicyMeansAnnotationsOnly() throws Exception {
		assertThatThrownBy(() -> TestGuardUtils.extractDeadline(context(NoPolicy.class, "test")))
				.isInstanceOf(AnnotationFormatError.class);
	}

	/** A deactivated policy contributes nothing. */
	@Test
	void deactivatedPolicyMeansAnnotationsOnly() throws Exception {
		assertThatThrownBy(() -> TestGuardUtils.extractDeadline(context(DeactivatedPolicy.class, "test")))
				.isInstanceOf(AnnotationFormatError.class);
	}

	/** A policy without the category contributes nothing. */
	@Test
	void aPolicyWithoutTheCategoryContributesNothing() throws Exception {
		assertThat(ConfigurationUtils.findPolicyHiddenTests(context(WithoutCategory.class, "test"))).isEmpty();
	}

	/** A listed class makes its unannotated tests hidden. */
	@Test
	void aListedClassMakesItsTestsHidden() throws Exception {
		assertThat(jupiterContext(ListedClass.class, "test").findTestType()).contains(TestType.HIDDEN);
	}

	/** A listed class covers its nested classes. */
	@Test
	void aListedClassCoversItsNestedClasses() throws Exception {
		assertThat(jupiterContext(ListedClass.Inner.class, "test").findTestType()).contains(TestType.HIDDEN);
	}

	/** A listed method is hidden; another method of its class is not. */
	@Test
	void onlyTheListedMethodIsHidden() throws Exception {
		assertThat(jupiterContext(Methods.class, "listedMethod").findTestType()).contains(TestType.HIDDEN);
		assertThat(jupiterContext(Methods.class, "otherMethod").findTestType()).isEmpty();
	}

	/** A class-level {@code @Public} wins over the list. */
	@Test
	void aClassAnnotationWinsOverTheList() throws Exception {
		assertThat(jupiterContext(PublicClass.class, "test").findTestType()).contains(TestType.PUBLIC);
	}

	/** A method annotation wins over a listed class. */
	@Test
	void aMethodAnnotationWinsOverTheList() throws Exception {
		assertThat(jupiterContext(ListedClass.class, "publicTest").findTestType()).contains(TestType.PUBLIC);
	}

	/** An entry naming no loadable class fails a public test, naming the entry. */
	@Test
	void anUnmatchedClassFailsAPublicTest() throws Exception {
		TestContext context = context(UnmatchedClass.class, "test");
		when(context.findTestType()).thenReturn(Optional.of(TestType.PUBLIC));

		assertThatThrownBy(() -> TestGuardUtils.checkForHidden(context)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("NoSuchClass");
	}

	/** An entry naming a method its class lacks fails a hidden test, naming it. */
	@Test
	void anUnmatchedMethodFailsAHiddenTest() throws Exception {
		TestContext context = context(UnmatchedMethod.class, "test");
		when(context.findTestType()).thenReturn(Optional.of(TestType.HIDDEN));

		assertThatThrownBy(() -> TestGuardUtils.checkForHidden(context)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Methods#noSuchMethod");
	}

	/**
	 * Under a policy that hides unlisted tests, an unannotated, unlisted test is
	 * hidden, a public entry makes its test public, and a class annotation still
	 * wins over both.
	 *
	 * @throws Exception if the fixture cannot be read
	 */
	@Test
	void theUnlistedSwitchComesLast() throws Exception {
		assertThat(jupiterContext(UnlistedHidden.class, "unlistedMethod").findTestType()).contains(TestType.HIDDEN);
		assertThat(jupiterContext(UnlistedHidden.class, "publicMethod").findTestType()).contains(TestType.PUBLIC);
		assertThat(jupiterContext(UnlistedHiddenButPublicClass.class, "test").findTestType()).contains(TestType.PUBLIC);
	}

	/**
	 * An unmatched public entry fails a public and a hidden test, naming the list
	 * and the entry.
	 *
	 * @throws Exception if the fixture cannot be read
	 */
	@Test
	void anUnmatchedPublicEntryFailsEveryTest() throws Exception {
		for (TestType type : TestType.values()) {
			TestContext context = context(UnmatchedPublic.class, "test");
			when(context.findTestType()).thenReturn(Optional.of(type));

			assertThatThrownBy(() -> TestGuardUtils.checkForHidden(context))
					.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("theFollowingTestsArePublic")
					.hasMessageContaining("Methods#noSuchPublicMethod");
		}
	}

	/**
	 * Entries naming interface default methods, directly or through another
	 * interface, by the class that inherits them are valid: a public test of that
	 * class still runs.
	 *
	 * @throws Exception if the fixture cannot be read
	 */
	@Test
	void interfaceDefaultMethodsListedThroughTheirClassAreValid() throws Exception {
		TestContext context = context(InheritsInterfaceDefaults.class, "test");
		when(context.findTestType()).thenReturn(Optional.of(TestType.PUBLIC));

		assertThatCode(() -> TestGuardUtils.checkForHidden(context)).doesNotThrowAnyException();
	}

	/**
	 * An interface default method listed through the class inheriting it is hidden
	 * when JUnit runs it for that class.
	 *
	 * @throws Exception if the fixture cannot be read
	 */
	@Test
	void anInheritedInterfaceDefaultMethodIsHidden() throws Exception {
		Method inherited = DefaultTests.class.getDeclaredMethod("interfaceDefault");

		assertThat(jupiterContext(InheritsInterfaceDefaults.class, inherited).findTestType()).contains(TestType.HIDDEN);
	}

	/**
	 * A mocked context for a fixture method.
	 *
	 * @param type       the fixture class.
	 * @param methodName the fixture method.
	 * @return the context
	 * @throws NoSuchMethodException if the method does not exist
	 */
	private static TestContext context(Class<?> type, String methodName) throws NoSuchMethodException {
		Method method = type.getDeclaredMethod(methodName);
		TestContext context = mock(TestContext.class);
		when(context.testMethod()).thenReturn(Optional.of(method));
		when(context.testClass()).thenReturn(Optional.of(type));
		when(context.displayName()).thenReturn(Optional.of(methodName));
		return context;
	}

	/**
	 * A Jupiter context over a mocked extension context for a fixture method.
	 *
	 * @param type       the fixture class.
	 * @param methodName the fixture method.
	 * @return the context
	 * @throws NoSuchMethodException if the method does not exist
	 */
	private static JupiterContext jupiterContext(Class<?> type, String methodName) throws NoSuchMethodException {
		return jupiterContext(type, type.getDeclaredMethod(methodName));
	}

	/**
	 * A Jupiter context over a mocked extension context for a test method JUnit
	 * runs for a class, which may be inherited from elsewhere.
	 *
	 * @param type   the class JUnit runs the test for.
	 * @param method the test method.
	 * @return the context
	 */
	private static JupiterContext jupiterContext(Class<?> type, Method method) {
		ExtensionContext extensionContext = mock(ExtensionContext.class);
		when(extensionContext.getTestMethod()).thenReturn(Optional.of(method));
		when(extensionContext.getTestClass()).thenReturn(Optional.of(type));
		when(extensionContext.getElement()).thenReturn(Optional.of(method));
		when(extensionContext.getParent()).thenReturn(Optional.empty());
		return JupiterContext.of(extensionContext);
	}

	/** A class under the full policy without annotations. */
	@Policy(FULL)
	static class PolicyOnly {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/** A class extending the policy's deadline at class and method level. */
	@Policy(FULL)
	@ExtendedDeadline("1d")
	static class ClassExtension {
		/** Read reflectively. */
		@ExtendedDeadline("1d")
		void test() {
			// Fixture only.
		}
	}

	/** A class with its own deadline under the full policy. */
	@Policy(FULL)
	@Deadline("2010-01-01 00:00 UTC")
	static class ClassDeadline {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}

		/** Read reflectively. */
		@Deadline("2020-01-01 00:00 UTC")
		void methodDeadline() {
			// Fixture only.
		}
	}

	/** A class with its own always-run-before date under the full policy. */
	@Policy(FULL)
	@ActivateHiddenBefore("1995-01-01 00:00 UTC")
	static class ClassActivation {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/** A class without a policy. */
	static class NoPolicy {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/** A class under the full policy, deactivated. */
	@Policy(value = FULL, activated = false)
	static class DeactivatedPolicy {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/** A class under a policy without the behaviour category. */
	@Policy(FIXTURES + "PolicyWithoutTestBehavior.yaml")
	static class WithoutCategory {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/** A class the full policy lists. */
	@Policy(FULL)
	static class ListedClass {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}

		/** Read reflectively. */
		@PublicTest
		void publicTest() {
			// Fixture only.
		}

		/** A nested class inside the listed one. */
		class Inner {
			/** Read reflectively. */
			void test() {
				// Fixture only.
			}
		}
	}

	/** A class one of whose methods the full policy lists. */
	@Policy(FULL)
	static class Methods {
		/** Read reflectively. */
		void listedMethod() {
			// Fixture only.
		}

		/** Read reflectively. */
		void otherMethod() {
			// Fixture only.
		}
	}

	/** A public class the full policy lists. */
	@Policy(FULL)
	@Public
	static class PublicClass {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/** A class under a policy listing a class that does not exist. */
	@Policy(FIXTURES + "PolicyHiddenTestsUnmatchedClass.yaml")
	static class UnmatchedClass {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/** A class under a policy listing a method that does not exist. */
	@Policy(FIXTURES + "PolicyHiddenTestsUnmatchedMethod.yaml")
	static class UnmatchedMethod {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/** An interface whose default method is inherited through another one. */
	interface GrandparentTests {
		/** Read reflectively. */
		default void grandparentDefault() {
			// Fixture only.
		}
	}

	/** An interface with a default test method, as JUnit runs it. */
	interface DefaultTests extends GrandparentTests {
		/** Read reflectively. */
		default void interfaceDefault() {
			// Fixture only.
		}
	}

	/** A class under a policy listing the default methods it inherits. */
	@Policy(FIXTURES + "PolicyHiddenTestsInheritedInterface.yaml")
	static class InheritsInterfaceDefaults implements DefaultTests {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/**
	 * A class under a policy that hides unlisted tests, one method listed public.
	 */
	@Policy(FIXTURES + "PolicyHiddenTestsUnlistedHidden.yaml")
	static class UnlistedHidden {
		/** Read reflectively. */
		void unlistedMethod() {
			// Fixture only.
		}

		/** Read reflectively. */
		void publicMethod() {
			// Fixture only.
		}
	}

	/** A public class under a policy that hides unlisted tests. */
	@Policy(FIXTURES + "PolicyHiddenTestsUnlistedHidden.yaml")
	@Public
	static class UnlistedHiddenButPublicClass {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}

	/** A class under a policy whose public list names a missing method. */
	@Policy(FIXTURES + "PolicyHiddenTestsUnmatchedPublic.yaml")
	static class UnmatchedPublic {
		/** Read reflectively. */
		void test() {
			// Fixture only.
		}
	}
}
