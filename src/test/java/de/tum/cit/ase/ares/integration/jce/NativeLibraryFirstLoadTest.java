package de.tum.cit.ase.ares.integration.jce;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;

/**
 * Proves in fresh JVMs, in instrumentation mode, that the JDK may load its own
 * native library the first time student code needs it, and that Ares decides
 * that load during the student's use, before the library is loaded.
 */
class NativeLibraryFirstLoadTest {

	/** Marker the child prints right before the student's first use. */
	private static final String FIRST_USE = "NATIVE_LIBRARY_FIRST_USE";

	/** Marker the child prints in front of the outcome of the first use. */
	private static final String OUTCOME = "NATIVE_LIBRARY_OUTCOME ";

	/** Prefix of the trace line the test agent prints for each library decision. */
	private static final String DECISION = "NATIVE_LIBRARY_DECISION ";

	/**
	 * The first image the student creates makes the JDK load {@code awt}, which is
	 * allowed, so the image works.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*_INSTRUMENTATION", mode = EnumSource.Mode.MATCH_ALL)
	void firstImageUseLoadsTheJdkLibrary(ProgrammingLanguageConfiguration configuration, @TempDir Path directory)
			throws Exception {
		String output = JceCryptoPolicyReadTest.runProbe(configuration, "image", directory);
		assertDecidedDuringFirstUse(output, "awt");
		assertTrue(outcome(output).startsWith("OK "), JceCryptoPolicyReadTest.summary(output));
	}

	/**
	 * The first use of the preferences makes the JDK load {@code prefs}, which is
	 * allowed. The policy lets the student read and create on every path, the only
	 * rule under which Ares lets student code open the preferences. Whatever the
	 * JDK does next may be refused for other reasons, but never by a library
	 * decision.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*_INSTRUMENTATION", mode = EnumSource.Mode.MATCH_ALL)
	void firstPreferencesUseLoadsTheJdkLibrary(ProgrammingLanguageConfiguration configuration, @TempDir Path directory)
			throws Exception {
		String output = JceCryptoPolicyReadTest.runProbe(configuration, "preferences", directory);
		assertDecidedDuringFirstUse(output, "prefs");
		assertFalse(outcome(output).contains("Runtime.loadLibrary0"), JceCryptoPolicyReadTest.summary(output));
	}

	/**
	 * Requires, in this order: the policy armed, the first use started, the JDK's
	 * own request allowed, and the library loaded. Also requires the launcher's
	 * request for the same library, made beneath student code, to be the only
	 * refused decision for that library.
	 */
	private static void assertDecidedDuringFirstUse(String output, String library) {
		String summary = JceCryptoPolicyReadTest.summary(output);
		int armed = output.indexOf("JCE_PROBE_ARMED");
		int firstUse = output.indexOf(FIRST_USE);
		int decision = output.indexOf(DECISION + library + " java.");
		int load = output.indexOf(File.separator + System.mapLibraryName(library) + ", handle");
		assertTrue(armed >= 0 && armed < firstUse, summary);
		assertTrue(firstUse < decision && decision < load, summary);
		assertTrue(lineAt(output, decision).endsWith(" ALLOWED"), summary);
		String launcherRefusal = DECISION + library + " " + NativeLibraryForkProbe.class.getName() + " DENIED";
		assertTrue(output.lines().filter(line -> line.startsWith(DECISION + library + " "))
				.filter(line -> line.endsWith(" DENIED")).allMatch(launcherRefusal::equals), summary);
		assertTrue(output.contains(launcherRefusal), summary);
		assertTrue(output.contains("NATIVE_LIBRARY_LAUNCHER_LOAD_DENIED"), summary);
	}

	/** Returns the outcome the child reported for the first use. */
	private static String outcome(String output) {
		return lineAt(output, output.indexOf(OUTCOME)).substring(OUTCOME.length());
	}

	/** Returns the rest of the line that starts at the given position. */
	private static String lineAt(String output, int start) {
		return output.substring(start).lines().findFirst().orElse("");
	}
}
