package de.tum.cit.ase.ares.api.phobos;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Checks the vendored network sandbox library against the digests recorded for
 * it, and that it reads the rules named by {@code NETBLOCKER_CONF}.
 * <p>
 * No Java build recompiles that library, so it can fall behind the source
 * beside it unnoticed; a library reading any other configuration file applies
 * one rule list to every exercise.
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
class NetblockerProvenanceTest {
	/**
	 * The template directory Maven copied for packaging, resolved from where the
	 * classes were loaded. These are the bytes the jar is built from, not the jar
	 * itself, which {@code build-netblocker.sh --check-jar} reads instead.
	 */
	private static final Path TEMPLATES = FileTools.resolveFileOnSourceDirectory("templates", "phobos");

	/** The library preloaded ahead of libc to decide what may be reached. */
	private static final Path LIBRARY = TEMPLATES.resolve("libnetblocker.so");

	/** The source the library must have been built from. */
	private static final Path SOURCE = TEMPLATES.resolve("netblocker.c");

	/**
	 * What {@code tools/netblocker/build-netblocker.sh} recorded when it built the
	 * library.
	 */
	private static final Path MANIFEST = TEMPLATES.resolve("netblocker.provenance");

	/**
	 * A configuration path predating {@code NETBLOCKER_CONF}, which must not
	 * return.
	 */
	private static final String ABANDONED_PATH = "/var/tmp/opt/core/allowedList.cfg";

	/** A loopback address, so the probe below needs no network of any kind. */
	private static final String PROBED_HOST = "127.0.0.1";

	/** Documentation range address, reserved so it can never name a real host. */
	private static final String OTHER_HOST = "203.0.113.1";

	/**
	 * How long a probe may hold the library before the run is treated as wedged.
	 */
	private static final long PROBE_TIMEOUT_SECONDS = 60;

	/** Holds the rule files a probe reads, and is removed after each test. */
	@TempDir
	Path temporaryDirectory;

	/**
	 * Compares the copied source and library against their recorded digests.
	 * Editing one without regenerating the other leaves a digest behind.
	 */
	@Test
	void recordedDigestsMatchTheCopiedSourceAndLibrary() throws Exception {
		Map<String, String> manifest = readManifest();
		assertEquals(digestOf(SOURCE), manifest.get("source-sha256"),
				"netblocker.c changed without libnetblocker.so being rebuilt; run tools/netblocker/build-netblocker.sh");
		assertEquals(digestOf(LIBRARY), manifest.get("artifact-sha256"),
				"libnetblocker.so is not the artefact recorded for it; run tools/netblocker/build-netblocker.sh");
	}

	/**
	 * Reads the ELF header of the copied library. Only 64-bit x86 is built and
	 * tested, so anything else would ship unverified.
	 */
	@Test
	void copiedLibraryIsBuiltForTheOneSupportedArchitecture() throws Exception {
		byte[] header = Files.readAllBytes(LIBRARY);
		assertArrayEquals(new byte[] { 0x7f, 'E', 'L', 'F' }, Arrays.copyOf(header, 4),
				"the copied library is not an ELF object");
		assertEquals(2, header[4], "the copied library is not 64-bit");
		assertEquals(0x3e, header[18] & 0xff, "the copied library is not built for x86-64");
		assertEquals("x86_64", readManifest().get("architecture"), "the manifest claims another architecture");
	}

	/**
	 * Supplements the decision test below rather than standing in for it: a path
	 * absent from the file still says nothing about which file is read.
	 */
	@Test
	void copiedLibraryNoLongerCarriesTheAbandonedConfigurationPath() throws Exception {
		String contents = new String(Files.readAllBytes(LIBRARY), StandardCharsets.ISO_8859_1);
		assertFalse(contents.contains(ABANDONED_PATH),
				"the copied library still carries " + ABANDONED_PATH + ", so it predates NETBLOCKER_CONF");
	}

	/**
	 * Loads the copied library into a real process and flips only the file that
	 * {@code NETBLOCKER_CONF} names. A library that reads any other file answers
	 * both runs the same way, which is exactly what the stale one did.
	 */
	@Test
	@EnabledOnOs(OS.LINUX)
	void copiedLibraryDecidesByTheFileNetblockerConfNames() throws Exception {
		Path allowing = writeRule(PROBED_HOST, "allowing.rules");
		Path refusing = writeRule(OTHER_HOST, "refusing.rules");
		int allowed = resolveUnder(allowing);
		int refused = resolveUnder(refusing);
		assertEquals(0, allowed,
				"the copied library refused a host its NETBLOCKER_CONF rules allow, so it reads another file");
		assertNotEquals(0, refused,
				"the copied library allowed a host its NETBLOCKER_CONF rules omit, so it reads another file");
	}

	/**
	 * A library that fails to load is skipped by the dynamic linker, which would
	 * leave every decision above looking permissive for the wrong reason.
	 */
	@Test
	@EnabledOnOs(OS.LINUX)
	void copiedLibraryLoadsRatherThanBeingSkipped() throws Exception {
		Process process = probe(writeRule(PROBED_HOST, "loading.rules"));
		String diagnostics = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
		process.waitFor();
		assertFalse(diagnostics.contains("cannot be preloaded") || diagnostics.contains("cannot open shared object"),
				"the dynamic linker could not load the copied library: " + diagnostics);
	}

	/**
	 * Writes a rule file allowing one host on any port.
	 *
	 * @param host the only host the rules allow
	 * @param name the file name to write within the temporary directory
	 * @return the written rule file
	 */
	private Path writeRule(String host, String name) throws IOException {
		Path rules = temporaryDirectory.resolve(name);
		Files.writeString(rules, host + " *\n");
		return rules;
	}

	/**
	 * Runs the probe under the given rules and reports its exit status.
	 *
	 * @param rules the file NETBLOCKER_CONF names for this run
	 * @return zero when the interposed lookup allowed the probed host
	 */
	private int resolveUnder(Path rules) throws IOException, InterruptedException {
		Process process = probe(rules);
		process.getErrorStream().readAllBytes();
		if (!process.waitFor(PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			process.destroyForcibly();
			throw new IllegalStateException("the probe holding the copied library never finished");
		}
		return process.exitValue();
	}

	/**
	 * Asks {@code getent} to resolve the probed host in a child process holding the
	 * copied library. Its exit status alone says whether the interposed lookup
	 * allowed the host, so nothing has to be read from a message.
	 */
	private Process probe(Path rules) throws IOException {
		ProcessBuilder builder = new ProcessBuilder("getent", "ahosts", PROBED_HOST);
		builder.environment().put("LD_PRELOAD", LIBRARY.toString());
		builder.environment().put("NETBLOCKER_CONF", rules.toString());
		builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
		return builder.start();
	}

	/**
	 * Reads the provenance manifest into key and value pairs. Lines are stripped,
	 * because a Windows checkout leaves a carriage return on each of them.
	 *
	 * @return every recorded entry of the manifest
	 */
	private Map<String, String> readManifest() throws IOException {
		return Files.readAllLines(MANIFEST, StandardCharsets.UTF_8).stream().map(String::strip)
				.filter(line -> line.contains("=") && !line.startsWith("#")).collect(Collectors.toMap(
						line -> line.substring(0, line.indexOf('=')), line -> line.substring(line.indexOf('=') + 1)));
	}

	/**
	 * Computes the SHA-256 digest of a file in the lower-case hexadecimal form the
	 * manifest records.
	 *
	 * @param file the file to digest
	 * @return the digest as hexadecimal text
	 */
	private String digestOf(Path file) throws IOException, NoSuchAlgorithmException {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
	}
}
