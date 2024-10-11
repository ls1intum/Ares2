package de.tum.cit.ase.ares.api.security;

import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.*;

import de.tum.cit.ase.ares.api.context.TestContext;

@API(status = Status.INTERNAL)
public final class AresSecurityConfigurationBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(AresSecurityConfigurationBuilder.class);

	private static final Path EXPECTED_MAVEN_POM_PATH = Path
			.of(System.getProperty(AresSystemProperties.ARES_MAVEN_POM, "pom.xml")); //$NON-NLS-1$
	private static final Path EXPECTED_GRADLE_BUILD_PATH = Path
			.of(System.getProperty(AresSystemProperties.ARES_GRADLE_BUILD, "build.gradle")); //$NON-NLS-1$
	private static final String MAVEN_ENFORCER_FILE_ENTRY = "<file>${project.build.outputDirectory}%s</file>"; //$NON-NLS-1$
	private static final String GRADLE_ENFORCER_FILE_ENTRY = "\"$studentOutputDir%s\""; //$NON-NLS-1$
	private static final boolean IS_MAVEN;
	private static final boolean IS_GRADLE;
	static {
		// Check if we are in a maven environment and don't intend to ignore that fact
		IS_MAVEN = (StackWalker.getInstance().walk(sfs -> sfs.anyMatch(sf -> sf.getClassName().contains("maven"))) //$NON-NLS-1$
				|| Files.exists(EXPECTED_MAVEN_POM_PATH))
				&& !Boolean.getBoolean(AresSystemProperties.ARES_MAVEN_IGNORE);
		IS_GRADLE = (StackWalker.getInstance().walk(sfs -> sfs.anyMatch(sf -> sf.getClassName().contains("gradle"))) //$NON-NLS-1$
				|| Files.exists(EXPECTED_GRADLE_BUILD_PATH))
				&& !Boolean.getBoolean(AresSystemProperties.ARES_GRADLE_IGNORE);
	}
	/**
	 * Cache for the content of the build file so that we don't need to read it each
	 * time. It must not change during program execution, anyway.
	 */
	private static String buildConfigurationFileContent;

	private Optional<Class<?>> testClass;
	private Optional<Method> testMethod;
	private Path executionPath;

	private AresSecurityConfigurationBuilder() {
		testClass = Optional.empty();
		testMethod = Optional.empty();
	}

	public AresSecurityConfigurationBuilder withPath(Path executionPath) {
		this.executionPath = Objects.requireNonNull(executionPath);
		return this;
	}

	public AresSecurityConfigurationBuilder configureFromContext(TestContext context) {
		testClass = Objects.requireNonNull(context.testClass());
		testMethod = Objects.requireNonNull(context.testMethod());
		return this;
	}

	public AresSecurityConfiguration build() {
		validate();
		return new AresSecurityConfiguration(testClass, testMethod, executionPath);
	}

	private void validate() {}public static AresSecurityConfigurationBuilder create() {
		return new AresSecurityConfigurationBuilder();
	}
}
