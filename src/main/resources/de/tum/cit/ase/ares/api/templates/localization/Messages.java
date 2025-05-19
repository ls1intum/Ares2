package %s.api.localization;

import %s.api.util.LruCache;
import org.opentest4j.AssertionFailedError;

import java.util.Collections;
import java.util.Locale;
import java.util.Locale.Category;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {

	private static final String BUNDLE_NAME = "%s.api.localization.messages"; //$NON-NLS-1$

	private static Map<Locale, ResourceBundle> resourceBundleCache = Collections.synchronizedMap(new LruCache<>(100));

	private Messages() {
	}

	public static String localized(String key, Object... args) {
		try {
			String localizedText = getBundleForCurrentLocale().getString(key);
			if (args.length == 0)
				return localizedText;
			return String.format(localizedText, args);
		} catch (@SuppressWarnings("unused") MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static AssertionFailedError localizedFailure(String key, Object... args) {
		return new AssertionFailedError(localized(key, args));
	}

	public static AssertionFailedError localizedFailure(Throwable cause, String key, Object... args) {
		return new AssertionFailedError(localized(key, args), cause);
	}

	private static ResourceBundle getBundleForCurrentLocale() {
		return resourceBundleCache.computeIfAbsent(Locale.getDefault(Category.DISPLAY), Messages::loadBundleForLocale);
	}

	private static ResourceBundle loadBundleForLocale(Locale locale) {
		return ResourceBundle.getBundle(BUNDLE_NAME, locale);
	}

	public static void init() {
		// just for initialization
	}
}
