package de.tum.cit.ase.ares.api.policy.director;

import java.nio.file.Path;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.director.java.SecurityPolicyJavaDirector;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.creator.Creator;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml.EssentialDataYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.Executer;
import de.tum.cit.ase.ares.api.securitytest.java.executer.JavaExecuter;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProgrammingExerciseProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.ProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.JavaWriter;
import de.tum.cit.ase.ares.api.securitytest.java.writer.Writer;

/**
 * Abstract class for directing the creation of security test cases.
 * <p>
 * Description: This abstract class defines the contract for a director that
 * creates security test cases based on a given SecurityPolicy, leaving the
 * instantiation and configuration to a concrete implementation that works with
 * an abstract factory.
 * <p>
 * Design Rationale: Keeping test case creation behind this class decouples it
 * from the rest of the system.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public abstract class SecurityPolicyDirector {

	// <editor-fold desc="Attributes">
	/**
	 * Creator for Java security test cases.
	 */
	@Nonnull
	protected final Creator creator;

	/**
	 * Writer for Java security test cases.
	 */
	@Nonnull
	protected final Writer writer;

	/**
	 * Executer for Java security test cases.
	 */
	@Nonnull
	protected final Executer executer;

	/**
	 * Reader for essential classes YAML files.
	 */
	@Nonnull
	protected final EssentialDataReader essentialDataReader;

	/**
	 * Scanner for Java programming exercises.
	 */
	@Nonnull
	protected final ProjectScanner projectScanner;

	/**
	 * Path to the essential packages YAML file.
	 */
	@Nonnull
	protected final Path essentialPackagesPath;

	/**
	 * Path to the essential classes YAML file.
	 */
	@Nonnull
	protected final Path essentialClassesPath;
	// </editor-fold>

	// <editor-fold desc="Constructor">
	/**
	 * Constructs a new SecurityPolicyDirector with provided dependencies.
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
	public SecurityPolicyDirector(@Nonnull Creator creator, @Nonnull Writer writer, @Nonnull Executer executer,
			@Nonnull EssentialDataReader essentialDataReader, @Nonnull ProjectScanner projectScanner,
			@Nonnull Path essentialPackagesPath, @Nonnull Path essentialClassesPath) {
		this.creator = Objects.requireNonNull(creator, "creator must not be null");
		this.writer = Objects.requireNonNull(writer, "writer must not be null");
		this.executer = Objects.requireNonNull(executer, "executer must not be null");
		this.essentialDataReader = Objects.requireNonNull(essentialDataReader, "essentialDataReader must not be null");
		this.projectScanner = Objects.requireNonNull(projectScanner, "javaScanner must not be null");
		this.essentialPackagesPath = Objects.requireNonNull(essentialPackagesPath,
				"essentialPackagesPath must not be null");
		this.essentialClassesPath = Objects.requireNonNull(essentialClassesPath,
				"essentialClassesPath must not be null");
	}
	// </editor-fold>

	// <editor-fold desc="Test case creation">
	/**
	 * Creates and configures security test cases based on the provided
	 * SecurityPolicy and project path.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param securityPolicy  the SecurityPolicy driving the test case creation; may
	 *                        be null if no policy is provided.
	 * @param projectRootPath the project root directory path where test cases
	 *                        should be applied; may be null.
	 * @return a non-null instance of TestCaseAbstractFactoryAndBuilder configured
	 *         according to the security policy.
	 */
	@Nonnull
	public abstract TestCaseAbstractFactoryAndBuilder createTestCases(@Nullable SecurityPolicy securityPolicy,
			@Nullable Path projectRootPath);

	/**
	 * Creates security test cases scoped to {@code withinPath}.
	 * <p>
	 * This default, kept concrete for subclasses built against 2.1.0, refuses any
	 * scope but the empty path, which means the whole project.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param securityPolicy  the policy; may be null.
	 * @param projectRootPath the project root; may be null.
	 * @param withinPath      the scope; must not be null.
	 * @return the configured factory.
	 * @throws UnsupportedOperationException for a non-empty scope.
	 */
	@Nonnull
	public TestCaseAbstractFactoryAndBuilder createTestCases(@Nullable SecurityPolicy securityPolicy,
			@Nullable Path projectRootPath, @Nonnull Path withinPath) {
		Objects.requireNonNull(withinPath, "withinPath must not be null");
		if (!withinPath.toString().isEmpty()) {
			throw new UnsupportedOperationException(
					getClass().getName() + " cannot scope the analysis to a path within the project; override"
							+ " createTestCases(SecurityPolicy, Path, Path) to honour withinPath");
		}
		return createTestCases(securityPolicy, projectRootPath);
	}
	// </editor-fold>

	// <editor-fold desc="Static methods">
	@Nonnull
	public static SecurityPolicyDirector selectSecurityPolicyDirector(@Nullable SecurityPolicy securityPolicy) {
		if (securityPolicy == null) {
			return buildJavaDirector();
		}
		return switch (securityPolicy.regardingTheSupervisedCode()
				.theFollowingProgrammingLanguageConfigurationIsUsed()) {
		case JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ, JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_GRADLE_WALA_AND_ASPECTJ, JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION, JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_MAVEN_WALA_AND_ASPECTJ, JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION -> buildJavaDirector();
		};
	}

	/**
	 * Builds the standard Java director. Extracted so the no-policy branch and the
	 * per-configuration branches cannot drift apart. Uses
	 * {@link JavaProgrammingExerciseProjectScanner} so the TUM-specific scan
	 * defaults take effect (previously a plain {@link JavaProjectScanner} was
	 * hard-coded, so the subclass defaults never applied).
	 */
	@Nonnull
	private static SecurityPolicyDirector buildJavaDirector() {
		return SecurityPolicyJavaDirector.javaBuilder().creator(new JavaCreator()).writer(new JavaWriter())
				.executer(new JavaExecuter()).essentialDataReader(new EssentialDataYAMLReader())
				.javaScanner(new JavaProgrammingExerciseProjectScanner())
				.essentialPackagesPath(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_PACKAGES_PATH)
				.essentialClassesPath(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_CLASSES_PATH).build();
	}
	// </editor-fold>
}
