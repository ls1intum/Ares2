package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;

import net.bytebuddy.agent.builder.AgentBuilder;
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
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.NetworkTarget;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.ThreadTarget;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;

/**
 * This class is the entry point for the Java instrumentation agent. It installs
 * the agent builder for the different types of file operations.
 */
public class JavaInstrumentationAgent {

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

		putToolboxOnBootClassLoader(unsafeFactory);

		// Pre-warm the StackWalker infrastructure on the bootstrap class loader before
		// any pointcut goes live. The toolbox advice paths use StackWalker for the fast
		// caller-package check; without this warm-up the first invocation from a
		// pointcut would lazily load StackStreamFactory + StackFrameInfo while the
		// advice is already on the stack, which manifests as a ClassCircularityError
		// the moment the JDK retransforms java.io.* during agent install.
		java.lang.StackWalker walker = java.lang.StackWalker.getInstance();
		walker.walk(stream -> stream.limit(1L).count());

		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles,
				JavaInstrumentationBindingDefinitions::createReadPathMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles,
				JavaInstrumentationBindingDefinitions::createOverwritePathMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateFiles,
				JavaInstrumentationBindingDefinitions::createCreatePathMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles,
				JavaInstrumentationBindingDefinitions::createExecutePathMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles,
				JavaInstrumentationBindingDefinitions::createDeletePathMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateThreads,
				JavaInstrumentationBindingDefinitions::createCreateThreadMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteCommands,
				JavaInstrumentationBindingDefinitions::createExecuteCommandMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanConnectToNetwork,
				JavaInstrumentationBindingDefinitions::createConnectNetworkMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanSendToNetwork,
				JavaInstrumentationBindingDefinitions::createSendNetworkMethodBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanReceiveFromNetwork,
				JavaInstrumentationBindingDefinitions::createReceiveNetworkMethodBinding);

		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles,
				JavaInstrumentationBindingDefinitions::createReadPathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles,
				JavaInstrumentationBindingDefinitions::createOverwritePathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateFiles,
				JavaInstrumentationBindingDefinitions::createCreatePathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles,
				JavaInstrumentationBindingDefinitions::createExecutePathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles,
				JavaInstrumentationBindingDefinitions::createDeletePathConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateThreads,
				JavaInstrumentationBindingDefinitions::createCreateThreadConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteCommands,
				JavaInstrumentationBindingDefinitions::createExecuteCommandConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanConnectToNetwork,
				JavaInstrumentationBindingDefinitions::createConnectNetworkConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanSendToNetwork,
				JavaInstrumentationBindingDefinitions::createSendNetworkConstructorBinding);
		installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanReceiveFromNetwork,
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

	private static void putToolboxOnBootClassLoader(Factory unsafeFactory) {
		try {
			ClassInjector classInjector = null;

			// Try different injection strategies
			try {
				classInjector = unsafeFactory.make(null, null);
			} catch (Exception e) {
				// Fallback to a safer injection strategy
				classInjector = ClassInjector.UsingReflection.ofSystemClassLoader();
			}

			if (classInjector != null) {
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
				injectClassesSafely(classInjector,
						Map.ofEntries(
								Map.entry(FileTarget.class.getName(),
										ClassFileLocator.ForClassLoader.read(FileTarget.class)),
								Map.entry(NetworkTarget.class.getName(),
										ClassFileLocator.ForClassLoader.read(NetworkTarget.class)),
								Map.entry(ThreadTarget.class.getName(),
										ClassFileLocator.ForClassLoader.read(ThreadTarget.class)),
								Map.entry(CommandTarget.class.getName(),
										ClassFileLocator.ForClassLoader.read(CommandTarget.class))));

				// Step 4: Load concrete toolbox implementations (depend on abstract toolbox and targets)
				injectClassesSafely(classInjector,
						Map.ofEntries(
								Map.entry(JavaInstrumentationAdviceFileSystemToolbox.class.getName(),
										ClassFileLocator.ForClassLoader
												.read(JavaInstrumentationAdviceFileSystemToolbox.class)),
								Map.entry(JavaInstrumentationAdviceThreadSystemToolbox.class.getName(),
										ClassFileLocator.ForClassLoader
												.read(JavaInstrumentationAdviceThreadSystemToolbox.class)),
								Map.entry(JavaInstrumentationAdviceNetworkSystemToolbox.class.getName(),
										ClassFileLocator.ForClassLoader
												.read(JavaInstrumentationAdviceNetworkSystemToolbox.class)),
								Map.entry(JavaInstrumentationAdviceCommandSystemToolbox.class.getName(),
										ClassFileLocator.ForClassLoader
												.read(JavaInstrumentationAdviceCommandSystemToolbox.class))));
			}
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
					// Ignore deepest JDK internals that must never be instrumented. sun.nio.ch.*Impl
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
}
