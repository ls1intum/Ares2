package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fixture using collections, lambdas and streams. These must not be permitted
 * by the default baseline; an exercise has to grant {@code java.util}
 * explicitly.
 */
public class UsesCollections {

	private final List<String> items = new ArrayList<>();

	public List<String> shout() {
		return items.stream().map(item -> item.toUpperCase()).filter(item -> !item.isEmpty())
				.collect(Collectors.toList());
	}

	public void add(String item) {
		items.add(item);
	}
}
