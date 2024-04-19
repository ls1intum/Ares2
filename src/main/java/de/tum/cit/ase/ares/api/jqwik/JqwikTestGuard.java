package de.tum.cit.ase.ares.api.jqwik;

import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.checkForHidden;

import org.apiguardian.api.API;

import net.jqwik.api.lifecycle.*;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.internal.ReportingUtils;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;

/**
 * This class' main purpose is to guard the {@link HiddenTest}s execution and
 * evaluate the {@link Deadline}.
 * <p>
 * <i>Adaption for jqwik.</i>
 *
 * @author Christian Femers
 */
@API(status = API.Status.INTERNAL)
public final class JqwikTestGuard implements AroundPropertyHook {

	@Override
	public int aroundPropertyProximity() {
		return 10;
	}

	@Override
	public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property)
			throws Throwable {
		JqwikContext jqwikContext = JqwikContext.of(context);
		checkForHidden(jqwikContext);
		return ReportingUtils.doProceedAndPostProcess(() -> postProcess(property.execute(), jqwikContext),
				jqwikContext);
	}

	private static PropertyExecutionResult postProcess(PropertyExecutionResult per, TestContext context) {
		var t = per.throwable();
		if (t.isEmpty())
			return per;
		Throwable newT = ReportingUtils.processThrowable(t.get(), context);
		if (newT instanceof Error && !(newT instanceof AssertionError))
			throw (Error) newT;
		return per.mapTo(per.status(), newT);
	}
}
