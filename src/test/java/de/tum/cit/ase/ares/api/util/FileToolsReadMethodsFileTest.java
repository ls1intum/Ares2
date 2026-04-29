package de.tum.cit.ase.ares.api.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileToolsReadMethodsFileTest {

	@Test
	void stripsAndFiltersBlanksCommentsWhitespaceLines(@TempDir Path tmp) throws IOException {
		// EM SPACE (U+2003) is Character.isWhitespace -> true -> stripped to empty -> filtered.
		// Mixed line endings, leading/trailing whitespace, comment with leading spaces, tabs.
		String content = String.join("\n",
				"# pure comment",
				"   # comment with leading spaces",
				"",
				"   ",
				"\t\t",
				" ",
				"  java.lang.String.length()  ",
				"java.lang.String.isEmpty()",
				"") + "\r\n# trailing comment after CRLF\r\n";
		File file = tmp.resolve("methods.txt").toFile();
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);

		Set<String> methods = FileTools.readMethodsFile(file);

		assertThat(methods).containsExactlyInAnyOrder(
				"java.lang.String.length()",
				"java.lang.String.isEmpty()");
	}

	@Test
	void preservesAlreadyTrimmedHappyPathEntries(@TempDir Path tmp) throws IOException {
		String content = "java.io.FileInputStream.<init>(java.io.File)\njava.lang.Class.forName(java.lang.String)\n";
		File file = tmp.resolve("methods.txt").toFile();
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);

		assertThat(FileTools.readMethodsFile(file)).containsExactlyInAnyOrder(
				"java.io.FileInputStream.<init>(java.io.File)",
				"java.lang.Class.forName(java.lang.String)");
	}

	@Test
	void doesNotStripNonBreakingSpace(@TempDir Path tmp) throws IOException {
		// Document the intentional behaviour: U+00A0 NBSP is Character.isSpaceChar
		// but NOT Character.isWhitespace, so String.strip() does not remove it.
		String content = " java.lang.String.length() \n";
		File file = tmp.resolve("methods.txt").toFile();
		Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);

		assertThat(FileTools.readMethodsFile(file)).containsExactly(" java.lang.String.length() ");
	}
}
