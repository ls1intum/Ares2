package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Signed, atomic and process-safe persistence for WALA rule outcomes. */
final class WalaOutcomeCacheStore {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final java.util.concurrent.ConcurrentHashMap<Path, Object> JVM_LOCKS = new java.util.concurrent.ConcurrentHashMap<>();

	private WalaOutcomeCacheStore() {
		throw new SecurityException("WalaOutcomeCacheStore is a utility class");
	}

	static Map<String, Optional<String>> load(Path cacheFile, byte[] secret) {
		if (cacheFile == null || secret == null || !Files.isRegularFile(cacheFile)) {
			return Map.of();
		}
		try {
			String content = Files.readString(cacheFile, StandardCharsets.UTF_8);
			int newline = content.indexOf('\n');
			if (newline < 0) {
				return Map.of();
			}
			String suppliedMac = content.substring(0, newline);
			String body = content.substring(newline + 1);
			String expectedMac = Base64.getEncoder()
					.encodeToString(hmac(secret, body.getBytes(StandardCharsets.UTF_8)));
			if (!MessageDigest.isEqual(suppliedMac.getBytes(StandardCharsets.UTF_8),
					expectedMac.getBytes(StandardCharsets.UTF_8))) {
				return Map.of();
			}
			return parseBody(body);
		} catch (IOException | GeneralSecurityException | RuntimeException invalidCache) {
			return Map.of();
		}
	}

	static void mergeAndSave(Path cacheFile, Path lockFile, byte[] secret, Map<String, Optional<String>> updates)
			throws IOException, GeneralSecurityException {
		Files.createDirectories(cacheFile.getParent());
		Object jvmLock = JVM_LOCKS.computeIfAbsent(lockFile.toAbsolutePath().normalize(), ignored -> new Object());
		synchronized (jvmLock) {
			mergeAndSaveWithProcessLock(cacheFile, lockFile, secret, updates);
		}
	}

	private static void mergeAndSaveWithProcessLock(Path cacheFile, Path lockFile, byte[] secret,
			Map<String, Optional<String>> updates) throws IOException, GeneralSecurityException {
		try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				FileLock ignored = channel.lock()) {
			Map<String, Optional<String>> merged = new HashMap<>(load(cacheFile, secret));
			merged.putAll(updates);
			String body = encodeBody(merged);
			String mac = Base64.getEncoder().encodeToString(hmac(secret, body.getBytes(StandardCharsets.UTF_8)));
			Path temporary = Files.createTempFile(cacheFile.getParent(), "outcomes", ".tmp");
			try {
				Files.writeString(temporary, mac + "\n" + body, StandardCharsets.UTF_8);
				try {
					Files.move(temporary, cacheFile, StandardCopyOption.ATOMIC_MOVE,
							StandardCopyOption.REPLACE_EXISTING);
				} catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
					Files.move(temporary, cacheFile, StandardCopyOption.REPLACE_EXISTING);
				}
			} finally {
				Files.deleteIfExists(temporary);
			}
		}
	}

	private static Map<String, Optional<String>> parseBody(String body) {
		Map<String, Optional<String>> outcomes = new HashMap<>();
		for (String line : body.split("\n")) {
			if (line.isEmpty()) {
				continue;
			}
			String[] parts = line.split("\t", 3);
			if (parts.length != 3 || !(parts[1].equals("0") || parts[1].equals("1"))) {
				continue;
			}
			try {
				String key = decode(parts[0]);
				Optional<String> message = parts[1].equals("1") ? Optional.of(decode(parts[2])) : Optional.empty();
				outcomes.put(key, message);
			} catch (IllegalArgumentException malformedLine) {
				// A signed but malformed line is ignored; it cannot manufacture a PASS for
				// any key because only well-formed entries are returned.
			}
		}
		return Map.copyOf(outcomes);
	}

	private static String encodeBody(Map<String, Optional<String>> outcomes) {
		StringBuilder body = new StringBuilder();
		outcomes.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
			Optional<String> message = entry.getValue();
			body.append(encode(entry.getKey())).append('\t').append(message.isPresent() ? '1' : '0').append('\t')
					.append(message.map(WalaOutcomeCacheStore::encode).orElse("")).append('\n');
		});
		return body.toString();
	}

	private static String encode(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static String decode(String value) {
		return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
	}

	private static byte[] hmac(byte[] secret, byte[] body) throws GeneralSecurityException {
		Mac mac = Mac.getInstance(HMAC_ALGORITHM);
		mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
		return mac.doFinal(body);
	}
}
