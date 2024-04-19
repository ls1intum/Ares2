package de.tum.cit.ase.ares.api.jupiter;

import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.*;

import de.tum.cit.ase.ares.api.internal.ConfigurationUtils;
//REMOVED: Import of ArtemisSecurityManager

@API(status = Status.INTERNAL)
public final class JupiterSecurityExtension implements UnifiedInvocationInterceptor {

	@Override
	public <T> T interceptGenericInvocation(Invocation<T> invocation, ExtensionContext extensionContext,
			Optional<ReflectiveInvocationContext<?>> invocationContext) throws Throwable {
		var testContext = JupiterContext.of(extensionContext);
		var configuration = ConfigurationUtils.generateConfiguration(testContext);
//REMOVED: Installing of ArtemisSecurityManager
		Throwable failure = null;
		try {
			return invocation.proceed();
		} catch (Throwable t) {
			failure = t;
		} finally {
			try {
				//REMOVED: Uninstallation of ArtemisSecurityManager
			} catch (Exception e) {
				if (failure == null)
					failure = e;
				else
					failure.addSuppressed(e);
			}
		}
		throw failure;
	}
}
