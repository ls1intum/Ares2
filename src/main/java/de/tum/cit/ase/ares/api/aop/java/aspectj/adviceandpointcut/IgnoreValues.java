package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class IgnoreValues {

	public static final IgnoreValues NONE = new IgnoreValues("NONE");

	public static final IgnoreValues ALL = new IgnoreValues("ALL");

	private final String type;

	private final Integer index;

	private IgnoreValues(String type) {
		this(type, null);
	}

	private IgnoreValues(@Nonnull String type, Integer index) {
		this.type = Objects.requireNonNull(type, "type must not be null");
		this.index = index;
	}

	public static IgnoreValues noneExcept(int index) {
		if (index < 0) {
			throw new SecurityException(localize("security.instrumentation.ignore.values.index.negative"));
		}

		return new IgnoreValues("NONE_EXCEPT", index);
	}

	public static IgnoreValues allExcept(int index) {
		if (index < 0) {
			throw new SecurityException(localize("security.instrumentation.ignore.values.index.negative"));
		}

		return new IgnoreValues("ALL_EXCEPT", index);
	}

	public String getType() {
		return type;
	}

	public int getIndex() {
		if (index == null) {
			throw new IllegalStateException(localize("aop.ignore.index.not.available", type));
		}
		return index;
	}

	/**
	 * Retrieves a localised message based on a key and optional arguments.
	 * <p>
	 * Description: Attempts to fetch a localized string from the Messages class
	 * using reflection. Falls back to the key if localization fails. This mirrors
	 * the localize helper in JavaAspectJAbstractAdviceDefinitions so the AspectJ
	 * back-end does not depend on the Instrumentation package.
	 *
	 * @param key  the localization key identifying the message
	 * @param args optional arguments to format the localized message
	 * @return the localized message string, or the key itself if localization fails
	 */
	@Nonnull
	private static String localize(@Nonnull String key, @Nullable Object... args) {
		try {
			@Nonnull
			Class<?> messagesClass = Class.forName("de.tum.cit.ase.ares.api.localization.Messages", true,
					Thread.currentThread().getContextClassLoader());
			@Nonnull
			Method localized = messagesClass.getDeclaredMethod("localized", String.class, Object[].class);
			@Nullable
			Object result = localized.invoke(null, key, args);
			if (result instanceof String str) {
				return str;
			} else {
				return key;
			}
		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
				| IllegalAccessException e) {
			// Fallback: Return the key if localization fails
			return key;
		}
	}
}
