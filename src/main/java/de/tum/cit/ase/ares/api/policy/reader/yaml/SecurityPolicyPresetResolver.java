package de.tum.cit.ase.ares.api.policy.reader.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityPolicyPreset;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Resolves and merges a policy's referenced preset, entirely at read time.
 * <p>
 * Description: When a policy names a preset, its own bundled resource is
 * loaded, validated, and bound the same way any policy is, then merged with the
 * referencing policy into a single result — every list-typed permission field
 * is concatenated (relying on that field's own existing multi-entry resolution
 * to combine correctly, e.g. an OR-across-entries allow-list or a
 * minimum-of-entries timeout). The supervised package and main class stay
 * policy-local, so a preset can never move enforcement to another package.
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

	/**
	 * Where the bundled preset files live, relative to the root of the Ares
	 * artifact.
	 */
	@Nonnull
	private static final String PRESET_RESOURCE_BASE_PATH = "de/tum/cit/ase/ares/api/policy/presets/";

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

	/**
	 * Reads the bundled resource of the given preset.
	 *
	 * @param preset       the preset to read; must not be null.
	 * @param objectMapper the mapper to read the resource with; must not be null.
	 * @return the bound {@link SecurityPolicy} the preset resolves to.
	 */
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
		String resourcePath = "/" + PRESET_RESOURCE_BASE_PATH + resourceFileName;
		try (InputStream resourceStream = openFromOwnCodeSource(PRESET_RESOURCE_BASE_PATH + resourceFileName)) {
			JsonNode presetRoot = objectMapper.readTree(resourceStream);
			SecurityPolicySchemaValidator.validate(presetRoot, false);
			SecurityPolicy presetPolicy = objectMapper.treeToValue(presetRoot, SecurityPolicy.class);
			if (presetPolicy == null || pinsPackageOrMainClass(presetPolicy)) {
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

	/**
	 * Tells whether a preset names a supervised package or main class, which only
	 * the referencing policy may do.
	 *
	 * @param presetPolicy the bound preset; must not be null.
	 * @return true if the preset names either one.
	 */
	private static boolean pinsPackageOrMainClass(@Nonnull SecurityPolicy presetPolicy) {
		SupervisedCode presetSupervisedCode = presetPolicy.regardingTheSupervisedCode();
		return presetSupervisedCode.theSupervisedCodeUsesTheFollowingPackage() != null
				|| presetSupervisedCode.theMainClassInsideThisPackageIs() != null;
	}

	/**
	 * Opens a resource inside the Ares artifact itself, ignoring every copy with
	 * the same path elsewhere on the classpath. Exercise classes come before the
	 * Ares JAR on a normal test classpath, so a plain lookup would let a student
	 * file replace a bundled preset.
	 *
	 * @param relativePath the resource path from the artifact root, without a
	 *                     leading slash; must not be null.
	 * @return a stream over the copy inside the Ares artifact.
	 * @throws IOException       if the classpath cannot be searched or read.
	 * @throws SecurityException if the Ares artifact holds no such resource.
	 */
	@Nonnull
	private static InputStream openFromOwnCodeSource(@Nonnull String relativePath) throws IOException {
		String ownPrefix = ownCodeSourcePrefix();
		ClassLoader loader = SecurityPolicyPresetResolver.class.getClassLoader();
		if (ownPrefix != null && loader != null) {
			Enumeration<URL> candidates = loader.getResources(relativePath);
			while (candidates.hasMoreElements()) {
				URL candidate = candidates.nextElement();
				if (candidate.toExternalForm().equals(ownPrefix + relativePath)) {
					return candidate.openStream();
				}
			}
		}
		throw new SecurityException(Messages.localized("security.policy.preset.resource.missing", "/" + relativePath));
	}

	/**
	 * Returns the URL prefix every resource inside the Ares artifact starts with:
	 * the directory itself, or {@code jar:<file>!/} for a JAR.
	 *
	 * @return the prefix, or null when the location of Ares is unknown.
	 */
	@Nullable
	private static String ownCodeSourcePrefix() {
		CodeSource codeSource = SecurityPolicyPresetResolver.class.getProtectionDomain().getCodeSource();
		if (codeSource == null || codeSource.getLocation() == null) {
			return null;
		}
		String location = codeSource.getLocation().toExternalForm();
		return location.endsWith("/") ? location : "jar:" + location + "!/";
	}

	/**
	 * Merges a preset with the policy that references it.
	 *
	 * @param presetPolicy the bound preset; must not be null.
	 * @param policy       the referencing policy; must not be null.
	 * @return the merged policy.
	 */
	@Nonnull
	private static SecurityPolicy merge(@Nonnull SecurityPolicy presetPolicy, @Nonnull SecurityPolicy policy) {
		SupervisedCode mergedSupervisedCode = mergeSupervisedCode(presetPolicy.regardingTheSupervisedCode(),
				policy.regardingTheSupervisedCode());
		return new SecurityPolicy(policy.thisPolicyFileCompliesToThePolicyVersion(), mergedSupervisedCode,
				policy.basedOnTheFollowingPreset());
	}

	/**
	 * Merges the supervised-code sections. The package, main class, language and
	 * test behaviour always come from the policy; only the lists are combined.
	 *
	 * @param presetSupervisedCode the preset's section; must not be null.
	 * @param policySupervisedCode the policy's section; must not be null.
	 * @return the merged section.
	 */
	@Nonnull
	private static SupervisedCode mergeSupervisedCode(@Nonnull SupervisedCode presetSupervisedCode,
			@Nonnull SupervisedCode policySupervisedCode) {
		TestBehaviorConfiguration testBehavior = policySupervisedCode.theFollowingTestBehaviorIsConfigured();
		List<String> mergedTestClasses = concatenate(presetSupervisedCode.theFollowingClassesAreTestClasses(),
				policySupervisedCode.theFollowingClassesAreTestClasses());
		ResourceAccesses mergedResourceAccesses = mergeResourceAccesses(
				presetSupervisedCode.theFollowingResourceAccessesArePermitted(),
				policySupervisedCode.theFollowingResourceAccessesArePermitted());
		return new SupervisedCode(policySupervisedCode.theFollowingProgrammingLanguageConfigurationIsUsed(),
				policySupervisedCode.theSupervisedCodeUsesTheFollowingPackage(),
				policySupervisedCode.theMainClassInsideThisPackageIs(), mergedTestClasses, mergedResourceAccesses,
				testBehavior);
	}

	/**
	 * Concatenates every permission list of the preset and the policy.
	 *
	 * @param presetResourceAccesses the preset's permissions; must not be null.
	 * @param policyResourceAccesses the policy's permissions; must not be null.
	 * @return the combined permissions.
	 */
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

	/**
	 * Returns the preset entries followed by the policy entries.
	 *
	 * @param <T>           the entry type.
	 * @param presetEntries the preset's entries; must not be null.
	 * @param policyEntries the policy's entries; must not be null.
	 * @return an unmodifiable combined list.
	 */
	@Nonnull
	private static <T> List<T> concatenate(@Nonnull List<T> presetEntries, @Nonnull List<T> policyEntries) {
		List<T> combined = new ArrayList<>(presetEntries);
		combined.addAll(policyEntries);
		return List.copyOf(combined);
	}
}
