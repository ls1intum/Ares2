package de.tum.cit.ase.ares.api.ast.model;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.ForStmt;

/**
 * Checks where {@link NodePosition} says a node starts and how it orders and
 * prints positions.
 */
class NodePositionTest {

	/**
	 * Source with five for loops, at line 3 column 3, line 4 column 1, line 5
	 * column 5, and line 6 columns 3 and 16.
	 */
	private static final String LOOPS = "class A {\n" + "void m() {\n" + "  for (;;) {}\n" + "for (;;) {}\n"
			+ "    for (;;) {}\n" + "  for (;;) {} for (;;) {}\n" + "}\n" + "}\n";

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
	 * Parses the source and returns the positions of its for loops in tree order.
	 */
	private List<NodePosition> loopPositions() {
		CompilationUnit unit = StaticJavaParser.parse(LOOPS);
		return unit.findAll(ForStmt.class).stream().map(NodePosition::getPositionOf).toList();
	}

	/**
	 * A loop on line 3 starting in column 3 reports exactly that.
	 */
	@Test
	void reportsTheLineAndColumnWhereTheNodeBegins() {
		NodePosition position = loopPositions().get(0);
		assertThat(position.getBeginLine()).isEqualTo(3);
		assertThat(position.getBeginColumn()).isEqualTo(3);
	}

	/**
	 * A later line is greater whatever the column, then the column decides, and the
	 * same position is equal.
	 */
	@Test
	void ordersByLineFirstThenByColumn() {
		List<NodePosition> positions = loopPositions();
		NodePosition line3Column3 = positions.get(0);
		NodePosition line4Column1 = positions.get(1);
		NodePosition line5Column5 = positions.get(2);
		NodePosition line6Column3 = positions.get(3);
		NodePosition line6Column16 = positions.get(4);
		assertThat(line3Column3.compareTo(line4Column1)).isNegative();
		assertThat(line4Column1.compareTo(line3Column3)).isPositive();
		assertThat(line5Column5.compareTo(line6Column3)).isNegative();
		assertThat(line6Column3.compareTo(line6Column16)).isNegative();
		assertThat(line6Column16.compareTo(line6Column3)).isPositive();
		assertThat(line6Column3
				.compareTo(NodePosition.getPositionOf(StaticJavaParser.parse(LOOPS).findAll(ForStmt.class).get(3))))
						.isZero();
	}

	/**
	 * A node built by hand has no range and says so, a parsed node prints both
	 * ends.
	 */
	@Test
	void describesANodeWithoutRange() {
		assertThat(NodePosition.getPositionOf(new ForStmt()).toString()).isEqualTo(localized("ast.method.to_string",
				localized("ast.check.not_has_begin"), localized("ast.check.not_has_end")));
		assertThat(loopPositions().get(0).toString()).isEqualTo(localized("ast.method.to_string",
				localized("ast.check.has_begin_end", 3, 3), localized("ast.check.has_begin_end", 3, 13)));
	}
}
