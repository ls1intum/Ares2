package de.tum.cit.ase.ares.api.jupiter;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.*;

import de.tum.cit.ase.ares.api.internal.IOExtensionUtils;

/** Installs the I/O manager around each Jupiter test. */
@API(status = Status.INTERNAL)
public class JupiterIOExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

	/** The I/O manager installed for the current test. */
	private IOExtensionUtils ioTesterManager;

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return ioTesterManager.canProvideControllerFor(parameterContext.getParameter().getType());
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return ioTesterManager.getControllerInstance();
	}

	/** Starts I/O recording and redacts a hidden setup failure. */
	@Override
	public void beforeEach(ExtensionContext context) {
		try {
			ioTesterManager = new IOExtensionUtils(JupiterContext.of(context));
			ioTesterManager.beforeTestExecution();
		} catch (RuntimeException | Error failure) {
			throw JupiterTestGuard.callbackFailure(context, failure);
		}
	}

	/** Stops I/O recording and redacts a hidden teardown failure. */
	@Override
	public void afterEach(ExtensionContext context) {
		// If this is null, there was an exception in before, so ignore it here
		if (ioTesterManager != null) {
			try {
				ioTesterManager.afterTestExecution();
			} catch (RuntimeException | Error failure) {
				throw JupiterTestGuard.callbackFailure(context, failure);
			}
		}
	}
}
