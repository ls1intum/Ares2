package de.tum.cit.ase.ares.api.securitytest.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
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
 * Checks, in all four mode combinations, when the factory lets a supervised
 * package Ares derived from the project become the boundary.
 * <p>
 * The scanner is real and reads a compiled project; the creator, writer and
 * executer are mocks, so what is observed is whether enforcement would be armed
 * or generated, not the enforcement itself. Without a policy the modes cannot
 * come from one, so they are set on the factory here.
 */
class JavaTestCaseFactoryAndBuilderDerivedScopeTest {

	/** The temporary project the scanner reads. */
	@TempDir
	Path projectRoot;

	/** The executer, which arms enforcement in Postcompile. */
	private final JavaExecuter executer = mock(JavaExecuter.class);

	/** The writer, which generates enforcement in Precompile. */
	private final JavaWriter writer = mock(JavaWriter.class);

	/** A project whose classes all lie in the package Ares derives is armed. */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,ASPECTJ", "ARCHUNIT,INSTRUMENTATION", "WALA,ASPECTJ", "WALA,INSTRUMENTATION" })
	void postcompileArmsADerivedScopeTheOutputConfirms(ArchitectureMode analysis, AOPMode aop) throws IOException {
		write("de/tum/cit/aet/Assignment.java", "package de.tum.cit.aet; public class Assignment { }");
		write("de/tum/cit/aet/helper/Support.java", "package de.tum.cit.aet.helper; public class Support { }");

		factory(analysis, aop, null).executeTestCases();

		verify(executer).executeTestCases(eq(BuildMode.GRADLE), eq(analysis), eq(aop), anyList(), anyList(), anyList(),
				eq("de.tum.cit.aet"), any(), anyList(), anyList());
	}

	/**
	 * Nested packages yield their parent, whichever holds more files, and a group
	 * in gradle.properties covering two disjoint children yields that group. Both
	 * are armed with that package.
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,ASPECTJ,false", "ARCHUNIT,INSTRUMENTATION,false", "WALA,ASPECTJ,false",
			"WALA,INSTRUMENTATION,false", "ARCHUNIT,ASPECTJ,true", "ARCHUNIT,INSTRUMENTATION,true", "WALA,ASPECTJ,true",
			"WALA,INSTRUMENTATION,true" })
	void postcompileArmsTheParentOfNestedPackages(ArchitectureMode analysis, AOPMode aop, boolean group)
			throws IOException {
		if (group) {
			Files.writeString(projectRoot.resolve("gradle.properties"), "group=de.tum.cit.aet\n");
			write("de/tum/cit/aet/model/Model.java", "package de.tum.cit.aet.model; public class Model { }");
			write("de/tum/cit/aet/view/View.java", "package de.tum.cit.aet.view; public class View { }");
		} else {
			write("de/tum/cit/aet/Assignment.java", "package de.tum.cit.aet; public class Assignment { }");
			write("de/tum/cit/aet/util/First.java", "package de.tum.cit.aet.util; public class First { }");
			write("de/tum/cit/aet/util/Second.java", "package de.tum.cit.aet.util; public class Second { }");
		}

		factory(analysis, aop, null).executeTestCases();

		verify(executer).executeTestCases(eq(BuildMode.GRADLE), eq(analysis), eq(aop), anyList(), anyList(), anyList(),
				eq("de.tum.cit.aet"), any(), anyList(), anyList());
	}

	/**
	 * Nothing compiled, or only a package descriptor, leaves nothing to supervise.
	 * That is accepted with a warning, as an exercise with an empty package to fill
	 * is in exactly that state.
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,ASPECTJ,false", "ARCHUNIT,INSTRUMENTATION,false", "WALA,ASPECTJ,false",
			"WALA,INSTRUMENTATION,false", "ARCHUNIT,ASPECTJ,true", "ARCHUNIT,INSTRUMENTATION,true", "WALA,ASPECTJ,true",
			"WALA,INSTRUMENTATION,true" })
	void postcompileArmsAnOutputWithNothingToSupervise(ArchitectureMode analysis, AOPMode aop, boolean descriptor)
			throws IOException {
		if (descriptor) {
			write("de/tum/cit/aet/package-info.java", "@Deprecated package de.tum.cit.aet;");
		}

		factory(analysis, aop, null).executeTestCases();

		verify(executer).executeTestCases(eq(BuildMode.GRADLE), eq(analysis), eq(aop), anyList(), anyList(), anyList(),
				any(), any(), anyList(), anyList());
	}

	/**
	 * The attack: more classes in a package of the submitter's choosing make it the
	 * derived package, and the assignment would run outside it.
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,ASPECTJ", "ARCHUNIT,INSTRUMENTATION", "WALA,ASPECTJ", "WALA,INSTRUMENTATION" })
	void postcompileRefusesADecoyBeforeArming(ArchitectureMode analysis, AOPMode aop) throws IOException {
		write("de/tum/cit/aet/Assignment.java", "package de.tum.cit.aet; public class Assignment { }");
		write("decoy/First.java", "package decoy; public class First { }");
		write("decoy/Second.java", "package decoy; public class Second { }");
		JavaTestCaseFactoryAndBuilder factory = factory(analysis, aop, null);

		SecurityException refusal = assertThrows(SecurityException.class, factory::executeTestCases);

		assertTrue(refusal.getMessage().contains("de.tum.cit.aet"), refusal.getMessage());
		verify(executer, never()).executeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				anyList(), anyList());
	}

	/** A class file the check could not read is code it never saw. */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,ASPECTJ", "ARCHUNIT,INSTRUMENTATION", "WALA,ASPECTJ", "WALA,INSTRUMENTATION" })
	void postcompileRefusesAnUnreadableClassFileBeforeArming(ArchitectureMode analysis, AOPMode aop)
			throws IOException {
		write("de/tum/cit/aet/Assignment.java", "package de.tum.cit.aet; public class Assignment { }");
		Files.writeString(Files.createDirectories(output().resolve("decoy")).resolve("Hidden.class"), "garbage");
		JavaTestCaseFactoryAndBuilder factory = factory(analysis, aop, null);

		assertThrows(SecurityException.class, factory::executeTestCases);

		verify(executer, never()).executeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				anyList(), anyList());
	}

	/**
	 * Precompile writes the package into generated checks before anything is
	 * compiled, so a derived one is refused before the writer is reached.
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,ASPECTJ", "ARCHUNIT,INSTRUMENTATION", "WALA,ASPECTJ", "WALA,INSTRUMENTATION" })
	void precompileRefusesADerivedScope(ArchitectureMode analysis, AOPMode aop) throws IOException {
		write("de/tum/cit/aet/Assignment.java", "package de.tum.cit.aet; public class Assignment { }");
		JavaTestCaseFactoryAndBuilder factory = factory(analysis, aop, null);

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> factory.writeTestCases(projectRoot.resolve("src/test/java")));

		assertTrue(refusal.getMessage().contains("theSupervisedCodeUsesTheFollowingPackage"), refusal.getMessage());
		verify(writer, never()).writeTestCases(any(), any(), any(), anyList(), anyList(), anyList(), any(), any(),
				anyList(), anyList(), anyList(), any(), any());
	}

	/**
	 * A pinned package may deliberately supervise part of the output. Neither path
	 * second-guesses it, so it is armed and generated as the instructor wrote it.
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,ASPECTJ", "ARCHUNIT,INSTRUMENTATION", "WALA,ASPECTJ", "WALA,INSTRUMENTATION" })
	void aPinnedPackageMaySuperviseOnlyPartOfTheOutput(ArchitectureMode analysis, AOPMode aop) throws IOException {
		write("de/tum/cit/aet/part/Assignment.java", "package de.tum.cit.aet.part; public class Assignment { }");
		write("other/First.java", "package other; public class First { }");
		write("other/Second.java", "package other; public class Second { }");
		JavaTestCaseFactoryAndBuilder factory = factory(analysis, aop, pinned("de.tum.cit.aet.part"));

		factory.executeTestCases();
		factory.writeTestCases(projectRoot.resolve("src/test/java"));

		verify(executer).executeTestCases(eq(BuildMode.GRADLE), eq(analysis), eq(aop), anyList(), anyList(), anyList(),
				eq("de.tum.cit.aet.part"), any(), anyList(), anyList());
		verify(writer).writeTestCases(eq(BuildMode.GRADLE), eq(analysis), eq(aop), anyList(), anyList(), anyList(),
				eq("de.tum.cit.aet.part"), any(), anyList(), anyList(), anyList(), any(), any());
	}

	/**
	 * Builds the factory over the temporary project with a real scanner.
	 *
	 * @param analysis the architecture mode
	 * @param aop      the AOP mode
	 * @param policy   the policy, or null for a derived package
	 * @return the factory
	 * @throws IOException if the project layout cannot be created
	 */
	private JavaTestCaseFactoryAndBuilder factory(ArchitectureMode analysis, AOPMode aop, SecurityPolicy policy)
			throws IOException {
		BuildToolConfiguration configuration = new BuildToolConfiguration(BuildMode.GRADLE, projectRoot,
				List.of(sources()), List.of(Files.createDirectories(projectRoot.resolve("src/test/java"))), output(),
				Files.createDirectories(projectRoot.resolve("build/classes/java/test")));
		EssentialPackages packages = mock(EssentialPackages.class);
		when(packages.getEssentialPackages()).thenReturn(List.of());
		EssentialClasses classes = mock(EssentialClasses.class);
		when(classes.getEssentialClasses()).thenReturn(List.of());
		EssentialDataReader reader = mock(EssentialDataReader.class);
		when(reader.readEssentialPackagesFrom(any())).thenReturn(packages);
		when(reader.readEssentialClassesFrom(any())).thenReturn(classes);
		return new JavaTestCaseFactoryAndBuilder(mock(JavaCreator.class), writer, executer, reader,
				new JavaProjectScanner(configuration), Path.of("packages.yaml"), Path.of("classes.yaml"),
				BuildMode.GRADLE, analysis, aop, policy, null);
	}

	/**
	 * A policy pinning the given package.
	 *
	 * @param packageName the pinned package
	 * @return the policy
	 */
	private static SecurityPolicy pinned(String packageName) {
		return new SecurityPolicy(SecurityPolicy.CURRENT_POLICY_VERSION,
				new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ, packageName,
						"Assignment", List.of(), ResourceAccesses.createRestrictive()));
	}

	/**
	 * Writes a production source and compiles it into the production output.
	 *
	 * @param relativePath the source path below the source root
	 * @param source       the Java source
	 * @throws IOException if the fixture cannot be written
	 */
	private void write(String relativePath, String source) throws IOException {
		Path file = sources().resolve(relativePath);
		Files.createDirectories(file.getParent());
		Files.writeString(file, source);
		int status = ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", output().toString(),
				file.toString());
		assertEquals(0, status, "the fixture did not compile: " + relativePath);
	}

	/** The production source root, created on first use. */
	private Path sources() throws IOException {
		return Files.createDirectories(projectRoot.resolve("src/main/java"));
	}

	/** The production output root, created on first use. */
	private Path output() throws IOException {
		return Files.createDirectories(projectRoot.resolve("build/classes/java/main"));
	}
}
