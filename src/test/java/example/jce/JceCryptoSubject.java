package example.jce;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.net.ssl.SSLContext;

/** Student-like operations reached through the real runtime boundary. */
public final class JceCryptoSubject {

	/** Prevents instances of the fixture. */
	private JceCryptoSubject() {
	}

	/**
	 * Initialises jurisdiction policies, a cipher and local TLS without connecting.
	 */
	public static int initialiseCrypto(SecureRandom random) throws GeneralSecurityException {
		int maximum = Cipher.getMaxAllowedKeyLength("AES");
		Cipher.getInstance("AES");
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, null, random);
		return maximum;
	}

	/** Returns a file's contents only if the active boundary permits its read. */
	public static String read(Path file) throws IOException {
		return Files.readString(file);
	}

	/** Lists a directory using JCE's actual glob without treating it as a path. */
	public static long listPolicyFiles(Path directory) throws IOException {
		try (var entries = Files.newDirectoryStream(directory, "{default,exempt}_*.policy")) {
			long count = 0;
			var iterator = entries.iterator();
			while (iterator.hasNext()) {
				iterator.next();
				count++;
			}
			return count;
		}
	}

	/** Attempts a real create, overwrite or delete operation on a policy name. */
	public static void mutate(Path file, String action) throws IOException {
		switch (action) {
		case "create" -> Files.writeString(file, "changed", java.nio.file.StandardOpenOption.CREATE_NEW);
		case "overwrite" -> Files.writeString(file, "changed", java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
		case "delete" -> Files.delete(file);
		default -> throw new IllegalArgumentException(action);
		}
	}

	/** Selects the real provider route that invokes the supervised service. */
	public static void useProvider(Provider provider) throws GeneralSecurityException {
		Cipher.getInstance("AresRead", provider);
	}
}
