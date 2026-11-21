package de.tum.cit.ase.ares.api.ast.model;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.ForStmt;

import de.tum.cit.ase.ares.api.ast.AstSourceFiles;

/**
 * Checks how {@link JavaFile} reads Java files and drops the main method.
 */
class JavaFileTest {

	/**
	 * A fresh folder for the files of one test.
	 */
	@TempDir
	Path directory;

	/**
	 * The parser configuration found before the test, put back afterwards.
	 */
	private ParserConfiguration savedConfiguration;

	/**
	 * Installs the Java 17 parser and remembers the old one.
	 */
	@BeforeEach
	void useJava17() {
		savedConfiguration = StaticJavaParser.getParserConfiguration();
		StaticJavaParser.setConfiguration(
				new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17));
	}

	/**
	 * Puts the old parser configuration back.
	 */
	@AfterEach
	void restoreParser() {
		StaticJavaParser.setConfiguration(savedConfiguration);
	}

	/**
	 * Builds a class whose only method has the given declaration and holds one for
	 * loop.
	 */
	private static String withMethod(String declaration) {
		return "class A {\n" + declaration + " { for (int i = 0; i < 1; i++) {} }\n" + "}\n";
	}

	/**
	 * Writes the method into a file, reads it as {@link JavaFile} and counts the
	 * for loops left.
	 */
	private int loopsAfterReading(String declaration, boolean excludeMainMethod) throws IOException {
		Path file = AstSourceFiles.write(directory, "A.java", withMethod(declaration));
		JavaFile javaFile = JavaFile.convertFromFile(file, excludeMainMethod);
		return javaFile.getJavaFileAST().findAll(ForStmt.class).size();
	}

	/**
	 * A real main method disappears when it is asked for.
	 */
	@Test
	void removesTheMainMethodWhenAsked() throws IOException {
		assertThat(loopsAfterReading("public static void main(String[] args)", true)).isZero();
	}

	/**
	 * A real main method stays when it is not asked for.
	 */
	@Test
	void keepsTheMainMethodByDefault() throws IOException {
		assertThat(loopsAfterReading("public static void main(String[] args)", false)).isEqualTo(1);
	}

	/**
	 * A main method that is not public stays.
	 */
	@Test
	void keepsAMainMethodThatIsNotPublic() throws IOException {
		assertThat(loopsAfterReading("static void main(String[] args)", true)).isEqualTo(1);
	}

	/**
	 * A main method that is not static stays.
	 */
	@Test
	void keepsAMainMethodThatIsNotStatic() throws IOException {
		assertThat(loopsAfterReading("public void main(String[] args)", true)).isEqualTo(1);
	}

	/**
	 * A main method that returns a value stays.
	 */
	@Test
	void keepsAMainMethodThatReturnsAValue() throws IOException {
		assertThat(loopsAfterReading("public static int main(String[] args)", true)).isEqualTo(1);
	}

	/**
	 * A method with another name stays.
	 */
	@Test
	void keepsAMethodWithAnotherName() throws IOException {
		assertThat(loopsAfterReading("public static void mainly(String[] args)", true)).isEqualTo(1);
	}

	/**
	 * A main method with two parameters stays.
	 */
	@Test
	void keepsAMainMethodWithTwoParameters() throws IOException {
		assertThat(loopsAfterReading("public static void main(String[] args, String extra)", true)).isEqualTo(1);
	}

	/**
	 * A main method whose parameter is not a String array stays.
	 */
	@Test
	void keepsAMainMethodWithAnotherParameterType() throws IOException {
		assertThat(loopsAfterReading("public static void main(Object[] args)", true)).isEqualTo(1);
	}

	/**
	 * A file that is not Java gives nothing, and a folder lists only its Java
	 * files.
	 */
	@Test
	void ignoresFilesThatAreNotJava() throws IOException {
		Path readme = AstSourceFiles.write(directory, "readme.txt", "class A {}");
		AstSourceFiles.write(directory, "A.java", "class A {}");
		AstSourceFiles.write(directory, "sub/B.java", "class B {}");
		assertThat(JavaFile.convertFromFile(readme, false)).isNull();
		List<JavaFile> files = JavaFile.readFromDirectory(directory, false);
		assertThat(files).extracting(file -> file.getJavaFilePath().getFileName().toString())
				.containsExactlyInAnyOrder("A.java", "B.java");
	}

	/**
	 * A Java file that does not exist is reported with its full path.
	 */
	@Test
	void reportsAFileThatCannotBeRead() {
		Path missing = directory.resolve("Gone.java");
		assertThatThrownBy(() -> JavaFile.convertFromFile(missing, false)).isInstanceOf(AssertionError.class)
				.hasMessage(localized("ast.method.convert_from_file", missing.toAbsolutePath()));
	}

	/**
	 * A folder that does not exist is reported with its full path.
	 */
	@Test
	void reportsAFolderThatCannotBeRead() {
		Path missing = directory.resolve("gone");
		assertThatThrownBy(() -> JavaFile.readFromDirectory(missing, false)).isInstanceOf(AssertionError.class)
				.hasMessage(localized("ast.method.read_from_directory", missing.toAbsolutePath()));
	}
}
