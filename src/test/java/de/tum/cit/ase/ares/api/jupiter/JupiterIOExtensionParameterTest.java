package de.tum.cit.ase.ares.api.jupiter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import de.tum.cit.ase.ares.api.WithIOManager;
import de.tum.cit.ase.ares.api.io.AresIOContext;
import de.tum.cit.ase.ares.api.io.IOManager;
import de.tum.cit.ase.ares.api.io.IOTester;
import de.tum.cit.ase.ares.api.io.IOTesterManager;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Checks which test parameters {@link JupiterIOExtension} claims and how it
 * resolves them: an {@link IOTester} the I/O manager cannot provide, and an I/O
 * manager that cannot be created, fail with a prefixed Ares message, while
 * parameters of other resolvers are left alone. Public, so the I/O managers
 * below can be created from another package.
 */
public class JupiterIOExtensionParameterTest {

	/**
	 * The English prefix of a setup error: caused by the Ares setup, while it is
	 * built.
	 */
	private static final String ENGLISH_PREFIX = "Ares Security Error (Reason: Ares-Code; Stage: Creation): "; //$NON-NLS-1$

	/** The German prefix of a setup error. */
	private static final String GERMAN_PREFIX = "Ares Sicherheitsfehler (Grund: Ares-Code; Phase: Erstellung): "; //$NON-NLS-1$

	/**
	 * The extension under test, ended after each test so no tester stays installed.
	 */
	private JupiterIOExtension extension;

	/** The context of the current fixture method, kept to end the extension. */
	private ExtensionContext current;

	/** An I/O manager whose controller is a {@link StringBuilder}. */
	public static final class StringBuilderManager implements IOManager<StringBuilder> {

		/** The controller of the current test. */
		private StringBuilder controller;

		/**
		 * Creates the manager; I/O managers need a public constructor without
		 * parameters.
		 */
		public StringBuilderManager() {
			controller = null;
		}

		/** {@inheritDoc} */
		@Override
		public void beforeTestExecution(AresIOContext context) {
			controller = new StringBuilder();
		}

		/** {@inheritDoc} */
		@Override
		public void afterTestExecution(AresIOContext context) {
			controller = null;
		}

		/** {@inheritDoc} */
		@Override
		public StringBuilder getControllerInstance(AresIOContext context) {
			return controller;
		}

		/** {@inheritDoc} */
		@Override
		public Class<StringBuilder> getControllerClass() {
			return StringBuilder.class;
		}
	}

	/** An I/O manager Ares cannot create, because its constructor needs a value. */
	public static final class UncreatableManager implements IOManager<Void> {

		/** The value the constructor needs. */
		private final int value;

		/**
		 * Creates the manager with a value, which Ares cannot supply.
		 *
		 * @param value any value
		 */
		public UncreatableManager(int value) {
			this.value = value;
		}

		/** {@inheritDoc} */
		@Override
		public void beforeTestExecution(AresIOContext context) {
			throw new IllegalStateException("never created, so never started: " + value); //$NON-NLS-1$
		}

		/** {@inheritDoc} */
		@Override
		public void afterTestExecution(AresIOContext context) {
			throw new IllegalStateException("never created, so never ended: " + value); //$NON-NLS-1$
		}

		/** {@inheritDoc} */
		@Override
		public Void getControllerInstance(AresIOContext context) {
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public Class<Void> getControllerClass() {
			return null;
		}
	}

	/**
	 * Holds the fixture methods whose parameters and annotations the tests read.
	 */
	static class Fixtures {
		/**
		 * Asks for an {@link IOTester} under the default I/O manager.
		 *
		 * @param tester the tester
		 */
		void defaultManager(IOTester tester) {
		}

		/**
		 * Asks for an {@link IOTester} under an I/O manager that provides nothing.
		 *
		 * @param tester the tester
		 */
		@WithIOManager(WithIOManager.None.class)
		void noneManager(IOTester tester) {
		}

		/**
		 * Asks for an {@link IOTester} under an I/O manager that provides a
		 * {@link StringBuilder}.
		 *
		 * @param tester the tester
		 */
		@WithIOManager(StringBuilderManager.class)
		void customManagerAskedForTester(IOTester tester) {
		}

		/**
		 * Asks for the {@link StringBuilder} its custom I/O manager provides.
		 *
		 * @param output the controller
		 */
		@WithIOManager(StringBuilderManager.class)
		void customManager(StringBuilder output) {
		}

		/**
		 * Uses an I/O manager that cannot be created.
		 *
		 * @param tester the tester
		 */
		@WithIOManager(UncreatableManager.class)
		void uncreatableManager(IOTester tester) {
		}

		/**
		 * Asks for a parameter another resolver owns.
		 *
		 * @param info the test information
		 */
		void testInfo(TestInfo info) {
		}

		/**
		 * Asks for a parameter another resolver owns, under a custom I/O manager.
		 *
		 * @param info the test information
		 */
		@WithIOManager(StringBuilderManager.class)
		void customManagerTestInfo(TestInfo info) {
		}
	}

	/**
	 * Ends the extension of the last test, which uninstalls an installed tester.
	 */
	@AfterEach
	void endExtension() {
		if (extension != null && current != null) {
			extension.afterEach(current);
		}
	}

	/**
	 * Under the default I/O manager an {@link IOTester} is claimed and resolved.
	 */
	@Test
	void defaultManagerResolvesTheTester() throws Exception {
		ParameterContext parameter = start("defaultManager", IOTester.class); //$NON-NLS-1$

		assertThat(extension.supportsParameter(parameter, current)).isTrue();
		assertThat(extension.resolveParameter(parameter, current)).isInstanceOf(IOTester.class);
	}

	/** A custom I/O manager's own controller type is claimed and resolved. */
	@Test
	void customManagerResolvesItsOwnController() throws Exception {
		ParameterContext parameter = start("customManager", StringBuilder.class); //$NON-NLS-1$

		assertThat(extension.supportsParameter(parameter, current)).isTrue();
		assertThat(extension.resolveParameter(parameter, current)).isInstanceOf(StringBuilder.class);
	}

	/**
	 * An {@link IOTester} under an I/O manager that provides nothing is claimed,
	 * and resolving it fails with the prefixed Ares message naming the test, the
	 * manager and the fix, in English and German.
	 */
	@Test
	void testerUnderAManagerWithoutControllerFailsWithTheAresMessage() throws Exception {
		ParameterContext parameter = start("noneManager", IOTester.class); //$NON-NLS-1$

		assertThat(extension.supportsParameter(parameter, current)).isTrue();
		String english = AdviceActionWording.inLocale(Locale.ENGLISH, () -> refusalOf(parameter));
		String german = AdviceActionWording.inLocale(Locale.GERMAN, () -> refusalOf(parameter));

		assertThat(english).startsWith(ENGLISH_PREFIX)
				.isEqualTo(AdviceActionWording.inLocale(Locale.ENGLISH,
						() -> Messages.localized("io_tester.parameter_not_provided", //$NON-NLS-1$
								Fixtures.class.getCanonicalName() + "#noneManager", IOTester.class.getCanonicalName(), //$NON-NLS-1$
								WithIOManager.None.class.getCanonicalName(), "no controller", //$NON-NLS-1$
								IOTesterManager.class.getCanonicalName())));
		assertThat(german).startsWith(GERMAN_PREFIX).contains("keinen Controller") //$NON-NLS-1$
				.contains(WithIOManager.None.class.getCanonicalName());
	}

	/**
	 * An {@link IOTester} under an I/O manager that provides another controller
	 * fails with the Ares message naming that controller.
	 */
	@Test
	void testerUnderAManagerWithAnotherControllerNamesThatController() throws Exception {
		ParameterContext parameter = start("customManagerAskedForTester", IOTester.class); //$NON-NLS-1$

		assertThat(extension.supportsParameter(parameter, current)).isTrue();
		String message = AdviceActionWording.inLocale(Locale.ENGLISH, () -> refusalOf(parameter));

		assertThat(message).startsWith(ENGLISH_PREFIX).contains("provides java.lang.StringBuilder") //$NON-NLS-1$
				.contains(StringBuilderManager.class.getCanonicalName());
	}

	/**
	 * An I/O manager that cannot be created fails before the test with a prefixed
	 * Ares setup message naming the manager, in English and German.
	 */
	@Test
	void uncreatableManagerFailsWithTheAresSetupMessage() throws Exception {
		ExtensionContext context = contextFor(method("uncreatableManager", IOTester.class)); //$NON-NLS-1$
		JupiterIOExtension failing = new JupiterIOExtension();

		String english = AdviceActionWording.inLocale(Locale.ENGLISH,
				() -> assertThrows(SecurityException.class, () -> failing.beforeEach(context)).getMessage());
		String german = AdviceActionWording.inLocale(Locale.GERMAN,
				() -> assertThrows(SecurityException.class, () -> failing.beforeEach(context)).getMessage());

		assertThat(english).startsWith(ENGLISH_PREFIX)
				.isEqualTo(AdviceActionWording.inLocale(Locale.ENGLISH,
						() -> Messages.localized("io_tester.manager_creation_failed", //$NON-NLS-1$
								UncreatableManager.class.getCanonicalName())));
		assertThat(german).startsWith(GERMAN_PREFIX).contains(UncreatableManager.class.getCanonicalName());
	}

	/**
	 * A {@link TestInfo} parameter is left to JUnit under the default I/O manager.
	 */
	@Test
	void testInfoIsNotClaimedUnderTheDefaultManager() throws Exception {
		ParameterContext parameter = start("testInfo", TestInfo.class); //$NON-NLS-1$

		assertThat(extension.supportsParameter(parameter, current)).isFalse();
	}

	/** A {@link TestInfo} parameter is left to JUnit under a custom I/O manager. */
	@Test
	void testInfoIsNotClaimedUnderACustomManager() throws Exception {
		ParameterContext parameter = start("customManagerTestInfo", TestInfo.class); //$NON-NLS-1$

		assertThat(extension.supportsParameter(parameter, current)).isFalse();
	}

	/**
	 * Starts the extension for a fixture method and returns its only parameter.
	 *
	 * @param methodName    the name of the fixture method
	 * @param parameterType the type of its parameter
	 * @return the parameter, as JUnit hands it to a resolver
	 * @throws NoSuchMethodException if the fixture method does not exist
	 */
	private ParameterContext start(String methodName, Class<?> parameterType) throws NoSuchMethodException {
		Method method = method(methodName, parameterType);
		current = contextFor(method);
		extension = new JupiterIOExtension();
		extension.beforeEach(current);
		ParameterContext parameter = mock(ParameterContext.class);
		when(parameter.getParameter()).thenReturn(method.getParameters()[0]);
		return parameter;
	}

	/**
	 * Resolves a parameter that must be refused and returns the refusal text.
	 *
	 * @param parameter the parameter
	 * @return the text of the refusal
	 */
	private String refusalOf(ParameterContext parameter) {
		return assertThrows(SecurityException.class, () -> extension.resolveParameter(parameter, current)).getMessage();
	}

	/**
	 * Finds a fixture method.
	 *
	 * @param methodName    the name of the method
	 * @param parameterType the type of its only parameter
	 * @return the method
	 * @throws NoSuchMethodException if the method does not exist
	 */
	private static Method method(String methodName, Class<?> parameterType) throws NoSuchMethodException {
		return Fixtures.class.getDeclaredMethod(methodName, parameterType);
	}

	/**
	 * Builds a JUnit context for a fixture method, without a parent.
	 *
	 * @param method the fixture method
	 * @return the context
	 */
	private static ExtensionContext contextFor(Method method) {
		ExtensionContext context = mock(ExtensionContext.class);
		when(context.getTestMethod()).thenReturn(Optional.of(method));
		when(context.getTestClass()).thenReturn(Optional.<Class<?>>of(Fixtures.class));
		when(context.getElement()).thenReturn(Optional.of(method));
		when(context.getTestInstance()).thenReturn(Optional.empty());
		when(context.getParent()).thenReturn(Optional.empty());
		when(context.getDisplayName()).thenReturn(method.getName());
		return context;
	}

	/**
	 * A wrapped {@link SecurityException} that this resolver did not throw, for
	 * example one a test raised itself, is passed on unchanged rather than taken
	 * out of its wrapper.
	 */
	@Test
	void aWrappedExceptionTheResolverDidNotThrowIsPassedOnUnchanged() {
		JupiterIOExtension extension = new JupiterIOExtension();
		ParameterResolutionException wrapped = new ParameterResolutionException("wrapped",
				new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): forged"));
		Throwable thrown = assertThrows(Throwable.class,
				() -> extension.handleTestExecutionException(mock(ExtensionContext.class), wrapped));
		assertSame(wrapped, thrown);
	}

	/**
	 * A failure that is not a parameter resolution failure is passed on unchanged.
	 */
	@Test
	void anyOtherFailureIsPassedOnUnchanged() {
		JupiterIOExtension extension = new JupiterIOExtension();
		IllegalStateException failure = new IllegalStateException("student failure");
		Throwable thrown = assertThrows(Throwable.class,
				() -> extension.handleTestExecutionException(mock(ExtensionContext.class), failure));
		assertSame(failure, thrown);
	}
}
