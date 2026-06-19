package de.tum.cit.ase.ares.api.architecture.java;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Guards the symmetric coverage between the ArchUnit and WALA static-analysis
 * blacklists. The two engines use different signature formats (ArchUnit dotted
 * source form, WALA JVM descriptors), so parity is asserted on the declaring
 * class plus method name rather than on a byte-identical line.
 */
class BlacklistParityTest {

	private static List<String> readLines(Path path) throws IOException {
		return Files.readAllLines(path);
	}

	private static boolean containsEntryStartingWith(List<String> lines, String prefix) {
		return lines.stream().anyMatch(line -> line.startsWith(prefix));
	}

	@Test
	void bothNetworkListsCoverPublicConnectionApis() throws IOException {
		List<String> archunit = readLines(FileHandlerConstants.ARCHUNIT_NETWORK_METHODS);
		List<String> wala = readLines(FileHandlerConstants.WALA_NETWORK_METHODS);

		List<String> requiredArchunitPrefixes = List.of("java.net.URL.openConnection", "java.net.URLConnection.connect",
				"java.net.http.HttpClient.send", "java.net.http.HttpClient.sendAsync",
				"java.nio.channels.SocketChannel.open()", "java.nio.channels.ServerSocketChannel.accept()");
		for (String prefix : requiredArchunitPrefixes) {
			assertTrue(containsEntryStartingWith(archunit, prefix), "ArchUnit network blacklist must cover " + prefix);
		}

		List<String> requiredWalaPrefixes = List.of("java.net.URL.openConnection", "java.net.URLConnection.connect",
				"java.net.http.HttpClient.send", "java.net.http.HttpClient.sendAsync",
				"java.nio.channels.SocketChannel.open()", "java.nio.channels.ServerSocketChannel.accept()",
				"java.net.ServerSocket.accept()");
		for (String prefix : requiredWalaPrefixes) {
			assertTrue(containsEntryStartingWith(wala, prefix), "WALA network blacklist must cover " + prefix);
		}
	}

	@Test
	void bothSerializationListsCoverXmlDecoder() throws IOException {
		List<String> archunit = readLines(FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS);
		List<String> wala = readLines(FileHandlerConstants.WALA_SERIALIZATION_METHODS);

		for (String prefix : List.of("java.beans.XMLDecoder", "java.beans.XMLEncoder")) {
			assertTrue(containsEntryStartingWith(archunit, prefix),
					"ArchUnit serialization blacklist must cover " + prefix);
			assertTrue(containsEntryStartingWith(wala, prefix), "WALA serialization blacklist must cover " + prefix);
		}
	}

	@Test
	void walaReflectionListCoversMethodHandleInvoke() throws IOException {
		List<String> wala = readLines(FileHandlerConstants.WALA_REFLECTION_METHODS);
		assertTrue(containsEntryStartingWith(wala, "java.lang.invoke.MethodHandle.invoke"),
				"WALA reflection blacklist must cover java.lang.invoke.MethodHandle.invoke");
	}

	@Test
	void noMalformedFilesWriteDescriptorRemains() throws IOException {
		String malformed = "java.nio.file.Files.write(Ljava/nio/file/Path;[B[Ljava/nio/file/OpenOption)";
		assertFalse(readLines(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS).contains(malformed),
				"ArchUnit file-system blacklist must not contain the malformed Files.write descriptor");
		assertFalse(readLines(FileHandlerConstants.WALA_FILESYSTEM_METHODS).contains(malformed),
				"WALA file-system blacklist must not contain the malformed Files.write descriptor");
	}
}
