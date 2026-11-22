package de.tum.cit.ase.ares.api.internal;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.WithIOManager;
import de.tum.cit.ase.ares.api.context.*;
import de.tum.cit.ase.ares.api.io.*;

@API(status = Status.INTERNAL)
public final class IOExtensionUtils {

	private static final Class<IOTesterManager> DEFAULT_IO_MANAGER = IOTesterManager.class;

	// ConcurrentHashMap rather than HashMap: parallel JUnit test containers resolve
	// their IO
	// managers through computeIfAbsent on this shared static cache, and a plain
	// HashMap would
	// race on the concurrent insert and could corrupt its buckets.
	private static final ConcurrentHashMap<Class<? extends IOManager<?>>, Supplier<? extends IOManager<?>>> IO_MANAGER_CACHE = new ConcurrentHashMap<>();

	private final AresIOContext context;
	/** The I/O manager class the test asks for, or the default one. */
	private final Class<? extends IOManager<?>> ioManagerClass;
	private final IOManager<?> ioManager;
	private final Class<?> controllerClass;

	/**
	 * Creates the I/O manager the test asks for with {@link WithIOManager}, or the
	 * default one.
	 *
	 * @param testContext the context of the test
	 * @throws SecurityException if the I/O manager cannot be created
	 */
	public IOExtensionUtils(TestContext testContext) {
		context = AresIOContext.from(testContext);
		ioManagerClass = ioManagerClassFor(testContext);
		ioManager = IO_MANAGER_CACHE.computeIfAbsent(ioManagerClass, IOExtensionUtils::generateIOManagerSupplier).get();
		controllerClass = ioManager.getControllerClass();
	}

	public void beforeTestExecution() {
		ioManager.beforeTestExecution(context);
	}

	public void afterTestExecution() {
		ioManager.afterTestExecution(context);
	}

	public boolean providesController() {
		return controllerClass != null;
	}

	public Object getControllerInstance() {
		return providesController() ? ioManager.getControllerInstance(context) : null;
	}

	public boolean canProvideControllerFor(Class<?> targetType) {
		return providesController() && Objects.class != targetType && targetType.isAssignableFrom(controllerClass);
	}

	/**
	 * Gives the controller of the I/O manager for a test parameter. It is only
	 * asked for a type the parameter resolver claims, which is the controller's
	 * type or {@link IOTester}.
	 *
	 * @param requestedType the type of the parameter
	 * @param testContext   the context of the test, to name it in the error
	 * @return the controller
	 * @throws SecurityException if the I/O manager does not provide this type
	 */
	public Object resolveControllerFor(Class<?> requestedType, TestContext testContext) {
		if (canProvideControllerFor(requestedType)) {
			return getControllerInstance();
		}
		String provided = providesController() ? nameOf(controllerClass) : localized("io_tester.no_controller"); //$NON-NLS-1$
		throw new SecurityException(localized("io_tester.parameter_not_provided", testNameOf(testContext), //$NON-NLS-1$
				nameOf(requestedType), nameOf(ioManagerClass), provided, nameOf(DEFAULT_IO_MANAGER)));
	}

	/**
	 * Names a test as its class and method, joined by {@code #}, or by its display
	 * name when either is unknown.
	 *
	 * @param testContext the context of the test
	 * @return the name of the test
	 */
	private static String testNameOf(TestContext testContext) {
		var testClass = testContext.testClass();
		var testMethod = testContext.testMethod();
		if (testClass.isPresent() && testMethod.isPresent()) {
			return nameOf(testClass.get()) + "#" + testMethod.map(Method::getName).get(); //$NON-NLS-1$
		}
		return testContext.displayName().orElse("?"); //$NON-NLS-1$
	}

	/**
	 * Names a class as it is written in Java source, or by its binary name when it
	 * has no such name, as for an anonymous class.
	 *
	 * @param type the class
	 * @return the name of the class
	 */
	private static String nameOf(Class<?> type) {
		return Optional.ofNullable(type.getCanonicalName()).orElseGet(type::getName);
	}

	/**
	 * Finds the I/O manager class the test names with {@link WithIOManager}, or the
	 * default one.
	 *
	 * @param testContext the context of the test
	 * @return the I/O manager class
	 */
	private static Class<? extends IOManager<?>> ioManagerClassFor(TestContext testContext) {
		return TestContextUtils.findAnnotationIn(testContext, WithIOManager.class)
				.<Class<? extends IOManager<?>>>map(WithIOManager::value).orElse(DEFAULT_IO_MANAGER);
	}

	/**
	 * Builds a fast factory for the public constructor without parameters of an I/O
	 * manager class.
	 *
	 * @param ioManagerClass the I/O manager class
	 * @return the factory
	 * @throws SecurityException if the class has no such constructor
	 */
	private static Supplier<IOManager<?>> generateIOManagerSupplier(Class<? extends IOManager<?>> ioManagerClass) {
		try {
			var lookup = MethodHandles.lookup();
			var contructor = lookup.findConstructor(ioManagerClass, methodType(void.class));
			var factory = LambdaMetafactory
					.metafactory(lookup, "get", methodType(Supplier.class), contructor.type().generic(), contructor, //$NON-NLS-1$
							contructor.type())
					.getTarget();
			return (Supplier<IOManager<?>>) factory.invokeExact();
		} catch (Throwable e) {
			throw new SecurityException(localized("io_tester.manager_creation_failed", nameOf(ioManagerClass)), e); //$NON-NLS-1$
		}
	}
}
