package de.tum.cit.ase.ares.api.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The system properties as this JVM started with them, copied from the JDK's
 * own record when this class is first used. {@code System.setProperty} does not
 * change that record; code reaching into the JDK's internals before the copy
 * could. Reading it needs
 * {@code --add-exports java.base/jdk.internal.misc=ALL-UNNAMED}.
 */
@API(status = Status.INTERNAL)
public final class StartUpProperties {

	/** The JDK class that keeps the start-up values. */
	private static final String START_UP_PROPERTIES_CLASS = "jdk.internal.misc.VM";

	/** The snapshot, taken when this class is first used. */
	private static final Snapshot SNAPSHOT = take();

	/**
	 * What reading the start-up values gave: a copy of them, or why the JDK refused
	 * them.
	 *
	 * @param values     the start-up values, or null where they could not be read
	 * @param unreadable why they could not be read, or null where they could
	 */
	public record Snapshot(@Nullable Map<String, String> values, @Nullable String unreadable) {

		/**
		 * One start-up value.
		 *
		 * @param key the property name
		 * @return its start-up value, empty where it was not set or nothing could be
		 *         read
		 */
		@Nonnull
		public Optional<String> get(@Nonnull String key) {
			return values == null ? Optional.empty() : Optional.ofNullable(values.get(key));
		}
	}

	/** Not instantiable: it only offers static methods. */
	private StartUpProperties() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization", "StartUpProperties"));
	}

	/**
	 * The start-up values of this JVM.
	 *
	 * @return the snapshot taken when this class was first used
	 */
	@Nonnull
	public static Snapshot snapshot() {
		return SNAPSHOT;
	}

	/**
	 * Copies the start-up values, or records why the JDK did not hand them out: the
	 * class missing, the module not exported, or the call failing. The JDK returns
	 * a view of a map it may change, so only a copy stays fixed.
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	private static Snapshot take() {
		try {
			Object values = Class.forName(START_UP_PROPERTIES_CLASS).getMethod("getSavedProperties").invoke(null);
			return new Snapshot(Map.copyOf((Map<String, String>) Map.class.cast(values)), null);
		} catch (ClassNotFoundException | NoSuchMethodException missing) {
			return new Snapshot(null, "this JDK has no " + START_UP_PROPERTIES_CLASS + ".getSavedProperties");
		} catch (IllegalAccessException | RuntimeException notExported) {
			return new Snapshot(null, "the JVM does not export jdk.internal.misc to Ares (" + notExported + ")");
		} catch (InvocationTargetException failed) {
			return new Snapshot(null, "reading the start-up values failed (" + failed.getCause() + ")");
		}
	}
}
