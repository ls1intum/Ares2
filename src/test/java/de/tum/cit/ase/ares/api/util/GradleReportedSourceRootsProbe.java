package de.tum.cit.ase.ares.api.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.GradleReportedSourceRoots;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner;

/**
 * Runs in a JVM of its own, started by
 * {@link GradleReportedSourceRootsForkedTest} with or without a report, and
 * prints what Ares makes of the report: its outcome, the discovered folders and
 * the test classes that would be exempt.
 */
public final class GradleReportedSourceRootsProbe {

	/** Not instantiable: it only offers static methods. */
	private GradleReportedSourceRootsProbe() {
	}

	/**
	 * Prints {@code OUTCOME=}, {@code TEST=}, {@code MAIN=}, {@code OUTPUT=},
	 * {@code EXEMPT=} and {@code DIRECTOR=} lines for the project. With
	 * {@code tamper}, it first sets the test-folder property to the main folder, as
	 * test code might.
	 *
	 * @param arguments the project root, then {@code tamper} or {@code plain}
	 */
	public static void main(String[] arguments) {
		Path root = BuildToolConfiguration.canonicalise(Path.of(arguments[0]));
		if ("tamper".equals(arguments[1])) {
			System.setProperty("ares.gradle.testSourceRoots",
					URLEncoder.encode(root.resolve("src/main/java").toString(), StandardCharsets.UTF_8));
		}
		System.out.println("OUTCOME=" + GradleReportedSourceRoots.read(root).outcome());
		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(root);
		System.out.println("TEST=" + configuration.testSourceRoots().stream().map(root::relativize).toList());
		System.out.println("MAIN=" + configuration.productionSourceRoots().stream().map(root::relativize).toList());
		System.out.println("OUTPUT=" + root.relativize(configuration.productionOutputRoot()) + ","
				+ root.relativize(configuration.testOutputRoot()));
		System.out.println("EXEMPT=" + Arrays.toString(new JavaProjectScanner(configuration).scanForTestClasses()));
		System.out.println("DIRECTOR=" + directorOutcome(root));
	}

	/**
	 * What the director makes of the project without a policy, as a test run calls
	 * it: the exempt test classes of the security test cases it creates, or its
	 * refusal.
	 *
	 * @param root the project root
	 * @return {@code exempt [...]} or {@code refused <message>}
	 */
	private static String directorOutcome(Path root) {
		try {
			return "exempt " + new SecurityPolicyReaderAndDirector(null, root).createTestCases().factoryAndBuilder()
					.testClasses();
		} catch (SecurityException refusal) {
			return "refused " + refusal.getMessage();
		}
	}
}
