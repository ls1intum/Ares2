package de.tum.cit.ase.ares.api.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks that a generated file fills in the placeholders of its header and
 * footer but writes its body, which holds policy values such as file paths,
 * exactly as given.
 */
class FileToolsThreePartedFileTest {

	/**
	 * A body holding every percent spelling that used to be read as a format code.
	 */
	private static final String BODY_WITH_PERCENT_SIGNS = "paths = { \"100%.txt\", \"reports%n/etc\", \"a%%b\", \"%1$s\", \"%s\" };";

	/**
	 * Formats the header and the footer with the package name and leaves every
	 * percent sign of the body as it stands.
	 *
	 * @param tmp a folder for the templates and the written file
	 * @throws IOException if a template cannot be written or the result read
	 */
	@Test
	void formatsHeaderAndFooterButWritesTheBodyUnchanged(@TempDir Path tmp) throws IOException {
		Path header = Files.writeString(tmp.resolve("Header.txt"), "package %s.ares;", StandardCharsets.UTF_8);
		Path footer = Files.writeString(tmp.resolve("Footer.txt"), "// end of %1$s", StandardCharsets.UTF_8);
		Path target = tmp.resolve("out").resolve("Settings.java");

		Path written = FileTools.createThreePartedFormatStringFile(header, BODY_WITH_PERCENT_SIGNS, footer, target,
				new String[] { "org.example" });

		assertThat(Files.readString(written, StandardCharsets.UTF_8))
				.isEqualTo("package org.example.ares;\n" + BODY_WITH_PERCENT_SIGNS + "\n// end of org.example");
	}

	/**
	 * Still refuses a header that is not a valid format string, and names that
	 * header, so a broken template cannot slip through unformatted.
	 *
	 * @param tmp a folder for the templates and the written file
	 * @throws IOException if a template cannot be written
	 */
	@Test
	void refusesAHeaderThatIsNotAValidFormatString(@TempDir Path tmp) throws IOException {
		Path header = Files.writeString(tmp.resolve("Header.txt"), "package %q;", StandardCharsets.UTF_8);
		Path footer = Files.writeString(tmp.resolve("Footer.txt"), "}", StandardCharsets.UTF_8);

		assertThatThrownBy(() -> FileTools.createThreePartedFormatStringFile(header, "", footer,
				tmp.resolve("Settings.java"), new String[] { "org.example" })).isInstanceOf(SecurityException.class)
						.hasMessageContaining("Illegal format").hasMessageContaining("Header.txt");
	}
}
