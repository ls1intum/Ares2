---
title: "Making a Project an Ares Project"
sidebar_position: 7
description: "Step-by-step guide to adding Ares 2 to an exercise repository, for Gradle and Maven."
---

> **Audience:** IT-Education experts with no security background.
> **Scope:** The `build.gradle` and `pom.xml` files.
> **Ares Version:** 2.1.1

> **Note:** This guide is a **setup guide**. It covers adding the Ares dependency, attaching the agent, configuring the build tool so that Ares can run, and rejecting student classes that would impersonate trusted code. For writing security policies that control what student code can do, see the [Security Policy Manual](security/policy-manual.md).

> **In a hurry?** Two complete, runnable exercises live in [`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples): [`ares-exercise-gradle`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-gradle) and [`ares-exercise-maven`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-maven). Copy one and adapt it. This manual explains what each part of them does and why.

> **Coming from Ares 1?** If you are converting an existing `de.tum.in.ase:artemis-java-test-sandbox` exercise, start from [How to Convert an Ares 1 Project into an Ares 2 Project](convert-ares1-to-ares2.md) instead. It is self-contained and covers everything this manual does, plus the annotation-to-policy translation.

**Related documentation:**
- [How to Convert an Ares 1 Project into an Ares 2 Project](convert-ares1-to-ares2.md), the migration guide for existing Ares 1 exercises
- [Security Policy Manual](security/policy-manual.md), which explains how to write a security policy YAML file
- [Security Policy Reader and Director Manual](/developer/policy/reader-and-director), which describes the internal processing pipeline
- [Enforcement Model](/developer/policy/enforcement-model), which defines what static analysis and the runtime layer are each responsible for, and specifies the reserved-package build boundary

---

## 1. Prerequisites

- **Java 17** or later
- **Gradle** in a version compatible with the chosen freefair AspectJ plugin (the freefair 9.x line used in this guide requires Gradle 9; older Gradle versions need an older freefair line) or **Maven 3.8+**
- **JUnit 5** (Jupiter) for test execution

---

## 2. Purpose: what problem does this solve?

Your Java project needs to run security tests that verify student submissions do not perform dangerous operations. To enable these tests, your `build.gradle` or `pom.xml` must:

1. Include the Ares library as a test dependency
2. Weave the Ares security aspects into the compiled bytecode with the AspectJ compiler
3. Download and attach the Ares agent JAR to the JVM at test startup
4. Grant the agent access to Java internals via specific JVM flags (required for bytecode instrumentation)
5. Reject student classes declared in reserved packages, so that student code cannot impersonate code Ares trusts by name

Steps 1 to 4 are covered in [Section 3](#3-add-ares-dependencies-and-agent-setup), step 5 in [Section 4](#4-reject-student-classes-in-reserved-packages). **All five are required.** Step 5 in particular is a deployment prerequisite rather than an optional extra: without it, a student can declare a class in a package Ares trusts and be trusted along with it.

---

## 3. Add Ares dependencies and agent setup

Blockquoted tips (marked `>`) throughout this section describe optional configuration that can be skipped on a first setup. Everything not in a blockquote is required.

### 3.1 Gradle (recommended)

First, add the AspectJ compiler plugin:

```gradle
plugins {
    id 'java'
    id 'io.freefair.aspectj.post-compile-weaving' version '9.2.0'
}
```

This plugin runs the AspectJ compiler (`ajc`) during your build to weave security aspects into bytecode. It also provides an `aspect` dependency configuration; the Ares JAR must be added to it (see [Section 3.1.4](#314-add-ares-dependencies)) so that `ajc` treats the aspects shipped inside the Ares JAR as an aspect library and weaves them into your bytecode.

#### 3.1.1 Configure repository lookup

```gradle
repositories {
    mavenCentral()
}
```

**Explanation:** `mavenCentral()` retrieves Ares and its dependencies from Maven Central. Gradle defines no repository by default, so this line is genuinely required. (Maven does define Central in its super-POM, which is why [Section 3.2](#32-maven-alternative) has no equivalent step.)

> **Note:** If your `build.gradle` already contains a `repositories` block, add `mavenCentral()` to that existing block instead of creating a new one.

#### 3.1.2 Declare the versions once

```gradle
ext {
    aresVersion = '2.1.1'
    aspectjVersion = '1.9.25.1'
}
```

**Explanation:** Both versions appear in several places below. Declaring each once means an upgrade is a single edit, and it removes the most common upgrade defect, which is changing the dependency coordinate but not the agent path that repeats the same version.

> **Tip (Gradle version catalog):** If your project uses a version catalog (for example `gradle/libs.versions.toml`), declare the versions there instead:
>
> ```toml
> [versions]
> ares = "2.1.1"
> aspectjrt = "1.9.25.1"
> [libraries]
> ares = { module = "de.tum.cit.ase:ares", version.ref = "ares" }
> aspectjrt = { module = "org.aspectj:aspectjrt", version.ref = "aspectjrt" }
> ```
>
> Then reference `libs.ares` and `libs.aspectjrt`. Note that version catalogs do not natively support Maven classifiers, so the agent dependency with the `:agent` classifier must remain a direct dependency string.

#### 3.1.3 Configure the Ares agent configurations

```gradle
configurations {
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
```

**Explanation:** A Gradle configuration is a named bucket of dependencies that can be resolved independently. Two dedicated buckets let the build refer to the agent JAR and the AspectJ runtime JAR by their exact file paths in [Section 3.1.5](#315-attach-the-agent-to-test-execution), without those files being mixed into the compile or test classpath.

Each property matters:

- `canBeResolved = true` and `canBeConsumed = false`: these buckets are resolved by this build and are not published to other projects.
- `transitive = false`: this is what makes the file selection safe. Each bucket then contains **exactly one** JAR, the one declared for it, so the build can take that file directly. Resolving transitively would pull in the whole dependency graph, and the build would have to guess which file it meant by matching on file names.

The agent JAR contains only Ares's own classes plus the `Premain-Class` manifest entry; it does not bundle Byte Buddy or any other dependency. Byte Buddy still reaches the instrumented JVM, because `-javaagent` attaches to the *same* JVM that runs your tests, and that JVM's classpath already carries Byte Buddy transitively via the `testImplementation` dependency in [Section 3.1.4](#314-add-ares-dependencies).

> **Note:** If your `build.gradle` already contains a `configurations` block, add these to that existing block instead of creating a new one.

#### 3.1.4 Add Ares dependencies

```gradle
dependencies {
    aresAgent "de.tum.cit.ase:ares:${aresVersion}:agent"
    aresAspectjRuntime "org.aspectj:aspectjrt:${aspectjVersion}"
    testImplementation "de.tum.cit.ase:ares:${aresVersion}"
    aspect "de.tum.cit.ase:ares:${aresVersion}"
    implementation "org.aspectj:aspectjrt:${aspectjVersion}"
}
```

**Explanation:**

- `aresAgent "de.tum.cit.ase:ares:...:agent"`: the Ares **agent** JAR, selected by the `agent` classifier. This is the artefact that carries the `Premain-Class` manifest entry, so it can be attached with `-javaagent` as published, with no repackaging.
- `aresAspectjRuntime "org.aspectj:aspectjrt:..."`: the same AspectJ runtime JAR again, in its own bucket, so that [Section 3.1.5](#315-attach-the-agent-to-test-execution) can put it on the bootstrap classpath.
- `testImplementation`: makes Ares classes available on the test classpath so your test code can use `@Policy`, `@Public` and the other Ares annotations. `testImplementation` rather than `implementation`, because Ares is needed during testing only. Using `implementation` would add Ares to the main classpath and the final artefact, where it could interfere with student code, and it violates the principle of minimal dependency scope.
- `aspect "de.tum.cit.ase:ares:..."`: registers the Ares JAR as an **aspect library** for the freefair plugin. This is what makes `ajc` weave the binary aspects shipped inside the Ares JAR; a `testImplementation` dependency alone is not enough, because `ajc` only weaves aspects that are on the aspect path (see [Section 3.1.6](#316-how-compile-time-weaving-works)).
- `implementation "org.aspectj:aspectjrt:..."`: the AspectJ runtime library, providing classes (for example `org.aspectj.lang.JoinPoint`) that woven bytecode references. `implementation` rather than `testImplementation`, because `ajc` weaves main classes during `compileJava`, and only the main compile classpath is visible at that point.

> **Note:** The freefair plugin also manages an `aspectjrt` version of its own. If your build fails with an AspectJ version conflict, drop the explicit `implementation "org.aspectj:aspectjrt:..."` line and let the plugin supply it, or align the plugin's version with `aspectjVersion` through its `aspectj { version = aspectjVersion }` extension.

> **Note:** If your `build.gradle` already contains a `dependencies` block, add these to that existing block instead of creating a new one.

#### 3.1.5 Attach the agent to test execution

```gradle
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider

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
    useJUnitPlatform()
    jvmArgumentProviders.add(aresJvmArguments)
}
```

**Explanation:**

- **Why an argument provider rather than `jvmArgs`.** Writing `jvmArgs += ["-javaagent:${configurations.aresAgent.singleFile}"]` looks simpler, but the string is evaluated while Gradle is *configuring* the build. That resolves the dependency even when you run an unrelated task, it fails the whole build if resolution fails, and it is incompatible with the configuration cache. A `CommandLineArgumentProvider` declares the JARs as task inputs and computes the arguments when the test task actually runs. The `@InputFiles` annotations are what let Gradle track them for the configuration and build caches.
- **Why `singleFile` is safe here.** Because both configurations are `transitive = false` with exactly one dependency each, each resolves to exactly one file. No file-name matching is involved, so there is no way to pick up the wrong JAR.
- `useJUnitPlatform()`: enables JUnit 5 (Jupiter) test discovery.
- `-javaagent:...`: loads the Ares agent before any user code runs, which is what the instrumentation enforcement path relies on.
- `-Xbootclasspath/a:...`: appends the AspectJ **runtime** JAR to the bootstrap classpath, so woven bytecode can resolve AspectJ runtime types at the bootstrap class-loader level.
- `tasks.withType(Test).configureEach`: applies to every test task, including custom ones, rather than only the default `test` task.
- **JVM module access flags.** All listed packages must be opened for Ares to introspect intercepted JDK objects and instrument bytecode. The list mirrors the `jvm.module.access.args` property in the Ares `pom.xml`:
  - `--add-exports java.base/java.lang`: makes the public `java.lang` API accessible to the unnamed module (test classpath)
  - `--add-exports java.base/jdk.internal.misc`: makes `jdk.internal.misc.Unsafe` accessible to the unnamed module for direct API calls
  - `--add-opens java.base/java.lang`: allows reflective access to private fields and methods in `java.lang` (for example `Class.declaredFields`)
  - `--add-opens java.base/java.lang.reflect`: allows reflective access to private members of the reflection API itself (for example `Field.setAccessible`)
  - `--add-opens java.base/jdk.internal.misc`: allows reflective access to private `Unsafe` fields (for example `theUnsafe`, read by the thread-system advice)
  - `--add-opens jdk.unsupported/sun.misc`: allows reflective access to the private `sun.misc.Unsafe` instance (legacy `Unsafe` path; `sun.misc` lives in `jdk.unsupported`, not in `java.base`)
  - `--add-opens java.base/java.io`, `java.net`, `java.nio`, `java.nio.channels`, `sun.net.www.protocol.http`, `sun.net.www.protocol.https`, `sun.nio.ch`: allow the Ares advice to reflectively read fields of intercepted file, network and channel objects (for example `Socket.delegate`)
  - `--add-opens java.base/java.util`, `java.util.concurrent`, `java.util.concurrent.atomic`, `java.util.concurrent.locks`: allow the Ares advice to reflectively read fields of intercepted collection and concurrency objects (for example `ThreadPoolExecutor.ctl`)

#### 3.1.6 How compile-time weaving works

1. **After compilation:** the `io.freefair.aspectj.post-compile-weaving` plugin runs `ajc` **after** `javac` finishes compiling your source files to bytecode.
2. **Aspect discovery:** `ajc` only weaves binary aspects from JARs placed on its **aspect path**; it does not pick them up from the ordinary compile classpath. The `aspect` dependency from [Section 3.1.4](#314-add-ares-dependencies) puts the Ares JAR there.
3. **Weaving:** `ajc` reads the compiled `.class` files in the project's output directory and weaves in any code whose execution matches the pointcut expressions defined in the discovered aspects.
4. **Runtime references:** the woven bytecode references AspectJ runtime classes, supplied by `aspectjrt` on the bootstrap classpath (configured in [Section 3.1.5](#315-attach-the-agent-to-test-execution)).

**Without the plugin, no weaving occurs** and the `-Xbootclasspath/a:` flag has no effect.

---

### 3.2 Maven (alternative)

There is no repository step for Maven: the super-POM already defines Central at `https://repo.maven.apache.org/maven2`, so re-declaring it adds nothing and can conflict with an organisational mirror.

#### 3.2.1 Declare the versions once

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <ares.version>2.1.1</ares.version>
    <aspectj.version>1.9.25.1</aspectj.version>
    <!-- Keeps @{argLine} resolvable when JaCoCo is not part of the run. -->
    <argLine></argLine>
</properties>
```

**Explanation:** the two version properties remove repetition, exactly as in [Section 3.1.2](#312-declare-the-versions-once). The empty `argLine` property matters for a different reason, explained in [Section 3.2.4](#324-attach-the-agent-via-maven-surefire-plugin).

#### 3.2.2 Add Ares dependencies

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

#### 3.2.3 Copy the agent and the AspectJ runtime to a known path

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

#### 3.2.4 Attach the agent via maven-surefire-plugin

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
- **`@{argLine}` is not optional if anything else contributes JVM arguments.** JaCoCo's `prepare-agent` goal works by *setting* the `argLine` property. A plain `<argLine>` overwrites it, and coverage then silently reports nothing. `@{argLine}` expands the property late, so both survive. That is also why [Section 3.2.1](#321-declare-the-versions-once) declares an empty `<argLine></argLine>` property: without it, a run in which JaCoCo does not participate fails with an unresolved `@{argLine}`.
- **Quote the two paths.** `${project.build.directory}` contains a space whenever the project sits under a directory such as `My Projects`. Surefire splits `argLine` on whitespace but honours double quotes, so the quotes are what keep such a path in one piece.
- **Merging with existing arguments.** If your exercise already sets `argLine`, append these entries to it rather than replacing them, keeping `@{argLine}` first. If another Java agent is present, order matters: put the Ares agent **after** a coverage agent such as JaCoCo, so coverage instrumentation is applied to the classes Ares then transforms rather than the reverse.
- The module access flags are identical to the Gradle ones; see [Section 3.1.5](#315-attach-the-agent-to-test-execution) for the per-flag explanation.

#### 3.2.5 Configure AspectJ compile-time weaving

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
- `<aspectLibraries>` puts the Ares JAR on the **aspect path**. Without it the plugin runs but weaves nothing, since `ajc` does not pick up binary aspects from the ordinary compile classpath. The entry references the dependency from [Section 3.2.2](#322-add-ares-dependencies), so no version is repeated.
- The execution binds `ajc` to `process-classes`, immediately after `javac`. Only main classes are woven; test classes are left untouched.

---

## 4. Reject student classes in reserved packages

**This section is required.** Ares trusts a number of runtime identities **by name**, including its own `de.tum.cit.ase.ares.api` package and the platform namespaces. If a student can put a class into one of those packages, that class inherits the trust and every other check can be walked around. The build must therefore refuse to compile student output into a reserved package.

The [Enforcement Model](/developer/policy/enforcement-model) specifies this boundary and calls it a deployment prerequisite, not an optional Ares runtime feature. Ares ships the executable snippets, so copy them rather than writing your own:

They ship inside the Ares JAR under `de/tum/cit/ase/ares/api/configuration/reservedPackages/`,
and live in the repository at
[`src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages/`](https://github.com/ls1intum/Ares2/tree/main/src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages):

- `GradleReservedPackages.gradle`
- `MavenReservedPackages.xml`
- `ReservedPackagePrefixes.txt` (the machine-readable prefix list)

Two versions are pinned. `RESERVED_PACKAGE_PREFIX_VERSION = 1` is the prefix data. `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION = 2` is the build-side contract that enforces it. Your exercise and its CI must pin both.

### 4.1 Gradle

Apply the shipped `GradleReservedPackages.gradle`, which registers `verifyAresReservedPackagesV2` over `sourceSets.main.output.classesDirs` and then attaches it in **two** places:

```gradle
tasks.named('check') { dependsOn tasks.named('verifyAresReservedPackagesV2') }
tasks.withType(Test).configureEach { dependsOn tasks.named('verifyAresReservedPackagesV2') }
```

Both are required, and the second is the one that is easy to get wrong. Gradle's Java plugin defines `check.dependsOn test`, **not** the reverse. A snippet that hangs the validation off `check` alone is therefore never executed by `gradlew test`, which is the command this manual gives you and the command a grading run invokes. That was the defect in boundary version 1: an exercise carrying it accepted student classes in reserved packages. If your exercise still contains a `verifyAresReservedPackagesV1` task, migrate it.

In a multi-project build, apply the snippet to **every** project that compiles student code: `tasks.withType(Test)` covers only the project it is applied to.

### 4.2 Maven

Apply the shipped `MavenReservedPackages.xml`, a `maven-antrun-plugin` execution bound to `process-classes` that scans `${project.build.outputDirectory}`. `process-classes` precedes `test`, so `mvn test` runs it. The Maven binding was already correct at boundary version 1; it carries version 2 only so that both build tools name the same contract.

### 4.3 What this boundary does not defend against

The build descriptor and the command that invokes it are **trusted instructor configuration**. The failure message says "No bypass flag is supported", and that is a statement about the shipped snippets: they offer no opt-out of their own. It is not a claim that the check survives an adversary who controls the build. Anyone who can edit `build.gradle` or `pom.xml`, or pass `-x verifyAresReservedPackagesV2`, can remove the boundary outright.

The threat this addresses is student **code** that declares a reserved package, not student control over the build. Your exercise template and its CI must own the build files and the invocation, and must fail visibly if either is altered.

---

## 5. Verify your setup

### 5.1 Start from a runnable example

The fastest check is to run something that is already known to work:

```bash
cd examples/ares-exercise-gradle && ./gradlew test
```

```bash
cd examples/ares-exercise-maven && mvn test
```

Each example is a complete exercise: supervised code, a policy, a test class, the reserved-package boundary and the full agent wiring. Each has a `README.md` stating the exact expected output. If an example passes but your project does not, the difference between the two is your defect.

### 5.2 The two controls that matter

A setup check is only worth running if it can fail for the right reason. The examples are therefore built around a **pair** of controls in the same domain:

- **Positive control:** supervised code reads `allowed.txt`, which the policy permits. This must succeed. If it fails, enforcement is too strict, or the policy does not say what you think it says.
- **Negative control:** supervised code reads `secret.txt`, which the policy does not permit. The test asserts that Ares rejects it. If it succeeds, enforcement is not active at all.

Two details make this a genuine test rather than a reassuring one:

1. **The forbidden read must happen in supervised code, not in the test.** A test class named in `theFollowingClassesAreTestClasses` is exempt from enforcement, so a read performed by the test itself is *supposed* to succeed. Put the read in the student-facing class and let the test assert the exception.
2. **The policy must permit one file in the domain, not zero.** This is the part that is easy to get wrong. Ares adds a static deny-all rule only while a domain has **no** allowance ([Enforcement Model](/developer/policy/enforcement-model)). Under a fully restrictive file policy, ArchUnit or WALA rejects the operation before any runtime mechanism is consulted, so the negative control passes even with `-javaagent` removed and the weaving switched off, and it proves nothing. Granting exactly one permitted file makes the runtime layer authoritative for that domain, and only then does the negative control actually exercise the agent or the woven aspects.

A correct run is therefore **green**, and contains an asserted rejection. It is not a failed build.

### 5.3 What a green run does and does not prove

A minimal test with no `@Policy` annotation confirms only that the Ares classes are on the test classpath and that the JVM started with the configured arguments. It does **not** prove that the agent instrumented anything, and it is not necessarily enforcement-free either: see [Section 6](#6-exercises-without-a-policy-annotation) for what does and does not happen without a policy.

To prove enforcement, use the paired controls from [Section 5.2](#52-the-two-controls-that-matter), then break the setup deliberately and confirm each break is detected:

- Remove `-javaagent` from an exercise whose configuration ends in `_INSTRUMENTATION`. The negative control must now fail to reject.
- Remove the `aspect` dependency (Gradle) or `<aspectLibraries>` (Maven) from an exercise whose configuration ends in `_ASPECTJ`. Same expectation.
- Add a class in `de.tum.cit.ase.ares.api` to the student sources. The build must fail with the reserved-package diagnostic.

Each break must be made in the mode that depends **only** on the removed component. Removing `-javaagent` from an AspectJ exercise changes nothing, because the aspects were woven at compile time and still enforce the policy.

> **What happens without the agent?** Ares's **static analysis** (ArchUnit/WALA) still works, and **AspectJ enforcement** still works, because those aspects are woven at compile time. Only the **ByteBuddy instrumentation** path is inactive, since it relies on the agent to transform classes at load time. If you use an `INSTRUMENTATION` configuration, students could then bypass runtime restrictions, so always ensure the agent is loaded.

---

## 6. Exercises without a `@Policy` annotation

A security policy is not always necessary. If your supervised code is meant to touch no files, open no connections, run no commands and start no threads, you can omit the policy file entirely and let Ares apply its policy-free configuration, which grants none of those things. This section explains exactly what that configuration does, because it is easy to over-estimate in both directions.

### 6.1 When Ares is active at all

Enforcement depends on **two** independent things: whether the Ares JUnit extension is registered, and whether a policy is present. Only the first is a precondition.

| Your test declares | Ares enforcement |
|---|---|
| A plain JUnit `@Test`, with or without `@Policy` | **None.** No Ares security code runs at all |
| An Ares test annotation (`@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest`), no `@Policy`, or a `@Policy` whose `value` is blank | The policy-free configuration described below |
| An Ares test annotation and a `@Policy` naming a policy file | That policy governs the five resource domains |
| An Ares test annotation and `@Policy(activated = false)` | **None.** This is the explicit opt-out |

The first row is the trap. `@Policy` is not itself a JUnit extension: it carries no `@ExtendWith`, and it registers nothing. What registers `JupiterSecurityExtension` is the `@JupiterAresTest` meta-annotation carried by `@Public`, `@Hidden`, `@PublicTest` and `@HiddenTest`. A test annotated only with `@Test` and `@Policy` therefore runs entirely unsupervised, and it does so silently, with no warning that the policy was never read.

> **Rule of thumb:** the Ares test annotation is what turns Ares on. The policy only decides how strict it then is.

### 6.2 What the policy-free configuration actually restricts

With the extension registered and no policy present, Ares builds a restrictive configuration in which all five permission lists are empty:

- **File system, network, command execution and thread creation: denied.** No allowance exists in any of those domains, so nothing is permitted.
- **Package imports: denied *outside an implicit allowlist*.** This is the part that is commonly overstated. Ares always unions three sources into the permitted set: the essential packages it ships, the supervised package itself, and the packages of the recognised test classes. The shipped essential list includes the `java` prefix, so all of `java.*` remains importable. Package imports are restricted, not eliminated.
- **No default execution timeout applies yet.** The policy-free configuration does construct a 10,000 ms limit, but timeouts belong to the **Phobos** test-case family, which Ares 2.1.1 generates without yet dispatching it from the in-process execution path. That part of the pipeline has not been migrated across, so the limit does not bound a test today. Add [`@StrictTimeout`](#9-glossary) wherever a test needs a deadline.

Two further points apply whether or not a policy is present:

- Ares installs fixed restrictions that no policy can grant, covering reflection, native access, JVM termination, class loading, JNDI and related domains. A policy governs the five resource domains, not everything.
- The reserved-package boundary of [Section 4](#4-reject-student-classes-in-reserved-packages) is still required. It is a build-side check and does not depend on the Ares extension activating at all.

The policy-free path also **fixes the analysis and enforcement modes**: it always uses ArchUnit for static analysis and AspectJ for the runtime layer, and it discovers the build tool from the project itself. Two consequences follow:

1. The ByteBuddy agent is not the enforcing mechanism here. The AspectJ weaving configured in [Section 3.1](#31-gradle-recommended) or [Section 3.2](#32-maven-alternative) is what enforces at runtime. A project that is not woven gets the static ArchUnit checks only.
2. Discovery has to succeed first, and it can fail. With no policy there is no explicitly selected build tool, so a project containing both a `pom.xml` and a `build.gradle` is rejected as ambiguous, and one containing neither is rejected as unsupported. Either failure happens **before** any enforcement is configured, so the restrictive configuration described above never takes effect in those cases; the build fails instead. See the [troubleshooting table](#8-troubleshooting).

A minimal test that runs under this configuration:

```java
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PenguinTest {

    @PublicTest
    void name() {
        assertEquals("Julian", new Penguin("Julian").getName());
    }
}
```

Note the absence of `@Test`: `@PublicTest` is itself a test annotation. If you prefer `@Public` you must add `@Test` alongside it, because `@Public` marks the test type without causing execution.

### 6.3 What it derives from the project, and why that matters

With a policy, the enforcement scope and the trusted test classes are **pinned by the instructor**. Without one, Ares derives both by scanning the project, and the project includes the student's submission:

- **The supervised package** is chosen as the most frequent non-reserved package among the production sources. A submission whose file distribution differs from what you expect can therefore shift the scope away from the code you meant to supervise.
- **The exempt test classes** are collected by scanning the discovered test source roots for annotated test classes. If students can add files beneath a test source root, they can obtain that exemption. A nested test class is covered only when its enclosing class is also recognised, because the scanner reports nested types in source notation (`Outer.Inner`) while the exemption check matches binary notation (`Outer$Inner`).

Neither is a defect in the fallback; it is what a fallback with no instructor input can do. But both mean the policy-free path is only as trustworthy as your control over the source roots. With a policy present, `theFollowingClassesAreTestClasses` pins the exempt set and Ares never scans for it.

### 6.4 When to use it

**Reasonable:**

- Exercises whose supervised code needs no file, network, command or thread access, and no package imports beyond the implicit allowlist.
- Smoke-testing a fresh setup, to confirm the wiring before writing a policy.

**Not reasonable:**

- Graded exercises, in general. The criterion is not "graded" as such but ownership: if students can influence which package dominates the production sources, or can add files beneath a discovered test root, then the scope and the exempt set are partly theirs to choose.
- Anything where you need to grant a specific allowance. As soon as one permission is required, write the policy; a policy with five empty lists is equally strict and additionally pins the scope, the exempt set and the mode, so it is the better default even when it grants nothing.

---

## 7. Next steps

1. **Create a security policy and annotate tests:** follow the [Security Policy Manual](security/policy-manual.md), which explains how to write `SecurityPolicy.yaml` files and apply `@Policy` to your tests. If your exercise needs no resource access at all, [Section 6](#6-exercises-without-a-policy-annotation) describes the alternative.
2. **Choose the right configuration:** select one of the eight `ProgrammingLanguageConfiguration` values matching your build tool, architecture analysis and runtime enforcement:

| Value | Build Tool | Static Analysis | Runtime Enforcement |
|---|---|---|---|
| `JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ` | Maven | ArchUnit (rule-based) | AspectJ (compile-time weaving) |
| `JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION` | Maven | ArchUnit (rule-based) | ByteBuddy agent (runtime) |
| `JAVA_USING_MAVEN_WALA_AND_ASPECTJ` | Maven | WALA (call-graph) | AspectJ (compile-time weaving) |
| `JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION` | Maven | WALA (call-graph) | ByteBuddy agent (runtime) |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ` | Gradle | ArchUnit (rule-based) | AspectJ (compile-time weaving) |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION` | Gradle | ArchUnit (rule-based) | ByteBuddy agent (runtime) |
| `JAVA_USING_GRADLE_WALA_AND_ASPECTJ` | Gradle | WALA (call-graph) | AspectJ (compile-time weaving) |
| `JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION` | Gradle | WALA (call-graph) | ByteBuddy agent (runtime) |

**How to choose:**
- **Build tool:** match your project (`MAVEN` or `GRADLE`).
- **Static analysis:** `ARCHUNIT` is simpler and faster; `WALA` detects transitive violations through call chains.
- **Runtime enforcement:** `INSTRUMENTATION` (ByteBuddy agent) or `ASPECTJ` (compile-time weaving). Configure both mechanisms regardless of which you choose, so that switching is a policy edit rather than a build change.

---

## 8. Troubleshooting

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| `ClassNotFoundException: de.tum.cit.ase.ares.api.Policy` | Ares not on the test classpath | Verify the `testImplementation` dependency is present |
| `The artifact de.tum.cit.ase:ares referenced in aspectj plugin as an aspect library, is not found the project dependencies` | Maven only: Ares is `<scope>test</scope>`, but the aspect library is resolved against the compile-visible dependencies | Change the scope to `provided` ([Section 3.2.2](#322-add-ares-dependencies)) |
| A test asserting on the violation text fails, although the operation was blocked | Ares localises its violation messages, so "blocked by Ares" reads "von Ares blockiert" on a German JVM | Assert on locale-stable content, such as the file name or the offending method, rather than on the prose |
| `Could not find de.tum.cit.ase:ares:<version>` | The version does not exist on Maven Central, or a mirror has not synchronised it | Check the coordinate against Maven Central; only released versions resolve |
| `Failed to find premain agent`, or agent-related errors | Agent JAR not found, or wrong classifier | Ensure the dependency uses the `:agent` classifier (Gradle) or the `<classifier>agent</classifier>` artefact item (Maven) |
| Tests pass but student code is not restricted | `-javaagent` missing **and** no weaving | Check the argument provider (Gradle) or `<argLine>` (Maven), and that the `aspect` / `<aspectLibraries>` entry is present |
| **The build succeeds but nothing is woven** | The Ares JAR is on the compile classpath but not on the **aspect path** | Add `aspect "de.tum.cit.ase:ares:..."` (Gradle) or the `<aspectLibraries>` entry (Maven). `ajc` ignores binary aspects that are not on the aspect path, so this fails silently |
| `InaccessibleObjectException` at runtime | Missing `--add-opens` / `--add-exports` flags | Ensure the complete list from [Section 3.1.5](#315-attach-the-agent-to-test-execution) / [Section 3.2.4](#324-attach-the-agent-via-maven-surefire-plugin) is present. A partial list fails only once a policy exercises the corresponding advice |
| Coverage reports nothing after adding Ares | A plain `<argLine>` overwrote the property JaCoCo sets | Prefix the Surefire `<argLine>` with `@{argLine}` and declare an empty `<argLine>` property |
| `Could not resolve all files for configuration ':aresAgent'`, or "expected exactly one file" | The configuration is transitive, so it holds more than the agent JAR | Set `transitive = false` on the dedicated configurations, as in [Section 3.1.3](#313-configure-the-ares-agent-configurations) |
| `IllegalStateException: Ambiguous project: both Maven and Gradle descriptors are active` | The project has both a `pom.xml` and a `build.gradle`, and the no-policy path has no explicitly selected build tool, so it cannot tell which is authoritative. Discovery fails before any enforcement is configured | Remove the descriptor you do not use, or supply a policy that names the configuration explicitly |
| `IllegalStateException: Unsupported project: no pom.xml, build.gradle or build.gradle.kts` | The directory the tests run from carries no supported build descriptor | Run from the project root that holds the build descriptor |
| `logback.xml occurs multiple times on the classpath` | The agent JAR and the ordinary Ares JAR each carry one | A warning only; enforcement is unaffected |
| The reserved-package check never runs under `gradlew test` | A boundary version 1 snippet hooked `check` alone | Migrate to boundary version 2, which also gates every `Test` task ([Section 4.1](#41-gradle)) |
| Policy seems to have no effect | Wrong `withinPath` | Gradle: `classes/java/main/<package/path>`, Maven: `classes/<package/path>` |

---

## 9. Glossary

| Term | Meaning |
|------|----------|
| **Java Agent** | A JVM mechanism (`-javaagent`) that allows code to transform class bytecode at load time. Ares uses a ByteBuddy-based agent to intercept forbidden operations at runtime. |
| **ByteBuddy** | A library for creating and modifying Java classes at runtime, used by Ares to implement the instrumentation agent. |
| **Instrumentation** | The runtime AOP approach where class bytecode is modified at load time via the `java.lang.instrument` API. One of the two runtime enforcement mechanisms in Ares, alongside AspectJ. |
| **AspectJ** | A compile-time AOP framework used for runtime enforcement. Requires the AspectJ compiler plugin to weave aspects during the build, and the AspectJ runtime JAR on the bootstrap classpath. The compiler weaves the aspects from the Ares JAR only if that JAR is on the aspect path (Gradle: the `aspect` configuration; Maven: an `<aspectLibraries>` entry). |
| **Aspect path** | The set of JARs `ajc` reads binary aspects from. Distinct from the compile classpath: a JAR on the classpath alone contributes no aspects. |
| **`CommandLineArgumentProvider`** | The Gradle interface used here to compute test JVM arguments when the task runs rather than when the build is configured, which keeps dependency resolution out of the configuration phase and the build configuration-cache compatible. |
| **`--add-opens` / `--add-exports`** | JVM flags that grant access to internal Java modules. Required by Ares to introspect intercepted JDK objects. |
| **`withinPath`** | The path to compiled student bytecode, relative to the build output directory. Differs between Gradle (`classes/java/main/...`) and Maven (`classes/...`). |
| **`ProgrammingLanguageConfiguration`** | An enum encoding the combination of build tool, static analysis framework and runtime enforcement mechanism. |
| **Classifier (`:agent`)** | A Maven/Gradle coordinate qualifier selecting a variant of an artefact. The `:agent` classifier selects the agent JAR, which carries the `Premain-Class` manifest entry and needs no repackaging. |
| **Reserved package** | A package prefix that student code may not declare, because Ares trusts that identity by name. Enforced by the build, see [Section 4](#4-reject-student-classes-in-reserved-packages). |
| **Phobos** | A test-case family covering the file-system, network and timeout domains. Ares 2.1.1 generates Phobos cases but does not yet dispatch them from the in-process execution path, so a policy timeout does not bound a test today. Use `@StrictTimeout` for a deadline. |
| **`@StrictTimeout`** | The annotation that actually bounds test execution. Applied to a test class or method, and unchanged from Ares 1 apart from its package. |
| **Positive / negative control** | The paired checks of [Section 5.2](#52-the-two-controls-that-matter): one permitted operation that must succeed, one forbidden operation that must be rejected. Neither alone demonstrates that enforcement works. |

---

## 10. Appendix A: complete `build.gradle`

The working version of this file, together with the sources and policy it refers to, is [`examples/ares-exercise-gradle`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-gradle).

```gradle
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider

plugins {
    id 'java'
    id 'io.freefair.aspectj.post-compile-weaving' version '9.2.0'
}

ext {
    aresVersion = '2.1.1'
    aspectjVersion = '1.9.25.1'
}

repositories {
    mavenCentral()
}

configurations {
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
    aresAgent "de.tum.cit.ase:ares:${aresVersion}:agent"
    aresAspectjRuntime "org.aspectj:aspectjrt:${aspectjVersion}"
    testImplementation "de.tum.cit.ase:ares:${aresVersion}"
    aspect "de.tum.cit.ase:ares:${aresVersion}"
    implementation "org.aspectj:aspectjrt:${aspectjVersion}"
}

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
    useJUnitPlatform()
    jvmArgumentProviders.add(aresJvmArguments)
}

// Ares reserved-package build boundary, version 2. See Section 4.
apply from: 'gradle/AresReservedPackages.gradle'
```

## 11. Appendix B: complete `pom.xml`

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
        <ares.version>2.1.1</ares.version>
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
