package de.tum.cit.ase.ares.integration.jce;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;

import example.jce.JceCryptoSubject;
import example.jce.JceProviderService;

/**
 * Trusted fixtures that verify real student operations in packaged and copied
 * advice.
 */
public final class JceRuntimeContract {

	/** Prevents contract instances. */
	private JceRuntimeContract() {
	}

	/**
	 * Runs denials and matching permitted controls without changing other
	 * permissions.
	 */
	public static void verify(Path directory, String namespace) throws Exception {
		for (String name : new String[] { "default_local.policy", "default_US_export.policy", "exempt_local.policy",
				"default_tokens.policy" }) {
			Path file = Files.writeString(directory.resolve(name), "protected fixture");
			requireDenied(() -> JceCryptoSubject.read(file), file);
			permitReads(namespace, directory, () -> requireContents(JceCryptoSubject.read(file)));
		}
		requireDenied(() -> JceCryptoSubject.listPolicyFiles(directory), directory);
		permitReads(namespace, directory, () -> {
			if (JceCryptoSubject.listPolicyFiles(directory) != 4) {
				throw new AssertionError("The permitted JCE glob did not list the actual fixtures");
			}
		});
		Path file = directory.resolve("default_local.policy");
		for (String action : new String[] { "create", "overwrite", "delete" }) {
			Path target = "create".equals(action) ? directory.resolve("exempt_created.policy") : file;
			requireDenied(() -> JceCryptoSubject.mutate(target, action), target);
			if ("create".equals(action) ? Files.exists(target)
					: !Files.readString(target).equals("protected fixture")) {
				throw new AssertionError("A denied mutation changed the fixture: " + action);
			}
		}
		verifyProvider(file, namespace);
		verifyClassIdentity(file, namespace);
		System.out.println("JCE_BOUNDARY_PASSED");
	}

	/**
	 * Requires a working JCE callback and its file-specific denial, then permits
	 * the same read.
	 */
	private static void verifyProvider(Path file, String namespace) throws Exception {
		Object cipher = Security.getProvider("SunJCE").getService("Cipher", "AES").newInstance(null);
		ReadCheckingProvider provider = new ReadCheckingProvider(file, cipher);
		try {
			if (Security.addProvider(provider) == -1) {
				throw new AssertionError("The fixture provider was already registered");
			}
			requireDenied(() -> JceCryptoSubject.useProvider(provider), file);
			if (!provider.service.wasCalled() || provider.service.readSucceeded()) {
				throw new AssertionError("The provider denial did not occur in its callback");
			}
			permitReads(namespace, file, () -> JceCryptoSubject.useProvider(provider));
			if (!provider.service.readSucceeded()) {
				throw new AssertionError("The permitted provider callback did not read its fixture");
			}
			System.out.println("JCE_PROVIDER_CALLBACK_DENIED_AND_PERMITTED");
		} finally {
			Security.removeProvider(provider.getName());
		}
	}

	/**
	 * Defines a non-bootstrap fixture with the exact historical class and method
	 * names.
	 */
	private static void verifyClassIdentity(Path file, String namespace) throws Exception {
		Class<?> fixture = new ByteBuddy().subclass(Object.class).name("javax.crypto.JceSecurity")
				.defineMethod("setupJurisdictionPolicies", String.class,
						java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.STATIC)
				.withParameters(Path.class)
				.intercept(MethodCall.invoke(JceCryptoSubject.class.getMethod("read", Path.class)).withArgument(0))
				.make().load(JceRuntimeContract.class.getClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST)
				.getLoaded();
		if (fixture.getClassLoader() == null || !fixture.getName().equals("javax.crypto.JceSecurity")) {
			throw new AssertionError("The class identity fixture was not defined as requested");
		}
		Method read = fixture.getMethod("setupJurisdictionPolicies", Path.class);
		requireDenied(() -> read.invoke(null, file), file);
		permitReads(namespace, file, () -> requireContents((String) read.invoke(null, file)));
		System.out.println("JCE_CLASS_IDENTITY_DENIED_AND_PERMITTED");
	}

	/**
	 * Grants only fixture reads in every real settings copy, then restores them
	 * under their locks.
	 */
	private static void permitReads(String namespace, Path allowed, CheckedOperation operation) throws Exception {
		Map<Field, Object> saved = new LinkedHashMap<>();
		try {
			for (Class<?> settings : settingsCopies(namespace, !isAspectJ(namespace))) {
				Field paths = settings.getDeclaredField("pathsAllowedToBeRead");
				paths.setAccessible(true);
				Object lock = settings.getMethod("getSettingsLock").invoke(null);
				synchronized (lock) {
					saved.put(paths, paths.get(null));
					paths.set(null, new String[] { allowed.toAbsolutePath().toString() });
				}
			}
			operation.run();
		} finally {
			for (var entry : saved.entrySet()) {
				Object lock = entry.getKey().getDeclaringClass().getMethod("getSettingsLock").invoke(null);
				synchronized (lock) {
					entry.getKey().set(null, entry.getValue());
				}
			}
		}
	}

	/**
	 * Returns the application copy of the settings and the bootstrap copy the agent
	 * puts on the boot class path. The bootstrap copy may be missing only where it
	 * is not required, that is for AspectJ, which runs without an agent.
	 */
	static List<Class<?>> settingsCopies(String namespace, boolean bootstrapRequired) throws ClassNotFoundException {
		String name = namespace + ".api.aop.java.JavaAOPTestCaseSettings";
		List<Class<?>> copies = new ArrayList<>();
		copies.add(Class.forName(name, false, JceRuntimeContract.class.getClassLoader()));
		try {
			copies.add(Class.forName(name, false, null));
		} catch (ClassNotFoundException noBootstrapCopy) {
			if (bootstrapRequired) {
				throw noBootstrapCopy;
			}
		}
		return copies;
	}

	/** Tells whether the armed application settings select AspectJ. */
	private static boolean isAspectJ(String namespace) throws ReflectiveOperationException {
		Class<?> settings = Class.forName(namespace + ".api.aop.java.JavaAOPTestCaseSettings", false,
				JceRuntimeContract.class.getClassLoader());
		Field mode = settings.getDeclaredField("aopMode");
		mode.setAccessible(true);
		return "ASPECTJ".equals(mode.get(null));
	}

	/**
	 * Accepts only a filesystem SecurityException naming the operation's actual
	 * fixture.
	 */
	private static void requireDenied(CheckedOperation operation, Path file) throws Exception {
		try {
			operation.run();
		} catch (Exception failure) {
			Throwable cause = failure;
			while (cause != null) {
				if (cause instanceof SecurityException
						&& cause.getMessage().contains(file.toAbsolutePath().toString())) {
					return;
				}
				cause = cause.getCause();
			}
			throw new AssertionError("The operation did not receive a file-specific Ares denial", failure);
		}
		throw new AssertionError("An unlisted operation succeeded: " + file);
	}

	/**
	 * Requires the permitted operation to return the protected fixture's contents.
	 */
	private static void requireContents(String contents) {
		if (!"protected fixture".equals(contents)) {
			throw new AssertionError("The permitted read returned different contents");
		}
	}

	/** A trusted callback for an actual operation that may fail. */
	@FunctionalInterface
	private interface CheckedOperation {
		/** Performs one fixture operation. */
		void run() throws Exception;
	}

	/** Installs a supervised service into a real, temporary provider. */
	private static final class ReadCheckingProvider extends Provider {
		/** Preserves Provider's serial form. */
		private static final long serialVersionUID = 1L;
		/** The supervised callback whose entry and result are asserted. */
		private final JceProviderService service;

		/**
		 * Publishes the fixture cipher without changing the global permission policy.
		 */
		private ReadCheckingProvider(Path file, Object cipher) {
			super("AresReadFixture", "1", "Local filesystem boundary regression fixture");
			service = new JceProviderService(this, file, cipher);
			putService(service);
		}
	}
}
