package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassInjector.UsingUnsafe.Factory;
import net.bytebuddy.matcher.ElementMatchers;

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

/**
 * This class is the entry point for the Java instrumentation agent. It installs
 * the agent builder for the different types of file operations.
 */
public final class JavaInstrumentationAgent {
	private static volatile Instrumentation instrumentation;
	private static volatile Factory classInjectorFactory;
	private static final Set<String> INSTRUMENTED_THREAD_MONITOR_PACKAGES = ConcurrentHashMap.newKeySet();

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
		Factory unsafeFactory = Factory.resolve(inst);
		instrumentation = inst;
		classInjectorFactory = unsafeFactory;

		putToolboxOnBootClassLoader(unsafeFactory);

		// Pre-warm the StackWalker infrastructure on the bootstrap class loader before
		// any pointcut goes live. The toolbox advice paths use StackWalker for the fast
		// caller-package check; without this warm-up the first invocation from a
		// pointcut would lazily load StackStreamFactory + StackFrameInfo while the
		// advice is already on the stack, which manifests as a ClassCircularityError
		// the moment the JDK retransforms java.io.* during agent install.
		java.lang.StackWalker walker = java.lang.StackWalker.getInstance();
		walker.walk(stream -> stream.limit(1L).count());

		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES,
				JavaInstrumentationBindingDefinitions::createReadPathMethodBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_OVERWRITE_FILES,
				JavaInstrumentationBindingDefinitions::createOverwritePathMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_FILES,
				JavaInstrumentationBindingDefinitions::createCreatePathMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_FILES,
				JavaInstrumentationBindingDefinitions::createExecutePathMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_DELETE_FILES,
				JavaInstrumentationBindingDefinitions::createDeletePathMethodBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_THREADS,
				JavaInstrumentationBindingDefinitions::createCreateThreadMethodBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_COMMANDS,
				JavaInstrumentationBindingDefinitions::createExecuteCommandMethodBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CONNECT_TO_NETWORK,
				JavaInstrumentationBindingDefinitions::createConnectNetworkMethodBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_SEND_TO_NETWORK,
				JavaInstrumentationBindingDefinitions::createSendNetworkMethodBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_RECEIVE_FROM_NETWORK,
				JavaInstrumentationBindingDefinitions::createReceiveNetworkMethodBinding);

		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES,
				JavaInstrumentationBindingDefinitions::createReadPathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_OVERWRITE_FILES,
				JavaInstrumentationBindingDefinitions::createOverwritePathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_FILES,
				JavaInstrumentationBindingDefinitions::createCreatePathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_FILES,
				JavaInstrumentationBindingDefinitions::createExecutePathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_DELETE_FILES,
				JavaInstrumentationBindingDefinitions::createDeletePathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_THREADS,
				JavaInstrumentationBindingDefinitions::createCreateThreadConstructorBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_COMMANDS,
				JavaInstrumentationBindingDefinitions::createExecuteCommandConstructorBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CONNECT_TO_NETWORK,
				JavaInstrumentationBindingDefinitions::createConnectNetworkConstructorBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_SEND_TO_NETWORK,
				JavaInstrumentationBindingDefinitions::createSendNetworkConstructorBinding);
		installAgentBuilder(inst, unsafeFactory,
				JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_RECEIVE_FROM_NETWORK,
				JavaInstrumentationBindingDefinitions::createReceiveNetworkConstructorBinding);
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
	 * Installs call-site substitutions for Thread monitor operations in one
	 * restricted package.
	 * <p>
	 * Object's final native monitor methods cannot be advised at their declaration,
	 * so their application-side call sites have to be rewritten. Restricting the
	 * transformer to the package governed by the current policy is both the precise
	 * security boundary and avoids transforming every framework and dependency
	 * class in the JVM. Retransformation covers restricted classes that were loaded
	 * during test discovery before the policy became available.
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
		if (INSTRUMENTED_THREAD_MONITOR_PACKAGES.add(restrictedPackage)) {
			installThreadMonitorCallSiteBuilder(currentInstrumentation, currentFactory, restrictedPackage);
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

			// Load classes in dependency order to ensure proper initialization

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
	 * This method installs an agent builder for the provided methods map. It
	 * applies a pointcut to match the methods and installs the transformer for
	 * bytecode modification.
	 *
	 * @param inst          The instrumentation instance used to instrument
	 *                      bytecode.
	 * @param unsafeFactory Factory for unsafe class injection.
	 * @param methodsMap    A map containing method signatures as keys with lists of
	 *                      method names. These define which method CALLS should be
	 *                      intercepted.
	 * @param transformer   The transformer responsible for applying the bytecode
	 *                      modifications at runtime.
	 * @throws SecurityException If the installation of the agent builder fails.
	 */
	private static void installAgentBuilder(Instrumentation inst, ClassInjector.UsingUnsafe.Factory unsafeFactory,
			Map<String, List<String>> methodsMap, AgentBuilder.Transformer transformer) {
		try {
			new AgentBuilder.Default()
					// Ignore ByteBuddy's own classes to avoid infinite recursion
					.ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
					// Ignore deepest JDK internals that must never be instrumented.
					// sun.nio.ch.*Impl
					// classes are intentionally NOT excluded here so SocketChannelImpl,
					// DatagramChannelImpl, AsynchronousSocketChannel impls remain instrumentable
					// for their connect / send / receive methods which the abstract NIO base
					// classes only declare.
					.ignore(ElementMatchers.nameStartsWith("jdk.internal."))
					.ignore(ElementMatchers.nameStartsWith("java.lang.invoke."))
					.ignore(ElementMatchers.nameStartsWith("java.lang.reflect."))
					// StackWalker plumbing must stay un-instrumented; the advice toolbox uses
					// StackWalker on every call-stack inspection and any retransformation here
					// trips ClassCircularityError when the first pointcut fires.
					.ignore(ElementMatchers.nameStartsWith("java.lang.StackWalker"))
					.ignore(ElementMatchers.nameStartsWith("java.lang.StackStreamFactory"))
					.ignore(ElementMatchers.nameStartsWith("java.lang.StackFrameInfo"))
					.ignore(ElementMatchers.nameStartsWith("java.lang.LiveStackFrameInfo"))
					// Ignore Ares internal classes to avoid self-instrumentation
					.ignore(ElementMatchers.nameStartsWith("de.tum.cit.ase.ares.api.aop.java.instrumentation."))
					.with(AgentBuilder.TypeStrategy.Default.REBASE)
					.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
					.with(new AgentBuilder.InjectionStrategy.UsingUnsafe.OfFactory(unsafeFactory))
					.disableClassFormatChanges()
					.type(JavaInstrumentationPointcutDefinitions.getClassesMatcher(methodsMap)).transform(transformer)
					.installOn(inst);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
					"security.instrumentation.agent.installation.error", String.join(", ", methodsMap.keySet())), e);
		}
	}

	/**
	 * Rewrites application-side invocations of Object's final monitor methods. The
	 * wrappers preserve ordinary Object behaviour and invoke the thread policy only
	 * when the runtime receiver is a Thread.
	 */
	private static void installThreadMonitorCallSiteBuilder(Instrumentation inst,
			ClassInjector.UsingUnsafe.Factory unsafeFactory, String restrictedPackage) {
		try {
			Method notifyMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("notify", Object.class);
			Method startMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("start", Thread.class);
			Method notifyAllMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("notifyAll", Object.class);
			Method waitMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("wait", Object.class);
			Method timedWaitMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("wait", Object.class,
					long.class);
			Method preciseWaitMethod = JavaInstrumentationThreadSystemCallSite.class.getMethod("wait", Object.class,
					long.class, int.class);

			new AgentBuilder.Default().ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
					.ignore(ElementMatchers.nameStartsWith("de.tum.cit.ase.ares.api."))
					.with(AgentBuilder.TypeStrategy.Default.REBASE)
					.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
					.with(new AgentBuilder.InjectionStrategy.UsingUnsafe.OfFactory(unsafeFactory))
					.disableClassFormatChanges()
					.type(ElementMatchers.nameStartsWith(restrictedPackage)
							.and(ElementMatchers.not(ElementMatchers.isInterface())),
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

	private static net.bytebuddy.asm.AsmVisitorWrapper.ForDeclaredMethods monitorSubstitution(String methodName,
			int argumentCount, Method replacement) {
		net.bytebuddy.matcher.ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers
				.isDeclaredBy(Object.class).and(ElementMatchers.named(methodName))
				.and(ElementMatchers.takesArguments(argumentCount));
		return MemberSubstitution.relaxed().method(matcher).replaceWith(replacement).on(ElementMatchers.any());
	}

	private static net.bytebuddy.asm.AsmVisitorWrapper.ForDeclaredMethods threadStartSubstitution(Method replacement) {
		net.bytebuddy.matcher.ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers
				.isDeclaredBy(Thread.class).and(ElementMatchers.named("start")).and(ElementMatchers.takesArguments(0));
		return MemberSubstitution.relaxed().method(matcher).replaceWith(replacement).on(ElementMatchers.any());
	}
}
