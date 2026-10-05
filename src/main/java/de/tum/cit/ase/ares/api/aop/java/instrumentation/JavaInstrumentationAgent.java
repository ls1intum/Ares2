package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassInjector.UsingUnsafe.Factory;
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
	private static volatile Instrumentation instrumentation;
	private static volatile Factory classInjectorFactory;
	private static final Set<String> INSTRUMENTED_THREAD_MONITOR_PACKAGES = ConcurrentHashMap.newKeySet();
	private static final Object THREAD_MONITOR_PACKAGE_REGISTRATION_LOCK = new Object();
	private static final AtomicReference<TransformationFailure> TRANSFORMATION_FAILURE = new AtomicReference<>();
	private static final AgentBuilder.Listener TRANSFORMATION_FAILURE_LISTENER = new AgentBuilder.Listener.Adapter() {
		@Override
		public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded,
				Throwable throwable) {
			TRANSFORMATION_FAILURE.compareAndSet(null, new TransformationFailure(typeName, throwable));
		}
	};

	private JavaInstrumentationAgent() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
				.localize("security.instrumentation.utility.initialization", "JavaInstrumentationAgent"));
	}

	/**
	 * This method is called before the application's main method is called. It
	 * installs the agent builder for the different types of file operations.
	 *
	 * @param agentArgs The agent arguments.
	 * @param inst      The instrumentation instance.
	 */
	public static void premain(String agentArgs, Instrumentation inst) {
		// ResourceBundle loading performs file-system operations. Populate Ares'
		// localisation cache before those JDK operations are instrumented, otherwise a
		// first security diagnostic can recursively request its own message bundle.
		Messages.init();
		Factory unsafeFactory = Factory.resolve(inst);
		instrumentation = inst;
		classInjectorFactory = unsafeFactory;

		putToolboxOnBootClassLoader(unsafeFactory);
		initializeToolboxes();

		// Pre-warm the StackWalker infrastructure on the bootstrap class loader before
		// any pointcut goes live. The toolbox advice paths use StackWalker for the fast
		// caller-package check; without this warm-up the first invocation from a
		// pointcut would lazily load StackStreamFactory + StackFrameInfo while the
		// advice is already on the stack, which manifests as a ClassCircularityError
		// the moment the JDK retransforms java.io.* during agent install.
		java.lang.StackWalker walker = java.lang.StackWalker.getInstance();
		walker.walk(stream -> stream.limit(1L).count());
		// File-policy checks call Files.exists/toRealPath. Load the platform's file
		// provider exception classes before those JDK methods are instrumented,
		// avoiding
		// a cold-start ClassCircularityError inside the first security check.
		java.nio.file.Files.exists(java.nio.file.Path.of("."));
		preloadPlatformClass("sun.nio.fs.UnixException");
		preloadPlatformClass("sun.nio.fs.WindowsException");

		installAgentBuilder(inst, unsafeFactory,
				List.of(new Pointcut(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES,
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
								JavaInstrumentationBindingDefinitions::createReceiveNetworkConstructorBinding)));
		installThreadCallSiteBuilder(inst, unsafeFactory);
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
	 * Installs call-site substitutions for Thread start and monitor operations in
	 * one restricted package.
	 * <p>
	 * Object's final native monitor methods cannot be advised at their declaration,
	 * so their application-side call sites have to be rewritten. Restricting the
	 * transformer to the package governed by the current policy is both the precise
	 * security boundary and avoids transforming every framework and dependency
	 * class in the JVM. The package is registered before supervised classes are
	 * first used, so their definitions pass through this transformer without a
	 * structurally unsafe second retransformation.
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
	 * Installs one agent builder that carries every pointcut, so each loaded class
	 * is matched once rather than once per pointcut, and shares one type pool
	 * cache, so each superclass is parsed once. Retransformation repeats until no
	 * new class appears, because classes loaded while installing are neither in the
	 * first snapshot nor seen by the load hook.
	 *
	 * @param inst          The instrumentation instance used to instrument
	 *                      bytecode.
	 * @param unsafeFactory Factory for unsafe class injection.
	 * @param pointcuts     The method calls to intercept, each with the transformer
	 *                      that applies its bytecode modification.
	 * @throws SecurityException If the installation of the agent builder fails.
	 */
	private static void installAgentBuilder(Instrumentation inst, ClassInjector.UsingUnsafe.Factory unsafeFactory,
			List<Pointcut> pointcuts) {
		try {
			AgentBuilder agentBuilder = new AgentBuilder.Default()
					// Use one combined matcher: AgentBuilder.ignore replaces the previous
					// matcher, so separate calls would accidentally re-enable earlier groups.
					.ignore(ElementMatchers.nameStartsWith("net.bytebuddy.")
							// Mockito injects MockMethodDispatcher into the bootstrap loader while its
							// advice class is loading. Resolving that class's hierarchy from another
							// transformer before injection has completed fails. Exclude only the advice
							// class itself: Mockito-generated implementations of guarded interfaces must
							// remain instrumentable.
							.or(ElementMatchers
									.nameStartsWith("org.mockito.internal.creation.bytebuddy.MockMethodAdvice"))
							// Ignore deepest JDK internals that must never be instrumented.
							// sun.nio.ch.*Impl
							// classes are intentionally NOT excluded here so SocketChannelImpl,
							// DatagramChannelImpl, AsynchronousSocketChannel impls remain instrumentable
							// for their connect / send / receive methods which the abstract NIO base
							// classes only declare.
							.or(ElementMatchers.nameStartsWith("jdk.internal."))
							.or(ElementMatchers.nameStartsWith("java.lang.invoke."))
							.or(ElementMatchers.nameStartsWith("java.lang.reflect."))
							// StackWalker plumbing must stay un-instrumented; the advice toolbox uses
							// StackWalker on every call-stack inspection and any retransformation here
							// trips ClassCircularityError when the first pointcut fires.
							.or(ElementMatchers.nameStartsWith("java.lang.StackWalker"))
							.or(ElementMatchers.nameStartsWith("java.lang.StackStreamFactory"))
							.or(ElementMatchers.nameStartsWith("java.lang.StackFrameInfo"))
							.or(ElementMatchers.nameStartsWith("java.lang.LiveStackFrameInfo"))
							// Ignore Ares internal classes to avoid self-instrumentation
							.or(ElementMatchers.nameStartsWith("de.tum.cit.ase.ares.api.aop.java.instrumentation.")))
					.with(TRANSFORMATION_FAILURE_LISTENER)
					.with(new AgentBuilder.PoolStrategy.WithTypePoolCache.Simple(new ConcurrentHashMap<>()))
					.with(AgentBuilder.TypeStrategy.Default.REBASE)
					.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
					.with(AgentBuilder.RedefinitionStrategy.DiscoveryStrategy.Reiterating.INSTANCE)
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
				TRANSFORMATION_FAILURE.compareAndSet(null,
						new TransformationFailure(typeDescription.getName(), failure));
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
	private record Pointcut(Map<String, List<String>> methodsMap, AgentBuilder.Transformer transformer) {
	}
}
