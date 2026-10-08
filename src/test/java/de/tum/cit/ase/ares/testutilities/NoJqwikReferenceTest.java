package de.tum.cit.ase.ares.testutilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks that jqwik is gone from Ares: no source, build file, documentation
 * page, example or workflow names it, apart from the pages that tell users it
 * was removed.
 */
class NoJqwikReferenceTest {

	/** The trees and files searched, relative to the repository root. */
	private static final List<String> SEARCHED = List.of("src", "pom.xml", "documentation/docs", "examples", ".github");

	/**
	 * The folders an example exercise's build writes into, which a clean checkout
	 * does not have.
	 */
	private static final Set<String> BUILD_OUTPUT = Set.of("build", "target", ".gradle");

	/** The files that may name jqwik, because they say it was removed. */
	private static final Set<String> ALLOWED = Set.of(
			"documentation/docs/instructor/transform-ares-1-into-ares-2/index.md", "CLAUDE.md",
			"src/test/java/de/tum/cit/ase/ares/testutilities/NoJqwikReferenceTest.java");

	/**
	 * No searched file names jqwik in any letter case, apart from the allowed ones.
	 */
	@Test
	void noFileNamesJqwik() {
		List<String> naming = SEARCHED.stream().map(Path::of).filter(Files::exists)
				.flatMap(NoJqwikReferenceTest::filesBelow).filter(NoJqwikReferenceTest::namesJqwik)
				.map(file -> file.toString().replace('\\', '/')).filter(file -> !ALLOWED.contains(file)).sorted()
				.toList();

		assertThat(naming).as("files naming jqwik").isEmpty();
	}

	/**
	 * Every regular file at or below a path.
	 *
	 * @param root a file or folder.
	 * @return its files
	 */
	private static Stream<Path> filesBelow(Path root) {
		try (Stream<Path> files = Files.walk(root)) {
			return files.filter(Files::isRegularFile).filter(NoJqwikReferenceTest::isSource).toList().stream();
		} catch (IOException unreadable) {
			throw new UncheckedIOException(unreadable);
		}
	}

	/**
	 * Whether a file is part of the repository rather than an example's build
	 * output.
	 *
	 * @param file the file, relative to the repository root.
	 * @return false only below an example's build output folder
	 */
	private static boolean isSource(Path file) {
		return !(file.startsWith("examples") && file.getNameCount() > 3
				&& BUILD_OUTPUT.contains(file.getName(2).toString()));
	}

	/**
	 * Whether a file's bytes contain "jqwik" in any letter case.
	 *
	 * @param file the file.
	 * @return true when it names jqwik
	 */
	private static boolean namesJqwik(Path file) {
		try {
			return new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1).toLowerCase(Locale.ROOT)
					.contains("jqwik");
		} catch (IOException unreadable) {
			throw new UncheckedIOException(unreadable);
		}
	}
}
