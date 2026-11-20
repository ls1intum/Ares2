package example.nativelibrary;

import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;

/**
 * Student code whose first use of an image or of the user preferences makes the
 * JDK load one of its own native libraries.
 */
public final class NativeLibraryFirstUser {

	/** Prevents instances of the student class. */
	private NativeLibraryFirstUser() {
	}

	/**
	 * Reads one pixel of a new image, which makes the JDK load its {@code awt}
	 * library the first time.
	 *
	 * @return the colour of that pixel
	 */
	public static int readPixel() {
		return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getRGB(0, 0);
	}

	/**
	 * Opens the user preferences, which makes the JDK load its {@code prefs}
	 * library the first time.
	 *
	 * @return the path of the preference root
	 */
	public static String openPreferences() {
		return Preferences.userRoot().absolutePath();
	}

	/**
	 * Runs code handed in by the launcher, so that student code is on the stack
	 * while it runs.
	 *
	 * @param callback the code to run
	 */
	public static void run(Runnable callback) {
		callback.run();
	}
}
