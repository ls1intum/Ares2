package de.tum.cit.ase.ares.api.policy.director.java;

import java.nio.file.Path;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.director.SecurityPolicyDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.JavaTestCaseFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml.EssentialDataYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.JavaExecuter;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProgrammingExerciseProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.JavaWriter;
import de.tum.cit.ase.ares.api.util.FileTools;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

/**
 * Java security policy director.
 * <p>
 * Description: A director for creating security test cases for Java projects
 * based on a given SecurityPolicy. It creates and configures instances of
 * JavaTestCaseFactoryAndBuilder using information extracted from the provided
 * security policy. If no security policy is provided, a default configuration
 * is used.
 * <p>
 * Design Rationale: This class encapsulates the logic required to select the
 * appropriate Java build mode, architecture mode, and AOP mode based on the
 * security policy. It utilises a switch expression to map each
 * ProgrammingLanguageConfiguration to a corresponding test case factory
 * configuration, and the use of the essential classes YAML reader abstracts the
 * details of reading supporting metadata, adhering to the Single Responsibility
 * Principle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class SecurityPolicyJavaDirector extends SecurityPolicyDirector {

	/**
	 * Default path to the essential packages YAML file.
	 */
	@Nonnull
	public static final Path DEFAULT_ESSENTIAL_PACKAGES_PATH = Objects.requireNonNull(FileTools
			.resolveFileOnSourceDirectory("configuration", "essentialFiles", "java", "EssentialPackages.yaml"));

	/**
	 * Default path to the essential classes YAML file.
	 */
	@Nonnull
	public static final Path DEFAULT_ESSENTIAL_CLASSES_PATH = Objects.requireNonNull(
			FileTools.resolveFileOnSourceDirectory("configuration", "essentialFiles", "java", "EssentialClasses.yaml"));

	// <editor-fold desc="Constructor">

	/**
	 * Constructs a new SecurityPolicyJavaDirector with provided dependencies.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param creator               the creator for Java security test cases; must
	 *                              not be null.
	 * @param writer                the writer for Java security test cases; must
	 *                              not be null.
	 * @param executer              the executer for Java security test cases; must
	 *                              not be null.
	 * @param essentialDataReader   the reader for essential data files; must not be
	 *                              null.
	 * @param projectScanner        the scanner for Java programming exercises; must
	 *                              not be null.
	 * @param essentialPackagesPath the path to the essential packages YAML file;
	 *                              must not be null.
	 * @param essentialClassesPath  the path to the essential classes YAML file;
	 *                              must not be null.
	 */
	public SecurityPolicyJavaDirector(@Nonnull JavaCreator creator, @Nonnull JavaWriter writer,
			@Nonnull JavaExecuter executer, @Nonnull EssentialDataYAMLReader essentialDataReader,
			@Nonnull JavaProjectScanner projectScanner, @Nonnull Path essentialPackagesPath,
			@Nonnull Path essentialClassesPath) {
		super(creator, writer, executer, essentialDataReader, projectScanner, essentialPackagesPath,
				essentialClassesPath);
	}
	// </editor-fold>

	// <editor-fold desc="Create security test cases methods">

	private TestCaseAbstractFactoryAndBuilder generateFactoryAndBuilder(@Nullable BuildMode buildMode,
			@Nullable ArchitectureMode architectureMode, @Nullable AOPMode aopMode,
			@Nullable SecurityPolicy securityPolicy, @Nullable Path projectPath, @Nonnull JavaCreator effectiveCreator,
			@Nonnull JavaProjectScanner scanner, @Nonnull JavaWriter effectiveWriter) {
		return JavaTestCaseFactoryAndBuilder.builder().creator(effectiveCreator).writer(effectiveWriter)
				.executer((JavaExecuter) executer).essentialDataReader(essentialDataReader).projectScanner(scanner)
				.essentialPackagesPath(essentialPackagesPath).essentialClassesPath(essentialClassesPath)
				.buildMode(buildMode).architectureMode(architectureMode).aopMode(aopMode).securityPolicy(securityPolicy)
				.projectPath(projectPath).build();
	}

	/**
	 * Creates security test cases based on the provided security policy and project
	 * path.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param securityPolicy    the security policy to base test case creation on;
	 *                          may be null.
	 * @param projectFolderPath the project directory path where test cases will be
	 *                          applied; may be null.
	 * @return a non-null instance of TestCaseAbstractFactoryAndBuilder configured
	 *         for Java security tests.
	 */
	@Nonnull
	@Override
	public TestCaseAbstractFactoryAndBuilder createTestCases(@Nullable SecurityPolicy securityPolicy,
			@Nullable Path projectFolderPath) {
		return createTestCases(securityPolicy, projectFolderPath, Path.of(""));
	}

	@Override
	@Nonnull
	public TestCaseAbstractFactoryAndBuilder createTestCases(@Nullable SecurityPolicy securityPolicy,
			@Nullable Path projectRootPath, @Nonnull Path withinPath) {
		Path root = projectRootPath == null ? Path.of("").toAbsolutePath() : projectRootPath;
		BuildMode selectedMode = securityPolicy == null ? null : buildModeOf(securityPolicy);
		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(root, selectedMode);
		JavaProjectScanner configuredScanner = projectScanner instanceof JavaProgrammingExerciseProjectScanner
				? new JavaProgrammingExerciseProjectScanner(configuration)
				: new JavaProjectScanner(configuration);
		JavaCreator configuredCreator = creator.getClass() == JavaCreator.class ? new JavaCreator(configuration)
				: (JavaCreator) creator;
		JavaWriter configuredWriter = new JavaWriter(configuration);
		if (securityPolicy == null) {
			return generateFactoryAndBuilder(configuration.buildMode(), ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
					null, withinPath, configuredCreator, configuredScanner, configuredWriter);
		}
		@Nonnull
		ProgrammingLanguageConfiguration config = Objects.requireNonNull(
				securityPolicy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed());
		return switch (config) {
		case JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ -> generateFactoryAndBuilder(BuildMode.MAVEN,
				ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, securityPolicy, withinPath, configuredCreator,
				configuredScanner, configuredWriter);
		case JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION -> generateFactoryAndBuilder(BuildMode.MAVEN,
				ArchitectureMode.ARCHUNIT, AOPMode.INSTRUMENTATION, securityPolicy, withinPath, configuredCreator,
				configuredScanner, configuredWriter);
		case JAVA_USING_MAVEN_WALA_AND_ASPECTJ -> generateFactoryAndBuilder(BuildMode.MAVEN, ArchitectureMode.WALA,
				AOPMode.ASPECTJ, securityPolicy, withinPath, configuredCreator, configuredScanner, configuredWriter);
		case JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION -> generateFactoryAndBuilder(BuildMode.MAVEN,
				ArchitectureMode.WALA, AOPMode.INSTRUMENTATION, securityPolicy, withinPath, configuredCreator,
				configuredScanner, configuredWriter);
		case JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ -> generateFactoryAndBuilder(BuildMode.GRADLE,
				ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, securityPolicy, withinPath, configuredCreator,
				configuredScanner, configuredWriter);
		case JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION -> generateFactoryAndBuilder(BuildMode.GRADLE,
				ArchitectureMode.ARCHUNIT, AOPMode.INSTRUMENTATION, securityPolicy, withinPath, configuredCreator,
				configuredScanner, configuredWriter);
		case JAVA_USING_GRADLE_WALA_AND_ASPECTJ -> generateFactoryAndBuilder(BuildMode.GRADLE, ArchitectureMode.WALA,
				AOPMode.ASPECTJ, securityPolicy, withinPath, configuredCreator, configuredScanner, configuredWriter);
		case JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION -> generateFactoryAndBuilder(BuildMode.GRADLE,
				ArchitectureMode.WALA, AOPMode.INSTRUMENTATION, securityPolicy, withinPath, configuredCreator,
				configuredScanner, configuredWriter);
		};
	}

	private BuildMode buildModeOf(SecurityPolicy policy) {
		return switch (policy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed()) {
		case JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_MAVEN_WALA_AND_ASPECTJ, JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION -> BuildMode.MAVEN;
		case JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ, JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_GRADLE_WALA_AND_ASPECTJ, JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION -> BuildMode.GRADLE;
		};
	}
	// </editor-fold>

	// <editor-fold desc="Builder">
	public static Builder javaBuilder() {
		return new Builder();
	}

	public static class Builder {
		@Nullable
		private JavaCreator creator;
		@Nullable
		private JavaWriter writer;
		@Nullable
		private JavaExecuter executer;
		@Nullable
		private EssentialDataYAMLReader essentialDataReader;
		@Nullable
		private JavaProjectScanner javaScanner;
		@Nullable
		private Path essentialPackagesPath;
		@Nullable
		private Path essentialClassesPath;

		@Nonnull
		public Builder creator(@Nonnull JavaCreator creator) {
			this.creator = Objects.requireNonNull(creator, "creator must not be null");
			return this;
		}

		@Nonnull
		public Builder writer(@Nonnull JavaWriter writer) {
			this.writer = Objects.requireNonNull(writer, "writer must not be null");
			return this;
		}

		@Nonnull
		public Builder executer(@Nonnull JavaExecuter executer) {
			this.executer = Objects.requireNonNull(executer, "executer must not be null");
			return this;
		}

		@Nonnull
		public Builder essentialDataReader(@Nonnull EssentialDataYAMLReader essentialDataReader) {
			this.essentialDataReader = Objects.requireNonNull(essentialDataReader,
					"essentialDataReader must not be null");
			return this;
		}

		@Nonnull
		public Builder javaScanner(@Nonnull JavaProjectScanner javaScanner) {
			this.javaScanner = Objects.requireNonNull(javaScanner, "javaScanner must not be null");
			return this;
		}

		@Nonnull
		public Builder essentialPackagesPath(@Nonnull Path essentialPackagesPath) {
			this.essentialPackagesPath = Objects.requireNonNull(essentialPackagesPath,
					"essentialPackagesPath must not be null");
			return this;
		}

		@Nonnull
		public Builder essentialClassesPath(@Nonnull Path essentialClassesPath) {
			this.essentialClassesPath = Objects.requireNonNull(essentialClassesPath,
					"essentialClassesPath must not be null");
			return this;
		}

		@Nonnull
		public SecurityPolicyJavaDirector build() {
			return new SecurityPolicyJavaDirector(Objects.requireNonNull(creator, "creator must not be null"),
					Objects.requireNonNull(writer, "writer must not be null"),
					Objects.requireNonNull(executer, "executer must not be null"),
					Objects.requireNonNull(essentialDataReader, "essentialDataReader must not be null"),
					Objects.requireNonNull(javaScanner, "javaScanner must not be null"),
					Objects.requireNonNull(essentialPackagesPath, "essentialPackagesPath must not be null"),
					Objects.requireNonNull(essentialClassesPath, "essentialClassesPath must not be null"));
		}
	}
	// </editor-fold>
}
