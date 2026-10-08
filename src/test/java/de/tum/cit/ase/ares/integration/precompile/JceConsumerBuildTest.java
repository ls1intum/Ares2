package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;

/**
 * Builds actual emitted exercises with Maven and Gradle and executes Jupiter
 * tests.
 */
class JceConsumerBuildTest {

	/**
	 * Runs the selected build tool against generated advice, its copied agent and
	 * real student operations.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void generatedExerciseBuildsWithSelectedTool(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = JceGeneratedExerciseTest.generate(configuration);
		Files.createDirectories(project.resolve("fake-home"));
		Files.createDirectories(project.resolve("tmp"));
		Files.writeString(project.resolve("fake-home/default_local.policy"), "protected fixture");
		Path test = project.resolve("src/test/java/fixture/JceConsumerTest.java");
		Files.writeString(test, consumerTest());
		boolean gradle = configuration.name().contains("GRADLE");
		Files.writeString(project.resolve(gradle ? "build.gradle" : "pom.xml"),
				gradle ? gradleBuild(project) : mavenBuild(project));
		if (gradle) {
			Files.writeString(project.resolve("settings.gradle"), "rootProject.name = 'jce-generated-consumer'");
		}
		runBuild(project, gradle);
	}

	/**
	 * Builds packaged advice with each consumer tool and exercises its actual
	 * policy lifecycle.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void postcompileExerciseBuildsWithSelectedTool(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = Files.createTempDirectory(Path.of("target").toAbsolutePath(), "jce-postcompile-consumer-");
		boolean gradle = configuration.name().contains("GRADLE");
		Path main = Files.createDirectories(project.resolve("src/main/java/example/jce"));
		for (String name : new String[] { "JceCryptoSubject", "JceProviderService" }) {
			Files.writeString(main.resolve(name + ".java"),
					Files.readString(Path.of("src/test/java/example/jce", name + ".java")));
		}
		Path helpers = Files.createDirectories(project.resolve("src/test/java/de/tum/cit/ase/ares/integration/jce"));
		for (String name : new String[] { "JceForkProbe", "JcePolicyLifecycleUser", "JceRuntimeContract" }) {
			Files.writeString(helpers.resolve(name + ".java"),
					Files.readString(Path.of("src/test/java/de/tum/cit/ase/ares/integration/jce", name + ".java")));
		}
		Path test = project.resolve("src/test/java/fixture/JceConsumerTest.java");
		Files.createDirectories(test.getParent());
		Files.writeString(test, postcompileTest(gradle));
		Files.writeString(project.resolve("default_local.policy"), "protected fixture");
		Files.writeString(project.resolve("allowed.txt"), "permitted fixture");
		Files.createDirectories(project.resolve("tmp"));
		Path generated = JceGeneratedExerciseTest.generate(configuration);
		Files.writeString(project.resolve("policy.yaml"), Files.readString(generated.resolve("policy.yaml"))
				.replace(generated.resolve("allowed.txt").toString(), project.resolve("allowed.txt").toString()));
		Path agent = ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
				.filter(value -> value.startsWith("-javaagent:") && value.endsWith("-agent.jar"))
				.map(value -> Path.of(value.substring("-javaagent:".length()))).findFirst().orElseThrow();
		Files.copy(agent, project.resolve("agent.jar"));
		Files.copy(agent.resolveSibling(agent.getFileName().toString().replace("-agent.jar", ".jar")),
				project.resolve("ares.jar"));
		Files.writeString(project.resolve(gradle ? "build.gradle" : "pom.xml"),
				gradle ? gradleBuild(project, false) : mavenBuild(project, false));
		if (gradle) {
			Files.writeString(project.resolve("settings.gradle"), "rootProject.name = 'jce-postcompile-consumer'");
		}
		runBuild(project, gradle);
	}

	/** Requires an actual build and a successful, nonempty Jupiter report. */
	private static void runBuild(Path project, boolean gradle) throws Exception {
		List<String> command = new ArrayList<>();
		if (gradle) {
			command.add(Path.of("examples/ares-exercise-gradle/gradlew").toAbsolutePath().toString());
			command.addAll(List.of("--no-daemon", "--console=plain", "-p", project.toString(), "test"));
		} else {
			command.add("mvn");
			command.addAll(List.of("-B", "-f", project.resolve("pom.xml").toString(), "test"));
		}
		Path log = project.resolve("consumer-build.log");
		ProcessBuilder builder = new ProcessBuilder(command).directory(project.toFile()).redirectErrorStream(true)
				.redirectOutput(log.toFile());
		builder.environment().put("JAVA_HOME",
				Path.of(JceGeneratedExerciseTest.javaExecutable()).getParent().getParent().toString());
		Process child = builder.start();
		try {
			assertTrue(child.waitFor(180, TimeUnit.SECONDS), "The consumer build timed out: " + log);
			assertEquals(0, child.exitValue(), () -> readLog(log));
			Path reports = project.resolve(gradle ? "build/test-results/test" : "target/surefire-reports");
			String report = Files.readString(reports.resolve("TEST-fixture.JceConsumerTest.xml"));
			assertTrue(report.contains("tests=\"1\"") && report.contains("failures=\"0\"")
					&& report.contains("errors=\"0\""), report);
			assertTrue(report.contains("JCE_CONSUMER_PASSED"), report);
		} finally {
			child.destroyForcibly();
		}
	}

	/**
	 * Runs genuine crypto, the capture control and every real boundary operation
	 * inside Jupiter.
	 */
	private static String consumerTest() {
		return """
				package fixture;
				/** Runs the generated exercise under its actual build tool. */
				public class JceConsumerTest {
				    /** Proves test execution as well as local crypto and filesystem verdicts. */
				    @org.junit.jupiter.api.Test
				    void generatedAdviceKeepsItsPermissions() throws Exception {
				        java.nio.file.Path project = java.nio.file.Path.of(System.getProperty("jce.project"));
				        String file = project.resolve("fake-home/default_local.policy").toString();
				        String allowed = project.resolve("allowed.txt").toString();
				        GeneratedCaptureLauncher.main(new String[] { file, "crypto", allowed });
				        GeneratedCaptureLauncher.main(new String[] { file, "snapshot", allowed });
				        GeneratedCaptureLauncher.main(new String[] { file, "boundary", allowed });
				        System.out.println("JCE_CONSUMER_PASSED");
				    }
				}
				""";
	}

	/**
	 * Uses already resolved dependency files so the fixture measures this build's
	 * exact libraries.
	 */
	private static List<Path> dependencies() {
		return java.util.Arrays
				.stream(JceGeneratedExerciseTest.classpath()
						.split(java.util.regex.Pattern.quote(java.io.File.pathSeparator)))
				.map(Path::of).filter(path -> path.toString().endsWith(".jar")).toList();
	}

	/**
	 * Preserves the required bootstrap runtime and module access alongside the
	 * copied agent.
	 */
	private static List<String> runtimeArguments(Path project) {
		List<String> arguments = new ArrayList<>();
		ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
				.filter(argument -> argument.startsWith("-Xbootclasspath/a:") || argument.startsWith("--add-opens=")
						|| argument.startsWith("--add-exports="))
				.forEach(arguments::add);
		arguments.add("-javaagent:" + project.resolve("agent.jar"));
		return arguments;
	}

	/**
	 * Compiles emitted Java and AspectJ through Maven's lifecycle before Surefire
	 * runs Jupiter.
	 */
	private static String mavenBuild(Path project) {
		return mavenBuild(project, true);
	}

	/**
	 * Compiles copied or packaged advice in Maven using the corresponding source
	 * roots.
	 */
	private static String mavenBuild(Path project, boolean generated) {
		StringBuilder dependencies = new StringBuilder();
		int index = 0;
		for (Path jar : buildDependencies(project, generated)) {
			dependencies.append("<dependency><groupId>fixture</groupId><artifactId>dependency-").append(index++)
					.append("</artifactId><version>1</version><scope>system</scope><systemPath>")
					.append(xml(jar.toString())).append("</systemPath></dependency>");
		}
		String arguments = runtimeArguments(project).stream().map(value -> "\"" + value + "\"")
				.collect(java.util.stream.Collectors.joining(" "));
		return """
				<project xmlns="http://maven.apache.org/POM/4.0.0">
				  <modelVersion>4.0.0</modelVersion><groupId>fixture</groupId><artifactId>jce-consumer</artifactId><version>1</version>
				  <dependencies>%s</dependencies>
				  <build><testOutputDirectory>%s</testOutputDirectory><plugins>
				    <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId><version>3.16.0</version>
				      <executions><execution><id>default-compile</id><phase>none</phase></execution><execution><id>default-testCompile</id><phase>none</phase></execution></executions>
				    </plugin>
				    <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-antrun-plugin</artifactId><version>3.2.0</version>
				      <executions><execution><phase>test-compile</phase><goals><goal>run</goal></goals><configuration><target>
				        <mkdir dir="${project.build.testOutputDirectory}"/>
				        <java classname="org.aspectj.tools.ajc.Main" fork="true" failonerror="true">
				          <classpath><pathelement location="%s"/><path refid="maven.test.classpath"/></classpath>
				          <arg value="-17"/><arg value="-d"/><arg value="${project.build.testOutputDirectory}"/>
				          <arg value="-classpath"/><arg pathref="maven.test.classpath"/>
				          <arg value="-sourceroots"/><arg value="%s"/>%s
				        </java>%s
				      </target></configuration></execution></executions>
				    </plugin>
				    <plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId><version>3.6.0</version>
				      <dependencies><dependency><groupId>org.apache.maven.surefire</groupId><artifactId>surefire-junit-platform</artifactId><version>3.6.0</version></dependency></dependencies>
				                  <configuration><failIfNoTests>true</failIfNoTests><includes><include>**/JceConsumerTest.java</include></includes><argLine>%s</argLine>
				        <systemPropertyVariables><jce.project>%s</jce.project><java.io.tmpdir>%s</java.io.tmpdir></systemPropertyVariables>
				      </configuration>
				    </plugin>
				  </plugins></build>
				</project>
				"""
				.formatted(dependencies, "${project.basedir}/target/test-classes",
						xml(JceGeneratedExerciseTest.compiler().toString()), xml(sourceRoots(project, generated)),
						generated ? ""
								: "<arg value=\"-aspectpath\"/><arg value=\""
										+ xml(project.resolve("ares.jar").toString()) + "\"/>",
						generated ? "" : """
								<mkdir dir="${project.build.outputDirectory}"/>
								<move todir="${project.build.outputDirectory}">
								  <fileset dir="${project.build.testOutputDirectory}" includes="example/**"/>
								</move>
								""", xml(arguments), xml(project.toString()), xml(project.resolve("tmp").toString()));
	}

	/**
	 * Uses Gradle's JavaExec compilation task and Test task on the actual copied
	 * sources.
	 */
	private static String gradleBuild(Path project) {
		return gradleBuild(project, true);
	}

	/**
	 * Compiles copied or packaged advice in Gradle using the corresponding source
	 * roots.
	 */
	private static String gradleBuild(Path project, boolean generated) {
		String files = buildDependencies(project, generated).stream().map(path -> groovy(path.toString()))
				.collect(java.util.stream.Collectors.joining(","));
		String arguments = runtimeArguments(project).stream().map(JceConsumerBuildTest::groovy)
				.collect(java.util.stream.Collectors.joining(","));
		return """
				plugins { id 'java' }
				dependencies { testImplementation files(%s) }
				tasks.named('compileJava') { enabled = false }
				tasks.named('compileTestJava') { enabled = false }
				tasks.register('compileGeneratedAdvice', JavaExec) {
				    classpath = files(%s)
				    mainClass = 'org.aspectj.tools.ajc.Main'
				    args '-17', '-d', %s, '-classpath', sourceSets.test.runtimeClasspath.asPath, '-sourceroots', %s
				    %s
				    doFirst { file(%s).mkdirs() }
				    %s
				}
				tasks.named('test') {
				    dependsOn 'compileGeneratedAdvice'
				    useJUnitPlatform()
				    include '**/JceConsumerTest.class'
				    testClassesDirs = files(%s)
				    classpath += files(%s)
				    jvmArgs %s
				    systemProperty 'jce.project', %s
				    systemProperty 'java.io.tmpdir', %s
				}
				""".formatted(files, groovy(JceGeneratedExerciseTest.compiler().toString()),
				groovy(project.resolve("build/classes/java/test").toString()), groovy(sourceRoots(project, generated)),
				generated ? "" : "args '-aspectpath', " + groovy(project.resolve("ares.jar").toString()),
				groovy(project.resolve("build/classes/java/test").toString()), generated ? "" : """
						doLast {
						    copy {
						        from file('build/classes/java/test/example')
						        into file('build/classes/java/main/example')
						    }
						    delete file('build/classes/java/test/example')
						}
						""", groovy(project.resolve("build/classes/java/test").toString()),
				groovy(project.resolve("build/classes/java/test").toString()), arguments, groovy(project.toString()),
				groovy(project.resolve("tmp").toString()));
	}

	/** Adds the freshly packaged library only for a Postcompile exercise. */
	private static List<Path> buildDependencies(Path project, boolean generated) {
		List<Path> jars = new ArrayList<>(dependencies());
		if (!generated) {
			jars.add(project.resolve("ares.jar"));
		}
		return jars;
	}

	/** Selects real fixture source roots for the generated or packaged workflow. */
	private static String sourceRoots(Path project, boolean generated) {
		return generated ? project.resolve("src/test/java").toString()
				: project.resolve("src/main/java") + java.io.File.pathSeparator + project.resolve("src/test/java");
	}

	/**
	 * Runs real policy analysis and Jupiter cleanup through the packaged consumer
	 * library.
	 */
	private static String postcompileTest(boolean gradle) {
		return """
				package fixture;
				/** Exercises the packaged library through the consumer's actual build. */
				public class JceConsumerTest {
				    /** Verifies crypto, file permissions, trusted capture and Jupiter cleanup. */
				    @org.junit.jupiter.api.Test
				    void packagedAdviceKeepsItsPermissions() throws Exception {
				        java.nio.file.Path project = java.nio.file.Path.of(System.getProperty("jce.project"));
				        String scope = project.resolve("%s/example/jce").toString();
				        for (String operation : new String[] { "lifecycle", "crypto", "snapshot", "boundary" }) {
				            de.tum.cit.ase.ares.integration.jce.JceForkProbe.main(new String[] { operation,
				                project.resolve("policy.yaml").toString(), project.resolve("default_local.policy").toString(), project.toString(), scope });
				        }
				        System.out.println("JCE_CONSUMER_PASSED");
				    }
				}
				"""
				.formatted(gradle ? "build/classes/java/main" : "target/classes");
	}

	/** Quotes build configuration values as literal XML text. */
	private static String xml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
	}

	/** Quotes a single literal Groovy value without interpolation. */
	private static String groovy(String text) {
		return "'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'";
	}

	/** Retains the complete build failure for review. */
	private static String readLog(Path log) {
		try {
			return Files.readString(log);
		} catch (java.io.IOException failure) {
			throw new IllegalStateException("Cannot read consumer build evidence: " + log, failure);
		}
	}
}
