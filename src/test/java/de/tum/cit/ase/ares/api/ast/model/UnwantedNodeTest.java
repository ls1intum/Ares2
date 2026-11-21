package de.tum.cit.ase.ares.api.ast.model;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import de.tum.cit.ase.ares.api.ast.AstSourceFiles;
import de.tum.cit.ase.ares.api.ast.type.LoopType;

/**
 * Checks how {@link UnwantedNode} finds, orders and prints the unwanted nodes
 * of Java files.
 */
class UnwantedNodeTest {

	/**
	 * Source with a while loop on line 3 and a for loop on line 5.
	 */
	private static final String WHILE_THEN_FOR = "class A {\n" + "void m() {\n" + "  while (true) {}\n" + "\n"
			+ "  for (;;) {}\n" + "}\n" + "}\n";

	/**
	 * Source with two conditional expressions in type annotations, where the tree
	 * walk reaches the later one first.
	 */
	private static final String TYPE_USE_CONDITIONALS = "import java.lang.annotation.ElementType;\n"
			+ "import java.lang.annotation.Target;\n" + "class A {\n"
			+ "@Target(ElementType.TYPE_USE) @interface Ann { int value(); }\n"
			+ "String @Ann(true ? 1 : 2) [] m(String @Ann(false ? 3 : 4) [] p) { return p; }\n" + "}\n";

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
	 * Parses the source into a {@link JavaFile} named A.java that keeps the main
	 * method.
	 */
	private JavaFile parsed(String source) {
		return new JavaFile(Path.of("A.java"), StaticJavaParser.parse(source), false);
	}

	/**
	 * Kinds come back in the order of their first position, not the order of the
	 * map.
	 */
	@Test
	void ordersKindsByWhereTheyFirstOccur() {
		Map<String, Class<? extends Node>> kinds = new LinkedHashMap<>();
		kinds.put("For", ForStmt.class);
		kinds.put("While", WhileStmt.class);
		List<UnwantedNode> result = UnwantedNode.getUnwantedNodesInJavaFile(parsed(WHILE_THEN_FOR), kinds);
		assertThat(result).extracting(UnwantedNode::getUnwantedNodeName).containsExactly("While", "For");
	}

	/**
	 * A kind without a match is left out, and a file without a match or a file that
	 * is not Java gives an empty map.
	 */
	@Test
	void leavesOutKindsThatDoNotOccur() throws IOException {
		Map<String, Class<? extends Node>> kinds = new LinkedHashMap<>();
		kinds.put("For", ForStmt.class);
		kinds.put("While", WhileStmt.class);
		List<UnwantedNode> result = UnwantedNode
				.getUnwantedNodesInJavaFile(parsed("class A { void m() { while (true) {} } }"), kinds);
		assertThat(result).extracting(UnwantedNode::getUnwantedNodeName).containsExactly("While");
		Path withoutMatch = AstSourceFiles.write(directory, "Plain.java", "class Plain { void m() {} }");
		Path notJava = AstSourceFiles.write(directory, "notes.txt", WHILE_THEN_FOR);
		assertThat(UnwantedNode.getUnwantedNodesForFileAt(withoutMatch, kinds, false)).isEqualTo(Map.of());
		assertThat(UnwantedNode.getUnwantedNodesForFileAt(notJava, kinds, false)).isEqualTo(Map.of());
	}

	/**
	 * The report lists the files in path order, each with its kinds and every
	 * position, and an empty folder gives no report.
	 */
	@Test
	void formatsEveryPositionOfAFile() throws IOException {
		Path fileA = AstSourceFiles.write(directory, "A.java", "class A {\nvoid m() {\n  for (;;) {}\n}\n}\n");
		Path fileB = AstSourceFiles.write(directory, "B.java",
				"class B {\nvoid m() {\n  for (;;) {}\n  for (;;) {}\n}\n}\n");
		String lineSeparator = System.lineSeparator();
		String blockA = block(fileA, position(3, 3, 3, 13));
		String blockB = block(fileB, position(3, 3, 3, 13), position(4, 3, 4, 13));
		Optional<String> report = UnwantedNode.getMessageForUnwantedNodesForAllFilesBelow(directory,
				LoopType.FORSTMT.getNodeNameNodeMap(), false);
		assertThat(report).contains(" " + (blockA + lineSeparator + blockB + lineSeparator).trim());
		Path empty = directory.resolve("empty");
		assertThat(empty.toFile().mkdir()).isTrue();
		assertThat(UnwantedNode.getMessageForUnwantedNodesForAllFilesBelow(empty, LoopType.FORSTMT.getNodeNameNodeMap(),
				false)).isEmpty();
	}

	/**
	 * Builds the text of a position from its begin and end line and column.
	 */
	private static String position(int beginLine, int beginColumn, int endLine, int endColumn) {
		return localized("ast.method.to_string", localized("ast.check.has_begin_end", beginLine, beginColumn),
				localized("ast.check.has_begin_end", endLine, endColumn));
	}

	/**
	 * Builds the report block of one file with the given for-loop positions.
	 */
	private static String block(Path file, String... positions) {
		String lineSeparator = System.lineSeparator();
		StringBuilder block = new StringBuilder(localized("ast.method.get_formatted_file_string_prefix", file));
		block.append(lineSeparator);
		block.append(localized("ast.method.get_formatted_unwanted_node_string_prefix",
				LoopType.FORSTMT.getNodeNameNodeMap().keySet().iterator().next()));
		for (String position : positions) {
			block.append(lineSeparator).append("   - ").append(position);
		}
		return block.toString();
	}

	/**
	 * The positions of one kind are sorted, even where the tree walk reaches a
	 * later place first.
	 */
	@Test
	void sortsPositionsInSourceOrder() {
		UnwantedNode node = new UnwantedNode(parsed(TYPE_USE_CONDITIONALS), "Conditional", ConditionalExpr.class);
		List<NodePosition> positions = node.getUnwantedNodePositions();
		List<NodePosition> sorted = new ArrayList<>(positions);
		sorted.sort(NodePosition::compareTo);
		assertThat(positions).hasSize(2);
		assertThat(positions).containsExactlyElementsOf(sorted);
		assertThat(positions.get(0).getBeginColumn()).isLessThan(positions.get(1).getBeginColumn());
	}
}
