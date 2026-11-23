package de.tum.cit.ase.ares.api.architecture.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Pins the edges of the forbidden-method matcher: how it reads signatures, how
 * it compares them and how it refuses a broken policy file.
 */
class ForbiddenMethodMatcherTest {

	/** Folder that holds the policy files each test writes. */
	@TempDir
	Path folder;

	/**
	 * Writes one policy file with the given lines.
	 *
	 * @param lines the lines of the file
	 * @return the path of the new file
	 * @throws IOException if the file cannot be written
	 */
	private Path policyFile(String... lines) throws IOException {
		Path file = folder.resolve("methods.txt");
		Files.write(file, String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
		return file;
	}

	/**
	 * Builds the message the matcher gives for a malformed entry on line one.
	 *
	 * @param file  the policy file
	 * @param entry the refused entry
	 * @return the expected message
	 */
	private static String malformed(Path file, String entry) {
		return Messages.localized("security.architecture.blocklist.malformed.entry", file, 1, entry);
	}

	/** A missing signature on either side never matches. */
	@Test
	void matchesNothingWhenEitherSideIsMissing() {
		assertThat(ForbiddenMethodMatcher.matches(null, "a.B")).isFalse();
		assertThat(ForbiddenMethodMatcher.matches("a.B", null)).isFalse();
	}

	/** Nothing in gives nothing out. */
	@Test
	void canonicalisesNothingToNothing() {
		assertThat(ForbiddenMethodMatcher.canonicalise(null)).isNull();
	}

	/** An opening parenthesis without a closing one leaves the text as it is. */
	@Test
	void keepsASignatureWithoutAClosingParenthesis() {
		assertThat(ForbiddenMethodMatcher.canonicalise("a.B.m(")).isEqualTo("a.B.m(");
	}

	/** A parameter list that opens the signature is still rewritten. */
	@Test
	void canonicalisesAParameterListThatOpensTheSignature() {
		assertThat(ForbiddenMethodMatcher.canonicalise("(I)")).isEqualTo("(int)");
	}

	/** A closing parenthesis before the opening one is not the end of the list. */
	@Test
	void searchesTheClosingParenthesisAfterTheOpeningOne() {
		assertThat(ForbiddenMethodMatcher.canonicalise("a)(I)")).isEqualTo("a)(int)");
	}

	/** Every primitive letter gets its Java name, and an unknown letter stays. */
	@Test
	void namesEveryPrimitiveDescriptor() {
		assertThat(ForbiddenMethodMatcher.canonicalise("a.B.m(BCDFIJSZV)"))
				.isEqualTo("a.B.m(byte, char, double, float, int, long, short, boolean, void)");
		assertThat(ForbiddenMethodMatcher.canonicalise("a.B.m(IQ)")).isEqualTo("a.B.m(int, Q)");
	}

	/** A parameter list already in source form is not read as descriptors. */
	@Test
	void leavesASourceParameterAlone() {
		assertThat(ForbiddenMethodMatcher.canonicalise("a.B.m(int)")).isEqualTo("a.B.m(int)");
		assertThat(ForbiddenMethodMatcher.canonicalise("a.B.m(java.lang.String)")).isEqualTo("a.B.m(java.lang.String)");
	}

	/** A list that opens with an array marker is read as descriptors. */
	@Test
	void readsSeveralArraysWithoutCommasAsDescriptors() {
		assertThat(ForbiddenMethodMatcher.canonicalise("a.B.m([I[I)")).isEqualTo("a.B.m(int[], int[])");
	}

	/** An array marker with no type after it is dropped. */
	@Test
	void ignoresATrailingArrayMarker() {
		assertThat(ForbiddenMethodMatcher.canonicalise("a.B.m(I[)")).isEqualTo("a.B.m(int)");
	}

	/**
	 * An entry that opens with a parenthesis is compared exactly, never by prefix.
	 */
	@Test
	void treatsAForbiddenEntryOpeningWithAParenthesisAsExact() {
		assertThat(ForbiddenMethodMatcher.matches("(int.x", "(int")).isFalse();
		assertThat(ForbiddenMethodMatcher.matches("(int", "(int")).isTrue();
	}

	/** A tab inside an entry, or an entry without a package, is refused. */
	@Test
	void refusesAnEntryWithATabOrWithoutAPackage() throws IOException {
		String tabbed = "java.lang.\tSystem.exit";
		Path tabbedFile = policyFile(tabbed);
		assertThatThrownBy(() -> ForbiddenMethodMatcher.effectiveMethods(tabbedFile))
				.isInstanceOf(SecurityException.class).hasMessage(malformed(tabbedFile, tabbed));
		Path bareFile = policyFile("exit");
		assertThatThrownBy(() -> ForbiddenMethodMatcher.effectiveMethods(bareFile))
				.isInstanceOf(SecurityException.class).hasMessage(malformed(bareFile, "exit"));
	}

	/** An entry whose first character is the only dot is refused. */
	@Test
	void refusesAnEntryOpeningWithADot() throws IOException {
		String entry = ".java.lang.System.exit";
		Path file = policyFile(entry);
		assertThatThrownBy(() -> ForbiddenMethodMatcher.effectiveMethods(file)).isInstanceOf(SecurityException.class)
				.hasMessage(malformed(file, entry));
	}

	/** A file that does not exist is reported as unreadable. */
	@Test
	void reportsAListItCannotRead() {
		Path missing = folder.resolve("missing.txt");
		assertThatThrownBy(() -> ForbiddenMethodMatcher.effectiveMethods(missing)).isInstanceOf(SecurityException.class)
				.hasMessage(Messages.localized("security.file-tools.read.content.failure"));
	}

	/** An array parameter is reduced only when it is exactly one type. */
	@Test
	void keepsASourceArrayParameterItCannotReduceToOneType() {
		assertThat(ForbiddenMethodMatcher.canonicalise("a.B.m([I[I, int)")).isEqualTo("a.B.m([I[I, int)");
		assertThat(ForbiddenMethodMatcher.canonicalise("a.B.m([Ljava.lang.String;, int)"))
				.isEqualTo("a.B.m(java.lang.String[], int)");
	}

	/** Parentheses at the start or in the wrong order are refused. */
	@Test
	void refusesParenthesesAtTheStartOrOutOfOrder() throws IOException {
		for (String entry : new String[] { "(a.b", ")a.b", "(a.b)" }) {
			Path file = policyFile(entry);
			assertThatThrownBy(() -> ForbiddenMethodMatcher.effectiveMethods(file))
					.isInstanceOf(SecurityException.class).hasMessage(malformed(file, entry));
		}
		assertThat(ForbiddenMethodMatcher.effectiveMethods(policyFile("a.b(c)"))).containsExactly("a.b(c)");
	}

	/** A file outside the two engine folders is read on its own. */
	@Test
	void readsAListOutsideBothEngineFolders() throws IOException {
		Path file = policyFile("java.lang.System.exit(I)");
		assertThat(ForbiddenMethodMatcher.effectiveMethods(file)).isEqualTo(Set.of("java.lang.System.exit(int)"));
	}

	/** Only the two plain temporary-file calls are allowed by default. */
	@Test
	void allowsOnlyTheStandardTemporaryFileCalls() {
		assertThat(ForbiddenMethodMatcher.isStandardAllowed(null)).isFalse();
		assertThat(ForbiddenMethodMatcher
				.isStandardAllowed("java.io.File.createTempFile(java.lang.String, java.lang.String)")).isTrue();
		assertThat(ForbiddenMethodMatcher.isStandardAllowed("java.io.File.delete()")).isFalse();
	}
}
