package de.tum.cit.ase.ares.api.jupiter;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.*;

import de.tum.cit.ase.ares.api.internal.IOExtensionUtils;
import de.tum.cit.ase.ares.api.io.IOTester;

/**
 * Gives a test the console checker of its I/O manager and reports, with an Ares
 * message, a test that asks for a checker its manager cannot give.
 */
@API(status = Status.INTERNAL)
public class JupiterIOExtension
		implements BeforeEachCallback, AfterEachCallback, ParameterResolver, TestExecutionExceptionHandler {

	/** The I/O manager of the running test, set before each test. */
	private IOExtensionUtils ioTesterManager;

	/**
	 * The refusal this resolver threw last, so only that exact exception is taken
	 * out of the wrapper JUnit puts around it.
	 */
	private SecurityException lastRefusal;

	/**
	 * Claims an {@link IOTester} parameter always, so a test whose I/O manager
	 * cannot provide one gets an Ares message instead of JUnit's. It also claims
	 * what the active I/O manager provides, and nothing else, so it never takes a
	 * parameter such as {@code TestInfo} from another resolver.
	 *
	 * @param parameterContext the parameter to resolve
	 * @param extensionContext the context of the test
	 * @return whether this resolver resolves the parameter
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> type = parameterContext.getParameter().getType();
		return IOTester.class == type || ioTesterManager.canProvideControllerFor(type);
	}

	/**
	 * Gives the controller of the active I/O manager.
	 *
	 * @param parameterContext the parameter to resolve
	 * @param extensionContext the context of the test
	 * @return the controller
	 * @throws SecurityException if an {@link IOTester} is asked for and the active
	 *                           I/O manager provides something else or nothing
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		try {
			return ioTesterManager.resolveControllerFor(parameterContext.getParameter().getType(),
					JupiterContext.of(extensionContext));
		} catch (SecurityException refusal) {
			lastRefusal = refusal;
			throw refusal;
		}
	}

	/**
	 * Reports this resolver's own refusal as the test's failure. JUnit wraps it in
	 * a {@link ParameterResolutionException}, whose first line is JUnit's; only the
	 * exact exception {@link #resolveParameter} threw is unwrapped, every other
	 * failure is passed on unchanged.
	 *
	 * @param context   the context of the test
	 * @param throwable the failure of the test
	 * @throws Throwable the refusal itself, or the failure unchanged
	 */
	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		SecurityException refusal = lastRefusal;
		lastRefusal = null;
		if (refusal != null && throwable instanceof ParameterResolutionException && throwable.getCause() == refusal) {
			throw refusal;
		}
		throw throwable;
	}

	/**
	 * Sets up the I/O manager of the test.
	 *
	 * @param context the context of the test
	 */
	@Override
	public void beforeEach(ExtensionContext context) {
		lastRefusal = null;
		ioTesterManager = new IOExtensionUtils(JupiterContext.of(context));
		ioTesterManager.beforeTestExecution();
	}

	/**
	 * Tears down the I/O manager, unless setting it up already failed.
	 *
	 * @param context the context of the test
	 */
	@Override
	public void afterEach(ExtensionContext context) {
		if (ioTesterManager != null) {
			ioTesterManager.afterTestExecution();
		}
	}
}
