package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.JupiterContext;

/**
 * Checks that a policy's test behaviour is read once per file and test class,
 * so changing the file while tests run changes nothing, and that a method-level
 * policy keeps its own settings.
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

	/** Another test class reads the file afresh. */
	@Test
	void anotherClassReadsAfresh() throws Exception {
		ConfigurationUtils.prepareTestBehavior(JupiterContext.of(methodContext(classContext(), "classPolicyTest")));
		write(CLASS_POLICY, "2009");

		JupiterContext other = JupiterContext.of(methodContext(classContext(), "classPolicyTest"));
		assertThat(
				ConfigurationUtils.findPolicyVisibilityAndSchedule(other).orElseThrow().effectiveDeadline().getYear())
						.isEqualTo(2009);
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
				thisPolicyFileCompliesToThePolicyVersion: 1
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
		when(classContext.getStore(any())).thenReturn(store);
		when(classContext.getTestClass()).thenReturn(Optional.of(CacheFixture.class));
		when(classContext.getTestMethod()).thenReturn(Optional.empty());
		when(classContext.getElement()).thenReturn(Optional.of(CacheFixture.class));
		when(classContext.getParent()).thenReturn(Optional.empty());
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
			// Read through the mocked context only.
		}

		/** Uses its own policy. */
		@Policy(METHOD_POLICY)
		void methodPolicyTest() {
			// Read through the mocked context only.
		}

		/** Uses a deactivated policy. */
		@Policy(value = CLASS_POLICY, activated = false)
		void deactivatedPolicyTest() {
			// Read through the mocked context only.
		}
	}
}
