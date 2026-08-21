---
title: "Gradle"
sidebar_position: 2
description: "Protecting a Gradle project with Ares 2 in Postcompile mode, from dependency to enforced policy."
---

:::tip[ELI5]
This is the whole path for a Gradle exercise, in the order you have to do it.

Add Ares, mark your tests, write down what the student code may do, and wire the check into the
build so nobody can quietly remove it.
:::

## The path, in order

1. **Add the dependency and the agent**, below.
2. **[Set up the public and hidden test model](../setup.md)**.
3. **[Mark your tests](../test-annotations.md)** with `@PublicTest` or `@HiddenTest`, and give
   hidden tests a `@Deadline`.
4. **[Write the policy](../policy-configuration.md)**, choosing one of the four
   `JAVA_USING_GRADLE_*` configurations.
5. **Apply `@Policy` to the tests**, below.
6. **Reject student classes in reserved packages**, below. This step is **not optional**.
7. **Verify with both controls**, below.

## Add the Ares dependency and the agent

:::note[Blockquoted tips are optional]
Blockquoted tips (marked `>`) in the setup steps below describe optional configuration that can be
skipped on a first setup. Everything not in a blockquote is required.
:::

First, add the AspectJ compiler plugin:

```gradle
plugins {
    id 'java'
    id 'io.freefair.aspectj.post-compile-weaving' version '9.2.0'
}
```

This plugin runs the AspectJ compiler (`ajc`) during your build to weave security aspects into bytecode. It also provides an `aspect` dependency configuration; the Ares JAR must be added to it (see [Add Ares dependencies](#add-ares-dependencies)) so that `ajc` treats the aspects shipped inside the Ares JAR as an aspect library and weaves them into your bytecode.

### Configure repository lookup

```gradle
repositories {
    mavenCentral()
}
```

**Explanation:** `mavenCentral()` retrieves Ares and its dependencies from Maven Central. Gradle defines no repository by default, so this line is genuinely required. (Maven does define Central in its super-POM, which is why [the Maven page](../postcompile/maven.md) has no equivalent step.)

> **Note:** If your `build.gradle` already contains a `repositories` block, add `mavenCentral()` to that existing block instead of creating a new one.

### Declare the versions once

```gradle
ext {
    aresVersion = '2.1.2'
    aspectjVersion = '1.9.25.1'
}
```

**Explanation:** Both versions appear in several places below. Declaring each once means an upgrade is a single edit, and it removes the most common upgrade defect, which is changing the dependency coordinate but not the agent path that repeats the same version.

> **Tip (Gradle version catalog):** If your project uses a version catalog (for example `gradle/libs.versions.toml`), declare the versions there instead:
>
> ```toml
> [versions]
> ares = "2.1.2"
> aspectjrt = "1.9.25.1"
> [libraries]
> ares = { module = "de.tum.cit.ase:ares", version.ref = "ares" }
> aspectjrt = { module = "org.aspectj:aspectjrt", version.ref = "aspectjrt" }
> ```
>
> Then reference `libs.ares` and `libs.aspectjrt`. Note that version catalogs do not natively support Maven classifiers, so the agent dependency with the `:agent` classifier must remain a direct dependency string.

### Configure the Ares agent configurations

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

**Explanation:** A Gradle configuration is a named bucket of dependencies that can be resolved independently. Two dedicated buckets let the build refer to the agent JAR and the AspectJ runtime JAR by their exact file paths in the agent step above, without those files being mixed into the compile or test classpath.

Each property matters:

- `canBeResolved = true` and `canBeConsumed = false`: these buckets are resolved by this build and are not published to other projects.
- `transitive = false`: this is what makes the file selection safe. Each bucket then contains **exactly one** JAR, the one declared for it, so the build can take that file directly. Resolving transitively would pull in the whole dependency graph, and the build would have to guess which file it meant by matching on file names.

The agent JAR contains only Ares's own classes plus the `Premain-Class` manifest entry; it does not bundle Byte Buddy or any other dependency. Byte Buddy still reaches the instrumented Java Virtual Machine (JVM), because `-javaagent` attaches to the *same* JVM that runs your tests, and that JVM's classpath already carries Byte Buddy transitively via the `testImplementation` dependency in [Add Ares dependencies](#add-ares-dependencies).

> **Note:** If your `build.gradle` already contains a `configurations` block, add these to that existing block instead of creating a new one.

### Add Ares dependencies

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
- `aresAspectjRuntime "org.aspectj:aspectjrt:..."`: the same AspectJ runtime JAR again, in its own bucket, so that the agent step above can put it on the bootstrap classpath.
- `testImplementation`: makes Ares classes available on the test classpath so your test code can use `@Policy`, `@Public` and the other Ares annotations. `testImplementation` rather than `implementation`, because Ares is needed during testing only. Using `implementation` would add Ares to the main classpath and the final artefact, where it could interfere with student code, and it violates the principle of minimal dependency scope.
- `aspect "de.tum.cit.ase:ares:..."`: registers the Ares JAR as an **aspect library** for the freefair plugin. This is what makes `ajc` weave the binary aspects shipped inside the Ares JAR; a `testImplementation` dependency alone is not enough, because `ajc` only weaves aspects that are on the aspect path (see [How compile-time weaving works](#how-compile-time-weaving-works)).
- `implementation "org.aspectj:aspectjrt:..."`: the AspectJ runtime library, providing classes (for example `org.aspectj.lang.JoinPoint`) that woven bytecode references. `implementation` rather than `testImplementation`, because `ajc` weaves main classes during `compileJava`, and only the main compile classpath is visible at that point.

> **Note:** The freefair plugin also manages an `aspectjrt` version of its own. If your build fails with an AspectJ version conflict, drop the explicit `implementation "org.aspectj:aspectjrt:..."` line and let the plugin supply it, or align the plugin's version with `aspectjVersion` through its `aspectj { version = aspectjVersion }` extension.

> **Note:** If your `build.gradle` already contains a `dependencies` block, add these to that existing block instead of creating a new one.

### Attach the agent to test execution

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
- **Why `singleFile` is safe here.** Both configurations are `transitive = false` with exactly one dependency each, so each resolves to exactly one file. No file-name matching is involved, so there is no way to pick up the wrong JAR.
- `useJUnitPlatform()`: enables JUnit 5 (Jupiter) test discovery.
- `-javaagent:...`: loads the Ares agent before any user code runs, which is what the instrumentation enforcement path relies on.
- `-Xbootclasspath/a:...`: appends the AspectJ **runtime** JAR to the bootstrap classpath, so woven bytecode can resolve AspectJ runtime types at the bootstrap class-loader level.
- `tasks.withType(Test).configureEach`: applies to every test task, including custom ones, rather than only the default `test` task.
- **JVM module access flags.** All listed packages must be opened for Ares to introspect intercepted Java Development Kit (JDK) objects and instrument bytecode. The list mirrors the `jvm.module.access.args` property in the Ares `pom.xml`:
  - `--add-exports java.base/java.lang`: makes the public `java.lang` application programming interface (API) accessible to the unnamed module (test classpath)
  - `--add-exports java.base/jdk.internal.misc`: makes `jdk.internal.misc.Unsafe` accessible to the unnamed module for direct API calls
  - `--add-opens java.base/java.lang`: allows reflective access to private fields and methods in `java.lang` (for example `Class.declaredFields`)
  - `--add-opens java.base/java.lang.reflect`: allows reflective access to private members of the reflection API itself (for example `Field.setAccessible`)
  - `--add-opens java.base/jdk.internal.misc`: allows reflective access to private `Unsafe` fields (for example `theUnsafe`, read by the thread-system advice)
  - `--add-opens jdk.unsupported/sun.misc`: allows reflective access to the private `sun.misc.Unsafe` instance (legacy `Unsafe` path; `sun.misc` lives in `jdk.unsupported`, not in `java.base`)
  - `--add-opens java.base/java.io`, `java.net`, `java.nio`, `java.nio.channels`, `sun.net.www.protocol.http`, `sun.net.www.protocol.https`, `sun.nio.ch`: allow the Ares advice to reflectively read fields of intercepted file, network and channel objects (for example `Socket.delegate`)
  - `--add-opens java.base/java.util`, `java.util.concurrent`, `java.util.concurrent.atomic`, `java.util.concurrent.locks`: allow the Ares advice to reflectively read fields of intercepted collection and concurrency objects (for example `ThreadPoolExecutor.ctl`)

### How compile-time weaving works

1. **After compilation:** the `io.freefair.aspectj.post-compile-weaving` plugin runs `ajc` **after** `javac` finishes compiling your source files to bytecode.
2. **Aspect discovery:** `ajc` only weaves binary aspects from JARs placed on its **aspect path**; it does not pick them up from the ordinary compile classpath. The `aspect` dependency from [Add Ares dependencies](#add-ares-dependencies) puts the Ares JAR there.
3. **Weaving:** `ajc` reads the compiled `.class` files in the project's output directory and weaves in any code whose execution matches the pointcut expressions defined in the discovered aspects.
4. **Runtime references:** the woven bytecode references AspectJ runtime classes, supplied by `aspectjrt` on the bootstrap classpath (configured in the agent step above).

**Without the plugin, no weaving occurs** and the `-Xbootclasspath/a:` flag has no effect.

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

### Gradle

Apply the shipped `GradleReservedPackages.gradle`, which registers `verifyAresReservedPackagesV2` over `sourceSets.main.output.classesDirs` and then attaches it in **two** places:

```gradle
tasks.named('check') { dependsOn tasks.named('verifyAresReservedPackagesV2') }
tasks.withType(Test).configureEach { dependsOn tasks.named('verifyAresReservedPackagesV2') }
```

Both are required, and the second is the one that is easy to get wrong. Gradle's Java plugin defines `check.dependsOn test`, **not** the reverse. A snippet that hangs the validation off `check` alone is therefore never executed by `gradlew test`, which is the command this manual gives you and the command a grading run invokes. That was the defect in boundary version 1: an exercise carrying it accepted student classes in reserved packages. If your exercise still contains a `verifyAresReservedPackagesV1` task, migrate it.

In a multi-project build, apply the snippet to **every** project that compiles student code: `tasks.withType(Test)` covers only the project it is applied to.

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
2. **The policy must permit one file in the domain, not zero.** This is the part that is easy to get wrong. Ares adds a static deny-all rule only while a domain has **no** allowance ([Enforcement Model](/contributor/subsystems/policy/enforcement-model)). Under a fully restrictive file policy, ArchUnit or T. J. Watson Libraries for Analysis (WALA) rejects the operation before any runtime mechanism is consulted, so the negative control passes even with `-javaagent` removed and the weaving switched off, and it proves nothing. Granting exactly one permitted file makes the runtime layer authoritative for that domain, and only then does the negative control actually exercise the agent or the woven aspects.

A correct run is therefore **green**, and contains an asserted rejection. It is not a failed build.

### What a green run does and does not prove

A minimal test with no `@Policy` annotation confirms only that the Ares classes are on the test classpath and that the JVM started with the configured arguments. It does **not** prove that the agent instrumented anything, and it is not necessarily enforcement-free either: see [Further Options](../further-options.md) for what does and does not happen without a policy.

Prove enforcement with the paired controls from the two controls above, then break the setup deliberately and confirm each break is detected:

- Remove `-javaagent` from an exercise whose configuration ends in `_INSTRUMENTATION`. The negative control must now fail to reject.
- Remove the `aspect` dependency (Gradle) or `<aspectLibraries>` (Maven) from an exercise whose configuration ends in `_ASPECTJ`. Same expectation.
- Add a class in `de.tum.cit.ase.ares.api` to the student sources. The build must fail with the reserved-package diagnostic.

Each break must be made in the mode that depends **only** on the removed component. Removing `-javaagent` from an AspectJ exercise changes nothing, because the aspects were woven at compile time and still enforce the policy.

> **What happens without the agent?** Ares's **static analysis** (ArchUnit/WALA) still works, and **AspectJ enforcement** still works, because those aspects are woven at compile time. Only the **ByteBuddy instrumentation** path is inactive, since it relies on the agent to transform classes at load time. If you use an `INSTRUMENTATION` configuration, students could then bypass runtime restrictions, so always ensure the agent is loaded.

## Appendix: complete `build.gradle`

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
    aresVersion = '2.1.2'
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
