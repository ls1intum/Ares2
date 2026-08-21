---
title: "Maven"
sidebar_position: 1
description: "Protecting a Maven project with Ares 2 in Postcompile mode, from dependency to enforced policy."
---

:::tip[Simple Story]
This is the whole path for a Maven exercise, in the order you have to do it.

Add Ares, mark your tests, fill in the checklist, and wire the check into the build so nobody
can quietly take it off the desk.
:::

## The path, in order

1. **Add the dependency and the agent**, below.
2. **[Set up the public and hidden test model](../setup.md)**.
3. **[Mark your tests](../test-annotations.md)** with `@PublicTest` or `@HiddenTest`, and give
   hidden tests a `@Deadline`.
4. **[Write the policy](../policy-configuration.md)**, choosing one of the four
   `JAVA_USING_MAVEN_*` configurations.
5. **Apply `@Policy` to the tests**, below.
6. **Reject student classes in reserved packages**, below. This step is **not optional**.
7. **Verify with both controls**, below.

## Add the Ares dependency and the agent

:::note[Blockquoted tips are optional]
Blockquoted tips (marked `>`) in the setup steps below describe optional configuration that can be
skipped on a first setup. Everything not in a blockquote is required.
:::

There is no repository step for Maven: the super-POM already defines Central at `https://repo.maven.apache.org/maven2`, so re-declaring it adds nothing and can conflict with an organisational mirror.

### Declare the versions once

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <ares.version>2.1.3</ares.version>
    <aspectj.version>1.9.25.1</aspectj.version>
    <!-- Keeps @{argLine} resolvable when JaCoCo is not part of the run. -->
    <argLine></argLine>
</properties>
```

**Explanation:** the two version properties remove repetition, exactly as in [Declare the versions once](#declare-the-versions-once). The empty `argLine` property matters for a different reason, explained in the agent step above.

### Add Ares dependencies

```xml
<dependency>
    <groupId>de.tum.cit.ase</groupId>
    <artifactId>ares</artifactId>
    <version>${ares.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>${aspectj.version}</version>
</dependency>
```

**Explanation:**

- **Ares must be `provided`, not `test`.** This is the single most common way to get a Maven setup that compiles but never enforces anything. The `aspectj-maven-plugin` resolves its `<aspectLibraries>` against the **compile-visible** project dependencies, and it weaves the *main* classes during `process-classes`, where a test-scoped artefact does not exist. A `<scope>test</scope>` Ares therefore fails the build with `The artifact de.tum.cit.ase:ares referenced in aspectj plugin as an aspect library, is not found the project dependencies`. `provided` scope is visible at compile time **and** at test time, while still being absent from the packaged artefact and from anything that consumes the exercise transitively, which is exactly the scope an exercise wants.
- `aspectjrt` provides the classes woven bytecode references, and for the same reason it too has no `<scope>test</scope>`.

> **Note:** If your `pom.xml` already contains a `<dependencies>` section, add these to it instead of creating a new one.

### Copy the agent and the AspectJ runtime to a known path

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.9.0</version>
    <executions>
        <execution>
            <id>copy-ares-runtime-jars</id>
            <phase>process-test-classes</phase>
            <goals>
                <goal>copy</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>de.tum.cit.ase</groupId>
                        <artifactId>ares</artifactId>
                        <version>${ares.version}</version>
                        <classifier>agent</classifier>
                        <overWrite>true</overWrite>
                        <outputDirectory>${project.build.directory}/ares</outputDirectory>
                        <destFileName>ares-agent.jar</destFileName>
                    </artifactItem>
                    <artifactItem>
                        <groupId>org.aspectj</groupId>
                        <artifactId>aspectjrt</artifactId>
                        <version>${aspectj.version}</version>
                        <overWrite>true</overWrite>
                        <outputDirectory>${project.build.directory}/ares</outputDirectory>
                        <destFileName>aspectjrt.jar</destFileName>
                    </artifactItem>
                </artifactItems>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Explanation:** the agent must be named by an absolute path on the command line, so it has to exist at a path the build knows. Copying both JARs into `${project.build.directory}/ares` under fixed names gives that, and it means the version appears only in the properties.

The alternative some guides use, pointing `-javaagent` at `${settings.localRepository}/de/tum/cit/ase/ares/...`, is worse in three ways: it hard-codes the repository layout, it silently assumes the artefact has already been downloaded, and it repeats the version inside a path where an upgrade is easy to miss. `process-test-classes` runs immediately before `test`, so the JARs are in place when Surefire starts.

### Attach the agent via maven-surefire-plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.4</version>
    <configuration>
        <argLine>
            @{argLine}
            "-javaagent:${project.build.directory}/ares/ares-agent.jar"
            "-Xbootclasspath/a:${project.build.directory}/ares/aspectjrt.jar"
            --add-exports java.base/java.lang=ALL-UNNAMED
            --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
            --add-opens java.base/java.io=ALL-UNNAMED
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/java.lang.reflect=ALL-UNNAMED
            --add-opens java.base/java.net=ALL-UNNAMED
            --add-opens java.base/java.nio=ALL-UNNAMED
            --add-opens java.base/java.nio.channels=ALL-UNNAMED
            --add-opens java.base/java.util=ALL-UNNAMED
            --add-opens java.base/java.util.concurrent=ALL-UNNAMED
            --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED
            --add-opens java.base/java.util.concurrent.locks=ALL-UNNAMED
            --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
            --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED
            --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED
            --add-opens java.base/sun.nio.ch=ALL-UNNAMED
            --add-opens jdk.unsupported/sun.misc=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

**Explanation:**

- **Pin the version.** Without `<version>`, the plugin floats with whatever the super-POM binds, and a Maven upgrade silently changes how your tests are launched.
- **`@{argLine}` is not optional if anything else contributes Java Virtual Machine (JVM) arguments.** JaCoCo's `prepare-agent` goal works by *setting* the `argLine` property. A plain `<argLine>` overwrites it, and coverage then silently reports nothing. `@{argLine}` expands the property late, so both survive. That is why [Declare the versions once](#declare-the-versions-once) declares an empty `<argLine></argLine>` property: without it, a run in which JaCoCo does not participate fails with an unresolved `@{argLine}`.
- **Quote the two paths.** `${project.build.directory}` contains a space whenever the project sits under a directory such as `My Projects`. Surefire splits `argLine` on whitespace but honours double quotes, so the quotes are what keep such a path in one piece.
- **Merging with existing arguments.** If your exercise already sets `argLine`, append these entries to it rather than replacing them, keeping `@{argLine}` first. If another Java agent is present, order matters: put the Ares agent **after** a coverage agent such as JaCoCo, so coverage instrumentation is applied to the classes Ares then transforms rather than the reverse.
- The module access flags are identical to the Gradle ones; see the agent step above for the per-flag explanation.

### Configure AspectJ compile-time weaving

Your build must run the AspectJ compiler to weave the Ares security aspects into student bytecode. Without this step, the `-Xbootclasspath/a:` flag only provides runtime classes and no weaving ever occurs.

```xml
<plugin>
    <groupId>dev.aspectj</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.14.1</version>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjtools</artifactId>
            <version>${aspectj.version}</version>
        </dependency>
    </dependencies>
    <configuration>
        <forceAjcCompile>true</forceAjcCompile>
        <complianceLevel>${maven.compiler.source}</complianceLevel>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
        <aspectLibraries>
            <aspectLibrary>
                <groupId>de.tum.cit.ase</groupId>
                <artifactId>ares</artifactId>
            </aspectLibrary>
        </aspectLibraries>
    </configuration>
    <executions>
        <execution>
            <id>weave-main-classes</id>
            <phase>process-classes</phase>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Explanation:**

- `aspectjtools` is the AspectJ compiler itself, declared as a plugin dependency so the plugin uses the version from `${aspectj.version}`.
- `forceAjcCompile` forces `ajc` to run even though the student project contains no `.aj` source files. This is essential, because the aspects come from the Ares library rather than from the project.
- `<aspectLibraries>` puts the Ares JAR on the **aspect path**. Without it the plugin runs but weaves nothing, since `ajc` does not pick up binary aspects from the ordinary compile classpath. The entry references the dependency from the dependency step above, so no version is repeated.
- The execution binds `ajc` to `process-classes`, immediately after `javac`. Only main classes are woven; test classes are left untouched.

## Provide the policy file

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
thisPolicyFileCompliesToThePolicyVersion: 1
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

## Annotate the test with `@Policy`

`@Policy` links the test to the configuration file and defines the part of the student project
that should be supervised. It does not switch the sandbox on by itself: the Ares test annotation
beneath it is what registers the extensions, and a method carrying a plain JUnit `@Test` with a
`@Policy` runs unsupervised without saying so. See
[Test Annotations](../test-annotations.md).

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

## Reject student classes in reserved packages

**This section is required.** Ares trusts a number of runtime identities **by name**, including its own `de.tum.cit.ase.ares.api` package and the platform namespaces. If a student can put a class into one of those packages, that class inherits the trust and every other check can be walked around. The build must therefore refuse to compile student output into a reserved package.

The [Enforcement Model](/contributor/subsystems/policy/enforcement-model) specifies this boundary and calls it a deployment prerequisite, not an optional Ares runtime feature. Ares ships the executable snippets, so copy them rather than writing your own:

They ship inside the Ares JAR under `de/tum/cit/ase/ares/api/configuration/reservedPackages/`,
and live in the repository at
[`src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages/`](https://github.com/ls1intum/Ares2/tree/main/src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages):

- `GradleReservedPackages.gradle`
- `MavenReservedPackages.xml`
- `ReservedPackagePrefixes.txt` (the machine-readable prefix list)

Two versions are pinned. `RESERVED_PACKAGE_PREFIX_VERSION = 1` is the prefix data. `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION = 2` is the build-side contract that enforces it. Your exercise and its continuous integration (CI) must pin both.

### Maven

Apply the shipped `MavenReservedPackages.xml`, a `maven-antrun-plugin` execution bound to `process-classes` that scans `${project.build.outputDirectory}`. `process-classes` precedes `test`, so `mvn test` runs it. The Maven binding was already correct at boundary version 1; it carries version 2 only so that both build tools name the same contract.

### About the forbidden package list

The list above is the versioned reserved-package boundary, and it is deliberately a superset of
the canonical Ares list. Besides the packages Ares trusts by name, it stops student code
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

### What this boundary does not defend against

The build descriptor and the command that invokes it are **trusted instructor configuration**. The failure message says "No bypass flag is supported", and that is a statement about the shipped snippets: they offer no opt-out of their own. It is not a claim that the check survives an adversary who controls the build. Anyone who can edit `build.gradle` or `pom.xml`, or pass `-x verifyAresReservedPackagesV2`, can remove the boundary outright.

The threat this addresses is student **code** that declares a reserved package, not student control over the build. Your exercise template and its CI must own the build files and the invocation, and must fail visibly if either is altered.

## Verify your setup

### Start from a runnable example

The fastest check is to run something that is already known to work:

```bash
cd examples/ares-exercise-gradle && ./gradlew test
```

```bash
cd examples/ares-exercise-maven && mvn test
```

Each example is a complete exercise: supervised code, a policy, a test class, the reserved-package boundary and the full agent wiring. Each has a `README.md` stating the exact expected output. If an example passes but your project does not, the difference between the two is your defect.

### The two controls that matter

A setup check is only worth running if it can fail for the right reason. The examples are therefore built around a **pair** of controls in the same domain:

- **Positive control:** supervised code reads `allowed.txt`, which the policy permits. This must succeed. If it fails, enforcement is too strict, or the policy does not say what you think it says.
- **Negative control:** supervised code reads `secret.txt`, which the policy does not permit. The test asserts that Ares rejects it. If it succeeds, enforcement is not active at all.

Two details make this a genuine test rather than a reassuring one:

1. **The forbidden read must happen in supervised code, not in the test.** A test class named in `theFollowingClassesAreTestClasses` is exempt from enforcement, so a read performed by the test itself is *supposed* to succeed. Put the read in the student-facing class and let the test assert the exception.
2. **The policy must permit one file in the domain, not zero.** This is the part that is easy to get wrong. Ares adds a static deny-all rule only while a domain has **no** allowance ([Enforcement Model](/contributor/subsystems/policy/enforcement-model)). Under a fully restrictive file policy, ArchUnit or T. J. Watson Libraries for Analysis (WALA) rejects the operation before any runtime mechanism is consulted, so the negative control passes even with `-javaagent` removed and the weaving switched off, and it proves nothing. Granting exactly one permitted file makes the runtime layer authoritative for that domain, and only then does the negative control exercise the agent or the woven aspects.

A correct run is therefore **green**, and contains an asserted rejection. It is not a failed build.

### What a green run does and does not prove

A minimal test with no `@Policy` annotation confirms only that the Ares classes are on the test classpath and that the JVM started with the configured arguments. It does **not** prove that the agent instrumented anything, and it is not necessarily enforcement-free either: see [Further Options](../further-options.md) for what does and does not happen without a policy.

Prove enforcement with the paired controls from the two controls above, then break the setup deliberately and confirm each break is detected:

- Remove `-javaagent` from an exercise whose configuration ends in `_INSTRUMENTATION`. The negative control must now fail to reject.
- Remove the `aspect` dependency (Gradle) or `<aspectLibraries>` (Maven) from an exercise whose configuration ends in `_ASPECTJ`. Same expectation.
- Add a class in `de.tum.cit.ase.ares.api` to the student sources. The build must fail with the reserved-package diagnostic.

Each break must be made in the mode that depends **only** on the removed component. Removing `-javaagent` from an AspectJ exercise changes nothing, because the aspects were woven at compile time and still enforce the policy.

> **What happens without the agent?** Ares's **static analysis** (ArchUnit/WALA) still works, and **AspectJ enforcement** still works, because those aspects are woven at compile time. Only the **ByteBuddy instrumentation** path is inactive, since it relies on the agent to transform classes at load time. If you use an `INSTRUMENTATION` configuration, students could then bypass runtime restrictions, so always ensure the agent is loaded.

## Appendix: complete `pom.xml`

The working version of this file is [`examples/ares-exercise-maven`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-maven).

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.example</groupId>
    <artifactId>ares-exercise</artifactId>
    <version>1.0.0</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <ares.version>2.1.3</ares.version>
        <aspectj.version>1.9.25.1</aspectj.version>
        <!-- Keeps @{argLine} resolvable when JaCoCo is not part of the run. -->
        <argLine></argLine>
    </properties>

    <dependencies>
        <!-- provided, not test: see Section 3.2.2. -->
        <dependency>
            <groupId>de.tum.cit.ase</groupId>
            <artifactId>ares</artifactId>
            <version>${ares.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>${aspectj.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- 1. Weave the Ares aspects into the student classes. -->
            <plugin>
                <groupId>dev.aspectj</groupId>
                <artifactId>aspectj-maven-plugin</artifactId>
                <version>1.14.1</version>
                <dependencies>
                    <dependency>
                        <groupId>org.aspectj</groupId>
                        <artifactId>aspectjtools</artifactId>
                        <version>${aspectj.version}</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <forceAjcCompile>true</forceAjcCompile>
                    <complianceLevel>${maven.compiler.source}</complianceLevel>
                    <source>${maven.compiler.source}</source>
                    <target>${maven.compiler.target}</target>
                    <aspectLibraries>
                        <aspectLibrary>
                            <groupId>de.tum.cit.ase</groupId>
                            <artifactId>ares</artifactId>
                        </aspectLibrary>
                    </aspectLibraries>
                </configuration>
                <executions>
                    <execution>
                        <id>weave-main-classes</id>
                        <phase>process-classes</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- 2. Reserved-package build boundary, version 2. See Section 4. -->
            <!--    Copy the body of configuration/reservedPackages/MavenReservedPackages.xml here. -->

            <!-- 3. Put the agent and the AspectJ runtime at a known path. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>3.9.0</version>
                <executions>
                    <execution>
                        <id>copy-ares-runtime-jars</id>
                        <phase>process-test-classes</phase>
                        <goals>
                            <goal>copy</goal>
                        </goals>
                        <configuration>
                            <artifactItems>
                                <artifactItem>
                                    <groupId>de.tum.cit.ase</groupId>
                                    <artifactId>ares</artifactId>
                                    <version>${ares.version}</version>
                                    <classifier>agent</classifier>
                                    <overWrite>true</overWrite>
                                    <outputDirectory>${project.build.directory}/ares</outputDirectory>
                                    <destFileName>ares-agent.jar</destFileName>
                                </artifactItem>
                                <artifactItem>
                                    <groupId>org.aspectj</groupId>
                                    <artifactId>aspectjrt</artifactId>
                                    <version>${aspectj.version}</version>
                                    <overWrite>true</overWrite>
                                    <outputDirectory>${project.build.directory}/ares</outputDirectory>
                                    <destFileName>aspectjrt.jar</destFileName>
                                </artifactItem>
                            </artifactItems>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- 4. Attach the agent and open the JDK internals Ares introspects. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.4</version>
                <configuration>
                    <argLine>
                        @{argLine}
                        "-javaagent:${project.build.directory}/ares/ares-agent.jar"
                        "-Xbootclasspath/a:${project.build.directory}/ares/aspectjrt.jar"
                        --add-exports java.base/java.lang=ALL-UNNAMED
                        --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
                        --add-opens java.base/java.io=ALL-UNNAMED
                        --add-opens java.base/java.lang=ALL-UNNAMED
                        --add-opens java.base/java.lang.reflect=ALL-UNNAMED
                        --add-opens java.base/java.net=ALL-UNNAMED
                        --add-opens java.base/java.nio=ALL-UNNAMED
                        --add-opens java.base/java.nio.channels=ALL-UNNAMED
                        --add-opens java.base/java.util=ALL-UNNAMED
                        --add-opens java.base/java.util.concurrent=ALL-UNNAMED
                        --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED
                        --add-opens java.base/java.util.concurrent.locks=ALL-UNNAMED
                        --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
                        --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED
                        --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED
                        --add-opens java.base/sun.nio.ch=ALL-UNNAMED
                        --add-opens jdk.unsupported/sun.misc=ALL-UNNAMED
                    </argLine>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
