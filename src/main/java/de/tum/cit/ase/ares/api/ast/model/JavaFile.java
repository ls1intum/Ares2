package de.tum.cit.ase.ares.api.ast.model;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.*;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;

/**
 * Stores all required information about a Java file to be analyzed
 */
@API(status = Status.INTERNAL)
public class JavaFile {

	private static final PathMatcher JAVAFILEMATCHER = FileSystems.getDefault().getPathMatcher("glob:*.java"); //$NON-NLS-1$
	private static final Logger LOG = LoggerFactory.getLogger(JavaFile.class);

	private final Path javaFilePath;
	private final CompilationUnit javaFileAST;

	public JavaFile(Path javaFilePath, CompilationUnit javaFileAST, boolean excludeMainMethod) {
		this.javaFilePath = javaFilePath;
		if (excludeMainMethod) {
			excludeMainMethod(javaFileAST);
		}
		this.javaFileAST = javaFileAST;
	}

	/**
	 * Excludes the main method from the AST
	 * @param javaFileAST AST of the Java-file
	 */
	private static void excludeMainMethod(CompilationUnit javaFileAST) {
		javaFileAST.findAll(MethodDeclaration.class).stream()
				.filter(method ->
						method.isPublic() &&
								method.isStatic() &&
								method.getType().isVoidType() &&
								method.getNameAsString().equals("main") &&
								method.getParameters().size() == 1 &&
								method.getParameter(0).getTypeAsString().equals("String[]"))
				.forEach(Node::remove);
	}

	public Path getJavaFilePath() {
		return javaFilePath;
	}

	public CompilationUnit getJavaFileAST() {
		return javaFileAST;
	}

	/**
	 * Turns the Java-file into an AST in case the provided path points to a
	 * Java-file
	 *
	 * @param pathOfFile Path to the Java-file
	 * @return The information of the Java-file packed into a JavaFile object (null
	 *         if the file is not a Java-file)
	 */
	public static JavaFile convertFromFile(Path pathOfFile, boolean excludeMainMethod) {
		if (!JAVAFILEMATCHER.matches(pathOfFile.getFileName())) {
			return null;
		}
		try {
			return new JavaFile(pathOfFile, StaticJavaParser.parse(pathOfFile), excludeMainMethod);
		} catch (IOException e) {
			LOG.error("Error reading Java file '{}'", pathOfFile.toAbsolutePath(), e); //$NON-NLS-1$
			throw new AssertionError(localized("ast.method.convert_from_file", pathOfFile.toAbsolutePath()));
		}
	}

	/**
	 * Turns all Java-file below a certain path into ASTs
	 *
	 * @param pathOfDirectory Path to the highest analysis level
	 * @return List of Java-file information packed into JavaFile objects (empty
	 *         list if the directory does not exist or if none of the files in the
	 *         directory or its subdirectories is a Java-file)
	 */
	public static List<JavaFile> readFromDirectory(Path pathOfDirectory, boolean excludeMainMethod) {
		try (Stream<Path> directoryContentStream = Files.walk(pathOfDirectory)) {
			return directoryContentStream.map(path -> convertFromFile(path, excludeMainMethod)).filter(Objects::nonNull).toList();
		} catch (IOException e) {
			LOG.error("Error reading Java files in '{}'", pathOfDirectory.toAbsolutePath(), e); //$NON-NLS-1$
			throw new AssertionError(localized("ast.method.read_from_directory", pathOfDirectory.toAbsolutePath()));
		}
	}
}
