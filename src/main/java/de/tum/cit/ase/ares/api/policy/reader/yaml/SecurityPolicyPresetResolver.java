package de.tum.cit.ase.ares.api.policy.reader.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityPolicyPreset;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

/**
 * Resolves and merges a policy's referenced preset, entirely at read time.
 * <p>
 * Description: When a policy names a preset, its own bundled resource is
 * loaded, validated, and bound the same way any policy is, then merged with the
 * referencing policy into a single result — every list-typed permission field
 * is concatenated (relying on that field's own existing multi-entry resolution
 * to combine correctly, e.g. an OR-across-entries allow-list or a
 * minimum-of-entries timeout), every other field falls back to the preset's
 * value only when the policy leaves it unset.
 * <p>
 * Design Rationale: Doing this inside the reader means neither the precompile
 * write spine nor the postcompile execute spine needs any awareness that
 * presets exist — both already only ever consume a fully-resolved
 * {@link SecurityPolicy}.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
final class SecurityPolicyPresetResolver {

	@Nonnull
	private static final String PRESET_RESOURCE_BASE_PATH = "/de/tum/cit/ase/ares/api/policy/presets/";

	private SecurityPolicyPresetResolver() {
		throw new UnsupportedOperationException("SecurityPolicyPresetResolver is a utility class");
	}

	/**
	 * Returns {@code policy} unchanged when it names no preset, or the result of
	 * merging its named preset with {@code policy} otherwise.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @param policy       the already-bound policy; must not be null.
	 * @param objectMapper the mapper to read the preset resource with; must not be
	 *                     null.
	 * @return the policy to use, with any named preset already merged in.
	 */
	@Nonnull
	static SecurityPolicy resolveAndMerge(@Nonnull SecurityPolicy policy, @Nonnull ObjectMapper objectMapper) {
		SecurityPolicyPreset preset = policy.basedOnTheFollowingPreset();
		if (preset == null) {
			return policy;
		}
		SecurityPolicy presetPolicy = readPreset(preset, objectMapper);
		return merge(presetPolicy, policy);
	}

	@Nonnull
	private static SecurityPolicy readPreset(@Nonnull SecurityPolicyPreset preset, @Nonnull ObjectMapper objectMapper) {
		return readPresetResource(preset.resourceFileName(), objectMapper);
	}

	/**
	 * Reads and validates a preset resource by file name directly, without going
	 * through a {@link SecurityPolicyPreset} constant — package-visible so a test
	 * can exercise the missing/unreadable-resource fail-closed path with a
	 * deliberately absent file name, which no real, shipped enum constant can
	 * exhibit by construction.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @param resourceFileName the resource file name under {@code policy/presets/};
	 *                         must not be null.
	 * @param objectMapper     the mapper to read the resource with; must not be
	 *                         null.
	 * @return the bound {@link SecurityPolicy} the resource resolves to.
	 */
	@Nonnull
	static SecurityPolicy readPresetResource(@Nonnull String resourceFileName, @Nonnull ObjectMapper objectMapper) {
		String resourcePath = PRESET_RESOURCE_BASE_PATH + resourceFileName;
		try (InputStream resourceStream = SecurityPolicyPresetResolver.class.getResourceAsStream(resourcePath)) {
			if (resourceStream == null) {
				throw new SecurityException(
						Messages.localized("security.policy.preset.resource.missing", resourcePath));
			}
			JsonNode presetRoot = objectMapper.readTree(resourceStream);
			SecurityPolicySchemaValidator.validate(presetRoot, false);
			SecurityPolicy presetPolicy = objectMapper.treeToValue(presetRoot, SecurityPolicy.class);
			if (presetPolicy == null) {
				throw new SecurityException(
						Messages.localized("security.policy.preset.resource.invalid", resourcePath));
			}
			return presetPolicy;
		} catch (DatabindException e) {
			throw new SecurityException(Messages.localized("security.policy.preset.resource.invalid", resourcePath), e);
		} catch (IOException e) {
			throw new SecurityException(Messages.localized("security.policy.preset.resource.missing", resourcePath), e);
		}
	}

	@Nonnull
	private static SecurityPolicy merge(@Nonnull SecurityPolicy presetPolicy, @Nonnull SecurityPolicy policy) {
		SupervisedCode mergedSupervisedCode = mergeSupervisedCode(presetPolicy.regardingTheSupervisedCode(),
				policy.regardingTheSupervisedCode());
		return new SecurityPolicy(policy.thisPolicyFileCompliesToThePolicyVersion(), mergedSupervisedCode,
				policy.basedOnTheFollowingPreset());
	}

	@Nonnull
	private static SupervisedCode mergeSupervisedCode(@Nonnull SupervisedCode presetSupervisedCode,
			@Nonnull SupervisedCode policySupervisedCode) {
		String supervisedPackage = policySupervisedCode.theSupervisedCodeUsesTheFollowingPackage() != null
				? policySupervisedCode.theSupervisedCodeUsesTheFollowingPackage()
				: presetSupervisedCode.theSupervisedCodeUsesTheFollowingPackage();
		String mainClass = policySupervisedCode.theMainClassInsideThisPackageIs() != null
				? policySupervisedCode.theMainClassInsideThisPackageIs()
				: presetSupervisedCode.theMainClassInsideThisPackageIs();
		List<String> mergedTestClasses = concatenate(presetSupervisedCode.theFollowingClassesAreTestClasses(),
				policySupervisedCode.theFollowingClassesAreTestClasses());
		ResourceAccesses mergedResourceAccesses = mergeResourceAccesses(
				presetSupervisedCode.theFollowingResourceAccessesArePermitted(),
				policySupervisedCode.theFollowingResourceAccessesArePermitted());
		return new SupervisedCode(policySupervisedCode.theFollowingProgrammingLanguageConfigurationIsUsed(),
				supervisedPackage, mainClass, mergedTestClasses, mergedResourceAccesses);
	}

	@Nonnull
	private static ResourceAccesses mergeResourceAccesses(@Nonnull ResourceAccesses presetResourceAccesses,
			@Nonnull ResourceAccesses policyResourceAccesses) {
		return new ResourceAccesses(
				concatenate(presetResourceAccesses.regardingFileSystemInteractions(),
						policyResourceAccesses.regardingFileSystemInteractions()),
				concatenate(presetResourceAccesses.regardingNetworkConnections(),
						policyResourceAccesses.regardingNetworkConnections()),
				concatenate(presetResourceAccesses.regardingCommandExecutions(),
						policyResourceAccesses.regardingCommandExecutions()),
				concatenate(presetResourceAccesses.regardingThreadCreations(),
						policyResourceAccesses.regardingThreadCreations()),
				concatenate(presetResourceAccesses.regardingPackageImports(),
						policyResourceAccesses.regardingPackageImports()),
				concatenate(presetResourceAccesses.regardingTimeouts(), policyResourceAccesses.regardingTimeouts()));
	}

	@Nonnull
	private static <T> List<T> concatenate(@Nonnull List<T> presetEntries, @Nonnull List<T> policyEntries) {
		List<T> combined = new ArrayList<>(presetEntries);
		combined.addAll(policyEntries);
		return List.copyOf(combined);
	}
}
