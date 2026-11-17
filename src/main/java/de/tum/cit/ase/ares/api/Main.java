package de.tum.cit.ase.ares.api;

import java.nio.file.Path;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

/**
 * Command line entry point that reads a security policy and writes the security
 * test cases it describes into a project.
 * <p>
 * The two paths are arguments rather than constants: they used to be absolute
 * paths into a particular developer's home directory, which only worked on that
 * machine and shipped inside the published artefact.
 */
public final class Main {
	private Main() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization", "Main"));
	}

	/**
	 * Reads the given security policy and writes the resulting test cases into the
	 * given project.
	 * <p>
	 * The project path is made absolute first, since the writer reads a relative
	 * target as relative to the project root and would nest it there.
	 *
	 * @param args the path of the security policy YAML file, followed by the root
	 *             of the project the test cases are written into, each absolute or
	 *             relative to the working directory
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"Usage: Main <path to the security policy YAML file> <path to the project root>");
		}
		Path securityPolicyPath = Path.of(args[0]);
		Path projectRootPath = Path.of(args[1]).toAbsolutePath();
		SecurityPolicyReaderAndDirector securityPolicyReaderAndDirector = new SecurityPolicyReaderAndDirector(
				securityPolicyPath, projectRootPath).createTestCases();
		securityPolicyReaderAndDirector.writeTestCases(projectRootPath.resolve(Path.of("src", "test", "java")));
	}
}
