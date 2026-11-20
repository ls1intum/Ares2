package de.tum.cit.ase.ares.api.securitytest.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.phobos.PhobosTestCase;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
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

	/** The scanner, which answers a fixed package as if derived. */
	private JavaProjectScanner scanner;

	/** The reader of the essential packages and classes. */
	private EssentialDataReader reader;

	/** The factory under test. */
	private JavaTestCaseFactoryAndBuilder factory;

	/** Builds the factory around mocks, without a policy, as a derived scope. */
	@BeforeEach
	void setUp() {
		creator = mock(JavaCreator.class);
		writer = mock(JavaWriter.class);
		executer = mock(JavaExecuter.class);
		scanner = mock(JavaProjectScanner.class);
		when(scanner.scanForPackageName()).thenReturn("example.nest");
		when(scanner.scanForMainClassInPackage()).thenReturn("Main");
		when(scanner.scanForTestClasses()).thenReturn(new String[] { "example.nest.NestHostFixture" });
		when(creator.exemptedClassNames()).thenReturn(EXEMPTED);
		EssentialPackages packages = mock(EssentialPackages.class);
		when(packages.getEssentialPackages()).thenReturn(List.of());
		EssentialClasses classes = mock(EssentialClasses.class);
		when(classes.getEssentialClasses()).thenReturn(List.of());
		reader = mock(EssentialDataReader.class);
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

	/**
	 * The writer gets the same exempted names. Precompile refuses a derived scope,
	 * so this factory pins the package in a policy.
	 */
	@Test
	void theWriterGetsTheExemptedNames() {
		SecurityPolicy pinned = new SecurityPolicy(SecurityPolicy.CURRENT_POLICY_VERSION,
				new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
						"example.nest", "Main", List.of("example.nest.NestHostFixture"),
						ResourceAccesses.createRestrictive()));
		JavaTestCaseFactoryAndBuilder pinnedFactory = new JavaTestCaseFactoryAndBuilder(creator, writer, executer,
				reader, scanner, Path.of("packages.yaml"), Path.of("classes.yaml"), BuildMode.MAVEN,
				ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, pinned, null);
		pinnedFactory.writeTestCases(Path.of("target/generated"));
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

	/** A scope that missed the compiled classes stops the run before arming. */
	@Test
	void aScopeWithoutClassesStopsTheRunBeforeArming() {
		doThrow(new SecurityException("no classes")).when(creator).requireSupervisedClasses(BuildMode.MAVEN);
		assertThrows(SecurityException.class, factory::executeTestCases);
		verify(executer, never()).executeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				anyList(), anyList());
	}

	/** The architecture case the mocked creator adds. */
	private JavaArchitectureTestCase architectureCase;

	/** The AOP case the mocked creator adds. */
	private JavaAOPTestCase aopCase;

	/** The Phobos case the mocked creator adds. */
	private JavaPhobosTestCase phobosCase;

	/**
	 * Builds a factory with a pinned package, whose creator adds one case of each
	 * kind to the lists it is given.
	 *
	 * @return the factory, with its scope not derived
	 */
	private JavaTestCaseFactoryAndBuilder pinnedFactoryWithCreatedCases() {
		architectureCase = mock(JavaArchitectureTestCase.class);
		aopCase = mock(JavaAOPTestCase.class);
		phobosCase = mock(JavaPhobosTestCase.class);
		doAnswer(invocation -> {
			List<ArchitectureTestCase> architectureCases = invocation.getArgument(8);
			architectureCases.add(architectureCase);
			List<AOPTestCase> aopCases = invocation.getArgument(9);
			aopCases.add(aopCase);
			List<PhobosTestCase> phobosCases = invocation.getArgument(10);
			phobosCases.add(phobosCase);
			return null;
		}).when(creator).createTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(), anyList(),
				anyList(), anyList(), any(), any(), anyBoolean());
		SecurityPolicy pinned = new SecurityPolicy(SecurityPolicy.CURRENT_POLICY_VERSION,
				new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
						"example.nest", "Main", List.of("example.nest.NestHostFixture"),
						ResourceAccesses.createRestrictive()));
		return new JavaTestCaseFactoryAndBuilder(creator, writer, executer, reader, scanner, Path.of("packages.yaml"),
				Path.of("classes.yaml"), BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, pinned, null);
	}

	/**
	 * The writer gets exactly the three created cases, and the factory answers the
	 * very list the writer returned.
	 */
	@Test
	void writesTheCreatedCasesAndReturnsTheWritersFiles() {
		JavaTestCaseFactoryAndBuilder pinnedFactory = pinnedFactoryWithCreatedCases();
		List<Path> writerFiles = List.of(Path.of("Sentinel.java"));
		when(writer.writeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(), anyList(),
				anyList(), anyList(), any(), any())).thenReturn(writerFiles);

		List<Path> written = pinnedFactory.writeTestCases(Path.of("target/generated"));

		assertSame(writerFiles, written);
		ArgumentCaptor<List<JavaArchitectureTestCase>> architectureCases = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List<JavaAOPTestCase>> aopCases = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List<JavaPhobosTestCase>> phobosCases = ArgumentCaptor.forClass(List.class);
		verify(writer).writeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				architectureCases.capture(), aopCases.capture(), phobosCases.capture(), any(), any());
		assertEquals(List.of(architectureCase), architectureCases.getValue());
		assertEquals(List.of(aopCase), aopCases.getValue());
		assertEquals(List.of(phobosCase), phobosCases.getValue());
	}

	/** The executer gets exactly the architecture and the AOP case. */
	@Test
	void executesTheCreatedCases() {
		JavaTestCaseFactoryAndBuilder pinnedFactory = pinnedFactoryWithCreatedCases();

		pinnedFactory.executeTestCases();

		ArgumentCaptor<List<JavaArchitectureTestCase>> architectureCases = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List<JavaAOPTestCase>> aopCases = ArgumentCaptor.forClass(List.class);
		verify(executer).executeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				architectureCases.capture(), aopCases.capture());
		assertEquals(List.of(architectureCase), architectureCases.getValue());
		assertEquals(List.of(aopCase), aopCases.getValue());
	}
}
