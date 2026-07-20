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

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

class PolicyValueContractTest {
	@Test
	void requiresExactlyTheCurrentPolicyVersion() {
		SecurityPolicy policy = SecurityPolicy
				.createRestrictive(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ);
		assertEquals(SecurityPolicy.CURRENT_POLICY_VERSION, policy.thisPolicyFileCompliesToThePolicyVersion());
		assertThrows(IllegalArgumentException.class, () -> new SecurityPolicy(0, policy.regardingTheSupervisedCode()));
		assertThrows(IllegalArgumentException.class, () -> new SecurityPolicy(2, policy.regardingTheSupervisedCode()));
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
		assertEquals(new SecurityPolicy(SecurityPolicy.CURRENT_POLICY_VERSION, first),
				new SecurityPolicy(SecurityPolicy.CURRENT_POLICY_VERSION, second));
		assertThrows(NullPointerException.class, () -> supervisedCode(java.util.Arrays.asList((String) null)));
		assertThrows(IllegalArgumentException.class, () -> supervisedCode(List.of(" ")));
	}

	private SupervisedCode supervisedCode(List<String> testClasses) {
		return new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, "example",
				"Main", testClasses, ResourceAccesses.createRestrictive());
	}
}
