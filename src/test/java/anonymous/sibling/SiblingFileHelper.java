package anonymous.sibling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compiled sibling-package fixture reached from the narrowly analysed package.
 */
public final class SiblingFileHelper {
	private SiblingFileHelper() {
	}

	public static String read(Path path) throws IOException {
		return Files.readString(path);
	}
}
