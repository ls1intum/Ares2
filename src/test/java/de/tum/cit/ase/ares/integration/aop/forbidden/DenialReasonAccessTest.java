package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.runtime.RuntimeExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.byteStream.FileinputStreamMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.networkSystem.connect.socket.SocketConnectMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executorService.CreateExecutorServiceMain;

/**
 * Integration tests that verify the informative denial-reason suffix of Ares
 * {@code SecurityException} messages end-to-end (real policy, weaving and
 * subject execution), for the {@code NOT_PERMITTED} reason: a rule is
 * configured for the resource type, but it does not cover the requested
 * resource.
 * <p>
 * Each scenario is exercised across all four runner variants (ArchUnit/WALA x
 * AspectJ/Instrumentation) so both enforcement paths are covered. The network
 * subsystem is covered only for the AspectJ variants: in the instrumentation
 * path the runtime layer does not intercept {@code java.net.Socket}
 * connections, so a network denial-reason cannot be observed end-to-end there.
 * <p>
 * The {@code NO_ALLOWLIST} reason (no rule configured at all) is intentionally
 * not covered here: with an empty permission list the static architecture layer
 * (ArchUnit/WALA) rejects the forbidden capability before the runtime AOP layer
 * runs, so the denial-reason suffix is never produced end-to-end. That reason
 * is covered at the toolbox/unit level instead (see
 * {@code JavaInstrumentationAdvice*SystemToolboxTest}).
 */
class DenialReasonAccessTest extends SystemAccessTest {

	// <editor-fold desc="Within Paths">
	private static final String FILE_READ_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/ByteStream";
	private static final String COMMAND_RUNTIME_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/runtime";
	private static final String THREAD_EXECUTOR_SERVICE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/executorService";
	private static final String NETWORK_SOCKET_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/networkSystem/connect/socket";
	// </editor-fold>

	// <editor-fold desc="File System - NOT_PERMITTED">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READ_WITHIN_PATH)
	void test_fileReadNotPermittedMavenArchunitAspectJ() {
		SecurityException exception = assertAresSecurityExceptionRead(
				FileinputStreamMain::accessFileSystemViaFileInputStream, FileinputStreamMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READ_WITHIN_PATH)
	void test_fileReadNotPermittedMavenArchunitInstrumentation() {
		SecurityException exception = assertAresSecurityExceptionRead(
				FileinputStreamMain::accessFileSystemViaFileInputStream, FileinputStreamMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READ_WITHIN_PATH)
	void test_fileReadNotPermittedMavenWalaAspectJ() {
		SecurityException exception = assertAresSecurityExceptionRead(
				FileinputStreamMain::accessFileSystemViaFileInputStream, FileinputStreamMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = FILE_READ_WITHIN_PATH)
	void test_fileReadNotPermittedMavenWalaInstrumentation() {
		SecurityException exception = assertAresSecurityExceptionRead(
				FileinputStreamMain::accessFileSystemViaFileInputStream, FileinputStreamMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}
	// </editor-fold>

	// <editor-fold desc="Command System - NOT_PERMITTED">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = COMMAND_RUNTIME_WITHIN_PATH)
	void test_commandNotPermittedMavenArchunitAspectJ() {
		SecurityException exception = assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime,
				RuntimeExecuteMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = COMMAND_RUNTIME_WITHIN_PATH)
	void test_commandNotPermittedMavenArchunitInstrumentation() {
		SecurityException exception = assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime,
				RuntimeExecuteMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = COMMAND_RUNTIME_WITHIN_PATH)
	void test_commandNotPermittedMavenWalaAspectJ() {
		SecurityException exception = assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime,
				RuntimeExecuteMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = COMMAND_RUNTIME_WITHIN_PATH)
	void test_commandNotPermittedMavenWalaInstrumentation() {
		SecurityException exception = assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime,
				RuntimeExecuteMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}
	// </editor-fold>

	// <editor-fold desc="Thread System - NOT_PERMITTED">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_EXECUTOR_SERVICE_WITHIN_PATH)
	void test_threadNotPermittedMavenArchunitAspectJ() {
		SecurityException exception = assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitCallable,
				CreateExecutorServiceMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_EXECUTOR_SERVICE_WITHIN_PATH)
	void test_threadNotPermittedMavenArchunitInstrumentation() {
		SecurityException exception = assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitCallable,
				CreateExecutorServiceMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_EXECUTOR_SERVICE_WITHIN_PATH)
	void test_threadNotPermittedMavenWalaAspectJ() {
		SecurityException exception = assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitCallable,
				CreateExecutorServiceMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_THREAD_ALLOWED_CREATION, withinPath = THREAD_EXECUTOR_SERVICE_WITHIN_PATH)
	void test_threadNotPermittedMavenWalaInstrumentation() {
		SecurityException exception = assertAresSecurityExceptionThread(CreateExecutorServiceMain::submitCallable,
				CreateExecutorServiceMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}
	// </editor-fold>

	// <editor-fold desc="Network System - NOT_PERMITTED (AspectJ only)">
	// The instrumentation path does not intercept java.net.Socket connections, so
	// network denial-reason coverage is limited to the AspectJ variants here and to
	// the toolbox-level test for the instrumentation logic.
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = NETWORK_SOCKET_WITHIN_PATH)
	void test_networkNotPermittedMavenArchunitAspectJ() {
		SecurityException exception = assertAresSecurityExceptionNetwork(SocketConnectMain::connectViaSocket,
				SocketConnectMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_NETWORK_CONNECTION_ALLOWED, withinPath = NETWORK_SOCKET_WITHIN_PATH)
	void test_networkNotPermittedMavenWalaAspectJ() {
		SecurityException exception = assertAresSecurityExceptionNetwork(SocketConnectMain::connectViaSocket,
				SocketConnectMain.class);
		assertDenialReason(exception.getMessage(), DenialReason.NOT_PERMITTED);
	}
	// </editor-fold>
}
