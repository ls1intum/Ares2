package de.tum.cit.ase.ares.api.policy.reader.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceLimitsPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityPolicyPreset;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

class SecurityPolicyPresetResolverTest {

	private final YAMLMapper yamlMapper = new YAMLMapper();

	private static ResourceAccesses ownResourceAccesses() {
		return new ResourceAccesses(
				List.of(new FilePermission("policy-own-marker.txt", true, false, false, false, false)), List.of(),
				List.of(), List.of(), List.of(), List.of(new ResourceLimitsPermission(3000)));
	}

	@Test
	void returnsThePolicyUnchangedWhenNoPresetIsConfigured() {
		SupervisedCode supervisedCode = new SupervisedCode(
				ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, null, null, List.of(),
				ownResourceAccesses());
		SecurityPolicy policy = new SecurityPolicy(SecurityPolicy.CURRENT_POLICY_VERSION, supervisedCode);

		SecurityPolicy result = SecurityPolicyPresetResolver.resolveAndMerge(policy, yamlMapper);

		assertThat(result).isSameAs(policy);
	}

	@Test
	void concatenatesFileSystemAndTimeoutPermissionsFromBothSources() {
		SupervisedCode supervisedCode = new SupervisedCode(
				ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, null, null, List.of(),
				ownResourceAccesses());
		SecurityPolicy policy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode)
				.basedOnTheFollowingPreset(SecurityPolicyPreset.SMOKE_TEST).build();

		SecurityPolicy merged = SecurityPolicyPresetResolver.resolveAndMerge(policy, yamlMapper);

		List<FilePermission> mergedFilePermissions = merged.regardingTheSupervisedCode()
				.theFollowingResourceAccessesArePermitted().regardingFileSystemInteractions();
		assertThat(mergedFilePermissions).extracting(FilePermission::onThisPathAndAllPathsBelow)
				.containsExactlyInAnyOrder(
						"src/test/java/de/tum/cit/ase/ares/integration/testuser/subject/presetMerge/smoke-test-marker.txt",
						"policy-own-marker.txt");

		List<ResourceLimitsPermission> mergedTimeouts = merged.regardingTheSupervisedCode()
				.theFollowingResourceAccessesArePermitted().regardingTimeouts();
		assertThat(mergedTimeouts).extracting(ResourceLimitsPermission::timeout).containsExactlyInAnyOrder(5000L,
				3000L);
	}

	@Test
	void concatenatesTestClassesAdditively() {
		SupervisedCode supervisedCode = new SupervisedCode(
				ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, null, null,
				List.of("policy.own.PolicyOwnTest"), ownResourceAccesses());
		SecurityPolicy policy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode)
				.basedOnTheFollowingPreset(SecurityPolicyPreset.SMOKE_TEST).build();

		SecurityPolicy merged = SecurityPolicyPresetResolver.resolveAndMerge(policy, yamlMapper);

		assertThat(merged.regardingTheSupervisedCode().theFollowingClassesAreTestClasses())
				.containsExactlyInAnyOrder("smoke.test.preset.SmokeTestPresetTest", "policy.own.PolicyOwnTest");
	}

	@Test
	void neverTakesThePackageOrMainClassFromThePreset() {
		SupervisedCode supervisedCode = new SupervisedCode(
				ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, null, null, List.of(),
				ownResourceAccesses());
		SecurityPolicy policy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode)
				.basedOnTheFollowingPreset(SecurityPolicyPreset.SMOKE_TEST).build();

		SecurityPolicy merged = SecurityPolicyPresetResolver.resolveAndMerge(policy, yamlMapper);

		assertThat(merged.regardingTheSupervisedCode().theSupervisedCodeUsesTheFollowingPackage()).isNull();
		assertThat(merged.regardingTheSupervisedCode().theMainClassInsideThisPackageIs()).isNull();
	}

	@Test
	void keepsThePoliciesOwnTestBehaviorConfiguration() {
		TestBehaviorConfiguration testBehavior = new TestBehaviorConfiguration();
		SupervisedCode supervisedCode = new SupervisedCode(
				ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, null, null, List.of(),
				ownResourceAccesses(), testBehavior);
		SecurityPolicy policy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode)
				.basedOnTheFollowingPreset(SecurityPolicyPreset.SMOKE_TEST).build();

		SecurityPolicy merged = SecurityPolicyPresetResolver.resolveAndMerge(policy, yamlMapper);

		assertThat(merged.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured()).isEqualTo(testBehavior);
	}

	@Test
	void ignoresAPresetCopyShadowedOnTheTestClasspath() {
		SupervisedCode supervisedCode = new SupervisedCode(
				ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, null, null, List.of(),
				ownResourceAccesses());
		SecurityPolicy policy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode)
				.basedOnTheFollowingPreset(SecurityPolicyPreset.SMOKE_TEST).build();

		SecurityPolicy merged = SecurityPolicyPresetResolver.resolveAndMerge(policy, yamlMapper);

		assertThat(merged.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted()
				.regardingFileSystemInteractions()).extracting(FilePermission::onThisPathAndAllPathsBelow)
						.doesNotContain("shadow-grant.txt");
		assertThat(merged.regardingTheSupervisedCode().theFollowingClassesAreTestClasses())
				.doesNotContain("shadow.ShadowTest");
	}

	@Test
	void keepsThePoliciesOwnPackageAndMainClassWhenProvided() {
		SupervisedCode supervisedCode = new SupervisedCode(
				ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, "policy.own.pkg",
				"PolicyOwnMain", List.of(), ownResourceAccesses());
		SecurityPolicy policy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode)
				.basedOnTheFollowingPreset(SecurityPolicyPreset.SMOKE_TEST).build();

		SecurityPolicy merged = SecurityPolicyPresetResolver.resolveAndMerge(policy, yamlMapper);

		assertThat(merged.regardingTheSupervisedCode().theSupervisedCodeUsesTheFollowingPackage())
				.isEqualTo("policy.own.pkg");
		assertThat(merged.regardingTheSupervisedCode().theMainClassInsideThisPackageIs()).isEqualTo("PolicyOwnMain");
	}

	@Test
	void missingPresetResourceFailsClosed() {
		SecurityException exception = assertThrows(SecurityException.class,
				() -> SecurityPolicyPresetResolver.readPresetResource("does-not-exist.yaml", yamlMapper));
		assertThat(exception.getMessage()).isEqualTo(Messages.localized("security.policy.preset.resource.missing",
				"/de/tum/cit/ase/ares/api/policy/presets/does-not-exist.yaml"));
	}

	@Test
	void presetResourceOnlyOutsideAresFailsClosed() {
		SecurityException exception = assertThrows(SecurityException.class,
				() -> SecurityPolicyPresetResolver.readPresetResource("self-referencing.yaml", yamlMapper));
		assertThat(exception.getMessage()).isEqualTo(Messages.localized("security.policy.preset.resource.missing",
				"/de/tum/cit/ase/ares/api/policy/presets/self-referencing.yaml"));
	}
}
