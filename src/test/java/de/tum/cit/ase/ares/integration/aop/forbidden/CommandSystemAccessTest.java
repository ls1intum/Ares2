package de.tum.cit.ase.ares.integration.aop.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.processBuilder.ProcessBuilderExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.runtime.RuntimeExecuteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.redirect.RedirectMain;

/**
 * Integration tests to verify that command system execution is forbidden when
 * trying to execute commands that are not allowed by the security policy, even
 * when some commands are permitted.
 */
class CommandSystemAccessTest extends SystemAccessTest {

	private static final String RUNTIME_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/runtime";
	private static final String PROCESS_BUILDER_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/execute/processBuilder";

	/** Folder of the student code redirecting an allowed command. */
	private static final String REDIRECT_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/commandSystem/redirect";

	// <editor-fold desc="accessCommandSystemViaRuntime">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeMavenArchunitAspectJ() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime, RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime, RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeMavenWalaAspectJ() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime, RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeMavenWalaInstrumentation() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntime, RuntimeExecuteMain.class);
	}
	// </editor-fold>

	// <editor-fold desc="accessCommandSystemViaRuntimeWithArgs">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeWithArgsMavenArchunitAspectJ() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeWithArgs,
				RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeWithArgsMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeWithArgs,
				RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeWithArgsMavenWalaAspectJ() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeWithArgs,
				RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeWithArgsMavenWalaInstrumentation() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeWithArgs,
				RuntimeExecuteMain.class);
	}
	// </editor-fold>

	// <editor-fold desc="accessCommandSystemViaRuntimeArray">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeArrayMavenArchunitAspectJ() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeArray, RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeArrayMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeArray, RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeArrayMavenWalaAspectJ() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeArray, RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeArrayMavenWalaInstrumentation() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeArray, RuntimeExecuteMain.class);
	}
	// </editor-fold>

	// <editor-fold desc="accessCommandSystemViaRuntimeArrayWithArgs">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeArrayWithArgsMavenArchunitAspectJ() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeArrayWithArgs,
				RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeArrayWithArgsMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeArrayWithArgs,
				RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeArrayWithArgsMavenWalaAspectJ() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeArrayWithArgs,
				RuntimeExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = RUNTIME_WITHIN_PATH)
	void test_executeCommandViaRuntimeArrayWithArgsMavenWalaInstrumentation() {
		assertAresSecurityExceptionCommand(RuntimeExecuteMain::executeCommandViaRuntimeArrayWithArgs,
				RuntimeExecuteMain.class);
	}
	// </editor-fold>

	// <editor-fold desc="accessCommandSystemViaProcessBuilder">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_executeCommandViaProcessBuilderMavenArchunitAspectJ() {
		assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilder,
				ProcessBuilderExecuteMain.class);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_executeCommandViaProcessBuilderMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilder,
				ProcessBuilderExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_executeCommandViaProcessBuilderMavenWalaAspectJ() {
		assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilder,
				ProcessBuilderExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_executeCommandViaProcessBuilderMavenWalaInstrumentation() {
		assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilder,
				ProcessBuilderExecuteMain.class);
	}
	// </editor-fold>

	// <editor-fold desc="accessCommandSystemViaProcessBuilderStartPipeline">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_executeCommandViaProcessBuilderStartPipelineMavenArchunitAspectJ() {
		assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilderStartPipeline,
				ProcessBuilderExecuteMain.class);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_executeCommandViaProcessBuilderStartPipelineMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilderStartPipeline,
				ProcessBuilderExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_executeCommandViaProcessBuilderStartPipelineMavenWalaAspectJ() {
		assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilderStartPipeline,
				ProcessBuilderExecuteMain.class);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = PROCESS_BUILDER_WITHIN_PATH)
	void test_executeCommandViaProcessBuilderStartPipelineMavenWalaInstrumentation() {
		assertAresSecurityExceptionCommand(ProcessBuilderExecuteMain::executeCommandViaProcessBuilderStartPipeline,
				ProcessBuilderExecuteMain.class);
	}
	// </editor-fold>

	// <editor-fold desc="redirects of an allowed command">

	/**
	 * The allowed command is refused when writing its output into a file the policy
	 * does not allow.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_redirectOutputOfAnAllowedCommandMavenArchunitAspectJ() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::redirectOutput);
	}

	/**
	 * The allowed command is refused when appending its output to a file the policy
	 * does not allow.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_appendOutputOfAnAllowedCommandMavenArchunitAspectJ() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::appendOutput);
	}

	/**
	 * The allowed command is refused when reading its input from a forbidden file.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_redirectInputOfAnAllowedCommandMavenArchunitAspectJ() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::redirectInput);
	}

	/**
	 * The allowed command is refused when writing its output into a file the policy
	 * does not allow.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_redirectOutputOfAnAllowedCommandMavenArchunitInstrumentation() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::redirectOutput);
	}

	/**
	 * The allowed command is refused when appending its output to a file the policy
	 * does not allow.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_appendOutputOfAnAllowedCommandMavenArchunitInstrumentation() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::appendOutput);
	}

	/**
	 * The allowed command is refused when reading its input from a forbidden file.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_redirectInputOfAnAllowedCommandMavenArchunitInstrumentation() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::redirectInput);
	}

	/**
	 * The allowed command is refused when writing its output into a file the policy
	 * does not allow.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_redirectOutputOfAnAllowedCommandMavenWalaAspectJ() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::redirectOutput);
	}

	/**
	 * The allowed command is refused when appending its output to a file the policy
	 * does not allow.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_appendOutputOfAnAllowedCommandMavenWalaAspectJ() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::appendOutput);
	}

	/**
	 * The allowed command is refused when reading its input from a forbidden file.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_redirectInputOfAnAllowedCommandMavenWalaAspectJ() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::redirectInput);
	}

	/**
	 * The allowed command is refused when writing its output into a file the policy
	 * does not allow.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_redirectOutputOfAnAllowedCommandMavenWalaInstrumentation() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::redirectOutput);
	}

	/**
	 * The allowed command is refused when appending its output to a file the policy
	 * does not allow.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_appendOutputOfAnAllowedCommandMavenWalaInstrumentation() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::appendOutput);
	}

	/**
	 * The allowed command is refused when reading its input from a forbidden file.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_COMMAND_ALLOWED_EXECUTION, withinPath = REDIRECT_WITHIN_PATH)
	void test_redirectInputOfAnAllowedCommandMavenWalaInstrumentation() {
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, RedirectMain::redirectInput);
	}
	// </editor-fold>
}
