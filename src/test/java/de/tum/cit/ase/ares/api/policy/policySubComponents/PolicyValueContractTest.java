package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.AresConstants;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

class PolicyValueContractTest {
	/**
	 * Checks that a restrictive policy uses the newest supported policy version and
	 * that versions just outside the supported range are rejected, both by the
	 * constructor and by the builder, which shares the constructor's check. A
	 * supported version set through the builder is kept.
	 */
	@Test
	void requiresASupportedPolicyVersion() {
		SecurityPolicy policy = SecurityPolicy
				.createRestrictive(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ);
		assertEquals(AresConstants.MAXIMUM_POLICY_VERSION, policy.thisPolicyFileCompliesToThePolicyVersion());
		assertThrows(IllegalArgumentException.class, () -> new SecurityPolicy(AresConstants.MINIMUM_POLICY_VERSION - 1,
				policy.regardingTheSupervisedCode()));
		assertThrows(IllegalArgumentException.class, () -> new SecurityPolicy(AresConstants.MAXIMUM_POLICY_VERSION + 1,
				policy.regardingTheSupervisedCode()));
		assertThrows(IllegalArgumentException.class, () -> SecurityPolicy.builder()
				.thisPolicyFileCompliesToThePolicyVersion(AresConstants.MAXIMUM_POLICY_VERSION + 1));
		assertThrows(IllegalArgumentException.class, () -> SecurityPolicy.builder()
				.thisPolicyFileCompliesToThePolicyVersion(AresConstants.MINIMUM_POLICY_VERSION - 1));
		SecurityPolicy explicitVersion = SecurityPolicy.builder()
				.thisPolicyFileCompliesToThePolicyVersion(AresConstants.MINIMUM_POLICY_VERSION)
				.regardingTheSupervisedCode(policy.regardingTheSupervisedCode()).build();
		assertEquals(AresConstants.MINIMUM_POLICY_VERSION, explicitVersion.thisPolicyFileCompliesToThePolicyVersion());
	}

	@Test
	void validatesNamesNumericBoundariesAndRestrictiveFactories() {
		assertThrows(NullPointerException.class, () -> new ClassPermission(null));
		assertThrows(IllegalArgumentException.class, () -> new ClassPermission(" "));
		assertThrows(IllegalArgumentException.class, () -> new FilePermission(" ", false, false, false, false, false));
		assertThrows(IllegalArgumentException.class,
				() -> new FilePermission("../outside", false, false, false, false, false));
		assertThrows(IllegalArgumentException.class, () -> new NetworkPermission("host", -1, false, false, false));
		assertEquals(0, new NetworkPermission("host", 0, true, true, true).onThePort());
		assertEquals(65535, new NetworkPermission("host", 65535, true, true, true).onThePort());
		assertThrows(IllegalArgumentException.class, () -> new NetworkPermission("host", 65536, false, false, false));
		assertThrows(IllegalArgumentException.class, () -> new ThreadPermission(-1, "java.lang.Thread"));
		assertEquals(0, new ThreadPermission(0, "java.lang.Thread").createTheFollowingNumberOfThreads());
		assertEquals(Integer.MAX_VALUE,
				new ThreadPermission(Integer.MAX_VALUE, "java.lang.Thread").createTheFollowingNumberOfThreads());
		IllegalArgumentException zeroTimeout = assertThrows(IllegalArgumentException.class,
				() -> new ResourceLimitsPermission(0));
		assertEquals(Messages.localized("policy.permission.timeout.positive"), zeroTimeout.getMessage());
		assertThrows(IllegalArgumentException.class, () -> new ResourceLimitsPermission(-1));
		assertEquals(Long.MAX_VALUE, new ResourceLimitsPermission(Long.MAX_VALUE).timeout());

		FilePermission file = FilePermission.createRestrictive("/tmp");
		assertFalse(file.readAllFiles());
		assertFalse(file.overwriteAllFiles());
		assertFalse(file.createAllFiles());
		assertFalse(file.executeAllFiles());
		assertFalse(file.deleteAllFiles());
		NetworkPermission network = NetworkPermission.createRestrictive("localhost", 0);
		assertFalse(network.openConnections());
		assertFalse(network.sendData());
		assertFalse(network.receiveData());
		assertEquals(0, ThreadPermission.createRestrictive("java.lang.Thread").createTheFollowingNumberOfThreads());
		assertEquals("java.util", PackagePermission.allowPackage("java.util").importTheFollowingPackage());
		assertEquals(List.of(), CommandPermission.allowWithoutArguments("java").withTheseArguments());
	}

	@Test
	void everyResourceCollectionIsDefensivelyCopiedAndUnmodifiable() {
		List<FilePermission> files = new ArrayList<>(List.of(FilePermission.createRestrictive("/tmp")));
		List<NetworkPermission> networks = new ArrayList<>(
				List.of(NetworkPermission.createRestrictive("localhost", 0)));
		List<CommandPermission> commands = new ArrayList<>(List.of(CommandPermission.allowWithoutArguments("java")));
		List<ThreadPermission> threads = new ArrayList<>(
				List.of(ThreadPermission.createRestrictive("java.lang.Thread")));
		List<PackagePermission> packages = new ArrayList<>(List.of(PackagePermission.allowPackage("java.util")));
		List<ResourceLimitsPermission> timeouts = new ArrayList<>(List.of(new ResourceLimitsPermission(1)));
		ResourceAccesses accesses = new ResourceAccesses(files, networks, commands, threads, packages, timeouts);

		files.clear();
		networks.clear();
		commands.clear();
		threads.clear();
		packages.clear();
		timeouts.clear();
		assertEquals(1, accesses.regardingFileSystemInteractions().size());
		assertEquals(1, accesses.regardingNetworkConnections().size());
		assertEquals(1, accesses.regardingCommandExecutions().size());
		assertEquals(1, accesses.regardingThreadCreations().size());
		assertEquals(1, accesses.regardingPackageImports().size());
		assertEquals(1, accesses.regardingTimeouts().size());
		assertThrows(UnsupportedOperationException.class, () -> accesses.regardingFileSystemInteractions().clear());
		assertThrows(UnsupportedOperationException.class, () -> accesses.regardingNetworkConnections().clear());
		assertThrows(UnsupportedOperationException.class, () -> accesses.regardingCommandExecutions().clear());
		assertThrows(UnsupportedOperationException.class, () -> accesses.regardingThreadCreations().clear());
		assertThrows(UnsupportedOperationException.class, () -> accesses.regardingPackageImports().clear());
		assertThrows(UnsupportedOperationException.class, () -> accesses.regardingTimeouts().clear());

		assertThrows(NullPointerException.class,
				() -> new ResourceAccesses(null, List.of(), List.of(), List.of(), List.of(), List.of()));
		assertThrows(NullPointerException.class,
				() -> new ResourceAccesses(java.util.Arrays.asList((FilePermission) null), List.of(), List.of(),
						List.of(), List.of(), List.of()));
		assertThrows(NullPointerException.class,
				() -> new ResourceAccesses(List.of(), null, List.of(), List.of(), List.of(), List.of()));
		assertThrows(NullPointerException.class,
				() -> new ResourceAccesses(List.of(), List.of(), null, List.of(), List.of(), List.of()));
		assertThrows(NullPointerException.class,
				() -> new ResourceAccesses(List.of(), List.of(), List.of(), null, List.of(), List.of()));
		assertThrows(NullPointerException.class,
				() -> new ResourceAccesses(List.of(), List.of(), List.of(), List.of(), null, List.of()));
		assertThrows(NullPointerException.class,
				() -> new ResourceAccesses(List.of(), List.of(), List.of(), List.of(), List.of(), null));
		assertThrows(NullPointerException.class, () -> new ResourceAccesses(List.of(),
				java.util.Arrays.asList((NetworkPermission) null), List.of(), List.of(), List.of(), List.of()));
		assertThrows(NullPointerException.class, () -> new ResourceAccesses(List.of(), List.of(),
				java.util.Arrays.asList((CommandPermission) null), List.of(), List.of(), List.of()));
		assertThrows(NullPointerException.class, () -> new ResourceAccesses(List.of(), List.of(), List.of(),
				java.util.Arrays.asList((ThreadPermission) null), List.of(), List.of()));
		assertThrows(NullPointerException.class, () -> new ResourceAccesses(List.of(), List.of(), List.of(), List.of(),
				java.util.Arrays.asList((PackagePermission) null), List.of()));
		assertThrows(NullPointerException.class, () -> new ResourceAccesses(List.of(), List.of(), List.of(), List.of(),
				List.of(), java.util.Arrays.asList((ResourceLimitsPermission) null)));
	}

	@Test
	void commandAndSupervisedCodeHaveImmutableContentValueSemantics() {
		List<String> arguments = new ArrayList<>(List.of("--version"));
		CommandPermission command = new CommandPermission("java", arguments);
		arguments.clear();
		assertEquals(List.of("--version"), command.withTheseArguments());
		assertThrows(UnsupportedOperationException.class, () -> command.withTheseArguments().add("changed"));
		assertThrows(NullPointerException.class,
				() -> new CommandPermission("java", java.util.Arrays.asList((String) null)));

		List<String> testClasses = new ArrayList<>(List.of("example.PolicyTest"));
		SupervisedCode first = supervisedCode(testClasses);
		testClasses.clear();
		SupervisedCode second = supervisedCode(List.of("example.PolicyTest"));
		assertEquals(List.of("example.PolicyTest"), first.theFollowingClassesAreTestClasses());
		assertThrows(UnsupportedOperationException.class,
				() -> first.theFollowingClassesAreTestClasses().add("changed"));
		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
		assertEquals(1, new HashSet<>(List.of(first, second)).size());
		HashMap<SupervisedCode, String> map = new HashMap<>();
		map.put(first, "first");
		map.put(second, "second");
		assertEquals(1, map.size());
		assertTrue(first.toString().contains("example.PolicyTest"));
		assertEquals(new SecurityPolicy(AresConstants.MAXIMUM_POLICY_VERSION, first),
				new SecurityPolicy(AresConstants.MAXIMUM_POLICY_VERSION, second));
		assertThrows(NullPointerException.class, () -> supervisedCode(java.util.Arrays.asList((String) null)));
		assertThrows(IllegalArgumentException.class, () -> supervisedCode(List.of(" ")));
	}

	/**
	 * Checks that the public ClassPermission builder, which nothing inside Ares
	 * uses, builds the same record as the constructor and refuses a missing or
	 * blank name. Also checks that a package name, a nested class and a simple name
	 * are accepted as given, since the record checks no Java syntax.
	 */
	@Test
	void classPermissionIsConstructableThroughItsBuilder() {
		ClassPermission built = ClassPermission.builder().className("com.example.Trusted").build();
		assertEquals("com.example.Trusted", built.className());
		assertEquals(new ClassPermission("com.example.Trusted"), built);
		assertThrows(NullPointerException.class, () -> ClassPermission.builder().className(null));
		assertThrows(NullPointerException.class, () -> ClassPermission.builder().build());
		assertThrows(IllegalArgumentException.class, () -> ClassPermission.builder().className(" ").build());

		assertEquals("de.tum.cit.ase.ares.api.internal",
				new ClassPermission("de.tum.cit.ase.ares.api.internal").className());
		assertEquals("com.example.Outer$Inner", new ClassPermission("com.example.Outer$Inner").className());
		assertEquals("MainTest", new ClassPermission("MainTest").className());
	}

	/**
	 * Checks the record's own guards, which the builder and the factories built on
	 * it reach too. A command name, a path to one and the wildcard are accepted;
	 * control characters and surrounding whitespace are not, as a line break
	 * travels into generated sources and reports. allowWithoutArguments must be the
	 * restrictive factory, the way round that is easy to get wrong.
	 */
	@Test
	void commandPermissionAcceptsBothPolicyFormsAndRejectsEveryOtherShape() {
		assertThrows(NullPointerException.class, () -> new CommandPermission(null, List.of()));
		assertThrows(IllegalArgumentException.class, () -> new CommandPermission(" ", List.of()));
		assertThrows(NullPointerException.class, () -> new CommandPermission("git", null));
		assertThrows(NullPointerException.class,
				() -> new CommandPermission("git", java.util.Arrays.asList((String) null)));

		assertEquals("echo", new CommandPermission("echo", List.of()).executeTheCommand());
		assertEquals("src/test/resources/trustedExecute.sh",
				new CommandPermission("src/test/resources/trustedExecute.sh", List.of()).executeTheCommand());
		assertEquals("*", new CommandPermission("*", List.of("*")).executeTheCommand());

		assertEquals(List.of(), CommandPermission.allowWithoutArguments("git").withTheseArguments());
		assertEquals(List.of(CommandPermission.ANY_ARGUMENTS),
				CommandPermission.allowWithAnyArguments("git").withTheseArguments());
		assertEquals("git", CommandPermission.allowWithAnyArguments("git").executeTheCommand());
		assertThrows(NullPointerException.class, () -> CommandPermission.allowWithAnyArguments(null));
		assertThrows(IllegalArgumentException.class, () -> CommandPermission.allowWithAnyArguments(" "));
		assertEquals(List.of("--version", ""),
				new CommandPermission("git", List.of("--version", "")).withTheseArguments());
		assertThrows(IllegalArgumentException.class, () -> new CommandPermission("ec\nho", List.of()));
		assertThrows(IllegalArgumentException.class, () -> new CommandPermission(" echo", List.of()));
		assertThrows(IllegalArgumentException.class, () -> new CommandPermission("git", List.of("--ver\nsion")));
	}

	/**
	 * Checks that a package import built in code is refused wherever a policy file
	 * refuses it. A trailing dot mattered most: ArchUnit strips it, so "com.foo."
	 * was enforced as the grant "com.foo". SupervisedCode holds this check because
	 * PackagePermission is language-agnostic and does not know what a package looks
	 * like.
	 */
	@Test
	void aProgrammaticPackageImportIsHeldToTheSameRuleAsAWrittenOne() {
		assertThrows(IllegalArgumentException.class, () -> supervisedCodeAllowingImport("com.foo."));
		assertThrows(IllegalArgumentException.class, () -> supervisedCodeAllowingImport("com..foo"));
		assertThrows(IllegalArgumentException.class, () -> supervisedCodeAllowingImport(".com.foo"));
		assertThrows(IllegalArgumentException.class, () -> supervisedCodeAllowingImport("com.foo.*"));
		assertThrows(IllegalArgumentException.class, () -> supervisedCodeAllowingImport(""));
		assertThrows(IllegalArgumentException.class, () -> supervisedCodeAllowingImport("com.class"));
	}

	/**
	 * Checks that a package import built in code still accepts the wildcard, a
	 * dotted package and a single identifier. The wildcard is not a valid package
	 * name, so the check must use the package import rule rather than the package
	 * name rule.
	 */
	@Test
	void aProgrammaticPackageImportStillAcceptsEveryValidSelector() {
		assertEquals("*", supervisedCodeAllowingImport("*").theFollowingResourceAccessesArePermitted()
				.regardingPackageImports().get(0).importTheFollowingPackage());
		assertEquals("java.util", supervisedCodeAllowingImport("java.util").theFollowingResourceAccessesArePermitted()
				.regardingPackageImports().get(0).importTheFollowingPackage());
		assertEquals("bareidentifier",
				supervisedCodeAllowingImport("bareidentifier").theFollowingResourceAccessesArePermitted()
						.regardingPackageImports().get(0).importTheFollowingPackage());
	}

	private SupervisedCode supervisedCode(List<String> testClasses) {
		return new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, "example",
				"Main", testClasses, ResourceAccesses.createRestrictive(), null);
	}

	/**
	 * Builds Java supervised code whose only package import permission is the given
	 * one, so building it runs the package import check.
	 */
	private SupervisedCode supervisedCodeAllowingImport(String packageImport) {
		ResourceAccesses accesses = ResourceAccesses.builder().regardingFileSystemInteractions(List.of())
				.regardingNetworkConnections(List.of()).regardingCommandExecutions(List.of())
				.regardingThreadCreations(List.of())
				.regardingPackageImports(List.of(new PackagePermission(packageImport))).build();
		return new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, "example",
				"Main", List.of(), accesses);
	}
}
