package de.tum.cit.ase.ares.api.architecture.java.wala;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pins how the signed outcome file is read back: what it refuses and which of
 * its lines it skips.
 */
class WalaOutcomeCacheStoreTest {

	/** Secret that signs the files these tests write. */
	private static final byte[] SECRET = "test-secret".getBytes(StandardCharsets.UTF_8);

	/** Folder that holds the cache files each test writes. */
	@TempDir
	Path folder;

	/**
	 * Encodes text the way the store does.
	 *
	 * @param value the text
	 * @return the Base64 form
	 */
	private static String encode(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Writes a file whose first line is the signature of the rest.
	 *
	 * @param body the signed part
	 * @return the path of the file
	 * @throws IOException              if the file cannot be written
	 * @throws GeneralSecurityException if the signature cannot be made
	 */
	private Path signedFile(String body) throws IOException, GeneralSecurityException {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
		String signature = Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
		Path file = folder.resolve("outcomes.v3");
		Files.writeString(file, signature + "\n" + body, StandardCharsets.UTF_8);
		return file;
	}

	/** Without a file or a secret there is nothing to load. */
	@Test
	void loadsNothingWithoutAFileOrSecret() throws IOException {
		Path file = folder.resolve("present");
		Files.writeString(file, "abc\n");
		assertThat(WalaOutcomeCacheStore.load(null, SECRET)).isEmpty();
		assertThat(WalaOutcomeCacheStore.load(file, null)).isEmpty();
		assertThat(WalaOutcomeCacheStore.load(folder.resolve("missing"), SECRET)).isEmpty();
	}

	/** A file with no line break has no signature line. */
	@Test
	void loadsNothingFromAFileWithoutAMacLine() throws IOException {
		Path file = folder.resolve("nomac");
		Files.writeString(file, "abc");
		assertThat(WalaOutcomeCacheStore.load(file, SECRET)).isEmpty();
	}

	/** A file that is not valid UTF-8 is ignored. */
	@Test
	void loadsNothingFromAFileThatIsNotUtf8() throws IOException {
		Path file = folder.resolve("binary");
		Files.write(file, new byte[] { (byte) 0xC3, (byte) 0x28 });
		assertThat(WalaOutcomeCacheStore.load(file, SECRET)).isEmpty();
	}

	/**
	 * Blank, short, wrong-flag and undecodable lines are skipped, a good one is
	 * kept.
	 */
	@Test
	void skipsEmptyMalformedAndUndecodableLinesOfASignedFile() throws IOException, GeneralSecurityException {
		String body = "\n" + "a2V5\t2\t\n" + "a2V5\t0\n" + "!!!\t0\t\n" + encode("ok") + "\t0\t\n";
		Map<String, Optional<String>> loaded = WalaOutcomeCacheStore.load(signedFile(body), SECRET);
		assertThat(loaded).isEqualTo(Map.of("ok", Optional.empty()));
	}
}
