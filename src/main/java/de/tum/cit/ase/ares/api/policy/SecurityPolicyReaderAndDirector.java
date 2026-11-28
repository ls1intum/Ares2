package de.tum.cit.ase.ares.api.policy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.policy.director.SecurityPolicyDirector;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;
import de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.util.LruCache;
import de.tum.cit.ase.ares.api.util.YamlPlaceholderResolver;

/**
 * Security policy file reader and test case director.
 * <p>
 * Description: Handles the reading of a security policy from a security policy
 * file and manages the respective creation, the writing, and the execution of
 * security test cases. Acts as a client of the Abstract Factory Design Pattern,
 * and a director in the Builder Design Pattern.
 * <p>
 * Design Rationale: By separating the responsibilities of file reading and test
 * case management, this class adheres to the Single Responsibility Principle,
 * ensuring that each component handles its specific functionality.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href=
 *      "https://refactoring.guru/design-patterns/abstract-factory">Abstract
 *      Factory Design Pattern</a>
 * @see <a href="https://refactoring.guru/design-patterns/builder">Builder
 *      Design Pattern</a>
 */
@SuppressWarnings("unused")
public class SecurityPolicyReaderAndDirector {

	// <editor-fold desc="Attributes">
	/**
	 * The largest number of read policies kept. All 62 policies of the SCORE
	 * Reproducibility Package hold 52 distinct contents, so 256, the bound of the
	 * imported classes, covers a whole run; a dropped policy is only read again.
	 */
	private static final int MAX_READ_POLICIES = 256;

	/**
	 * Policies already read in this JVM, keyed by everything the reading depends
	 * on: the policy file, the project root, the file's bytes and the system
	 * properties a placeholder expands to. A {@link SecurityPolicy} is immutable,
	 * so one instance can be handed out again.
	 */
	private static final Map<PolicyFileContent, SecurityPolicy> READ_POLICIES = LruCache
			.synchronizedCache(MAX_READ_POLICIES);

	/**
	 * The reader for the security policy file (first dependency injection).
	 */
	@Nullable
	private SecurityPolicyReader securityPolicyReader;

	/**
	 * The director for creating security test cases based on the security policy
	 * (second dependency injection).
	 */
	@Nullable
	private SecurityPolicyDirector securityPolicyDirector;

	/**
	 * The path to the security policy file.
	 */
	@Nullable
	private final Path securityPolicyFilePath;

	/**
	 * The path to the project folder.
	 */
	@Nullable
	private final Path projectFolderPath;

	@Nonnull
	private final Path withinPath;

	/**
	 * The manager for creating and handling security test cases.
	 */
	@Nullable
	private TestCaseAbstractFactoryAndBuilder securityTestCaseFactoryAndBuilder;

	/** The content the policy was read from; {@code null} if not known. */
	@Nullable
	private PolicyFileContent readPolicyContent;
	// </editor-fold>

	// <editor-fold desc="Constructors">

	/**
	 * Constructs a SecurityPolicyReaderAndDirector with only a policy file.
	 * <p>
	 * The project folder defaults to null, which resolves to the current working
	 * directory, and the within-path defaults to the empty path.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param securityPolicyFilePath the path to the security policy file.
	 */
	public SecurityPolicyReaderAndDirector(@Nullable Path securityPolicyFilePath) {
		this(securityPolicyFilePath, null, Path.of(""));
	}

	/**
	 * Constructs a SecurityPolicyReaderAndDirector instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param securityPolicyFilePath the path to the security policy file.
	 * @param projectFolderPath      the path to the project folder.
	 */
	public SecurityPolicyReaderAndDirector(@Nullable Path securityPolicyFilePath, @Nullable Path projectFolderPath) {
		this(securityPolicyFilePath, projectFolderPath, Path.of(""));
	}

	public SecurityPolicyReaderAndDirector(@Nullable Path securityPolicyFilePath, @Nullable Path projectFolderPath,
			@Nonnull Path withinPath) {
		this.securityPolicyFilePath = securityPolicyFilePath;
		this.projectFolderPath = projectFolderPath;
		this.withinPath = Objects.requireNonNull(withinPath, "withinPath must not be null");
	}
	// </editor-fold>

	// <editor-fold desc="Create method">

	/**
	 * Creates the security test cases.
	 * <p>
	 * Reading the policy and directing it use one project root: the project folder,
	 * or the current working directory when none was given. So
	 * {@code ${PROJECT_ROOT}} in the policy names the directory the tests are made
	 * for, not the folder the policy file sits in.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return this instance.
	 */
	public SecurityPolicyReaderAndDirector createTestCases() {
		Path projectRoot = projectFolderPath == null ? Path.of("").toAbsolutePath() : projectFolderPath;
		@Nullable
		SecurityPolicy securityPolicy = null;
		if (securityPolicyFilePath != null && !securityPolicyFilePath.toString().isEmpty()) {
			@Nonnull
			Path nonNullPolicyPath = securityPolicyFilePath;
			securityPolicy = readSecurityPolicy(nonNullPolicyPath, projectRoot);
		}
		securityPolicyDirector = SecurityPolicyDirector.selectSecurityPolicyDirector(securityPolicy);
		this.securityTestCaseFactoryAndBuilder = securityPolicyDirector.createTestCases(securityPolicy, projectRoot,
				withinPath);
		return this;
	}

	/**
	 * Reads the policy, or hands out the one read from identical input before. A
	 * YAML policy is parsed from exactly the bytes and property values its key
	 * holds, so a policy is never remembered under input it was not read from; any
	 * other is read without being kept.
	 *
	 * @param policyPath  the policy file
	 * @param projectRoot the project root for {@code ${PROJECT_ROOT}}
	 * @return the policy
	 */
	@Nonnull
	private SecurityPolicy readSecurityPolicy(@Nonnull Path policyPath, @Nonnull Path projectRoot) {
		Optional<PolicyFileContent> before = PolicyFileContent.of(policyPath, projectRoot);
		SecurityPolicy known = before.map(READ_POLICIES::get).orElse(null);
		if (known != null) {
			readPolicyContent = before.get();
			return known;
		}
		securityPolicyReader = SecurityPolicyReader.selectSecurityPolicyReader(policyPath, projectRoot);
		if (before.isEmpty() || !(securityPolicyReader instanceof SecurityPolicyYAMLReader yamlReader)) {
			return securityPolicyReader.readSecurityPolicyFrom(policyPath);
		}
		SecurityPolicy policy = yamlReader.readSecurityPolicyFrom(policyPath, before.get().bytes().array(),
				before.get().propertyValues());
		READ_POLICIES.putIfAbsent(before.get(), policy);
		readPolicyContent = before.get();
		return policy;
	}

	/**
	 * Everything reading a policy file depends on.
	 *
	 * @param policyFile     the absolute policy file
	 * @param projectRoot    the absolute project root
	 * @param bytes          the file's content
	 * @param propertyValues the system property values placeholders expand to
	 */
	private record PolicyFileContent(Path policyFile, Path projectRoot, ByteBuffer bytes,
			Map<String, String> propertyValues) {

		/**
		 * Captures the input for a plain policy file; any other, or one that cannot be
		 * read, yields empty and is read without reuse.
		 *
		 * @param policyPath  the policy file
		 * @param projectRoot the project root
		 * @return the input, or empty if it cannot be captured
		 */
		static Optional<PolicyFileContent> of(Path policyPath, Path projectRoot) {
			Path policyFile = policyPath.toAbsolutePath().normalize();
			if (!Files.isRegularFile(policyFile)) {
				return Optional.empty();
			}
			try {
				return Optional.of(new PolicyFileContent(policyFile, projectRoot.toAbsolutePath().normalize(),
						ByteBuffer.wrap(Files.readAllBytes(policyFile)),
						YamlPlaceholderResolver.currentPropertyValues()));
			} catch (IOException unreadable) {
				return Optional.empty();
			}
		}
	}
	// </editor-fold>

	// <editor-fold desc="Write method">

	/**
	 * Writes the security test cases to the project folder.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a list of Paths where the security test cases were written.
	 */
	@Nonnull
	public List<Path> writeTestCases(Path testFolderPath) {
		Objects.requireNonNull(testFolderPath, "testFolderPath must not be null");
		// Fail closed, symmetrically with executeTestCases and
		// writeTestCasesAndContinue:
		// an out-of-order call (writeTestCases before createTestCases) must NOT
		// silently
		// write zero security tests - in a security boundary, silent no-op generation
		// is
		// the dangerous direction.
		return Objects.requireNonNull(this.securityTestCaseFactoryAndBuilder,
				"createTestCases() must be called before writeTestCases()").writeTestCases(testFolderPath);
	}

	/**
	 * Writes the security test cases to the project folder and continues.
	 * <p>
	 * Fails closed: called before {@link #createTestCases()}, it throws rather than
	 * silently writing zero security tests.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a list of Paths where the security test cases were written.
	 */
	@Nonnull
	public SecurityPolicyReaderAndDirector writeTestCasesAndContinue(Path testFolderPath) {
		Objects.requireNonNull(testFolderPath, "testFolderPath must not be null");
		Objects.requireNonNull(this.securityTestCaseFactoryAndBuilder,
				"createTestCases() must be called before writeTestCasesAndContinue()").writeTestCases(testFolderPath);
		return this;
	}
	// </editor-fold>

	// <editor-fold desc="Execute method">

	/**
	 * Executes the security test cases.
	 * <p>
	 * Fails closed: called before {@link #createTestCases()}, it throws rather than
	 * silently running zero security tests.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public SecurityPolicyReaderAndDirector executeTestCases() {
		Objects.requireNonNull(this.securityTestCaseFactoryAndBuilder,
				"createTestCases() must be called before executeTestCases()").executeTestCases();
		return this;
	}

	/**
	 * Tells, read afresh, whether the created test cases and the verdict of their
	 * analysis still apply, so that a later phase of the same test may arm them
	 * again without analysing. The policy file must still hold the content it was
	 * read from.
	 *
	 * @return whether the test cases may be armed again without analysis
	 */
	public boolean analysisStillApplies() {
		return securityTestCaseFactoryAndBuilder != null && readPolicyContent != null && securityPolicyFilePath != null
				&& PolicyFileContent.of(securityPolicyFilePath, readPolicyContent.projectRoot())
						.equals(Optional.of(readPolicyContent))
				&& securityTestCaseFactoryAndBuilder.analysisStillApplies();
	}

	/**
	 * Installs the security settings again for test cases whose analysis already
	 * passed and {@link #analysisStillApplies()} still holds.
	 *
	 * @return this instance.
	 */
	public SecurityPolicyReaderAndDirector rearmTestCases() {
		Objects.requireNonNull(this.securityTestCaseFactoryAndBuilder,
				"createTestCases() must be called before rearmTestCases()").rearmTestCases();
		return this;
	}
	// </editor-fold>

	// <editor-fold desc="Accessor">

	/**
	 * Returns the factory that createTestCases built, for a caller that inspects
	 * the pipeline rather than running it.
	 * <p>
	 * Description: Published in 2.1.0 and kept for one release cycle, since
	 * removing it would stop an exercise repository that calls it from compiling.
	 * Nothing inside Ares needs it.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @deprecated inspect the pipeline through its own methods instead; this
	 *             accessor is removed after 2.2.x.
	 * @return the factory, never {@code null}
	 */
	@Deprecated(since = "2.2.2", forRemoval = true)
	@Nonnull
	public TestCaseAbstractFactoryAndBuilder factoryAndBuilder() {
		return Objects.requireNonNull(this.securityTestCaseFactoryAndBuilder,
				"createTestCases() must be called before factoryAndBuilder()");
	}
	// </editor-fold>

	// <editor-fold desc="Builder">
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		@Nullable
		private Path securityPolicyFilePath;
		@Nullable
		private Path projectFolderPath;
		@Nonnull
		private Path withinPath = Path.of("");

		@Nonnull
		public Builder securityPolicyFilePath(@Nullable Path securityPolicyFilePath) {
			this.securityPolicyFilePath = securityPolicyFilePath;
			return this;
		}

		@Nonnull
		public Builder projectFolderPath(@Nullable Path projectFolderPath) {
			this.projectFolderPath = projectFolderPath;
			return this;
		}

		@Nonnull
		public Builder withinPath(@Nonnull Path withinPath) {
			this.withinPath = Objects.requireNonNull(withinPath, "withinPath must not be null");
			return this;
		}

		@Nonnull
		public SecurityPolicyReaderAndDirector build() {
			return new SecurityPolicyReaderAndDirector(securityPolicyFilePath, projectFolderPath, withinPath);
		}
	}
	// </editor-fold>
}
