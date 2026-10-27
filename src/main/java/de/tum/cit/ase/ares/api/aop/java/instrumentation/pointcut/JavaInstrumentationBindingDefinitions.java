package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationConnectNetworkConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationConnectNetworkMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationCreatePathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationCreatePathMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationCreateThreadConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationCreateThreadMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecuteCommandConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecuteCommandMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReceiveNetworkConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReceiveNetworkMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationSendNetworkConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationSendNetworkMethodAdvice;

/**
 * This class provides the definitions for the bindings of the Java
 * instrumentation. It is responsible for creating the bindings for different
 * pointcuts, which represent file system operations such as reading,
 * overwriting, executing, or deleting files. These bindings enable
 * security-related advice to be applied to monitor and control file operations
 * at runtime. The advice ensures that file system interactions adhere to the
 * established security policies, preventing unauthorised or malicious file
 * operations.
 */
public final class JavaInstrumentationBindingDefinitions {

	/**
	 * Each advice class resolved once, with the settings Byte Buddy's
	 * {@code AgentBuilder.Transformer.ForAdvice} uses, instead of re-reading and
	 * re-parsing the advice class file for every transformed class.
	 */
	private static final Map<Class<?>, Advice> RESOLVED_ADVICE = new ConcurrentHashMap<>();

	// <editor-fold desc="Constructor">
	/**
	 * This class is a utility class and should not be instantiated.
	 */
	private JavaInstrumentationBindingDefinitions() {
		throw new SecurityException(
				JavaInstrumentationAdviceAbstractToolbox.localize("security.general.utility.initialization"));
	}
	// </editor-fold>

	// <editor-fold desc="Create Path">
	/**
	 * Creates method bindings for pointcuts that can create files or directories.
	 *
	 * @param builder          the current dynamic type builder
	 * @param typeDescription  description of the type being transformed
	 * @param classLoader      loader that defined the type
	 * @param javaModule       originating module (ignored)
	 * @param protectionDomain originating protection domain (ignored)
	 * @return builder enriched with the binding
	 */
	public static DynamicType.Builder<?> createCreatePathMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_FILES,
					JavaInstrumentationCreatePathMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.create.method.binding.error"), e);
		}
	}

	/**
	 * Creates constructor bindings for pointcuts that can create files or
	 * directories.
	 *
	 * @param builder          the current dynamic type builder
	 * @param typeDescription  description of the type being transformed
	 * @param classLoader      loader that defined the type
	 * @param javaModule       originating module (ignored)
	 * @param protectionDomain originating protection domain (ignored)
	 * @return builder enriched with the binding
	 */
	public static DynamicType.Builder<?> createCreatePathConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_FILES,
					JavaInstrumentationCreatePathConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.create.constructor.binding.error"), e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Tools">
	/**
	 * This method creates a binding for the given type description, pointcuts, and
	 * advice. The binding connects the bytecode modification process to the
	 * specific methods defined by the pointcuts and applies the provided advice.
	 * This ensures that security policies are enforced on methods interacting with
	 * the file system, preventing unauthorised actions such as file manipulation or
	 * access.
	 *
	 * @param builder         The builder used to create the binding.
	 * @param typeDescription The description of the class whose methods are being
	 *                        instrumented.
	 * @param pointcuts       The pointcuts that specify which methods should be
	 *                        instrumented.
	 * @param advice          The advice to be applied to the methods matched by the
	 *                        pointcuts.
	 * @return The builder with the binding applied.
	 * @throws SecurityException If the binding could not be created, preventing the
	 *                           enforcement of security policies.
	 */
	private static DynamicType.Builder<?> createMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, Map<String, List<String>> pointcuts, Class<?> advice) {
		try {
			ElementMatcher<? super MethodDescription> matcher = JavaInstrumentationPointcutDefinitions
					.getMethodsMatcher(typeDescription, pointcuts);
			return builder.visit(resolvedAdvice(advice).on(matcher));
		} catch (Exception e) {
			throw new SecurityException(
					JavaInstrumentationAdviceAbstractToolbox.localize("security.instrumentation.binding.error"), e);
		}
	}

	/**
	 * Creates a binding that applies the given advice to the constructors of the
	 * type that the pointcuts name.
	 *
	 * @param builder         The builder used to create the binding.
	 * @param typeDescription The description of the class whose constructors are
	 *                        being instrumented.
	 * @param pointcuts       The pointcuts that specify which constructors should
	 *                        be instrumented.
	 * @param advice          The advice to be applied to the matched constructors.
	 * @return The builder with the binding applied.
	 * @throws SecurityException If the binding could not be created.
	 */
	private static DynamicType.Builder<?> createConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, Map<String, List<String>> pointcuts, Class<?> advice) {
		try {
			ElementMatcher<? super MethodDescription> matcher = JavaInstrumentationPointcutDefinitions
					.getConstructorsMatcher(typeDescription, pointcuts);
			return builder.visit(resolvedAdvice(advice).on(matcher));
		} catch (Exception e) {
			throw new SecurityException(
					JavaInstrumentationAdviceAbstractToolbox.localize("security.instrumentation.binding.error"), e);
		}
	}

	/**
	 * Returns the advice for the given advice class, resolving it on first use from
	 * the advice's own class loader with Byte Buddy's default advice settings.
	 *
	 * @param advice The advice class.
	 * @return The resolved advice, shared by all transformed classes.
	 */
	private static Advice resolvedAdvice(Class<?> advice) {
		return RESOLVED_ADVICE.computeIfAbsent(advice, adviceClass -> {
			ClassFileLocator classFileLocator = ClassFileLocator.ForClassLoader.of(adviceClass.getClassLoader());
			return Advice.withCustomMapping()
					.to(AgentBuilder.PoolStrategy.Default.FAST.typePool(classFileLocator, adviceClass.getClassLoader())
							.describe(adviceClass.getName()).resolve(), classFileLocator)
					.withAssigner(Assigner.DEFAULT).withExceptionHandler(Advice.ExceptionHandler.Default.SUPPRESSING);
		});
	}

	// </editor-fold>

	// <editor-fold desc="Read Path">
	/**
	 * This method creates a binding for the read path pointcut. It applies the
	 * instrumentation advice for file read operations defined in the corresponding
	 * pointcuts, ensuring that security-related advice is applied when methods that
	 * read files are invoked. This helps to enforce security policies related to
	 * file read operations, preventing unauthorised access to files.
	 *
	 * @param builder          The builder used to create the binding.
	 * @param typeDescription  The description of the class whose methods are being
	 *                         instrumented.
	 * @param classLoader      The class loader responsible for loading the class.
	 * @param javaModule       The Java module being ignored (for compatibility
	 *                         reasons).
	 * @param protectionDomain The protection domain being ignored (for
	 *                         compatibility reasons).
	 * @return The builder with the binding applied for file read operations.
	 * @throws SecurityException If the binding could not be created for the read
	 *                           path, preventing the security advice from being
	 *                           applied.
	 */
	public static DynamicType.Builder<?> createReadPathMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES,
					JavaInstrumentationReadPathMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.read.method.binding.error"), e);
		}
	}

	public static DynamicType.Builder<?> createReadPathConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES,
					JavaInstrumentationReadPathConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.read.constructor.binding.error"), e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Overwrite Path">
	/**
	 * This method creates a binding for the overwrite path pointcut. It applies the
	 * instrumentation advice for file overwrite operations defined in the
	 * corresponding pointcuts, ensuring that security-related advice is applied
	 * when methods that overwrite files are invoked. This ensures that unauthorised
	 * or potentially harmful file overwriting actions are detected and prevented.
	 *
	 * @param builder          The builder used to create the binding.
	 * @param typeDescription  The description of the class whose methods are being
	 *                         instrumented.
	 * @param classLoader      The class loader responsible for loading the class.
	 * @param javaModule       The Java module being ignored (for compatibility
	 *                         reasons).
	 * @param protectionDomain The protection domain being ignored (for
	 *                         compatibility reasons).
	 * @return The builder with the binding applied for file overwrite operations.
	 * @throws SecurityException If the binding could not be created for the
	 *                           overwrite path, preventing the application of
	 *                           security advice for file overwriting operations.
	 */
	public static DynamicType.Builder<?> createOverwritePathMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_OVERWRITE_FILES,
					JavaInstrumentationOverwritePathMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.overwrite.method.binding.error"), e);
		}
	}

	public static DynamicType.Builder<?> createOverwritePathConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_OVERWRITE_FILES,
					JavaInstrumentationOverwritePathConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.overwrite.constructor.binding.error"), e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Execute Path">
	/**
	 * This method creates a binding for the execute path pointcut. It applies the
	 * instrumentation advice for file execution operations defined in the
	 * corresponding pointcuts, ensuring that security-related advice is applied
	 * when methods that execute files are invoked. This helps to prevent
	 * unauthorised file executions that could compromise the system's integrity.
	 *
	 * @param builder          The builder used to create the binding.
	 * @param typeDescription  The description of the class whose methods are being
	 *                         instrumented.
	 * @param classLoader      The class loader responsible for loading the class.
	 * @param javaModule       The Java module being ignored (for compatibility
	 *                         reasons).
	 * @param protectionDomain The protection domain being ignored (for
	 *                         compatibility reasons).
	 * @return The builder with the binding applied for file execution operations.
	 * @throws SecurityException If the binding could not be created for the execute
	 *                           path, preventing the enforcement of security
	 *                           policies for file execution.
	 */
	public static DynamicType.Builder<?> createExecutePathMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_FILES,
					JavaInstrumentationExecutePathMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.execute.method.binding.error"), e);
		}
	}

	public static DynamicType.Builder<?> createExecutePathConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_FILES,
					JavaInstrumentationExecutePathConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.execute.constructor.binding.error"), e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Delete Path">
	/**
	 * This method creates a binding for the delete path pointcut. It applies the
	 * instrumentation advice for file deletion operations defined in the
	 * corresponding pointcuts, ensuring that security-related advice is applied
	 * when methods that delete files are invoked. This safeguards against
	 * unauthorised or harmful file deletion operations.
	 *
	 * @param builder          The builder used to create the binding.
	 * @param typeDescription  The description of the class whose methods are being
	 *                         instrumented.
	 * @param classLoader      The class loader responsible for loading the class.
	 * @param javaModule       The Java module being ignored (for compatibility
	 *                         reasons).
	 * @param protectionDomain The protection domain being ignored (for
	 *                         compatibility reasons).
	 * @return The builder with the binding applied for file deletion operations.
	 * @throws SecurityException If the binding could not be created for the delete
	 *                           path, preventing the enforcement of security
	 *                           policies for file deletion operations.
	 */
	public static DynamicType.Builder<?> createDeletePathMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_DELETE_FILES,
					JavaInstrumentationDeletePathMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.delete.method.binding.error"), e);
		}
	}

	public static DynamicType.Builder<?> createDeletePathConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_DELETE_FILES,
					JavaInstrumentationDeletePathConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.delete.constructor.binding.error"), e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Create Thread">
	/**
	 * This method creates a binding for the create thread pointcut. It applies the
	 * instrumentation advice for thread creation operations defined in the
	 * corresponding pointcuts, ensuring that security-related advice is applied
	 * when methods that create threads are invoked. This safeguards against
	 * unauthorised or harmful thread creation operations.
	 *
	 * @param builder          The builder used to create the binding.
	 * @param typeDescription  The description of the class whose methods are being
	 *                         instrumented.
	 * @param classLoader      The class loader responsible for loading the class.
	 * @param javaModule       The Java module being ignored (for compatibility
	 *                         reasons).
	 * @param protectionDomain The protection domain being ignored (for
	 *                         compatibility reasons).
	 * @return The builder with the binding applied for thread creation operations.
	 * @throws SecurityException If the binding could not be created for the create
	 *                           thread, preventing the enforcement of security
	 *                           policies for thread creation operations.
	 */
	public static DynamicType.Builder<?> createCreateThreadMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_THREADS,
					JavaInstrumentationCreateThreadMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.create.thread.method.binding.error"), e);
		}
	}

	public static DynamicType.Builder<?> createCreateThreadConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_THREADS,
					JavaInstrumentationCreateThreadConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.create.thread.constructor.binding.error"), e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Execute Command">
	/**
	 * This method creates a binding for the execute command pointcut. It applies
	 * the instrumentation advice for command execution operations defined in the
	 * corresponding pointcuts, ensuring that security-related advice is applied
	 * when methods that execute commands are invoked. This safeguards against
	 * unauthorised or harmful command execution operations.
	 *
	 * @param builder          The builder used to create the binding.
	 * @param typeDescription  The description of the class whose methods are being
	 *                         instrumented.
	 * @param classLoader      The class loader responsible for loading the class.
	 * @param javaModule       The Java module being ignored (for compatibility
	 *                         reasons).
	 * @param protectionDomain The protection domain being ignored (for
	 *                         compatibility reasons).
	 * @return The builder with the binding applied for command execution
	 *         operations.
	 * @throws SecurityException If the binding could not be created for the execute
	 *                           command, preventing the enforcement of security
	 *                           policies for command execution operations.
	 */
	public static DynamicType.Builder<?> createExecuteCommandMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_COMMANDS,
					JavaInstrumentationExecuteCommandMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.execute.command.method.binding.error"), e);
		}
	}

	public static DynamicType.Builder<?> createExecuteCommandConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_EXECUTE_COMMANDS,
					JavaInstrumentationExecuteCommandConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.execute.command.constructor.binding.error"), e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Network connect">
	public static DynamicType.Builder<?> createConnectNetworkMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CONNECT_TO_NETWORK,
					JavaInstrumentationConnectNetworkMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.connect.network.method.binding.error"), e);
		}
	}

	public static DynamicType.Builder<?> createConnectNetworkConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CONNECT_TO_NETWORK,
					JavaInstrumentationConnectNetworkConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.connect.network.constructor.binding.error"), e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Network send">
	public static DynamicType.Builder<?> createSendNetworkMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_SEND_TO_NETWORK,
					JavaInstrumentationSendNetworkMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.send.network.method.binding.error"), e);
		}
	}

	public static DynamicType.Builder<?> createSendNetworkConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_SEND_TO_NETWORK,
					JavaInstrumentationSendNetworkConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.send.network.constructor.binding.error"), e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Network receive">
	public static DynamicType.Builder<?> createReceiveNetworkMethodBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createMethodBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_RECEIVE_FROM_NETWORK,
					JavaInstrumentationReceiveNetworkMethodAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.receive.network.method.binding.error"), e);
		}
	}

	public static DynamicType.Builder<?> createReceiveNetworkConstructorBinding(DynamicType.Builder<?> builder,
			TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule,
			ProtectionDomain protectionDomain) {
		try {
			return createConstructorBinding(builder, typeDescription,
					JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_RECEIVE_FROM_NETWORK,
					JavaInstrumentationReceiveNetworkConstructorAdvice.class);
		} catch (Exception e) {
			throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox
					.localize("security.instrumentation.receive.network.constructor.binding.error"), e);
		}
	}
	// </editor-fold>
}
