package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Proves that precompile copying, which rewrites the Ares package in every
 * copied advice to the exercise's, leaves the generated timeout's thread
 * exemption intact in both runtime backends.
 */
class StrictTimeoutCopiedAdviceTest {

	/**
	 * The copied thread advice still names the generated timeout under its real
	 * name, next to the rewritten {@code TimeoutUtils}.
	 *
	 * @param configuration the policy's programming-language configuration.
	 * @param advice        where the copied thread advice lands, below the test
	 *                      sources.
	 * @param tempDir       where the project is generated.
	 * @throws Exception if generating fails
	 */
	@ParameterizedTest
	@CsvSource({
			"JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, com/example/ares/api/aop/java/aspectj/adviceandpointcut/JavaAspectJThreadSystemAdviceDefinitions.aj",
			"JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION, com/example/ares/api/aop/java/instrumentation/advice/JavaInstrumentationAdviceThreadSystemToolbox.java" })
	void theGeneratedTimeoutsExemptionSurvivesCopying(String configuration, String advice, @TempDir Path tempDir)
			throws Exception {
		Path policy = tempDir.resolve("SecurityPolicy.yaml");
		Files.writeString(policy, GeneratedStrictTimeoutTest.policyText(configuration));

		Path testSources = GeneratedStrictTimeoutTest.generate(tempDir, policy, "project", "<project/>");
		String copied = Files.readString(testSources.resolve(advice));

		assertThat(copied).contains("\"de.tum.cit.\" + \"ase.ares.generated.GeneratedStrictTimeout\"")
				.contains("\"com.example.ares.api.internal.TimeoutUtils\"")
				.doesNotContain("com.example.ares.generated.GeneratedStrictTimeout");
	}
}
