package example.jce;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.List;
import java.util.Map;

/**
 * Reads a fixture from a real supervised provider callback before returning its
 * SPI.
 */
public final class JceProviderService extends Provider.Service {

	/** The fixture that the callback attempts to read. */
	private final Path file;

	/** A working cipher SPI prepared by the trusted launcher. */
	private final Object cipher;

	/** Whether JCE entered this callback, before any read verdict. */
	private boolean called;

	/** Whether the callback actually obtained the expected fixture contents. */
	private boolean readSucceeded;

	/** Registers a test cipher backed by a real JDK cipher SPI. */
	public JceProviderService(Provider provider, Path file, Object cipher) {
		super(provider, "Cipher", "AresRead", cipher.getClass().getName(), List.of(), Map.of());
		this.file = file;
		this.cipher = cipher;
	}

	/** Attempts the supervised read on JCE's actual provider construction path. */
	@Override
	public Object newInstance(Object parameter) throws NoSuchAlgorithmException {
		called = true;
		try {
			readSucceeded = Files.readString(file).equals("protected fixture");
			return cipher;
		} catch (IOException failure) {
			throw new NoSuchAlgorithmException("The provider fixture was unreadable", failure);
		}
	}

	/** Reports that JCE entered the callback independently of its result. */
	public boolean wasCalled() {
		return called;
	}

	/** Reports whether the permitted control returned the actual contents. */
	public boolean readSucceeded() {
		return readSucceeded;
	}
}
