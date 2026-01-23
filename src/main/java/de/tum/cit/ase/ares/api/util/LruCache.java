package de.tum.cit.ase.ares.api.util;

import java.util.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Implements a LRU (least recently used) cache as described in
 * {@link LinkedHashMap#removeEldestEntry}.
 * <p>
 * <b>Thread Safety:</b> This class is NOT thread safe. For concurrent access,
 * use {@link #synchronizedCache(int)} to create a thread-safe wrapper, or wrap
 * instances with {@link Collections#synchronizedMap(Map)}.
 *
 * @author Christian Femers
 * @since 1.3.4
 * @version 1.0.0
 * @param <K> the type of the cache keys
 * @param <V> the type of the cache values
 */
@API(status = Status.STABLE)
public class LruCache<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	private final int maxCacheSize;

	public LruCache(int maxCacheSize) {
		// plus one because the old entry gets evicted after insertion of the new one
		super(maxCacheSize + 1, 0.75f, true);
		this.maxCacheSize = maxCacheSize;
	}

	public int getMaxCacheSize() {
		return maxCacheSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxCacheSize;
	}

	/**
	 * Creates a thread-safe synchronized LRU cache.
	 * <p>
	 * This method wraps an {@link LruCache} with
	 * {@link Collections#synchronizedMap(Map)} to provide thread-safe access in
	 * concurrent environments.
	 *
	 * @param <K> the type of the cache keys
	 * @param <V> the type of the cache values
	 * @param maxCacheSize the maximum number of entries in the cache
	 * @return a synchronized (thread-safe) LRU cache
	 */
	public static <K, V> Map<K, V> synchronizedCache(int maxCacheSize) {
		return Collections.synchronizedMap(new LruCache<>(maxCacheSize));
	}
}
