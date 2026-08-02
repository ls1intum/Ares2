package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The supervised class: this stands in for a student submission.
 * <p>
 * Both file reads deliberately live here rather than in the test. A test class
 * named in {@code theFollowingClassesAreTestClasses} is exempt from enforcement,
 * so a read performed by the test itself would be allowed and would prove
 * nothing. Only a read that originates in supervised code is subject to the
 * policy.
 * </p>
 */
public final class Penguin {
	private final String name;

	public Penguin(String name) {
		this.name = Objects.requireNonNull(name, "name must not be null");
	}

	public String getName() {
		return name;
	}

	/**
	 * Reads the one file the policy permits. This is the positive control: if it
	 * fails, enforcement is stricter than the policy says.
	 */
	public String readPermittedFile() throws IOException {
		return Files.readString(Path.of("allowed.txt")).trim();
	}

	/**
	 * Reads a file the policy does not permit. This is the negative control: if it
	 * succeeds, enforcement is not active at all.
	 */
	public String readForbiddenFile() throws IOException {
		return Files.readString(Path.of("secret.txt")).trim();
	}
}
