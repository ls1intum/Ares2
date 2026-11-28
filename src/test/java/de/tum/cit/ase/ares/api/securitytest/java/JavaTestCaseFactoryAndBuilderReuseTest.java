package de.tum.cit.ase.ares.api.securitytest.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
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
 * Checks when the factory lets a later phase of a test arm its test cases again
 * without analysing, and that arming again runs every check before arming and
 * installs the same settings, the analysis left out.
 */
class JavaTestCaseFactoryAndBuilderReuseTest {

	/** The creator, whose inputs the factory asks about. */
	private JavaCreator creator;

	/** The executer that arms the settings. */
	private JavaExecuter executer;

	/** The reader of the essential packages and classes. */
	private EssentialDataReader reader;

	/** The essential packages the reader answers. */
	private EssentialPackages packages;

	/** The architecture case the mocked creator adds. */
	private JavaArchitectureTestCase architectureCase;

	/** The AOP case the mocked creator adds. */
	private JavaAOPTestCase aopCase;

	/** Builds the mocks; the creator adds one architecture and one AOP case. */
	@BeforeEach
	void setUp() {
		creator = mock(JavaCreator.class);
		executer = mock(JavaExecuter.class);
		packages = mock(EssentialPackages.class);
		when(packages.getEssentialPackages()).thenReturn(List.of("java.lang"));
		EssentialClasses classes = mock(EssentialClasses.class);
		when(classes.getEssentialClasses()).thenReturn(List.of());
		reader = mock(EssentialDataReader.class);
		when(reader.readEssentialPackagesFrom(any())).thenReturn(packages);
		when(reader.readEssentialClassesFrom(any())).thenReturn(classes);
		when(creator.createdFromUnchangedInput(any())).thenReturn(true);
		when(creator.exemptedClassNames()).thenReturn(List.of());
		architectureCase = mock(JavaArchitectureTestCase.class);
		aopCase = mock(JavaAOPTestCase.class);
		addCreatedCases(architectureCase);
	}

	/**
	 * Lets the mocked creator add the given architecture case and the AOP case.
	 *
	 * @param createdArchitectureCase the architecture case to add
	 */
	private void addCreatedCases(JavaArchitectureTestCase createdArchitectureCase) {
		doAnswer(invocation -> {
			List<ArchitectureTestCase> architectureCases = invocation.getArgument(8);
			architectureCases.add(createdArchitectureCase);
			List<AOPTestCase> aopCases = invocation.getArgument(9);
			aopCases.add(aopCase);
			return null;
		}).when(creator).createTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(), anyList(),
				anyList(), anyList(), any(), any(), anyBoolean());
	}

	/**
	 * Builds a factory for a pinned package, or for a derived one without policy.
	 *
	 * @param architectureMode the architecture mode
	 * @param pinned           whether a policy pins the package
	 * @return the factory
	 */
	private JavaTestCaseFactoryAndBuilder factory(ArchitectureMode architectureMode, boolean pinned) {
		JavaProjectScanner scanner = mock(JavaProjectScanner.class);
		when(scanner.scanForPackageName()).thenReturn("example.task");
		when(scanner.scanForMainClassInPackage()).thenReturn("Main");
		when(scanner.scanForTestClasses()).thenReturn(new String[0]);
		SecurityPolicy policy = pinned
				? new SecurityPolicy(SecurityPolicy.CURRENT_POLICY_VERSION,
						new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
								"example.task", "Main", List.of(), ResourceAccesses.createRestrictive()))
				: null;
		return new JavaTestCaseFactoryAndBuilder(creator, mock(JavaWriter.class), executer, reader, scanner,
				Path.of("packages.yaml"), Path.of("classes.yaml"), BuildMode.MAVEN, architectureMode, AOPMode.ASPECTJ,
				policy, null);
	}

	/**
	 * Arming again runs the reserved-package check and every check that precedes
	 * arming, in the order a full run does, and hands the executer the AOP cases
	 * but no architecture case.
	 */
	@Test
	@SuppressWarnings("unchecked")
	void armingAgainChecksAndArmsButDoesNotAnalyse() {
		JavaTestCaseFactoryAndBuilder pinnedFactory = factory(ArchitectureMode.ARCHUNIT, true);

		pinnedFactory.rearmTestCases();

		InOrder order = inOrder(creator, executer);
		order.verify(creator).requireNoReservedSupervisedClass();
		order.verify(creator).requireSupervisedClasses(BuildMode.MAVEN);
		order.verify(creator).requireNoProductionClassNamedLikeAnExemptedOne(BuildMode.MAVEN);
		order.verify(creator).requireWovenSupervisedClasses(AOPMode.ASPECTJ);
		ArgumentCaptor<List<JavaArchitectureTestCase>> architectureCases = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List<JavaAOPTestCase>> aopCases = ArgumentCaptor.forClass(List.class);
		order.verify(executer).executeTestCases(eq(BuildMode.MAVEN), eq(ArchitectureMode.ARCHUNIT), eq(AOPMode.ASPECTJ),
				anyList(), anyList(), anyList(), eq("example.task"), eq("Main"), architectureCases.capture(),
				aopCases.capture());
		assertEquals(List.of(), architectureCases.getValue());
		assertEquals(List.of(aopCase), aopCases.getValue());
	}

	/** A refused check stops arming again before anything is armed. */
	@Test
	void aRefusedCheckStopsArmingAgain() {
		JavaTestCaseFactoryAndBuilder pinnedFactory = factory(ArchitectureMode.ARCHUNIT, true);
		doThrow(new SecurityException("not woven")).when(creator).requireWovenSupervisedClasses(AOPMode.ASPECTJ);

		assertThrows(SecurityException.class, pinnedFactory::rearmTestCases);

		verify(executer, never()).executeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				anyList(), anyList());
	}

	/** The analysis applies again only while the essential lists are unchanged. */
	@Test
	void changedEssentialListsEndTheReuse() {
		JavaTestCaseFactoryAndBuilder pinnedFactory = factory(ArchitectureMode.ARCHUNIT, true);
		assertTrue(pinnedFactory.analysisStillApplies());

		when(packages.getEssentialPackages()).thenReturn(List.of("java.lang", "java.util"));

		assertFalse(pinnedFactory.analysisStillApplies());
	}

	/** Changed input of the creator ends the reuse. */
	@Test
	void changedCreatorInputEndsTheReuse() {
		JavaTestCaseFactoryAndBuilder pinnedFactory = factory(ArchitectureMode.ARCHUNIT, true);
		when(creator.createdFromUnchangedInput(ArchitectureMode.ARCHUNIT)).thenReturn(false);

		assertFalse(pinnedFactory.analysisStillApplies());
	}

	/** Test cases without a policy, whose scope was derived, are never reused. */
	@Test
	void aDerivedScopeIsNeverReused() {
		assertFalse(factory(ArchitectureMode.ARCHUNIT, false).analysisStillApplies());
	}

	/**
	 * In WALA mode the analysis applies again only while every created case, as the
	 * creator builds it, reports the inputs of its last WALA verdict unchanged.
	 */
	@Test
	void aWalaVerdictWithChangedInputsEndsTheReuse() {
		when(creator.analysedClasses()).thenReturn(new ClassFileImporter().importClasses());
		JavaTestCaseFactoryAndBuilder walaFactory = factory(ArchitectureMode.WALA, true);

		when(architectureCase.verdictInputsUnchanged(anyString())).thenReturn(true);
		assertTrue(walaFactory.analysisStillApplies());
		when(architectureCase.verdictInputsUnchanged(anyString())).thenReturn(false);
		assertFalse(walaFactory.analysisStillApplies());
	}

	/** In ArchUnit mode there is no WALA verdict whose inputs could change. */
	@Test
	void anArchUnitRunAsksNoWalaInputs() {
		assertTrue(factory(ArchitectureMode.ARCHUNIT, true).analysisStillApplies());
		verify(architectureCase, never()).verdictInputsUnchanged(anyString());
	}
}
