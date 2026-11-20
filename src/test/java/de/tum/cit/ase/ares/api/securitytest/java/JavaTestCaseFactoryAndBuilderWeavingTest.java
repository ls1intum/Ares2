package de.tum.cit.ase.ares.api.securitytest.java;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
 * Checks that the factory asks the creator whether the supervised classes were
 * woven, with the selected runtime enforcement, before the executer arms
 * anything, and that a refusal stops the run there.
 */
class JavaTestCaseFactoryAndBuilderWeavingTest {

	/** The creator, whose weaving check the factory calls. */
	private JavaCreator creator;

	/** The executer, which arms enforcement. */
	private JavaExecuter executer;

	/**
	 * Builds the factory around mocks for the given runtime enforcement, without a
	 * policy.
	 *
	 * @param aopMode the runtime enforcement the factory is built for
	 * @return the factory under test
	 */
	private JavaTestCaseFactoryAndBuilder factoryFor(AOPMode aopMode) {
		creator = mock(JavaCreator.class);
		executer = mock(JavaExecuter.class);
		JavaProjectScanner scanner = mock(JavaProjectScanner.class);
		when(scanner.scanForPackageName()).thenReturn("example.nest");
		when(scanner.scanForMainClassInPackage()).thenReturn("Main");
		when(scanner.scanForTestClasses()).thenReturn(new String[0]);
		when(creator.exemptedClassNames()).thenReturn(List.of());
		EssentialPackages packages = mock(EssentialPackages.class);
		when(packages.getEssentialPackages()).thenReturn(List.of());
		EssentialClasses classes = mock(EssentialClasses.class);
		when(classes.getEssentialClasses()).thenReturn(List.of());
		EssentialDataReader reader = mock(EssentialDataReader.class);
		when(reader.readEssentialPackagesFrom(any())).thenReturn(packages);
		when(reader.readEssentialClassesFrom(any())).thenReturn(classes);
		return new JavaTestCaseFactoryAndBuilder(creator, mock(JavaWriter.class), executer, reader, scanner,
				Path.of("packages.yaml"), Path.of("classes.yaml"), BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, aopMode,
				null, null);
	}

	/**
	 * The weaving check gets the selected enforcement and runs before arming.
	 *
	 * @param aopMode the runtime enforcement under test
	 */
	@ParameterizedTest
	@EnumSource(AOPMode.class)
	void theWeavingCheckRunsBeforeArming(AOPMode aopMode) {
		factoryFor(aopMode).executeTestCases();
		InOrder order = inOrder(creator, executer);
		order.verify(creator).requireWovenSupervisedClasses(aopMode);
		order.verify(executer).executeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				anyList(), anyList());
	}

	/** A refused weaving check stops the run before the executer arms anything. */
	@ParameterizedTest
	@EnumSource(AOPMode.class)
	void aRefusedWeavingCheckStopsTheRunBeforeArming(AOPMode aopMode) {
		JavaTestCaseFactoryAndBuilder factory = factoryFor(aopMode);
		doThrow(new SecurityException("not woven")).when(creator).requireWovenSupervisedClasses(aopMode);
		assertThrows(SecurityException.class, factory::executeTestCases);
		verify(executer, never()).executeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				anyList(), anyList());
	}
}
