package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassInjector.UsingUnsafe.Factory;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.CommandTarget;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.FileTarget;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.IgnoreValues;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceCommandSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceNetworkSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceThreadSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationThreadSystemCallSite;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.NetworkTarget;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.ThreadTarget;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * This class is the entry point for the Java instrumentation agent. It installs
 * the agent builder for the different types of file operations.
 */
public final class JavaInstrumentationAgent {
	/** The instrumentation handle supplied by JVM startup. */
	private static volatile Instrumentation instrumentation;

	/** Whether trusted startup finished before student execution. */
	private static volatile boolean trustedStartupComplete;
	private static volatile Factory classInjectorFactory;
	private static final Set<String> INSTRUMENTED_THREAD_MONITOR_PACKAGES = ConcurrentHashMap.newKeySet();
	private static final Object THREAD_MONITOR_PACKAGE_REGISTRATION_LOCK = new Object();
	private static final AtomicReference<TransformationFailure> TRANSFORMATION_FAILURE = new AtomicReference<>();
	/**
	 * The classes the pointcut transformers have rewritten, each as its name and
	 * its class loader, so a check can prove that no watched class was left out.
	 */
	private static final Set<String> TRANSFORMED_TYPES = ConcurrentHashMap.newKeySet();
	/**
	 * Records every class the pointcut transformers rewrote, for
	 * {@link #wasTransformed(Class)}.
	 */
	private static final AgentBuilder.Listener TRANSFORMATION_RECORDER = new AgentBuilder.Listener.Adapter() {
		/**
		 * Notes the rewritten class with its class loader.
		 */
		@Override
		public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module,
				boolean loaded, DynamicType dynamicType) {
			TRANSFORMED_TYPES.add(transformedTypeKey(typeDescription.getName(), classLoader));
		}
	};
	/**
	 * Records the first class a transformer failed on, for
	 * {@link #throwIfTransformationFailed()}.
	 */
	private static final AgentBuilder.Listener TRANSFORMATION_FAILURE_LISTENER = new AgentBuilder.Listener.Adapter() {
		/**
		 * Keeps the failure unless an earlier one is still unreported.
		 */
		@Override
		public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
				Throwable throwable) {
			recordFailure(new TransformationFailure(typeName, throwable));
		}
	};
	/**
	 * Records a batch of already loaded classes the JVM refused to retransform.
	 * Byte Buddy otherwise drops such a failure silently, and the batch would stay
	 * unguarded.
	 */
	private static final AgentBuilder.RedefinitionStrategy.Listener RETRANSFORMATION_FAILURE_LISTENER = new AgentBuilder.RedefinitionStrategy.Listener.Adapter() {
		/**
		 * Records the refused batch and does not retry it.
		 */
		@Override
		public Iterable<? extends List<Class<?>>> onError(int index, List<Class<?>> batch, Throwable throwable,
				List<Class<?>> types) {
			recordFailure(new TransformationFailure(
					batch.stream().map(Class::getName).collect(Collectors.joining(", ")), throwable));
			return List.of();
		}
	};
	/**
	 * The first transformation failure of this JVM, kept for good. Unlike the
	 * per-test report, no test consumes it, so every later activation and test
	 * constructor still refuses to run next to a class left untransformed.
	 */
	private static final AtomicReference<TransformationFailure> PERMANENT_FAILURE = new AtomicReference<>();
	/**
	 * Guards the one-time installation of the transformers.
	 */
	private static final Object ACTIVATION_LOCK = new Object();
	/**
	 * Whether the transformers are installed. Once true, it stays true.
	 */
	private static volatile boolean activated;
	/**
	 * The failure of the first installation attempt, including a class it could not
	 * transform, rethrown by every later attempt, so that no test runs unguarded
	 * after it and no transformer is installed twice.
	 */
	private static volatile SecurityException activationFailure;

	/**
	 * The settings class the advice of both backends reads, named as a string so
	 * the bootstrap copy and the application copy can each be looked up.
	 */
	private static final String SETTINGS_CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings";

	/**
	 * The JDK classes that remember the default temp directory on first use.
	 * Loading them at start-up makes the JDK keep the start-up value.
	 */
	private static final List<String> JDK_TEMP_DIRECTORY_HOLDERS = List.of("java.io.File$TempDirectory",
			"java.nio.file.TempFileHelper");

	private JavaInstrumentationAgent() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
				.localize("security.instrumentation.utility.initialization", "JavaInstrumentationAgent"));
	}

	/**
	 * Initialises Ares before supervised code and optionally installs
	 * instrumentation.
	 */
	public static void premain(String agentArgs, Instrumentation inst) {
		Messages.init();
		Factory unsafeFactory = Factory.resolve(inst);
		instrumentation = inst;
		classInjectorFactory = unsafeFactory;

		putToolboxOnBootClassLoader(unsafeFactory);
		initializeToolboxes();
		captureTrustedStartupValues();
		java.lang.StackWalker walker = java.lang.StackWalker.getInstance();
		walker.walk(stream -> stream.limit(1L).count());
		java.nio.file.Files.exists(java.nio.file.Path.of("."));
		preloadPlatformClass("sun.nio.fs.UnixException");
		preloadPlatformClass("sun.nio.fs.WindowsException");

		if (isPolicyCompiledIn()) {
			installTransformersOnce();
		}
		trustedStartupComplete = true;
	}

	/**
	 * Installs the transformers for the first policy that asks for instrumentation,
	 * before any supervised code of that test runs, and reports any class a
	 * transformer has failed on since. Later calls only report. Safe to call from
	 * several threads at once.
	 *
	 * @throws SecurityException if the agent is not attached, if installing failed
	 *                           now or at an earlier attempt, or if a class could
	 *                           not be transformed
	 */
	public static void activate() {
		if (!activated) {
			installTransformersOnce();
		}
		throwIfActivationFailed();
		throwIfTransformationFailed();
	}

	/**
	 * Installs the pointcut transformers and the thread call-site transformer
	 * exactly once. A failed attempt, or a class it could not transform, is kept
	 * and rethrown by every later one, so a half-finished installation is never
	 * repeated and never ignored.
	 *
	 * @throws SecurityException if the agent is not attached or installing failed
	 */
	private static void installTransformersOnce() {
		synchronized (ACTIVATION_LOCK) {
			if (activated) {
				return;
			}
			throwIfActivationFailed();
			Instrumentation currentInstrumentation = instrumentation;
			Factory currentFactory = classInjectorFactory;
			try {
				if (currentInstrumentation == null || currentFactory == null) {
					throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
							.localize("security.instrumentation.agent.not.attached"));
				}
				installAgentBuilder(currentInstrumentation, currentFactory, guardedPointcuts());
				installThreadCallSiteBuilder(currentInstrumentation, currentFactory);
				throwIfPermanentFailure();
				activated = true;
			} catch (SecurityException failure) {
				activationFailure = failure;
				throw failure;
			} catch (RuntimeException | LinkageError failure) {
				activationFailure = new SecurityException(JavaInstrumentationAdviceAbstractToolbox
						.localize("security.instrumentation.agent.installation.error", "instrumentation"), failure);
				throw activationFailure;
			}
		}
	}

	/**
	 * Rethrows the failure of installing the transformers, or of transforming any
	 * class since, so a caller that otherwise tolerates a failed preparation still
	 * refuses to run code unguarded.
	 *
	 * @throws SecurityException if installing or a transformation failed
	 */
	public static void throwIfActivationFailed() {
		SecurityException failure = activationFailure;
		if (failure != null) {
			throw new SecurityException(failure.getMessage(), failure);
		}
		throwIfPermanentFailure();
	}

	/**
	 * Fails if any class of this JVM could not be transformed, during installation
	 * or after it.
	 *
	 * @throws SecurityException if a transformation has ever failed
	 */
	private static void throwIfPermanentFailure() {
		TransformationFailure failure = PERMANENT_FAILURE.get();
		if (failure != null) {
			throw new SecurityException(
					JavaInstrumentationAdviceAbstractToolbox
							.localize("security.instrumentation.agent.transformation.error", failure.typeName()),
					failure.cause());
		}
	}

	/** Reports whether the JVM's trusted agent startup has finished. */
	public static boolean hasCompletedTrustedStartup() {
		return trustedStartupComplete;
	}

	/**
	 * Captures, once and before any supervised code runs, the values the
	 * file-system checks trust: the default temp directory, kept by the JDK's own
	 * holders and stored in both settings copies, and the AspectJ aspect's Java
	 * home and Maven repository. The one start-up routine every runtime check
	 * relies on.
	 */
	private static void captureTrustedStartupValues() {
		JDK_TEMP_DIRECTORY_HOLDERS.forEach(holder -> initialiseIfPresent(holder, null));
		String defaultTempDirectory = resolveDefaultTempDirectory();
		if (defaultTempDirectory != null) {
			publishFrozenTempDirectory(defaultTempDirectory, null);
			publishFrozenTempDirectory(defaultTempDirectory, ClassLoader.getSystemClassLoader());
		}
		initialiseAspectJFileSystem();
	}

	/**
	 * Returns the default temp directory resolved to its real location, following
	 * symbolic links, or {@code null} if it cannot be resolved, in which case
	 * nothing is stored and temp files without a directory are refused.
	 *
	 * @return the real default temp directory, or {@code null}
	 */
	private static String resolveDefaultTempDirectory() {
		String property = System.getProperty("java.io.tmpdir");
		if (property == null) {
			return null;
		}
		try {
			return Path.of(property).toRealPath().toString();
		} catch (IOException | InvalidPathException unresolvable) {
			return null;
		}
	}

	/**
	 * Stores the default temp directory in one copy of the settings, unless that
	 * copy already holds one. A copy that does not exist, or that lacks the field,
	 * is left alone, and the checks then refuse temp files without a directory.
	 *
	 * @param directory the real default temp directory
	 * @param loader    the loader of the copy, {@code null} for the bootstrap copy
	 */
	private static void publishFrozenTempDirectory(String directory, ClassLoader loader) {
		try {
			Field field = Class.forName(SETTINGS_CLASS_NAME, true, loader)
					.getDeclaredField("frozenDefaultTempDirectory");
			field.setAccessible(true);
			if (field.get(null) == null) {
				field.set(null, directory);
			}
		} catch (ReflectiveOperationException | LinkageError | InaccessibleObjectException | SecurityException
				| IllegalArgumentException absent) {
			return;
		}
	}

	/**
	 * Initialises a class if the loader can find it, so its start-up values are
	 * read now rather than on first use.
	 *
	 * @param className the class to initialise
	 * @param loader    the loader to use, {@code null} for the bootstrap loader
	 */
	private static void initialiseIfPresent(String className, ClassLoader loader) {
		try {
			Class.forName(className, true, loader);
		} catch (ClassNotFoundException | LinkageError absent) {
			return;
		}
	}

	/** Captures the AspectJ filesystem root before entering student code. */
	private static void initialiseAspectJFileSystem() {
		try {
			Class.forName(
					"de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions",
					true, JavaInstrumentationAgent.class.getClassLoader());
		} catch (ClassNotFoundException missingAspect) {
			if (!isPolicyCompiledIn()) {
				throw new SecurityException(Messages.localized("security.advice.trusted.startup.missing"),
						missingAspect);
			}
		}
	}

	/**
	 * Keeps a transformation failure for the per-test report and for good. It takes
	 * no lock and builds no message, because it runs inside class loading.
	 *
	 * @param failure The class and the cause.
	 */
	private static void recordFailure(TransformationFailure failure) {
		TRANSFORMATION_FAILURE.compareAndSet(null, failure);
		PERMANENT_FAILURE.compareAndSet(null, failure);
	}

	/**
	 * Reports whether compiled settings or unreadable settings require JDK
	 * transformers at startup.
	 */
	private static boolean isPolicyCompiledIn() {
		try {
			Field aopMode = Class.forName(JavaAOPTestCaseSettings.class.getName(), true, null)
					.getDeclaredField("aopMode");
			aopMode.setAccessible(true);
			Object mode = aopMode.get(null);
			return mode != null && !"ASPECTJ".equals(mode);
		} catch (ReflectiveOperationException | RuntimeException | LinkageError unreadable) {
			return true;
		}
	}

	/**
	 * Tells whether a pointcut transformer has rewritten the given class.
	 *
	 * @param type The loaded class.
	 * @return True if the class was transformed at loading or retransformed.
	 */
	static boolean wasTransformed(Class<?> type) {
		return TRANSFORMED_TYPES.contains(transformedTypeKey(type.getName(), type.getClassLoader()));
	}

	/**
	 * Identifies a class by its name and its class loader, since two loaders may
	 * define classes of the same name.
	 *
	 * @param typeName    The fully qualified class name.
	 * @param classLoader The defining class loader, or null for the boot loader.
	 * @return The key under which the class is recorded.
	 */
	private static String transformedTypeKey(String typeName, ClassLoader classLoader) {
		return typeName + "@" + System.identityHashCode(classLoader);
	}

	/**
	 * Lists every group of method calls the agent intercepts, each paired with the
	 * transformer that rewrites it.
	 *
	 * @return The method and constructor groups for files, threads, commands and
	 *         network.
	 */
	static List<Pointcut> guardedPointcuts() {
		return List.of(
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES,
						JavaInstrumentationBindingDefinitions::createReadPathMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_OVERWRITE_FILES,
						JavaInstrumentationBindingDefinitions::createOverwritePathMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_FILES,
						JavaInstrumentationBindingDefinitions::createCreatePathMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_FILES,
						JavaInstrumentationBindingDefinitions::createExecutePathMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_DELETE_FILES,
						JavaInstrumentationBindingDefinitions::createDeletePathMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_THREADS,
						JavaInstrumentationBindingDefinitions::createCreateThreadMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_COMMANDS,
						JavaInstrumentationBindingDefinitions::createExecuteCommandMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CONNECT_TO_NETWORK,
						JavaInstrumentationBindingDefinitions::createConnectNetworkMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_SEND_TO_NETWORK,
						JavaInstrumentationBindingDefinitions::createSendNetworkMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_RECEIVE_FROM_NETWORK,
						JavaInstrumentationBindingDefinitions::createReceiveNetworkMethodBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES,
						JavaInstrumentationBindingDefinitions::createReadPathConstructorBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_OVERWRITE_FILES,
						JavaInstrumentationBindingDefinitions::createOverwritePathConstructorBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_FILES,
						JavaInstrumentationBindingDefinitions::createCreatePathConstructorBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_FILES,
						JavaInstrumentationBindingDefinitions::createExecutePathConstructorBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_DELETE_FILES,
						JavaInstrumentationBindingDefinitions::createDeletePathConstructorBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_THREADS,
						JavaInstrumentationBindingDefinitions::createCreateThreadConstructorBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_COMMANDS,
						JavaInstrumentationBindingDefinitions::createExecuteCommandConstructorBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CONNECT_TO_NETWORK,
						JavaInstrumentationBindingDefinitions::createConnectNetworkConstructorBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_SEND_TO_NETWORK,
						JavaInstrumentationBindingDefinitions::createSendNetworkConstructorBinding),
				new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_RECEIVE_FROM_NETWORK,
						JavaInstrumentationBindingDefinitions::createReceiveNetworkConstructorBinding));
	}

	/**
	 * Initialises the toolboxes injected into the boot class loader before any
	 * transformer is installed. Their static initialisers load guarded JDK classes
	 * such as {@link ProcessBuilder}; a class first loaded inside a running
	 * transformation never reaches any transformer, so it must be loaded now, where
	 * the installation's retransformation pass still covers it.
	 */
	private static void initializeToolboxes() {
		for (Class<?> toolbox : List.of(JavaInstrumentationAdviceAbstractToolbox.class,
				JavaInstrumentationAdviceFileSystemToolbox.class, JavaInstrumentationAdviceThreadSystemToolbox.class,
				JavaInstrumentationThreadSystemCallSite.class, JavaInstrumentationAdviceNetworkSystemToolbox.class,
				JavaInstrumentationAdviceCommandSystemToolbox.class)) {
			try {
				Class.forName(toolbox.getName(), true, null);
			} catch (ClassNotFoundException e) {
				throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
						.localize("security.instrumentation.agent.toolbox.installation.failed", toolbox.getName()), e);
			}
		}
	}

	private static void preloadPlatformClass(String className) {
		try {
			Class.forName(className, false, null);
		} catch (ClassNotFoundException ignored) {
			// The class belongs to a different operating system's file provider.
		}
	}

	/**
	 * This method is called when the agent is attached to a running JVM. It
	 * installs the agent builder for the different types of file operations.
	 *
	 * @param agentArgs The agent arguments.
	 * @param inst      The instrumentation instance.
	 */
	public static void agentmain(String agentArgs, Instrumentation inst) {
		premain(agentArgs, inst);
	}

	/**
	 * Fails the current security test if Byte Buddy could not transform a matched
	 * class. Byte Buddy reports transformer errors to its listeners instead of
	 * propagating them through the application call that triggered class loading.
	 * Without this explicit check, the affected class would continue uninstrumented
	 * and the sandbox would fail open.
	 *
	 * @throws SecurityException if any installed Ares transformer has failed
	 */
	public static void throwIfTransformationFailed() {
		TransformationFailure failure = TRANSFORMATION_FAILURE.getAndSet(null);
		if (failure != null) {
			throw new SecurityException(
					JavaInstrumentationAdviceAbstractToolbox
							.localize("security.instrumentation.agent.transformation.error", failure.typeName()),
					failure.cause());
		}
	}

	/**
	 * Rewrites calls to Thread start and the monitor methods in the package the
	 * current policy governs. Object's final native monitor methods cannot be
	 * advised where they are declared, so their call sites are rewritten instead;
	 * limiting this to the governed package leaves framework classes untouched.
	 * Register the package before supervised classes are first used, so they pass
	 * this transformer without a second retransformation.
	 *
	 * @param restrictedPackage package prefix governed by the current policy
	 */
	public static void registerThreadMonitorRestrictedPackage(String restrictedPackage) {
		Instrumentation currentInstrumentation = instrumentation;
		Factory currentFactory = classInjectorFactory;
		if (currentInstrumentation == null || currentFactory == null || restrictedPackage == null
				|| restrictedPackage.isBlank()) {
			return;
		}
		synchronized (THREAD_MONITOR_PACKAGE_REGISTRATION_LOCK) {
			if (!INSTRUMENTED_THREAD_MONITOR_PACKAGES.add(restrictedPackage)) {
				return;
			}
			// The already-installed transformer observes this concurrent set for every
			// subsequently defined restricted class. Deliberately avoid manually
			// retransformation here: all other Ares retransformation-capable REBASE
			// transformers would be invoked again as well, and HotSpot rejects that
			// repeated structural rebase with "class redefinition failed: invalid class".
			// Security setup runs before the supervised operation, so its application
			// classes are transformed when first defined.
		}
	}

	private static void putToolboxOnBootClassLoader(Factory unsafeFactory) {
		try {
			// Fail closed: the toolboxes MUST be injected into the boot class loader so
			// they are visible to the boot-class JDK sinks they guard. The previous
			// fallback to the system class loader left the agent half-wired (toolboxes on
			// the system loader, sinks on the boot loader) and silently weakened the
			// sandbox. A boot-injection failure now propagates to the outer handler, which
			// refuses to install the agent rather than running degraded.
			ClassInjector classInjector = unsafeFactory.make(null, null);
			if (classInjector == null) {
				throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
						"security.instrumentation.agent.toolbox.installation.failed", "bootstrap injector is null"));
			}

			// Load classes in dependency order to ensure proper initialisation

			// Step 1: Load fundamental classes
			injectClassesSafely(classInjector,
					Map.ofEntries(
							Map.entry(IgnoreValues.class.getName(),
									ClassFileLocator.ForClassLoader.read(IgnoreValues.class)),
							Map.entry(JavaAOPTestCaseSettings.class.getName(),
									ClassFileLocator.ForClassLoader.read(JavaAOPTestCaseSettings.class))));

			// Step 2: Load the abstract toolbox (depends on basic classes)
			injectClassesSafely(classInjector,
					Map.ofEntries(Map.entry(JavaInstrumentationAdviceAbstractToolbox.class.getName(),
							ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceAbstractToolbox.class))));

			// Step 3: Load target value types (used by concrete toolboxes)
			injectClassesSafely(classInjector, Map.ofEntries(
					Map.entry(FileTarget.class.getName(), ClassFileLocator.ForClassLoader.read(FileTarget.class)),
					Map.entry(NetworkTarget.class.getName(), ClassFileLocator.ForClassLoader.read(NetworkTarget.class)),
					Map.entry(ThreadTarget.class.getName(), ClassFileLocator.ForClassLoader.read(ThreadTarget.class)),
					Map.entry(CommandTarget.class.getName(),
							ClassFileLocator.ForClassLoader.read(CommandTarget.class))));

			// Step 4: Load concrete toolbox implementations (depend on abstract toolbox and
			// targets)
			injectClassesSafely(classInjector,
					Map.ofEntries(
							Map.entry(JavaInstrumentationAdviceFileSystemToolbox.class.getName(),
									ClassFileLocator.ForClassLoader
											.read(JavaInstrumentationAdviceFileSystemToolbox.class)),
							Map.entry(JavaInstrumentationAdviceThreadSystemToolbox.class.getName(),
									ClassFileLocator.ForClassLoader
											.read(JavaInstrumentationAdviceThreadSystemToolbox.class)),
							Map.entry(JavaInstrumentationThreadSystemCallSite.class.getName(),
									ClassFileLocator.ForClassLoader
											.read(JavaInstrumentationThreadSystemCallSite.class)),
							Map.entry(JavaInstrumentationAdviceNetworkSystemToolbox.class.getName(),
									ClassFileLocator.ForClassLoader
											.read(JavaInstrumentationAdviceNetworkSystemToolbox.class)),
							Map.entry(JavaInstrumentationAdviceCommandSystemToolbox.class.getName(),
									ClassFileLocator.ForClassLoader
											.read(JavaInstrumentationAdviceCommandSystemToolbox.class))));
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
					"security.instrumentation.agent.toolbox.installation.failed", String.valueOf(e.getMessage())), e);
		}
	}

	/**
	 * Safely inject classes with retry mechanism and proper error handling.
	 */
	private static void injectClassesSafely(ClassInjector classInjector, Map<String, byte[]> classes) {
		int maxRetries = 3;
		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				classInjector.injectRaw(classes);
				// Verify classes are loaded by trying to access them
				for (String className : classes.keySet()) {
					Class.forName(className, false, ClassLoader.getSystemClassLoader());
				}
				return; // Success
			} catch (Exception e) {
				if (attempt == maxRetries) {
					throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
							"security.instrumentation.agent.class.injection.failure", maxRetries,
							classes.keySet().toString()), e);
				}
				// Wait a bit before retry
				try {
					Thread.sleep(50 * attempt); // Progressive backoff
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
							.localize("security.instrumentation.agent.class.injection.interrupted"), ie);
				}
			}
		}
	}

	/**
	 * Installs one agent builder for every pointcut, so each class is matched once.
	 * Retransformation repeats until no new class appears, since classes loaded
	 * while installing are in neither the first snapshot nor the load hook.
	 *
	 * @param inst          The instrumentation instance.
	 * @param unsafeFactory Factory for unsafe class injection.
	 * @param pointcuts     The method groups to intercept, with their transformers.
	 * @throws SecurityException If installing fails.
	 */
	private static void installAgentBuilder(Instrumentation inst, ClassInjector.UsingUnsafe.Factory unsafeFactory,
			List<Pointcut> pointcuts) {
		try {
			AgentBuilder agentBuilder = new AgentBuilder.Default()
					// Use one combined matcher: AgentBuilder.ignore replaces the previous
					// matcher, so separate calls would accidentally re-enable earlier groups.
					.ignore(ignoredTypes()).with(TRANSFORMATION_FAILURE_LISTENER).with(TRANSFORMATION_RECORDER)
					.with(new AgentBuilder.PoolStrategy.WithTypePoolCache.Simple(new ConcurrentHashMap<>()))
					.with(AgentBuilder.TypeStrategy.Default.REBASE)
					.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
					.with(AgentBuilder.RedefinitionStrategy.DiscoveryStrategy.Reiterating.INSTANCE)
					.with(RETRANSFORMATION_FAILURE_LISTENER)
					.with(new AgentBuilder.InjectionStrategy.UsingUnsafe.OfFactory(unsafeFactory))
					.disableClassFormatChanges();
			for (Pointcut interception : pointcuts) {
				agentBuilder = agentBuilder
						.type(JavaInstrumentationPointcutDefinitions.getClassesMatcher(interception.methodsMap()))
						.transform(isolated(interception.transformer()));
			}
			agentBuilder.installOn(inst);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.agent.installation.error", pointcutNames(pointcuts)), e);
		}
	}

	/**
	 * Selects the classes no pointcut transformer may touch: Byte Buddy, Mockito's
	 * advice class (resolving it while Mockito injects its dispatcher fails), the
	 * deepest JDK internals, the stack walking the advice relies on (instrumenting
	 * it causes a ClassCircularityError), and the agent's own classes. The
	 * sun.nio.ch channel implementations stay watched on purpose: they implement
	 * the guarded connect, send and receive methods.
	 *
	 * @return The matcher of the ignored classes.
	 */
	static ElementMatcher.Junction<TypeDescription> ignoredTypes() {
		return ElementMatchers.nameStartsWith("net.bytebuddy.")
				.or(ElementMatchers.nameStartsWith("org.mockito.internal.creation.bytebuddy.MockMethodAdvice"))
				.or(ElementMatchers.nameStartsWith("jdk.internal."))
				.or(ElementMatchers.nameStartsWith("java.lang.invoke."))
				.or(ElementMatchers.nameStartsWith("java.lang.reflect."))
				.or(ElementMatchers.nameStartsWith("java.lang.StackWalker"))
				.or(ElementMatchers.nameStartsWith("java.lang.StackStreamFactory"))
				.or(ElementMatchers.nameStartsWith("java.lang.StackFrameInfo"))
				.or(ElementMatchers.nameStartsWith("java.lang.LiveStackFrameInfo"))
				.or(ElementMatchers.nameStartsWith("de.tum.cit.ase.ares.api.aop.java.instrumentation."));
	}

	/**
	 * Wraps one pointcut's transformer so that its failure on a class costs only
	 * that pointcut: the failure is recorded for
	 * {@link #throwIfTransformationFailed()}, as a failed class would be, and the
	 * other pointcuts still transform the class.
	 *
	 * @param transformer The pointcut's transformer.
	 * @return The transformer that records its own failure and leaves the class to
	 *         the other pointcuts.
	 */
	private static AgentBuilder.Transformer isolated(AgentBuilder.Transformer transformer) {
		return (builder, typeDescription, classLoader, module, protectionDomain) -> {
			try {
				return transformer.transform(builder, typeDescription, classLoader, module, protectionDomain);
			} catch (RuntimeException | LinkageError failure) {
				recordFailure(new TransformationFailure(typeDescription.getName(), failure));
				return builder;
			}
		};
	}

	/**
	 * Names every class the given pointcuts watch, for the message shown when the
	 * agent cannot be installed.
	 *
	 * @param pointcuts The pointcuts the agent builder was meant to install.
	 * @return The watched class names, without duplicates, joined by commas.
	 */
	private static String pointcutNames(List<Pointcut> pointcuts) {
		return pointcuts.stream().flatMap(interception -> interception.methodsMap().keySet().stream()).distinct()
				.collect(Collectors.joining(", "));
	}

	/**
	 * Rewrites application-side invocations of {@link Thread#start()} and Object's
	 * final monitor methods. The start wrapper retains the effective task class for
	 * later monitor checks; the monitor wrappers preserve ordinary Object behaviour
	 * and invoke the thread policy only when the runtime receiver is a Thread.
	 */
	private static void installThreadCallSiteBuilder(Instrumentation inst,
			ClassInjector.UsingUnsafe.Factory unsafeFactory) {
		try {
			Method startMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("start", Thread.class);
			Method notifyMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("notify", Object.class);
			Method notifyAllMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("notifyAll", Object.class);
			Method waitMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("wait", Object.class);
			Method timedWaitMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("wait", Object.class,
					long.class);
			Method preciseWaitMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("wait", Object.class,
					long.class, int.class);

			new AgentBuilder.Default()
					.ignore(ElementMatchers.nameStartsWith("net.bytebuddy.")
							.or(ElementMatchers.nameStartsWith("de.tum.cit.ase.ares.api.")))
					.with(TRANSFORMATION_FAILURE_LISTENER).with(AgentBuilder.TypeStrategy.Default.REBASE)
					.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
					.with(new AgentBuilder.InjectionStrategy.UsingUnsafe.OfFactory(unsafeFactory))
					.disableClassFormatChanges()
					.type(typeDescription -> !typeDescription.isInterface() && INSTRUMENTED_THREAD_MONITOR_PACKAGES
							.stream().anyMatch(prefix -> typeDescription.getName().startsWith(prefix)),
							ElementMatchers.not(ElementMatchers.isBootstrapClassLoader()))
					.transform((builder, typeDescription, classLoader, javaModule, protectionDomain) -> builder
							.visit(threadStartSubstitution(startMethod))
							.visit(monitorSubstitution("notify", 0, notifyMethod))
							.visit(monitorSubstitution("notifyAll", 0, notifyAllMethod))
							.visit(monitorSubstitution("wait", 0, waitMethod))
							.visit(monitorSubstitution("wait", 1, timedWaitMethod))
							.visit(monitorSubstitution("wait", 2, preciseWaitMethod)))
					.installOn(inst);
		} catch (ReflectiveOperationException e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.agent.installation.error", "Object monitor call sites"), e);
		}
	}

	private static net.bytebuddy.asm.AsmVisitorWrapper.ForDeclaredMethods threadStartSubstitution(Method replacement) {
		net.bytebuddy.matcher.ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers
				.isDeclaredBy(Thread.class).and(ElementMatchers.named("start")).and(ElementMatchers.takesArguments(0));
		return MemberSubstitution.relaxed().method(matcher).replaceWith(replacement).on(ElementMatchers.any());
	}

	private static net.bytebuddy.asm.AsmVisitorWrapper.ForDeclaredMethods monitorSubstitution(String methodName,
			int argumentCount, Method replacement) {
		net.bytebuddy.matcher.ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers
				.isDeclaredBy(Object.class).and(ElementMatchers.named(methodName))
				.and(ElementMatchers.takesArguments(argumentCount));
		return MemberSubstitution.relaxed().method(matcher).replaceWith(replacement).on(ElementMatchers.any());
	}

	private record TransformationFailure(String typeName, Throwable cause) {
	}

	/**
	 * One group of method calls the agent intercepts, paired with the transformer
	 * that rewrites them.
	 *
	 * @param methodsMap  The watched classes, each with the names of its methods.
	 * @param transformer The transformer that applies the bytecode modification.
	 */
	record Pointcut(Map<String, List<String>> methodsMap, AgentBuilder.Transformer transformer) {
	}
}
