package de.tum.cit.ase.ares.integration.aop.forbidden;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.nativeLibrary.LibraryCallbackRunner;

/**
 * Asks for a JDK native library by name from test code run beneath student
 * code, through every way of calling the JDK's load method, and requires Ares
 * to refuse each request at run time. The static analysis only reads the
 * student code, so it never sees these requests.
 */
class NativeLibraryLoadDeniedTest extends SystemAccessTest {

	/**
	 * Folder of the student code the static analysis inspects.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nativeLibrary";

	/**
	 * Policy with one unrelated create path for MavenArchunitAspectJ.
	 */
	private static final String POLICY_ARCHUNITASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyNativeLibraryLoad.yaml";

	/**
	 * Policy with one unrelated create path for MavenArchunitInstrumentation.
	 */
	private static final String POLICY_ARCHUNITINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyNativeLibraryLoad.yaml";

	/**
	 * Policy with one unrelated create path for MavenWalaAspectJ.
	 */
	private static final String POLICY_WALAASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyNativeLibraryLoad.yaml";

	/**
	 * Policy with one unrelated create path for MavenWalaInstrumentation.
	 */
	private static final String POLICY_WALAINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyNativeLibraryLoad.yaml";

	/**
	 * A library the JDK ships, asked for by name.
	 */
	private static final String LIBRARY = "net";

	/**
	 * The name as Ares reads it: a file in the working directory.
	 */
	private static final Path LIBRARY_AS_PATH = Path.of(LIBRARY).toAbsolutePath();

	/**
	 * The JDK method whose check decides every library load in instrumentation
	 * mode, as the refusal names it.
	 */
	private static final String DECIDING_METHOD = "java.lang.Runtime.loadLibrary0(java.lang.Class,java.lang.String)";

	/**
	 * The public entry of {@code System}, as an AspectJ refusal names it.
	 */
	private static final String SYSTEM_ENTRY = "java.lang.System.loadLibrary(java.lang.String)";

	/**
	 * The public entry of {@code Runtime}, as an AspectJ refusal names it.
	 */
	private static final String RUNTIME_ENTRY = "java.lang.Runtime.loadLibrary(java.lang.String)";

	/**
	 * Calls {@code System.loadLibrary} through reflection.
	 */
	private static void loadByReflection() {
		try {
			System.class.getMethod("loadLibrary", String.class).invoke(null, LIBRARY);
		} catch (ReflectiveOperationException failure) {
			throw new IllegalStateException(failure);
		}
	}

	/**
	 * Calls {@code System.loadLibrary} through a method handle.
	 */
	private static void loadByMethodHandle() {
		try {
			MethodHandles.lookup()
					.findStatic(System.class, "loadLibrary", MethodType.methodType(void.class, String.class))
					.invokeWithArguments(LIBRARY);
		} catch (Throwable failure) {
			throw new IllegalStateException(failure);
		}
	}

	/**
	 * Hands a method reference to {@code System.loadLibrary} to a JDK method that
	 * calls it.
	 */
	private static void loadThroughJdkCallback() {
		List.of(LIBRARY).forEach(System::loadLibrary);
	}

	/**
	 * Tells whether a stack frame belongs to the deciding method.
	 */
	private static boolean isDecidingFrame(StackTraceElement frame) {
		return "java.lang.Runtime".equals(frame.getClassName()) && "loadLibrary0".equals(frame.getMethodName());
	}

	/**
	 * Runs the request beneath student code and requires Ares's refusal of an
	 * execute of the library name in the working directory.
	 */
	private SecurityException assertRefused(Runnable request) {
		SecurityException refused = assertThrowsAresSecurityException(() -> LibraryCallbackRunner.run(request));
		assertGeneralErrorMessageWithPath(LIBRARY_AS_PATH, refused.getMessage(), "illegally execute", "illegal execute",
				LibraryCallbackRunner.class);
		return refused;
	}

	/**
	 * Requires the refusal to come from the deciding method's check, in
	 * instrumentation mode.
	 */
	private void assertRefusedAtDecidingMethod(Runnable request) {
		SecurityException refused = assertRefused(request);
		assertTrue(refused.getMessage().contains(DECIDING_METHOD), refused::getMessage);
		assertTrue(Arrays.stream(refused.getStackTrace()).anyMatch(NativeLibraryLoadDeniedTest::isDecidingFrame),
				refused::getMessage);
	}

	/**
	 * Requires the refusal to come from the woven call site of a public entry, in
	 * AspectJ mode, before the JDK runs.
	 */
	private void assertRefusedAtCallSite(Runnable request, String entry) {
		SecurityException refused = assertRefused(request);
		assertTrue(refused.getMessage().contains(entry), refused::getMessage);
		assertFalse(Arrays.stream(refused.getStackTrace()).anyMatch(NativeLibraryLoadDeniedTest::isDecidingFrame),
				refused::getMessage);
	}

	// <editor-fold desc="systemLoadLibrary">
	/**
	 * {@code System.loadLibrary} is refused at the deciding method.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_systemLoadLibraryMavenArchunitInstrumentation() {
		assertRefusedAtDecidingMethod(() -> System.loadLibrary(LIBRARY));
	}

	/**
	 * {@code System.loadLibrary} is refused at the deciding method.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_systemLoadLibraryMavenWalaInstrumentation() {
		assertRefusedAtDecidingMethod(() -> System.loadLibrary(LIBRARY));
	}

	/**
	 * The woven call site of {@code System.loadLibrary} is refused.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_systemLoadLibraryMavenArchunitAspectJ() {
		assertRefusedAtCallSite(() -> System.loadLibrary(LIBRARY), SYSTEM_ENTRY);
	}

	/**
	 * The woven call site of {@code System.loadLibrary} is refused.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_systemLoadLibraryMavenWalaAspectJ() {
		assertRefusedAtCallSite(() -> System.loadLibrary(LIBRARY), SYSTEM_ENTRY);
	}
	// </editor-fold>

	// <editor-fold desc="runtimeLoadLibrary">
	/**
	 * {@code Runtime.loadLibrary} is refused at the deciding method.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_runtimeLoadLibraryMavenArchunitInstrumentation() {
		assertRefusedAtDecidingMethod(() -> Runtime.getRuntime().loadLibrary(LIBRARY));
	}

	/**
	 * {@code Runtime.loadLibrary} is refused at the deciding method.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_runtimeLoadLibraryMavenWalaInstrumentation() {
		assertRefusedAtDecidingMethod(() -> Runtime.getRuntime().loadLibrary(LIBRARY));
	}

	/**
	 * The woven call site of {@code Runtime.loadLibrary} is refused.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_runtimeLoadLibraryMavenArchunitAspectJ() {
		assertRefusedAtCallSite(() -> Runtime.getRuntime().loadLibrary(LIBRARY), RUNTIME_ENTRY);
	}

	/**
	 * The woven call site of {@code Runtime.loadLibrary} is refused.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_runtimeLoadLibraryMavenWalaAspectJ() {
		assertRefusedAtCallSite(() -> Runtime.getRuntime().loadLibrary(LIBRARY), RUNTIME_ENTRY);
	}
	// </editor-fold>

	// <editor-fold desc="indirectLoadLibrary">
	/**
	 * {@code System.loadLibrary} through reflection is refused at the deciding
	 * method.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_reflectiveLoadLibraryMavenArchunitInstrumentation() {
		assertRefusedAtDecidingMethod(NativeLibraryLoadDeniedTest::loadByReflection);
	}

	/**
	 * {@code System.loadLibrary} through reflection is refused at the deciding
	 * method.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_reflectiveLoadLibraryMavenWalaInstrumentation() {
		assertRefusedAtDecidingMethod(NativeLibraryLoadDeniedTest::loadByReflection);
	}

	/**
	 * {@code System.loadLibrary} through a method handle is refused at the deciding
	 * method.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_methodHandleLoadLibraryMavenArchunitInstrumentation() {
		assertRefusedAtDecidingMethod(NativeLibraryLoadDeniedTest::loadByMethodHandle);
	}

	/**
	 * {@code System.loadLibrary} through a method handle is refused at the deciding
	 * method.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_methodHandleLoadLibraryMavenWalaInstrumentation() {
		assertRefusedAtDecidingMethod(NativeLibraryLoadDeniedTest::loadByMethodHandle);
	}

	/**
	 * A method reference to {@code System.loadLibrary} called by a JDK method is
	 * refused at the deciding method.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_methodReferenceLoadLibraryMavenArchunitInstrumentation() {
		assertRefusedAtDecidingMethod(NativeLibraryLoadDeniedTest::loadThroughJdkCallback);
	}

	/**
	 * A method reference to {@code System.loadLibrary} called by a JDK method is
	 * refused at the deciding method.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_methodReferenceLoadLibraryMavenWalaInstrumentation() {
		assertRefusedAtDecidingMethod(NativeLibraryLoadDeniedTest::loadThroughJdkCallback);
	}
	// </editor-fold>
}
