package de.tum.cit.ase.ares.api.jqwik;

import java.time.Duration;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.platform.engine.TestDescriptor;

import net.jqwik.api.domains.DomainContext;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.execution.lifecycle.*;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.internal.TimeoutUtils;

/**
 * This class manages the {@link StrictTimeout} annotation and how it is
 * processed, similar to
 * {@link Assertions#assertTimeoutPreemptively(Duration, org.junit.jupiter.api.function.ThrowingSupplier)}
 * <p>
 * <i>Adaption for jqwik.</i>
 * <p>
 * Use <code>@AddLifecycleHook(JqwikStrictTimeoutExtension.class)</code> only on
 * test methods or classes that are not marked {@link Public} or {@link Hidden}
 * to use {@link StrictTimeout}. <b>Doing otherwise will break the tests
 * completely because the extension will get registered and executed twice!</b>
 *
 * @author Christian Femers
 */
@API(status = Status.MAINTAINED)
public class JqwikStrictTimeoutExtension implements AroundTryHook {
	private static final Duration TERMINATION_GRACE_PERIOD = Duration.ofSeconds(1);

	@Override
	public int aroundTryProximity() {
		/*
		 * Keep the timeout inside jqwik's try lifecycle and Ares's security, IO and
		 * reporting hooks. Their setup and cleanup mutate engine-wide state and must
		 * remain on jqwik's owning thread; only one invocation of the property method
		 * belongs on the timeout worker.
		 */
		return 40;
	}

	@Override
	public TryExecutionResult aroundTry(TryLifecycleContext context, TryExecutor aTry, List<Object> parameters)
			throws Throwable {
		DomainContext domainContext = CurrentDomainContext.get();
		TestDescriptor desc = CurrentTestDescriptor.get();
		return TimeoutUtils.performTimeoutExecution(
				() -> CurrentDomainContext.runWithContext(domainContext,
						() -> CurrentTestDescriptor.runWithDescriptor(desc, () -> aTry.execute(parameters))),
				JqwikContext.of(context), TERMINATION_GRACE_PERIOD);
	}
}
