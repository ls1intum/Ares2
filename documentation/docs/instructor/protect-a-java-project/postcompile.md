---
title: "Postcompile Mode"
sidebar_position: 8
description: "Enforcing the policy at runtime by instrumenting the compiled bytecode."
---

This page shows how postcompile mode is integrated, using a concrete example.

Consider the following student implementation. The method attempts to write to a file
`secret.txt` inside `getName()`:

```java
public String getName() {
    try {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("secret.txt"));
        bufferedWriter.write("First Line");
        bufferedWriter.close();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    return name;
}
```

In a secure testing environment this must be blocked by the Ares 2 runtime sandbox. Postcompile
mode ensures that by instrumenting the compiled bytecode and intercepting all file system
operations performed by untrusted student code.

## 1. Provide a `SecurityConfiguration.yaml`

Include a security configuration file in your project. A common choice is the project's main
directory:

```text
project/
├── src/
├── build.gradle
├── pom.xml
├── secret.txt
├── something.txt
└── SecurityConfiguration.yaml   ← placed here
```

With the following minimal configuration, virtually all actions are denied by default,
providing a strict postcompile sandbox for the sample test:

```yaml
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION
  theSupervisedCodeUsesTheFollowingPackage: "org.example"
  theMainClassInsideThisPackageIs: "Main"
  theFollowingClassesAreTestClasses:
    - "org.example.PenguinTest"
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions:
      - onThisPathAndAllPathsBelow: "something.txt"
        readAllFiles: true
        overwriteAllFiles: true
        createAllFiles: true
        executeAllFiles: true
        deleteAllFiles: true
    regardingNetworkConnections: [ ]
    regardingCommandExecutions: [ ]
    regardingThreadCreations: [ ]
    regardingPackageImports: [ ]
    regardingTimeouts: [ ]
```

This permits file system operations **only** on `something.txt`. All other paths, including
`secret.txt`, are not listed and are therefore fully denied by the sandbox.

## 2. Annotate the test with `@Policy`

Ares 2 activates the sandbox for a test only when the test method is annotated with `@Policy`.
The annotation links the test to the configuration file and defines the part of the student
project that should be supervised.

```java
@Policy(
    value = "SecurityConfiguration.yaml",       // path to the configuration file
    withinPath = ""
)
@PublicTest
void testPenguinPublic() {
    Penguin pingu = new Penguin("Julian");
    assertEquals("Julian", pingu.getName(),
        "getName() does not return the name supplied to the constructor");
}
```

With Ares 2 active, the violation now produces:

```text
Ares Security Error (Reason: Student-Code; Stage: Execution): org.example.Penguin.getName tried to illegally overwrite File
/// target file location: secret.txt via java.io.FileOutputStream.write([B,int,int) but was blocked by Ares. ///
```

## 3. What you need to do outside Ares 2

Because of the way classes are loaded and the class path works when testing student code,
vulnerabilities remain if students manage to load classes that would sit in trusted packages.
This is especially problematic if they shadow library classes, such as JUnit's `Assertions`.

To prevent that, the build must ensure that no student content lands in a trusted package.

:::danger[This step is not optional]
Ares 2 cannot enforce this from inside the JVM it is trying to protect. Without the build-side
check below, the sandbox can be bypassed. See
[Security trust boundary](../ares-2/what-does-ares-2-not-protect-against.md).
:::

### Maven

```xml
<properties>
        <studentOutputDir>${project.basedir}/build/classes/java/main</studentOutputDir>
        <ares.version>2.1.2</ares.version>
        <agent.dir>${project.build.directory}/agents</agent.dir>
    </properties>

    <dependencies>
        <dependency>
            <groupId>de.tum.cit.ase</groupId>
            <artifactId>ares</artifactId>
            <version>${ares.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

<build>
    <plugins>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-enforcer-plugin</artifactId>
            <version>3.1.0</version>
            <executions>
                <execution>
                    <id>enforce-no-student-code-in-trusted-packages</id>
                    <phase>process-classes</phase>
                    <goals><goal>enforce</goal></goals>
                    <configuration>
                        <rules>
                            <requireFilesDontExist>
                                <files>
                                    <file>${studentOutputDir}/ch/qos/logback/</file>
                                    <file>${studentOutputDir}/com/github/javaparser/</file>
                                    <file>${studentOutputDir}/com/intellij/</file>
                                    <file>${studentOutputDir}/com/sun/</file>
                                    <file>${studentOutputDir}/de/tum/cit/ase/ares/api/</file>
                                    <file>${studentOutputDir}/java/</file>
                                    <file>${studentOutputDir}/javax/</file>
                                    <file>${studentOutputDir}/jdk/</file>
                                    <file>${studentOutputDir}/net/jqwik/</file>
                                    <file>${studentOutputDir}/org/assertj/</file>
                                    <file>${studentOutputDir}/org/apache/</file>
                                    <file>${studentOutputDir}/org/eclipse/</file>
                                    <file>${studentOutputDir}/org/gradle/</file>
                                    <file>${studentOutputDir}/org/jacoco/</file>
                                    <file>${studentOutputDir}/org/json/</file>
                                    <file>${studentOutputDir}/org/junit/</file>
                                    <file>${studentOutputDir}/org/opentest4j/</file>
                                    <file>${studentOutputDir}/sun/</file>
                                    <file>${studentOutputDir}/worker/org/gradle/</file>
                                </files>
                            </requireFilesDontExist>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>3.6.1</version>
            <executions>
                <execution>
                    <id>unpack-ares</id>
                    <phase>generate-resources</phase>
                    <goals><goal>unpack</goal></goals>
                    <configuration>
                        <artifactItems>
                            <artifactItem>
                                <groupId>de.tum.cit.ase</groupId>
                                <artifactId>ares</artifactId>
                                <version>${ares.version}</version>
                                <outputDirectory>${agent.dir}/unpacked</outputDirectory>
                            </artifactItem>
                        </artifactItems>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <version>3.4.2</version>
            <executions>
                <execution>
                    <id>build-ares-agent-jar</id>
                    <phase>test-compile</phase>
                    <goals><goal>jar</goal></goals>
                    <configuration>
                        <classesDirectory>${agent.dir}/unpacked</classesDirectory>
                        <finalName>ares-agent</finalName>
                        <outputDirectory>${agent.dir}</outputDirectory>
                        <archive>
                            <manifestEntries>
                                <Premain-Class>de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent</Premain-Class>
                                <Can-Redefine-Classes>true</Can-Redefine-Classes>
                                <Can-Retransform-Classes>true</Can-Retransform-Classes>
                                <Can-Set-Native-Method-Prefix>true</Can-Set-Native-Method-Prefix>
                            </manifestEntries>
                        </archive>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
            <configuration>
                <useSystemClassLoader>false</useSystemClassLoader>
                <argLine>
                    -javaagent:${agent.dir}/ares-agent.jar
                    --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
                    --add-opens java.base/java.lang=ALL-UNNAMED
                </argLine>
                <systemPropertyVariables>
                    <file.encoding>UTF-8</file.encoding>
                </systemPropertyVariables>
            </configuration>
        </plugin>

    </plugins>
</build>
```

### Gradle

```groovy
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider

configurations {
    // transitive = false keeps exactly one artefact per bucket, so the agent JAR
    // is selected by coordinate rather than by matching on file names.
    aresAgent {
        canBeConsumed = false
        canBeResolved = true
        transitive = false
    }
    aresAspectjRuntime {
        canBeConsumed = false
        canBeResolved = true
        transitive = false
    }
}

dependencies {
    // The published `agent` classifier already carries the Premain-Class manifest
    // entry, so the exercise does not have to repackage the JAR itself.
    aresAgent 'de.tum.cit.ase:ares:2.1.2:agent'
    aresAspectjRuntime 'org.aspectj:aspectjrt:1.9.25.1'
    testImplementation('de.tum.cit.ase:ares:2.1.2')
    // Puts the Ares JAR on ajc's aspect path; without it nothing is woven.
    aspect 'de.tum.cit.ase:ares:2.1.2'
    implementation 'org.aspectj:aspectjrt:1.9.25.1'
}

def forbiddenPackageFolders = [
    "$studentOutputDir/ch/qos/logback/",
    "$studentOutputDir/com/github/javaparser/",
    "$studentOutputDir/com/intellij/",
    "$studentOutputDir/com/sun/",
    "$studentOutputDir/de/tum/cit/ase/ares/api/",
    "$studentOutputDir/java/",
    "$studentOutputDir/javax/",
    "$studentOutputDir/jdk/",
    "$studentOutputDir/net/jqwik/",
    "$studentOutputDir/org/assertj/",
    "$studentOutputDir/org/apache/",
    "$studentOutputDir/org/eclipse/",
    "$studentOutputDir/org/gradle/",
    "$studentOutputDir/org/jacoco/",
    "$studentOutputDir/org/json/",
    "$studentOutputDir/org/junit/",
    "$studentOutputDir/org/opentest4j/",
    "$studentOutputDir/sun/",
    "$studentOutputDir/worker/org/gradle/"
]

// Computed when the test task runs rather than while the build is configured, so
// an unrelated task does not trigger dependency resolution and the configuration
// cache stays usable.
abstract class AresJvmArguments implements CommandLineArgumentProvider {
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    abstract ConfigurableFileCollection getAgentJar()

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    abstract ConfigurableFileCollection getAspectjRuntimeJar()

    @Override
    Iterable<String> asArguments() {
        [
                "-javaagent:${agentJar.singleFile.absolutePath}".toString(),
                "-Xbootclasspath/a:${aspectjRuntimeJar.singleFile.absolutePath}".toString(),
                '--add-exports', 'java.base/java.lang=ALL-UNNAMED',
                '--add-exports', 'java.base/jdk.internal.misc=ALL-UNNAMED',
                '--add-opens', 'java.base/java.io=ALL-UNNAMED',
                '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
                '--add-opens', 'java.base/java.lang.reflect=ALL-UNNAMED',
                '--add-opens', 'java.base/java.net=ALL-UNNAMED',
                '--add-opens', 'java.base/java.nio=ALL-UNNAMED',
                '--add-opens', 'java.base/java.nio.channels=ALL-UNNAMED',
                '--add-opens', 'java.base/java.util=ALL-UNNAMED',
                '--add-opens', 'java.base/java.util.concurrent=ALL-UNNAMED',
                '--add-opens', 'java.base/java.util.concurrent.atomic=ALL-UNNAMED',
                '--add-opens', 'java.base/java.util.concurrent.locks=ALL-UNNAMED',
                '--add-opens', 'java.base/jdk.internal.misc=ALL-UNNAMED',
                '--add-opens', 'java.base/sun.net.www.protocol.http=ALL-UNNAMED',
                '--add-opens', 'java.base/sun.net.www.protocol.https=ALL-UNNAMED',
                '--add-opens', 'java.base/sun.nio.ch=ALL-UNNAMED',
                '--add-opens', 'jdk.unsupported/sun.misc=ALL-UNNAMED'
        ]
    }
}

def aresJvmArguments = objects.newInstance(AresJvmArguments)
aresJvmArguments.agentJar.from(configurations.aresAgent)
aresJvmArguments.aspectjRuntimeJar.from(configurations.aresAspectjRuntime)

tasks.withType(Test).configureEach {
    doFirst {
        for (String packageFolder in forbiddenPackageFolders) {
            assert !file(packageFolder).exists(): "$packageFolder must not exist within the submission."
        }
    }
    defaultCharacterEncoding = 'UTF-8'
    testLogging.showStandardStreams = true
    useJUnitPlatform()
    jvmArgumentProviders.add(aresJvmArguments)
}
```

### About the forbidden package list

The list above is the versioned reserved-package boundary, and it is deliberately a superset of
the canonical Ares list. Besides the packages Ares trusts by name, it also stops student code
shadowing the test harness itself (JUnit, jqwik, AssertJ, Logback, Gradle).

Keep it aligned with `WalaPathClassification.RESERVED_PACKAGE_PREFIX_VERSION` (the prefix data)
and `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION` (the build-side contract), and do not disable it.

:::warning[Hook the check onto every `Test` task, not onto `check`]
Gradle defines `check.dependsOn test` and not the reverse, so a `check`-only hook never runs
for `gradlew test`.
:::

The canonical Maven and Gradle contract is documented in the
[enforcement model](/contributor/subsystems/policy/enforcement-model). The executable snippets ship inside
the Ares JAR and live in the repository at
[`src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages/`](https://github.com/ls1intum/Ares2/tree/main/src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages),
and complete working exercises are in
[`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples)
([Maven](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-maven),
[Gradle](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-gradle)).
