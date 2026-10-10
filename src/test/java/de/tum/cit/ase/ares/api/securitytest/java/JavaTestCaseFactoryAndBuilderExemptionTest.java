package de.tum.cit.ase.ares.api.securitytest.java;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialClasses;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialPackages;
import de.tum.cit.ase.ares.api.securitytest.java.executer.JavaExecuter;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.JavaWriter;

/**
 * Checks that the factory hands the creator's exempted names, nest members
 * included, to both the writer and the executer, and refuses a production class
 * named like one of them before the executer arms anything.
 */
class JavaTestCaseFactoryAndBuilderExemptionTest {

	/** The exempted names the creator works out. */
	private static final List<String> EXEMPTED = List.of("example.nest.NestHostFixture",
			"example.nest.NestHostFixture$Member");

	/** The creator, whose exempted names and collision check the factory uses. */
	private JavaCreator creator;

	/** The writer that receives the exempted names in Precompile. */
	private JavaWriter writer;

	/** The executer that receives them in Postcompile. */
	private JavaExecuter executer;

	/** The factory under test. */
	private JavaTestCaseFactoryAndBuilder factory;

	/** Builds the factory around mocks, without a policy, as a derived scope. */
	@BeforeEach
	void setUp() {
		creator = mock(JavaCreator.class);
		writer = mock(JavaWriter.class);
		executer = mock(JavaExecuter.class);
		JavaProjectScanner scanner = mock(JavaProjectScanner.class);
		when(scanner.scanForPackageName()).thenReturn("example.nest");
		when(scanner.scanForMainClassInPackage()).thenReturn("Main");
		when(scanner.scanForTestClasses()).thenReturn(new String[] { "example.nest.NestHostFixture" });
		when(creator.exemptedClassNames()).thenReturn(EXEMPTED);
		EssentialPackages packages = mock(EssentialPackages.class);
		when(packages.getEssentialPackages()).thenReturn(List.of());
		EssentialClasses classes = mock(EssentialClasses.class);
		when(classes.getEssentialClasses()).thenReturn(List.of());
		EssentialDataReader reader = mock(EssentialDataReader.class);
		when(reader.readEssentialPackagesFrom(any())).thenReturn(packages);
		when(reader.readEssentialClassesFrom(any())).thenReturn(classes);
		factory = new JavaTestCaseFactoryAndBuilder(creator, writer, executer, reader, scanner,
				Path.of("packages.yaml"), Path.of("classes.yaml"), BuildMode.MAVEN, ArchitectureMode.ARCHUNIT,
				AOPMode.ASPECTJ, null, null);
	}

	/**
	 * The collision check runs first, then the executer gets the exempted names.
	 */
	@Test
	void theExecuterGetsTheExemptedNamesAfterTheCollisionCheck() {
		factory.executeTestCases();
		InOrder order = inOrder(creator, executer);
		order.verify(creator).requireNoProductionClassNamedLikeAnExemptedOne(BuildMode.MAVEN);
		order.verify(executer).executeTestCases(eq(BuildMode.MAVEN), eq(ArchitectureMode.ARCHUNIT), eq(AOPMode.ASPECTJ),
				anyList(), anyList(), eq(EXEMPTED), eq("example.nest"), eq("Main"), anyList(), anyList());
	}

	/** The writer gets the same exempted names. */
	@Test
	void theWriterGetsTheExemptedNames() {
		factory.writeTestCases(Path.of("target/generated"));
		verify(writer).writeTestCases(eq(BuildMode.MAVEN), eq(ArchitectureMode.ARCHUNIT), eq(AOPMode.ASPECTJ),
				anyList(), anyList(), eq(EXEMPTED), eq("example.nest"), eq("Main"), anyList(), anyList(), anyList(),
				any(), any());
	}

	/** A refused collision stops the run before the executer arms anything. */
	@Test
	void aRefusedCollisionStopsTheRunBeforeArming() {
		doThrow(new SecurityException("collision")).when(creator)
				.requireNoProductionClassNamedLikeAnExemptedOne(BuildMode.MAVEN);
		assertThrows(SecurityException.class, factory::executeTestCases);
		verify(executer, never()).executeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				anyList(), anyList());
	}
}
