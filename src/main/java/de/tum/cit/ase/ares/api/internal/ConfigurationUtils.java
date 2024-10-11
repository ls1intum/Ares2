package de.tum.cit.ase.ares.api.internal;

import java.nio.file.Path;
import java.util.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.context.*;
import de.tum.cit.ase.ares.api.security.*;

@API(status = Status.INTERNAL)
public final class ConfigurationUtils {

	private ConfigurationUtils() {
	}

	public static AresSecurityConfiguration generateConfiguration(TestContext context) {
		var config = AresSecurityConfigurationBuilder.create();
		config.configureFromContext(context);
		config.withPath(Path.of("")); //$NON-NLS-1$
		return config.build();
	}

	public static boolean shouldMirrorOutput(TestContext context) {
		return TestContextUtils.findAnnotationIn(context, MirrorOutput.class).map(MirrorOutput::value)
				.map(MirrorOutputPolicy::isEnabled).orElse(false);
	}

	public static long getMaxStandardOutput(TestContext context) {
		return TestContextUtils.findAnnotationIn(context, MirrorOutput.class).map(MirrorOutput::maxCharCount)
				.orElse(MirrorOutput.DEFAULT_MAX_STD_OUT);
	}

	public static Optional<String> getNonprivilegedFailureMessage(TestContext context) {
		return TestContextUtils.findAnnotationIn(context, PrivilegedExceptionsOnly.class)
				.map(PrivilegedExceptionsOnly::value);
	}
}
