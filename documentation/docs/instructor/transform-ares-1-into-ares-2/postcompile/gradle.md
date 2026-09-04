---
title: "Gradle"
sidebar_position: 1
description: "Migrating an Ares 1 Gradle exercise onto Ares 2 in Postcompile mode: dependency, wiring, guard and verification."
---

:::tip[Simple Story]
This is the build-side half of the migration for a Gradle exercise.

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

Add the AspectJ compiler plugin:

```gradle
plugins {
    id 'java'
    id 'io.freefair.aspectj.post-compile-weaving' version '9.2.0'
}
```

Declare the versions once, so an upgrade is a single edit:

```gradle
ext {
    aresVersion = '2.1.4'
    aspectjVersion = '1.9.25.1'
}

repositories {
    mavenCentral()
}
```

Create two dedicated, **non-transitive** configurations. Non-transitive is what makes them safe to use by file path: each resolves to exactly one JAR, so no file-name matching is involved and the wrong JAR cannot be picked up.

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

Declare the dependencies:

```gradle
dependencies {
    aresAgent "de.tum.cit.ase:ares:${aresVersion}:agent"
    aresAspectjRuntime "org.aspectj:aspectjrt:${aspectjVersion}"
    testImplementation "de.tum.cit.ase:ares:${aresVersion}"
    aspect "de.tum.cit.ase:ares:${aresVersion}"
    implementation "org.aspectj:aspectjrt:${aspectjVersion}"
}
```

The `aspect` line is the one with no Ares 1 counterpart and the one most often forgotten. The AspectJ compiler only weaves binary aspects from JARs on its **aspect path**; a JAR on the ordinary compile classpath contributes nothing. Without that line the build succeeds and weaves nothing, silently.

Attach the agent through an argument provider rather than `jvmArgs`, so that dependency resolution happens when the test task runs rather than while Gradle configures the build:

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

The seventeen module-access flags are not optional and not a subset you can trim. They let the Ares advice introspect intercepted Java Development Kit (JDK) objects; a partial list fails only once a policy exercises the corresponding advice, which means it fails later and more confusingly than it should.

> **Note:** the freefair plugin manages an `aspectjrt` version of its own. On an AspectJ version conflict, either drop the explicit `implementation "org.aspectj:aspectjrt:..."` line and let the plugin supply it, or align the plugin through `aspectj { version = aspectjVersion }`.

## Point the build at your sources

Which directories Gradle compiles depends on your layout (see
[Know your project layout](../index.md#know-your-project-layout)).

- **Standard Gradle layout.** `src/main/java`, `src/test/java` and `src/test/resources` are the
  defaults. Add nothing.
- **Standard Artemis layout.** Student code compiles from `assignment/src`, and the test code and
  the structure oracle `test.json` live under `test/`. Redirect both source sets, and declare
  `test/` as test **resources** as well:

  ```gradle
  def assignmentSrcDir = 'assignment/src'

  sourceSets {
      main {
          java {
              srcDirs = [assignmentSrcDir]
          }
      }
      test {
          java {
              srcDir 'test'
          }
          resources {
              srcDir 'test'
          }
      }
  }
  ```

Two lines here are load-bearing. The `resources { srcDir 'test' }` block is separate from the
`java` one on purpose, because Gradle copies compiled classes and copied resources through different
declarations. Without it, `test.json` never reaches the test classpath, and every structural test
fails reporting a missing structure oracle. The `def assignmentSrcDir = 'assignment/src'` line
matters beyond Gradle. Ares reads the student source root by parsing this build file, and it
resolves exactly this variable form. An Artemis-layout exercise that points `srcDirs` somewhere Ares
cannot parse makes its structural tests report every expected class as not implemented, so use the
block above as written.

## Apply `@Policy` to the tests

Ares 1 needed no annotation to activate security; the test-type annotation was enough, and the security annotations configured it. Ares 2 keeps the test-type annotation and adds `@Policy`:

```java
package org.example;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

@Policy(value = "src/test/resources/SecurityPolicy.yaml", withinPath = "classes/java/main/org/example")
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
the build output directory, not at your sources. Gradle writes the main classes to
`build/classes/java/main` whether they came from `src/main/java` or `assignment/src`, so the Gradle
value stays `classes/java/main/<package/path>` either way.

The `value` path, in contrast, does follow your layout, matching where you created the policy file:
`src/test/resources/SecurityPolicy.yaml` for the standard layout, `test/SecurityPolicy.yaml` (or the
package-mirrored `test/org/example/SecurityPolicy.yaml`) for the Artemis layout.

> **The activation rule that has no Ares 1 counterpart.** `@Policy` is not itself a JUnit extension and registers nothing. What activates Ares is the test-type annotation (`@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest`). A test annotated with a plain JUnit `@Test` and a `@Policy` runs completely unsupervised, silently. If you migrate a test class and drop its `@Public` in the process, you lose all enforcement without any error.

## Replace the class-shadowing guard

Your Ares 1 exercise almost certainly contains a build-side guard preventing student classes from landing in trusted packages, as the Ares 1 documentation required. It must be replaced, not kept: the prefix list it uses names Ares 1's packages and third-party libraries that are no longer the relevant ones.

Ares 2 trusts a different set of identities by name, including its own `de.tum.cit.ase.ares.api` package, Byte Buddy, AspectJ, T. J. Watson Libraries for Analysis (WALA) and ArchUnit. Ares ships the executable snippets, so copy them rather than editing your old list:

- `GradleReservedPackages.gradle`
- `MavenReservedPackages.xml`
- `ReservedPackagePrefixes.txt` (the machine-readable list)

They ship inside the Ares JAR under `de/tum/cit/ase/ares/api/configuration/reservedPackages/`, and live in the Ares repository at `src/main/resources/de/tum/cit/ase/ares/api/configuration/reservedPackages/`. Both are reproduced in full below, so you can complete the migration without extracting them.

Two versions are pinned, and your exercise and its continuous integration (CI) must pin both. `RESERVED_PACKAGE_PREFIX_VERSION = 1` is the prefix data. `RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION = 2` is the build-side contract that enforces it.

### Gradle

Delete the Ares 1 `forbiddenPackageFolders` list and its `test { doFirst { ... } }` assertion. Create `gradle/AresReservedPackages.gradle` with the shipped content, reproduced here in full:

```gradle
// Ares reserved-package build boundary, version 2. Apply from the exercise build.
//
// Boundary version 2 supersedes version 1, which attached the validation to
// `check` alone. Gradle's Java plugin defines `check.dependsOn test`, not the
// reverse, so `gradlew test` never ran it and a student class under a reserved
// package survived a grading run that only invoked `test`. Every Test task is
// gated as well now. An exercise still carrying a version 1 snippet is
// bypassable and must be migrated.
//
// The build descriptor and the command used to invoke it are trusted instructor
// configuration. This validates student *code*; it is not a defence against
// someone who can edit the build or choose arbitrary Gradle arguments.
//
// In a multi-project build, apply this to every project that compiles student
// code: `tasks.withType(Test)` covers only the project it is applied to.
import org.gradle.api.tasks.testing.Test

def aresReservedPackageBoundaryVersion = '2'
def aresReservedPackagePatterns = [
        'java/**', 'javax/**', 'sun/**', 'jdk/**', 'com/sun/**',
        'de/tum/cit/ase/ares/api/**', 'net/bytebuddy/**', 'org/aspectj/**',
        'com/ibm/wala/**', 'com/tngtech/archunit/**', 'anonymous/toolclasses/**', 'metatest/**'
]

tasks.register('verifyAresReservedPackagesV2') {
    dependsOn tasks.named('classes')
    // Resolved at configuration time and declared as an input, so the task body
    // touches no Project API and the build stays configuration-cache compatible.
    def studentClassesDirs = sourceSets.main.output.classesDirs
    def reservedPrefixes = aresReservedPackagePatterns.collect { it.substring(0, it.length() - 2) }
    def boundaryVersion = aresReservedPackageBoundaryVersion
    inputs.files(studentClassesDirs).withPropertyName('studentClasses')
    doLast {
        def forbidden = []
        studentClassesDirs.files.each { root ->
            if (!root.isDirectory()) {
                return
            }
            def rootPath = root.toPath()
            root.eachFileRecurse { candidate ->
                if (!candidate.isFile()) {
                    return
                }
                // Compared as a '/'-separated relative path, so the same prefixes
                // apply on Windows as on Linux and macOS.
                def relative = rootPath.relativize(candidate.toPath()).toString().replace(File.separator, '/')
                if (reservedPrefixes.any { relative.startsWith(it) }) {
                    forbidden << relative
                }
            }
        }
        if (!forbidden.isEmpty()) {
            throw new GradleException("Ares reserved-package validation ${boundaryVersion} " +
                    "rejected student output: ${forbidden.sort()}. No bypass flag is supported.")
        }
    }
}

// Both hooks are required. `check` covers `gradlew check` and `gradlew build`;
// the Test hook covers `gradlew test` and any custom Test task, which is what a
// grading run actually invokes.
tasks.named('check') { dependsOn tasks.named('verifyAresReservedPackagesV2') }
tasks.withType(Test).configureEach { dependsOn tasks.named('verifyAresReservedPackagesV2') }
```

Then apply it from your `build.gradle`:

```gradle
// Ares reserved-package build boundary, version 2.
apply from: 'gradle/AresReservedPackages.gradle'
```

It registers `verifyAresReservedPackagesV2` over `sourceSets.main.output.classesDirs` and attaches it in **two** places, on the last two lines above:

```gradle
tasks.named('check') { dependsOn tasks.named('verifyAresReservedPackagesV2') }
tasks.withType(Test).configureEach { dependsOn tasks.named('verifyAresReservedPackagesV2') }
```

Both are required. Gradle's Java plugin defines `check.dependsOn test`, **not** the reverse, so a validation hung off `check` alone is never executed by `gradlew test`, which is what a grading run invokes. That was the defect in boundary version 1.

Your Ares 1 guard, being a `doFirst` on the `test` task, did run under `gradlew test`. So this is not a regression you are introducing by migrating; the problem is only that its prefix list is now the wrong one.

In a multi-project build, apply the snippet to **every** project that compiles student code: `tasks.withType(Test)` covers only the project it is applied to.

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
- **No default execution timeout yet.** A 10,000 ms limit is constructed, but timeouts belong to the Phobos test-case family, which Ares 2.1.4 generates without yet dispatching in-process. Add `@StrictTimeout` where a test needs a deadline.

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

:::note[`adviceDidNotMatch` warnings are expected]
An `_ASPECTJ` build prints one `[Xlint:adviceDidNotMatch]` warning for each Ares advice that found no
matching join point in the woven code. An exercise whose supervised code performs no file, network,
command or thread operation gives those advices nothing to match, so several such warnings are
expected and the build and tests still pass. Their presence shows the weaver processed the Ares
aspects; on its own it does not prove that the advice meant to match your code did, so keep the
positive and negative controls above.
:::

Two complete, runnable exercises are available in [`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples), one per build tool. If an example passes and your migrated project does not, the difference between the two is your defect.

## Appendix: complete `build.gradle`

A working version of this file, with the sources and policy it refers to, is [`examples/ares-exercise-gradle`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-gradle).

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
    aresVersion = '2.1.4'
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

// Ares reserved-package build boundary, version 2. See Section 8.1.
apply from: 'gradle/AresReservedPackages.gradle'
```
