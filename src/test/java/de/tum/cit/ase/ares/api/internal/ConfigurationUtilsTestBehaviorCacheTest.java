package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.JupiterContext;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Checks that a policy's test behaviour is read once per file and test run, so
 * changing or moving the file while tests run changes nothing, and that a
 * method-level policy keeps its own settings.
 */
class ConfigurationUtilsTestBehaviorCacheTest {

	/** The class-level policy, rewritten by the tests. */
	private static final String CLASS_POLICY = "target/test-behavior-cache/ClassPolicy.yaml";

	/** The method-level policy, with a different deadline. */
	private static final String METHOD_POLICY = "target/test-behavior-cache/MethodPolicy.yaml";

	/** Writes both policies before each test. */
	@BeforeEach
	void writePolicies() throws IOException {
		write(CLASS_POLICY, "2002");
		write(METHOD_POLICY, "2007");
	}

	/** A rewrite after the first read changes nothing for the same class. */
	@Test
	void aRewriteAfterTheFirstReadChangesNothing() throws Exception {
		ExtensionContext classContext = classContext();
		JupiterContext context = JupiterContext.of(methodContext(classContext, "classPolicyTest"));

		ConfigurationUtils.prepareTestBehavior(context);
		write(CLASS_POLICY, "2009");

		assertThat(
				ConfigurationUtils.findPolicyVisibilityAndSchedule(context).orElseThrow().effectiveDeadline().getYear())
						.isEqualTo(2002);
	}

	/** A test class of another run reads the file afresh. */
	@Test
	void anotherRunReadsAfresh() throws Exception {
		ConfigurationUtils.prepareTestBehavior(JupiterContext.of(methodContext(classContext(), "classPolicyTest")));
		write(CLASS_POLICY, "2009");

		JupiterContext other = JupiterContext.of(methodContext(classContext(), "classPolicyTest"));
		assertThat(
				ConfigurationUtils.findPolicyVisibilityAndSchedule(other).orElseThrow().effectiveDeadline().getYear())
						.isEqualTo(2009);
	}

	/**
	 * Another test class of the same run keeps the run's first reading, so test
	 * code that rewrites the file cannot change a later class's settings.
	 */
	@Test
	void anotherClassOfTheRunKeepsTheFirstReading() throws Exception {
		ExtensionContext firstClass = classContext();
		ConfigurationUtils.prepareTestBehavior(JupiterContext.of(methodContext(firstClass, "classPolicyTest")));
		write(CLASS_POLICY, "2009");

		JupiterContext other = JupiterContext.of(methodContext(classContextIn(firstClass), "classPolicyTest"));
		assertThat(
				ConfigurationUtils.findPolicyVisibilityAndSchedule(other).orElseThrow().effectiveDeadline().getYear())
						.isEqualTo(2002);
	}

	/** A policy letting tested code overwrite its own file is refused. */
	@Test
	void aWriteGrantOnThePolicyFileIsRefused() throws Exception {
		grantOnTheClassPolicy(CLASS_POLICY, true);
		assertThatThrownBy(() -> ConfigurationUtils
				.testBehavior(JupiterContext.of(methodContext(classContext(), "classPolicyTest"))))
						.isInstanceOf(SecurityException.class).hasMessage(Messages
								.localized("policy.behavior.write.grant.policy", Path.of(CLASS_POLICY), CLASS_POLICY));
	}

	/** A policy letting tested code write in its folder is refused. */
	@Test
	void aWriteGrantOnThePolicyFolderIsRefused() throws Exception {
		grantOnTheClassPolicy(Path.of(CLASS_POLICY).getParent().toString(), true);
		assertThatThrownBy(() -> ConfigurationUtils
				.testBehavior(JupiterContext.of(methodContext(classContext(), "classPolicyTest"))))
						.isInstanceOf(SecurityException.class)
						.hasMessage(Messages.localized("policy.behavior.write.grant.policy", Path.of(CLASS_POLICY),
								Path.of(CLASS_POLICY).getParent().toString()));
	}

	/**
	 * A write grant on a folder that links to the policy's folder is refused, since
	 * Ares compares where paths really lead, as the runtime checks do.
	 *
	 * @throws Exception if the link or the policy cannot be written
	 */
	@Test
	void aWriteGrantOnALinkToThePolicyFolderIsRefused() throws Exception {
		Path link = Path.of("target/test-behavior-cache-link");
		Files.deleteIfExists(link);
		try {
			Files.createSymbolicLink(link, Path.of(CLASS_POLICY).getParent().toAbsolutePath());
		} catch (IOException | UnsupportedOperationException unsupported) {
			Assumptions.abort("This file system cannot create a symbolic link");
		}
		try {
			grantOnTheClassPolicy("target/test-behavior-cache-link", true);
			assertThatThrownBy(() -> ConfigurationUtils
					.testBehavior(JupiterContext.of(methodContext(classContext(), "classPolicyTest"))))
							.isInstanceOf(SecurityException.class)
							.hasMessage(Messages.localized("policy.behavior.write.grant.policy", Path.of(CLASS_POLICY),
									"target/test-behavior-cache-link"));
		} finally {
			Files.deleteIfExists(link);
		}
	}

	/** A write grant elsewhere, or only reading the policy file, is accepted. */
	@Test
	void aWriteGrantElsewhereOrAReadGrantIsAccepted() throws Exception {
		grantOnTheClassPolicy("target/test-behavior-elsewhere", true);
		ConfigurationUtils.testBehavior(JupiterContext.of(methodContext(classContext(), "classPolicyTest")));
		grantOnTheClassPolicy(CLASS_POLICY, false);
		ConfigurationUtils.testBehavior(JupiterContext.of(methodContext(classContext(), "classPolicyTest")));
	}

	/**
	 * Gives the class policy one file rule on a path, reading and, if asked,
	 * overwriting it.
	 *
	 * @param path      the path of the rule.
	 * @param overwrite whether the rule lets the tested code overwrite files.
	 * @throws IOException if the policy cannot be rewritten
	 */
	private static void grantOnTheClassPolicy(String path, boolean overwrite) throws IOException {
		Path file = Path.of(CLASS_POLICY);
		Files.writeString(file, Files.readString(file).replace("    regardingFileSystemInteractions: []\n", """
				    regardingFileSystemInteractions:
				      - onThisPathAndAllPathsBelow: "%s"
				        readAllFiles: true
				        overwriteAllFiles: %s
				        createAllFiles: false
				        executeAllFiles: false
				        deleteAllFiles: false
				""".formatted(path, overwrite)));
	}

	/** A method-level policy is read up front and keeps its own settings. */
	@Test
	void aMethodLevelPolicyKeepsItsOwnSettings() throws Exception {
		ExtensionContext classContext = classContext();
		ConfigurationUtils.prepareTestBehavior(JupiterContext.of(methodContext(classContext, "classPolicyTest")));
		write(METHOD_POLICY, "2009");

		JupiterContext methodPolicy = JupiterContext.of(methodContext(classContext, "methodPolicyTest"));
		assertThat(ConfigurationUtils.findPolicyVisibilityAndSchedule(methodPolicy).orElseThrow().effectiveDeadline()
				.getYear()).isEqualTo(2007);
	}

	/**
	 * Another test class of the same run keeps the run's reading after the file was
	 * moved away, since the earlier reading is used before the file is looked for.
	 */
	@Test
	void anotherClassOfTheRunKeepsTheReadingOfAMovedFile() throws Exception {
		ExtensionContext firstClass = classContext();
		ConfigurationUtils.prepareTestBehavior(JupiterContext.of(methodContext(firstClass, "classPolicyTest")));
		Files.move(Path.of(CLASS_POLICY), Path.of(CLASS_POLICY + ".moved"), StandardCopyOption.REPLACE_EXISTING);

		JupiterContext other = JupiterContext.of(methodContext(classContextIn(firstClass), "classPolicyTest"));
		assertThat(
				ConfigurationUtils.findPolicyVisibilityAndSchedule(other).orElseThrow().effectiveDeadline().getYear())
						.isEqualTo(2002);
	}

	/** A deactivated policy contributes no test behaviour. */
	@Test
	void aDeactivatedPolicyContributesNothing() throws Exception {
		JupiterContext context = JupiterContext.of(methodContext(classContext(), "deactivatedPolicyTest"));
		assertThat(ConfigurationUtils.findPolicyVisibilityAndSchedule(context)).isEmpty();
	}

	/**
	 * Writes a policy whose deadline falls in the given year.
	 *
	 * @param path where to write it.
	 * @param year the deadline's year.
	 */
	private static void write(String path, String year) throws IOException {
		Path file = Path.of(path);
		Files.createDirectories(file.getParent());
		Files.writeString(file, """
				thisPolicyFileCompliesToThePolicyVersion: 2
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions: []
				    regardingNetworkConnections: []
				    regardingCommandExecutions: []
				    regardingThreadCreations: []
				    regardingPackageImports: []
				    regardingTimeouts: []
				  theFollowingTestBehaviorIsConfigured:
				    regardingTheVisibilityAndScheduleOfTheTestCases:
				      theDeadlineIs: "%s-01-01 00:00 UTC"
				      testsNotListedHereAreHidden: false
				""".formatted(year));
	}

	/**
	 * A class context of {@link CacheFixture} with a working store.
	 *
	 * @return the context.
	 */
	private static ExtensionContext classContext() {
		ExtensionContext classContext = mock(ExtensionContext.class);
		Map<Object, Object> values = new HashMap<>();
		ExtensionContext.Store store = mock(ExtensionContext.Store.class);
		when(store.getOrComputeIfAbsent(any(), any(), any())).thenAnswer(call -> {
			Function<Object, Object> creator = call.getArgument(1);
			return values.computeIfAbsent(call.getArgument(0), creator);
		});
		when(store.get(any(), any())).thenAnswer(call -> values.get(call.getArgument(0)));
		when(classContext.getStore(any())).thenReturn(store);
		when(classContext.getTestClass()).thenReturn(Optional.of(CacheFixture.class));
		when(classContext.getTestMethod()).thenReturn(Optional.empty());
		when(classContext.getElement()).thenReturn(Optional.of(CacheFixture.class));
		when(classContext.getParent()).thenReturn(Optional.empty());
		when(classContext.getRoot()).thenReturn(classContext);
		return classContext;
	}

	/**
	 * A class context of {@link CacheFixture} in the run of another class context.
	 *
	 * @param sameRun a class context of the run.
	 * @return the context.
	 */
	private static ExtensionContext classContextIn(ExtensionContext sameRun) {
		ExtensionContext classContext = classContext();
		when(classContext.getRoot()).thenReturn(sameRun);
		return classContext;
	}

	/**
	 * A method context below a class context.
	 *
	 * @param classContext the class context.
	 * @param methodName   the fixture method.
	 * @return the context.
	 */
	private static ExtensionContext methodContext(ExtensionContext classContext, String methodName)
			throws NoSuchMethodException {
		Method method = CacheFixture.class.getDeclaredMethod(methodName);
		ExtensionContext methodContext = mock(ExtensionContext.class);
		when(methodContext.getTestClass()).thenReturn(Optional.of(CacheFixture.class));
		when(methodContext.getTestMethod()).thenReturn(Optional.of(method));
		when(methodContext.getElement()).thenReturn(Optional.of(method));
		when(methodContext.getParent()).thenReturn(Optional.of(classContext));
		return methodContext;
	}

	/** The test class whose policies are read. */
	@Policy(CLASS_POLICY)
	static final class CacheFixture {

		/** Uses the class policy. */
		void classPolicyTest() {
		}

		/** Uses its own policy. */
		@Policy(METHOD_POLICY)
		void methodPolicyTest() {
		}

		/** Uses a deactivated policy. */
		@Policy(value = CLASS_POLICY, activated = false)
		void deactivatedPolicyTest() {
		}
	}
}
