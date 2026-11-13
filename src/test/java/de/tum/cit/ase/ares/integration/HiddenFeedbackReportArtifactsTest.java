package de.tum.cit.ase.ares.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Checks the actual Gradle and Maven artifacts for an opaque failed result. */
class HiddenFeedbackReportArtifactsTest {

	/** JUnit release used by the exercise report probes. */
	private static final String JUNIT_VERSION = "6.1.3";

	/** A failed hidden result and a readable public control. */
	private static final String FIXTURE = """
			package org.example;
			import java.io.OutputStream;
			import java.io.PrintStream;
			import java.lang.reflect.Method;
			import org.junit.jupiter.api.Test;
			import org.junit.jupiter.api.extension.ExtendWith;
			import org.junit.jupiter.api.extension.ExtensionContext;
			import org.junit.jupiter.api.extension.InvocationInterceptor;
			import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
			class HiddenFeedbackArtifactFixtureTest {
			    /** A marker with no diagnostic text. */
			    static final class HiddenTestFailure extends AssertionError {
			        /** Removes the stack from the marker. */
			        HiddenTestFailure() { setStackTrace(new StackTraceElement[0]); }
			        /** Leaves no text for a report renderer. */
			        @Override public String toString() { return ""; }
			    }
			    /** Reproduces hidden stream capture at the report boundary. */
			    static final class HiddenOutput implements InvocationInterceptor {
			        /** Discards test output while preserving the surrounding streams. */
			        @Override public void interceptTestMethod(Invocation<Void> invocation,
			                ReflectiveInvocationContext<Method> method, ExtensionContext context) throws Throwable {
			            PrintStream previousOut = System.out;
			            PrintStream previousErr = System.err;
			            try (PrintStream sink = new PrintStream(OutputStream.nullOutputStream())) {
			                System.setOut(sink);
			                System.setErr(sink);
			                invocation.proceed();
			            } finally {
			                System.setOut(previousOut);
			                System.setErr(previousErr);
			            }
			        }
			    }
			    /** Fails without publishing its streams. */
			    @ExtendWith(HiddenOutput.class) @Test void hidden() {
			        System.out.print("SECRET_HIDDEN_OUT");
			        System.err.print("SECRET_HIDDEN_ERR");
			        throw new HiddenTestFailure();
			    }
			    /** Keeps a public failure and its streams readable. */
			    @Test void publicFailure() {
			        System.out.print("PUBLIC_STDOUT");
			        System.err.print("PUBLIC_STDERR");
			        throw new AssertionError("VISIBLE_PUBLIC_REPORT");
			    }
			}
			""";

	/**
	 * The Gradle finalizer leaves a failed hidden test with an empty failure block.
	 */
	@Test
	void gradleReportHidesDiagnostics(@TempDir Path project) throws Exception {
		Path source = Files.createDirectories(project.resolve("src/test/java/org/example"));
		Files.writeString(source.resolve("HiddenFeedbackArtifactFixtureTest.java"), FIXTURE);
		Path script = Path.of("examples/ares-exercise-gradle/gradle/AresHiddenReports.gradle").toAbsolutePath();
		Files.writeString(project.resolve("build.gradle"),
				"""
						plugins { id 'java' }
						repositories { mavenCentral() }
						dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:%s'; testRuntimeOnly 'org.junit.platform:junit-platform-launcher:%s' }
						test { useJUnitPlatform() }
						apply from: '%s'
						"""
						.formatted(JUNIT_VERSION, JUNIT_VERSION, script.toString().replace("\\", "/")));
		String output = run(List.of(Path.of("examples/ares-exercise-gradle/gradlew").toAbsolutePath().toString(),
				"--no-daemon", "-p", project.toString(), "test"), project);
		Path xml = project.resolve("build/test-results/test/TEST-org.example.HiddenFeedbackArtifactFixtureTest.xml");
		assertOpaqueFailure(xml);
		assertThat(Files.readString(xml)).contains("VISIBLE_PUBLIC_REPORT", "PUBLIC_STDOUT", "PUBLIC_STDERR")
				.doesNotContain("SECRET_HIDDEN");
		assertThat(output).doesNotContain("HiddenTestFailure").doesNotContain("SECRET_HIDDEN");
		assertThat(Files.readString(findHtml(project, "hidden().html"))).doesNotContain("HiddenTestFailure")
				.doesNotContain("SECRET_HIDDEN");
		assertThat(Files.readString(findHtml(project, "publicFailure().html"))).contains("VISIBLE_PUBLIC_REPORT");
	}

	/** Surefire keeps failure status without a hidden failure message or stack. */
	@Test
	void mavenSurefireHidesDiagnostics(@TempDir Path project) throws Exception {
		Path source = Files.createDirectories(project.resolve("src/test/java/org/example"));
		Files.writeString(source.resolve("HiddenFeedbackArtifactFixtureTest.java"), FIXTURE);
		Files.writeString(project.resolve("pom.xml"),
				"""
						<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
						<modelVersion>4.0.0</modelVersion><groupId>org.example</groupId><artifactId>hidden-feedback-report</artifactId><version>1</version>
						<properties><maven.compiler.source>17</maven.compiler.source><maven.compiler.target>17</maven.compiler.target></properties>
						<dependencies><dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>%s</version><scope>test</scope></dependency></dependencies>
						<build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>3.6.0</version>
						<configuration><printSummary>false</printSummary><useFile>true</useFile></configuration></plugin></plugins></build>
						</project>
						"""
						.formatted(JUNIT_VERSION));
		String output = run(List.of("mvn", "-q", "test"), project);
		Path xml = project.resolve("target/surefire-reports/TEST-org.example.HiddenFeedbackArtifactFixtureTest.xml");
		assertOpaqueFailure(xml);
		assertThat(Files.readString(xml)).contains("VISIBLE_PUBLIC_REPORT", "PUBLIC_STDOUT", "PUBLIC_STDERR")
				.doesNotContain("SECRET_HIDDEN");
		assertThat(Files.readString(
				project.resolve("target/surefire-reports/org.example.HiddenFeedbackArtifactFixtureTest.txt")))
						.doesNotContain("HiddenTestFailure");
		assertThat(output).doesNotContain("HiddenTestFailure").doesNotContain("SECRET_HIDDEN");
	}

	/** Runs a probe that must fail because both fixture tests fail. */
	private static String run(List<String> command, Path project) throws Exception {
		Process process = new ProcessBuilder(command).directory(project.toFile()).redirectErrorStream(true).start();
		boolean finished = process.waitFor(120, TimeUnit.SECONDS);
		if (!finished) {
			process.destroyForcibly();
		}
		assertThat(finished).isTrue();
		assertThat(process.exitValue()).isNotZero();
		return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
	}

	/** Finds a generated Gradle page for the named test. */
	private static Path findHtml(Path project, String name) throws IOException {
		try (Stream<Path> files = Files.walk(project.resolve("build/reports/tests/test"))) {
			return files.filter(path -> path.getFileName().toString().equals(name)).findFirst().orElseThrow();
		}
	}

	/** Asserts that a hidden testcase retains a blank failure element. */
	private static void assertOpaqueFailure(Path xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		NodeList testcases = factory.newDocumentBuilder().parse(xml.toFile()).getElementsByTagName("testcase");
		for (int index = 0; index < testcases.getLength(); index++) {
			Element testcase = (Element) testcases.item(index);
			if (testcase.getAttribute("name").startsWith("hidden")) {
				NodeList failures = testcase.getElementsByTagName("failure");
				assertThat(failures.getLength()).isEqualTo(1);
				Element failure = (Element) failures.item(0);
				assertThat(failure.getTextContent()).isBlank();
				assertThat(failure.getAttribute("message")).isBlank();
				assertThat(failure.getAttribute("type")).isBlank();
				return;
			}
		}
		throw new AssertionError("Hidden test case was missing from the report");
	}
}
