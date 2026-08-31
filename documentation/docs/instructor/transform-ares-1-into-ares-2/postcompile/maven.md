---
title: "Maven"
sidebar_position: 2
description: "Migrating an Ares 1 Maven exercise onto Ares 2 in Postcompile mode: dependency, wiring, guard and verification."
---

:::tip[Simple Story]
This is the build-side half of the migration for a Maven exercise.

The parts that do not depend on your build tool are on [the common page](../index.md); do those
first, or alongside these.
:::

## Before this page

Read [the common page](../index.md) for why to migrate, what changes, the import rewrite and the
annotation-to-policy translation. This page assumes you have a `security-policy.yaml` in hand.

## Replace the dependency and wire up the build

Remove the Ares 1 dependency:

```xml
<!-- DELETE THIS -->
<dependency>
    <groupId>de.tum.in.ase</groupId>
    <artifactId>artemis-java-test-sandbox</artifactId>
    <version>1.15.0</version>
    <scope>test</scope>
</dependency>
```

```gradle
// DELETE THIS
testImplementation 'de.tum.in.ase:artemis-java-test-sandbox:1.15.0'
```

> **On the libraries Ares 1 bundled.** Ares 1 shipped JUnit 5, AssertJ and Hamcrest transitively, so exercises often relied on them without declaring them. Ares 2 still exposes JUnit and AssertJ; Hamcrest is the one that disappears. Regardless of what remains transitive, declare the test libraries your tests import. Relying on another library's transitive graph is what makes an upgrade break compilation for reasons unrelated to the upgrade.

Ares 2 needs four things where Ares 1 needed one: the dependency, AspectJ weaving of the student
bytecode, the agent attached to the test Java Virtual Machine (JVM), and a set of JVM module-access flags.

There is no repository step: Maven's super-POM already defines Central.

Declare the versions and an empty `argLine` property:

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

Add the dependencies. **Note the scope.**

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

Your Ares 1 dependency was `<scope>test</scope>`, and carrying that scope across is the single most common way to produce a Maven build that compiles but enforces nothing. `aspectj-maven-plugin` resolves its `<aspectLibraries>` against the **compile-visible** dependencies and weaves the *main* classes during `process-classes`, where a test-scoped artefact does not exist. It fails with `The artifact de.tum.cit.ase:ares referenced in aspectj plugin as an aspect library, is not found the project dependencies`. `provided` is visible at compile time and at test time while staying out of the packaged artefact, which is what an exercise wants.

Weave the aspects:

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

`forceAjcCompile` is required because your project contains no `.aj` sources: the aspects come from the Ares JAR, and without this flag `ajc` would decline to run.

Put the agent and the AspectJ runtime at a path the build knows:

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

Do not point `-javaagent` at `${settings.localRepository}` instead. That hard-codes the repository layout, assumes the artefact has already been downloaded, and repeats the version inside a path where an upgrade is easy to miss.

Attach the agent:

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

Three details that bite:

- **Pin the version.** Without `<version>`, Surefire floats with whatever the super-POM binds, and a Maven upgrade silently changes how tests are launched.
- **`@{argLine}` first.** JaCoCo's `prepare-agent` works by *setting* the `argLine` property. A plain `<argLine>` overwrites it and coverage silently reports nothing. This is why the properties block declares an empty `<argLine></argLine>`: without it, a run without JaCoCo fails on an unresolved `@{argLine}`.
- **Quote the two paths.** `${project.build.directory}` contains a space whenever the project sits under a directory such as `My Projects`. Surefire splits on whitespace but honours double quotes.

If another agent is present, put the Ares agent **after** a coverage agent, so coverage instrumentation is applied to the classes Ares then transforms rather than the reverse.

## Point the build at your sources

Which directories Maven compiles depends on your layout (see
[Know your project layout](../index.md#know-your-project-layout)).

- **Standard Maven layout.** `src/main/java`, `src/test/java` and `src/test/resources` are the
  defaults. Add nothing.
- **Standard Artemis layout.** Student code compiles from `assignment/src`, and the test code and
  the structure oracle `test.json` live under `test/`. Redirect the source and test directories, and
  declare `test/` as a test resource as well, so `test.json` reaches the test classpath:

  ```xml
  <build>
      <sourceDirectory>${project.basedir}/assignment/src</sourceDirectory>
      <testSourceDirectory>${project.basedir}/test</testSourceDirectory>
      <testResources>
          <testResource>
              <directory>${project.basedir}/test</directory>
          </testResource>
      </testResources>
      <!-- <plugins> ... </plugins> as below -->
  </build>
  ```

The `<testResources>` block is what puts `test.json` on the test classpath; without it every
structural test fails reporting a missing structure oracle. Ares reads these source roots by parsing
`pom.xml`, so declaring them here is also what lets its structural tests find the student classes.

## Apply `@Policy` to the tests

Ares 1 needed no annotation to activate security; the test-type annotation was enough, and the security annotations configured it. Ares 2 keeps the test-type annotation and adds `@Policy`:

```java
package org.example;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

@Policy(value = "src/test/resources/SecurityPolicy.yaml", withinPath = "classes/org/example")
public class PenguinTest {

    @PublicTest
    void name() {
        // ...
    }
}
```

The three parameters:

| Parameter | Meaning |
|---|---|
| `value` | Path to the policy file, relative to the working directory of the test run. Blank means the policy-free configuration of the step named in section 9 of this guide |
| `withinPath` | Path to the compiled student bytecode, relative to the build output directory. It must start with `classes` or `test-classes` |
| `activated` | Defaults to `true`. `@Policy(activated = false)` is the explicit opt-out |

`withinPath` differs by build tool, and getting it wrong is the classic "the policy seems to have no effect" symptom:

- **Gradle:** `classes/java/main/<package/path>`, for example `classes/java/main/org/example`
- **Maven:** `classes/<package/path>`, for example `classes/org/example`

`withinPath` does not change with your project layout. It points at the *compiled* bytecode under
the build output directory, not at your sources: Maven writes the main classes to `target/classes`
whether they came from `src/main/java` or `assignment/src`, so the Maven value stays
`classes/<package/path>` either way.

The `value` path, in contrast, does follow your layout, matching where you created the policy file:
`src/test/resources/SecurityPolicy.yaml` for the standard layout, `test/SecurityPolicy.yaml` (or the
package-mirrored `test/de/tum/cit/aet/SecurityPolicy.yaml`) for the Artemis layout.

> **The activation rule that has no Ares 1 counterpart.** `@Policy` is not itself a JUnit extension and registers nothing. What activates Ares is the test-type annotation (`@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest`). A test annotated with a plain JUnit `@Test` and a `@Policy` runs completely unsupervised, silently. If you migrate a test class and drop its `@Public` in the process, you lose all enforcement without any error.

## Replace the class-shadowing guard

Your Ares 1 exercise almost certainly contains a build-side guard preventing student classes from landing in trusted packages, as the Ares 1 documentation required. It must be replaced, not kept: the prefix list it uses names Ares 1's packages and third-party libraries that are no longer the relevant ones.

Ares 2 trusts a different set of identities by name, including its own `de.tum.cit.ase.ares.api` package, Byte Buddy, AspectJ, T. J. Watson Libraries for Analysis (WALA) and ArchUnit. Ares ships the executable snippets, so copy them rather than editing your old list:

- `GradleReservedPackages.gradle`
- `MavenReservedPackages.xml`
- `ReservedPackagePrefixes.txt` (the machine-readable list)

They ship inside the Ares JAR under `de/tum/cit/ase/ares/api/configuration/reservedPackages/`, and live in the Ares repository at `src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages/`. Both are reproduced in full below, so you can complete the migration without extracting them.

Two versions are pinned, and your exercise and its continuous integration (CI) must pin both. `RESERVED_PACKAGE_PREFIX_VERSION = 1` is the prefix data. `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION = 2` is the build-side contract that enforces it.

### Maven

Delete the `maven-enforcer-plugin` execution with the `requireFilesDontExist` rule. Add this to `<build><plugins>`:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-antrun-plugin</artifactId>
  <version>3.2.0</version>
  <executions>
    <execution>
      <id>verify-ares-reserved-packages-v2</id>
      <phase>process-classes</phase>
      <goals><goal>run</goal></goals>
      <configuration>
        <target>
          <resourcecount property="ares.reserved.package.count">
            <fileset dir="${project.build.outputDirectory}">
              <include name="java/**"/><include name="javax/**"/><include name="sun/**"/>
              <include name="jdk/**"/><include name="com/sun/**"/>
              <include name="de/tum/cit/ase/ares/api/**"/><include name="net/bytebuddy/**"/>
              <include name="org/aspectj/**"/><include name="com/ibm/wala/**"/>
              <include name="com/tngtech/archunit/**"/><include name="anonymous/toolclasses/**"/>
              <include name="metatest/**"/>
            </fileset>
          </resourcecount>
          <fail message="Ares reserved-package validation 2 rejected student output. No bypass flag is supported.">
            <condition><not><equals arg1="${ares.reserved.package.count}" arg2="0"/></not></condition>
          </fail>
        </target>
      </configuration>
    </execution>
  </executions>
</plugin>
```

`process-classes` precedes `test`, so `mvn test` runs it. The Maven binding was already correct at boundary version 1; the version moves to 2 only so both build tools name the same contract.

> **What this boundary does not defend against.** The build descriptor and the command that invokes it are **trusted instructor configuration**. "No bypass flag is supported" describes the shipped snippets: they offer no opt-out of their own. It is not a claim that the check survives an adversary who controls the build. Anyone who can edit `build.gradle` or `pom.xml`, or pass `-x verifyAresReservedPackagesV2`, can remove it. The threat addressed is student **code** that declares a reserved package. Your exercise template and its CI must own the build files and the invocation.

:::warning[Ares does not generate this boundary in either mode]
The shipped snippets under
[`configuration/reservedPackages/`](https://github.com/ls1intum/Ares2/tree/main/src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages)
appear in no copy configuration and are emitted by no code path. You install them by hand, in
Postcompile and in Precompile alike.
:::

## The alternative: no policy file at all

If your exercise's supervised code needs no file, network, command or thread access, you can skip Step 3 and Step 4's `value` parameter entirely. With the test-type annotation present and no `@Policy` (or a `@Policy` whose `value` is blank), Ares applies a policy-free configuration that grants none of those things.

What it restricts:

- **File system, network, command execution and thread creation: denied.** No allowance exists.
- **Package imports: restricted, not eliminated.** Ares always permits an implicit allowlist made of the essential packages it ships, the supervised package itself, and the packages of the recognised test classes. The essential list includes the `java` prefix, so all of `java.*` stays importable.
- **No default execution timeout yet.** A 10,000 ms limit is constructed, but timeouts belong to the Phobos test-case family, which Ares 2.1.3 generates without yet dispatching in-process. Add `@StrictTimeout` where a test needs a deadline.

It fixes the modes: always ArchUnit for static analysis and AspectJ for the runtime layer, with the build tool discovered from the project. So the AspectJ weaving of the step named in section 4 of this guide is what enforces at runtime here, and a project that is not woven falls back to the static checks alone.

That discovery step has to succeed before any of this applies. Without a policy there is no explicitly selected build tool, so a project containing both a `pom.xml` and a `build.gradle` is rejected as ambiguous, and one containing neither is rejected as unsupported. Both fail the build outright rather than falling back to the restrictive configuration.

> **This is not the Ares 1 default.** Do not reach for it because "Ares 1 denied everything until I whitelisted something". Ares 1 denied *file paths* in the absence of `@WhitelistPath`, but the other domains had their own defaults, and package access was mostly permitted: `@BlacklistPackage`'s own documentation states that by default all packages can be used apart from `java.lang.reflect` and Ares internals. Check every domain your exercise touches before concluding that the policy-free configuration matches your old behaviour.

The second reason for caution is that without a policy, Ares derives from the project what a policy would have pinned:

- **The supervised package** is chosen as the most frequent non-reserved package among the production sources, so a submission with an unexpected file distribution can shift the enforcement scope.
- **The exempt test classes** are found by scanning the discovered test source roots for annotated test classes. If students can add files beneath a test source root, they can obtain that exemption. Note that a nested test class is covered only when its enclosing class is recognised too: the scanner reports nested types in source notation (`Outer.Inner`), whereas the exemption check matches binary notation (`Outer$Inner`), so an independently detected nested class is not exempt on its own.

For a graded exercise, prefer a policy with six empty lists over no policy at all. It is equally strict and pins the scope, the exempt set and the mode.

## Verify the migration

A migration is verified when enforcement can be shown to fail for the right reason. Build a **pair** of controls in one domain:

- **Positive control:** supervised code performs an operation the policy permits. It must succeed. If it fails, enforcement is stricter than the policy says, or the policy does not say what you think.
- **Negative control:** supervised code performs an operation the policy does not permit. The test asserts the rejection. If it succeeds, enforcement is not active.

Two details decide whether this proves anything:

1. **The forbidden operation must happen in supervised code, not in the test.** A class named in `theFollowingClassesAreTestClasses` is exempt, so an operation performed by the test itself is *supposed* to succeed. Put it in the student-facing class and let the test assert the exception.
2. **The domain must permit exactly one thing, not nothing.** Ares adds a static deny-all rule only while a domain has **no** allowance. Under a fully restrictive file policy, the static analysis rejects the operation before the runtime layer is consulted, so the negative control passes even with the agent detached and the weaving switched off, and it proves nothing. One allowance makes the runtime layer authoritative for that domain.

A correct run is therefore **green** and contains an asserted rejection. Then break the setup deliberately and confirm each break is detected:

- Remove the `aspect` dependency (Gradle) or `<aspectLibraries>` (Maven) from an `_ASPECTJ` exercise. The negative control must stop rejecting.
- Remove `-javaagent` from an `_INSTRUMENTATION` exercise. Same expectation. Removing it from an `_ASPECTJ` exercise correctly changes nothing, because the aspects were woven at compile time.
- Add a class declaring `package de.tum.cit.ase.ares.api;` to the student sources. The build must fail with the reserved-package diagnostic.

Two complete, runnable exercises are available in [`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples), one per build tool. If an example passes and your migrated project does not, the difference between the two is your defect.

## Appendix: complete `pom.xml`

A working version of this file is [`examples/ares-exercise-maven`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-maven).

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
        <!-- provided, not test: see Section 4.2. -->
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

            <!-- 2. Reserved-package build boundary, version 2. Replaces the Ares 1
                    maven-enforcer-plugin rule. See Section 8.2. -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-antrun-plugin</artifactId>
                <version>3.2.0</version>
                <executions>
                    <execution>
                        <id>verify-ares-reserved-packages-v2</id>
                        <phase>process-classes</phase>
                        <goals><goal>run</goal></goals>
                        <configuration>
                            <target>
                                <resourcecount property="ares.reserved.package.count">
                                    <fileset dir="${project.build.outputDirectory}">
                                        <include name="java/**"/><include name="javax/**"/><include name="sun/**"/>
                                        <include name="jdk/**"/><include name="com/sun/**"/>
                                        <include name="de/tum/cit/ase/ares/api/**"/><include name="net/bytebuddy/**"/>
                                        <include name="org/aspectj/**"/><include name="com/ibm/wala/**"/>
                                        <include name="com/tngtech/archunit/**"/><include name="anonymous/toolclasses/**"/>
                                        <include name="metatest/**"/>
                                    </fileset>
                                </resourcecount>
                                <fail message="Ares reserved-package validation 2 rejected student output. No bypass flag is supported.">
                                    <condition><not><equals arg1="${ares.reserved.package.count}" arg2="0"/></not></condition>
                                </fail>
                            </target>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

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
