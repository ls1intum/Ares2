---
title: "How to transform an Ares 1 protected project into an Ares 2 protected project"
sidebar_position: 1
description: "The migration work that is the same whichever mode you choose: why, what changes, imports and the annotation-to-policy translation."
---

:::tip[Simple Story]
You have an exercise built to the old rules and you want it on the new ones.

This page is everything that has to happen either way. The build-side work depends on which
mode you pick, and lives on the four pages behind it.
:::

> **Audience:** IT-Education experts maintaining an existing Ares 1 exercise.
> **Scope:** The whole exercise: build files, test sources, security configuration.
> **From:** Ares 1 (`de.tum.in.ase:artemis-java-test-sandbox:1.15.0`)
> **To:** Ares 2 (`de.tum.cit.ase:ares:2.2.1`)

> **Version snapshot:** the configuration on these pages is correct for Ares 2.2.1. Later
> releases may change it; check [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md)
> before copying it into a new exercise.

**Related documentation:**
- [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md), the
  canonical setup guide. Where these pages duplicate it, it is the canonical source: if the two
  disagree, follow it and report the discrepancy.
- [Security Policy Manual](/contributor/subsystems/policy/security-policy-manual), the reference
  for the policy file
- [Enforcement Model](/contributor/subsystems/policy/enforcement-model), which defines what static
  analysis and the runtime layer are each responsible for

## How to read this section

The migration splits into work that is the same in both modes, Postcompile and Precompile, and
work that is not:

1. Read this page: why to migrate, what changes, the prerequisites, the import rewrite and the
   translation from Ares 1 annotations into an Ares 2 policy file. If your exercise uses jqwik, the
   move to JUnit Jupiter is part of it.
2. Choose a mode on [Postcompile or Precompile](./postcompile-or-precompile.md).
3. Follow the leaf for your mode and build tool. It carries the dependency and build wiring, the
   activation, the class-shadowing guard and the verification procedure.
4. Come back here for the behaviour differences and the glossary.

## Why migrate

Ares 1 enforces its restrictions with a `SecurityManager`. `ArtemisSecurityManager` installs itself by calling `System.setSecurityManager(...)` at runtime, and every permission decision is made by inspecting the call stack for non-whitelisted frames.

That mechanism has been withdrawn from the platform. JEP 411 deprecated the Security Manager for removal in Java 17. From Java 18, installing one at runtime is disallowed by default, so `System.setSecurityManager` throws unless the Java Virtual Machine (JVM) was started with `-Djava.security.manager=allow`. JEP 486, in Java 24, disabled it permanently: the call now always throws, and no flag re-enables it. An Ares 1 exercise therefore cannot be run on a current Java Development Kit (JDK), and the workarounds available on Java 18 to 23 expire.

Ares 2 does not use a `SecurityManager` at all. It combines two layers:

- **Static analysis** of the compiled student bytecode, using either ArchUnit (rule-based) or T. J. Watson Libraries for Analysis (WALA) (call-graph based), which rejects forbidden operations before anything runs.
- **A runtime layer**, using either AspectJ aspects woven into the bytecode at compile time, or a ByteBuddy `-javaagent` that transforms classes at load time, which intercepts operations as they happen.

Neither depends on a platform feature that is going away. The cost is that the build has more moving parts, which is what the step named in section 4 of this guide is about.

## What changes, at a glance

| | Ares 1 | Ares 2 |
|---|---|---|
| Coordinate | `de.tum.in.ase:artemis-java-test-sandbox` | `de.tum.cit.ase:ares` |
| Root package | `de.tum.in.test.api` | `de.tum.cit.ase.ares.api` |
| Enforcement | `SecurityManager`, stack-frame inspection | Static analysis (ArchUnit or WALA) plus a runtime layer (AspectJ or a ByteBuddy agent) |
| Configuration | Per-test annotations (`@WhitelistPath`, `@AllowThreads`, …) | A `SecurityPolicy.yaml` file referenced by `@Policy` |
| Combining configuration | The whitelist and blacklist annotations are repeatable and additive across class and method; others, such as `@AllowLocalPort` and `@AllowThreads`, resolve nearest-first | The **nearest** `@Policy` wins; policies are never merged |
| Default without configuration | Denies file paths; most package access permitted | Denies file, network, command and thread access; package imports restricted to an implicit allowlist |
| Build requirements | A dependency | A dependency, AspectJ weaving, an agent attachment, JVM module-access flags |
| Class-shadowing guard | `maven-enforcer-plugin`, or a Gradle `doFirst` assertion, with the Ares 1 prefix list | The shipped reserved-package boundary, version 2, with the Ares 2 prefix list |
| Test-type annotations | `@Public`, `@Hidden`, `@Deadline`, `@StrictTimeout`, … | The same, with new imports |
| Property-based tests | jqwik `@Property` or `@Example` with `@Public` or `@Hidden` | Not supported; rewrite them as JUnit Jupiter tests, see [Move jqwik tests to JUnit Jupiter](#move-jqwik-tests-to-junit-jupiter) |

The essential shape of the migration: **the test-type and lifecycle annotations survive a rename; the security annotations do not survive at all and must be re-expressed as a policy file.**

## Prerequisites

- **Java 17** or later
- **Gradle** in a version compatible with the chosen freefair AspectJ plugin (the freefair 9.x line used here requires Gradle 9; older Gradle versions need an older freefair line), or **Maven 3.8+**
- **JUnit 5** (Jupiter)

## Know your project layout

Ares 2 exercises usually follow one of two layouts, and every later step that names a source path or
the policy file gives it in both forms. Find yours here first, then read the matching row wherever a
path appears.

- **Standard Maven or Gradle layout.** Student code under `src/main/java`, test code under
  `src/test/java`, test resources under `src/test/resources`. The build tool finds all three on its
  own. The [`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples) projects use this
  layout.
- **Standard Artemis layout.** The tests repository keeps its test code and the structure oracle
  `test.json` under `test/`, and Artemis merges the student or solution repository in under
  `assignment/` when it grades, so student code compiles from `assignment/src`. The build tool does
  not look in either place by default, so the build file has to redirect it. The build-side page for
  your tool shows the exact block.

Ares reads the source roots from your build file, not from a fixed convention. Other layouts are
therefore possible, but these two are the ones exercises use, and the build-side page gives a block
known to work for each. The Artemis block is the easy one to get subtly wrong, so copy it verbatim.

## Rewrite the imports

The test-type and lifecycle annotations survive the migration. Rewrite the package and nothing else:

| Ares 1 | Ares 2 |
|---|---|
| `de.tum.in.test.api.jupiter.Public` | `de.tum.cit.ase.ares.api.jupiter.Public` |
| `de.tum.in.test.api.jupiter.Hidden` | `de.tum.cit.ase.ares.api.jupiter.Hidden` |
| `de.tum.in.test.api.jupiter.PublicTest` | `de.tum.cit.ase.ares.api.jupiter.PublicTest` |
| `de.tum.in.test.api.jupiter.HiddenTest` | `de.tum.cit.ase.ares.api.jupiter.HiddenTest` |
| `de.tum.in.test.api.Deadline` | `de.tum.cit.ase.ares.api.Deadline` |
| `de.tum.in.test.api.ExtendedDeadline` | `de.tum.cit.ase.ares.api.ExtendedDeadline` |
| `de.tum.in.test.api.ActivateHiddenBefore` | `de.tum.cit.ase.ares.api.ActivateHiddenBefore` |
| `de.tum.in.test.api.StrictTimeout` | `de.tum.cit.ase.ares.api.StrictTimeout` |
| `de.tum.in.test.api.MirrorOutput` | `de.tum.cit.ase.ares.api.MirrorOutput` |
| `de.tum.in.test.api.WithIOManager` | `de.tum.cit.ase.ares.api.WithIOManager` |
| `de.tum.in.test.api.PrivilegedExceptionsOnly` | `de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly` |
| `de.tum.in.test.api.TestUtils` | `de.tum.cit.ase.ares.api.TestUtils` |
| `de.tum.in.test.api.AresConfiguration` | `de.tum.cit.ase.ares.api.AresConfiguration` |
| `de.tum.in.test.api.localization.UseLocale` | `de.tum.cit.ase.ares.api.localization.UseLocale` |
| `de.tum.in.test.api.io.IOTester` | `de.tum.cit.ase.ares.api.io.IOTester` |
| `de.tum.in.test.api.structural.AttributeTestProvider` | `de.tum.cit.ase.ares.api.structural.AttributeTestProvider` |
| `de.tum.in.test.api.structural.ConstructorTestProvider` | `de.tum.cit.ase.ares.api.structural.ConstructorTestProvider` |
| `de.tum.in.test.api.structural.MethodTestProvider` | `de.tum.cit.ase.ares.api.structural.MethodTestProvider` |
| `de.tum.in.test.api.structural.ClassTestProvider` | `de.tum.cit.ase.ares.api.structural.ClassTestProvider` |
| `de.tum.in.test.api.util.ReflectionTestUtils` | `de.tum.cit.ase.ares.api.util.ReflectionTestUtils` |

The last five rows are the structure-oracle providers and the reflection helper that Artemis
structural tests use. They are a package rename like the rest, but the table names them so you can
confirm the rename against the Ares 2 JAR rather than trusting that the blanket replace found the
right target.

A blanket search and replace of `de.tum.in.test.api` with `de.tum.cit.ase.ares.api` handles all of these. It produces unresolved imports for every **security** annotation, which is the correct outcome: those have no Ares 2 counterpart and are the subject of the step named in section 6 of this guide. Delete them as you translate them, rather than before, so you do not lose the configuration they encoded.

> **Keep `@StrictTimeout`.** It is the effective timeout mechanism in Ares 2, exactly as in Ares 1. Do **not** rewrite it as a policy entry; see the step named in section 6.2 of this guide.

## Move jqwik tests to JUnit Jupiter

Ares 2.2.0 no longer supports jqwik. It has no `de.tum.cit.ase.ares.api.jqwik` package,
and version 2.1.5 and earlier still have it. A test class that imports `Public` or `Hidden`
from that package no longer compiles. Rewrite those
tests for JUnit Jupiter and use the `jupiter` annotations of Ares.

:::danger[Remove jqwik from your build]
Delete the `net.jqwik:jqwik` dependency from your build file. If you only change the import
and leave `@Property` or `@Example` in place, the test still compiles and jqwik still runs
it, but Ares's `@Public` and `@Hidden` do nothing there. The test then runs with no policy and
no deadline guard, and a hidden test is not held back. Without the dependency, a leftover
`@Property` or `@Example` stops compiling instead.
:::

Pick the Jupiter form by where the inputs come from:

| Your jqwik test | Jupiter replacement |
|---|---|
| `@Example` | `@PublicTest` or `@HiddenTest`, which already contain `@Test` |
| A property whose inputs you can list | `@Public` or `@Hidden` with `@ParameterizedTest` and `@MethodSource` (or `@ValueSource` for plain values), one entry per edge case |
| `@Property` with generated inputs | `@Public` or `@Hidden` with `@RepeatedTest`, drawing the inputs from a `java.util.Random` created with a fixed seed |

Details that matter for Ares:

- `@PublicTest` and `@HiddenTest` already contain JUnit's `@Test`. Next to `@ParameterizedTest`
  or `@RepeatedTest` that adds a second, plain test that fails, because it has parameters
  nothing supplies. Next to `@TestFactory` JUnit ignores the extra test with a warning. In all
  three cases use `@Public` or `@Hidden` beside the Jupiter annotation.
- `@Hidden` still needs a `@Deadline`, on the class or on the method, and `@Public` must not
  have one.
- A fixed seed gives every submission the same inputs, so the grading is fair and a failure
  repeats on every run. Put the generated input into the assertion message, so a failure
  shows what was tried.
- With `@TestFactory`, call the student's code only inside the executable you pass to
  `dynamicTest(...)`. Code that runs while the stream is built is not supervised. Console
  capture covers all dynamic tests of the factory together.
- jqwik applied the checks once per property. Jupiter applies them to every invocation, so
  the thread budget and the console capture start again for each repetition and each entry.
- A repetition or a parameter entry may appear as its own test in the report. Check how your
  platform lists them before you move a hidden test with many repetitions.

Before, with jqwik (Ares 2.1.5 package shown; Ares 1 used `de.tum.in.test.api.jqwik`):

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.tum.cit.ase.ares.api.jqwik.Public;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

class PenguinTest {

    @Public
    @Property
    void nameLengthIsKept(@ForAll String name) {
        assertEquals(name.length(), new Penguin(name).getName().length());
    }
}
```

After, with JUnit Jupiter:

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.tum.cit.ase.ares.api.jupiter.Public;
import java.util.Random;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PenguinTest {

    @Public
    @ParameterizedTest
    @ValueSource(strings = { "", "Julian", "a name with spaces" })
    void nameLengthIsKeptForEdgeCases(String name) {
        assertEquals(name.length(), new Penguin(name).getName().length());
    }

    @Public
    @RepeatedTest(100)
    void nameLengthIsKeptForGeneratedNames(RepetitionInfo info) {
        long seed = 42L + info.getCurrentRepetition();
        Random random = new Random(seed);
        String name = "x".repeat(random.nextInt(50));
        assertEquals(name.length(), new Penguin(name).getName().length(),
                "failed for name \"" + name + "\" (seed " + seed + ")");
    }
}
```

The `@ParameterizedTest` annotations come from the `junit-jupiter-params` artefact. In
Postcompile, Ares already brings it. In Precompile, add it to the test dependencies at the
same version as your JUnit Jupiter engine.

**What you lose.** jqwik shrinks a failing input to the simplest one that still fails, and
Jupiter does not. With a seeded `Random` you get a failing input that repeats on every run, but
it is the input as generated, not the smallest one. Keep the edge cases you care about in the
`@ValueSource` or `@MethodSource` list, since generated inputs will no longer find them for
you by shrinking.

## Translate the security annotations into a policy file

This is the substantive part of the migration. Ares 1 encoded its security configuration in annotations spread over test classes and methods. Ares 2 encodes it in one YAML file per scope.

### The policy file structure

Create the policy file where your layout puts test resources:

- **Standard Maven or Gradle layout:** `src/test/resources/SecurityPolicy.yaml`
- **Standard Artemis layout:** `test/SecurityPolicy.yaml`, or mirror the supervised package as
  `test/<package/path>/SecurityPolicy.yaml`, for example `test/org/example/SecurityPolicy.yaml`

The `@Policy(value = ...)` on your tests must name the same path; the build-side page shows the
`value` for each layout. The content is identical either way:

```yaml
thisPolicyFileCompliesToThePolicyVersion: 1
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
  theSupervisedCodeUsesTheFollowingPackage: "org.example"
  theMainClassInsideThisPackageIs: "Main"
  theFollowingClassesAreTestClasses:
    - "org.example.PenguinTest"
  theFollowingResourceAccessesArePermitted:
    regardingFileSystemInteractions: [ ]
    regardingNetworkConnections: [ ]
    regardingCommandExecutions: [ ]
    regardingThreadCreations: [ ]
    regardingPackageImports: [ ]
    regardingTimeouts: [ ]
```

Three structural rules that cause immediate rejection when broken:

1. `thisPolicyFileCompliesToThePolicyVersion` is **required** and must be exactly `1`. It is a
   schema-version guard, not an arbitrary constant. It pins the file to the policy format this Ares
   release understands. A later release that changes the format can then reject an old file instead
   of misreading it, and today `1` is the only accepted value.
2. **All six** lists under `theFollowingResourceAccessesArePermitted` must be present, even when empty. `regardingTimeouts: [ ]` is required too, for the reason in the step named in section 6.2 of this guide.
3. `theFollowingClassesAreTestClasses` must be present, though it may be empty.

Pick `theFollowingProgrammingLanguageConfigurationIsUsed` from these eight values:

| Value | Build tool | Static analysis | Runtime enforcement |
|---|---|---|---|
| `JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ` | Maven | ArchUnit | AspectJ |
| `JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION` | Maven | ArchUnit | ByteBuddy agent |
| `JAVA_USING_MAVEN_WALA_AND_ASPECTJ` | Maven | WALA | AspectJ |
| `JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION` | Maven | WALA | ByteBuddy agent |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ` | Gradle | ArchUnit | AspectJ |
| `JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION` | Gradle | ArchUnit | ByteBuddy agent |
| `JAVA_USING_GRADLE_WALA_AND_ASPECTJ` | Gradle | WALA | AspectJ |
| `JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION` | Gradle | WALA | ByteBuddy agent |

ArchUnit is simpler and faster; WALA detects transitive violations through call chains. Configure both runtime mechanisms in the build regardless of which you name here, so that switching later is a policy edit rather than a build change.

The permission entries have these shapes:

```yaml
regardingFileSystemInteractions:
  - onThisPathAndAllPathsBelow: "data"
    readAllFiles: true
    overwriteAllFiles: false
    createAllFiles: false
    executeAllFiles: false
    deleteAllFiles: false

regardingNetworkConnections:
  - onTheHost: "localhost"
    onThePort: 8080
    openConnections: true
    sendData: true
    receiveData: true

regardingCommandExecutions:
  - executeTheCommand: "echo"
    withTheseArguments:
      - "hello"

regardingThreadCreations:
  - createTheFollowingNumberOfThreads: 5
    ofThisClass: "java.lang.Thread"

regardingPackageImports:
  - importTheFollowingPackage: "java.util"

regardingTimeouts:
  - timeout: 10000
```

### The mapping table

Ares 1 and Ares 2 do not express the same things, so this is not a substitution table. The **Fidelity** column tells you how much thought each row needs.

| Ares 1 | Fidelity | Ares 2 |
|---|---|---|
| `@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest` | Faithful | Same annotations, new import |
| `@Deadline`, `@ExtendedDeadline`, `@ActivateHiddenBefore` | Faithful | Same annotations, new import |
| `@MirrorOutput`, `@WithIOManager`, `@PrivilegedExceptionsOnly`, `@UseLocale` | Faithful | Same annotations, new import |
| `@StrictTimeout` | Faithful | Same annotation, new import. **Do not** convert to `regardingTimeouts` |
| `@WhitelistPath` | Approximate | A `regardingFileSystemInteractions` entry with `onThisPathAndAllPathsBelow` |
| `PathActionLevel` | Approximate | The five independent booleans; see the conversion below |
| `@BlacklistPath` | **None** | No deny rule exists; narrow the allowance instead |
| `@AllowLocalPort` | Approximate | A `regardingNetworkConnections` entry on `localhost` |
| `@AllowThreads` | Approximate | A `regardingThreadCreations` entry |
| `@WhitelistPackage` | Approximate | A `regardingPackageImports` entry |
| `@BlacklistPackage` | **None** | No negative package rule exists |
| `@WhitelistClass` | Conditional | `theFollowingClassesAreTestClasses`, but only under the condition below |
| `@AddTrustedPackage` | **None** | Nothing. See the warning below |
| `@TrustedThreads`, `@DisableThreadGroupCheckFor` | **None** | Nothing |
| `ArtemisSecurityManager.requestThreadWhitelisting(Thread)` | **None** | Nothing |
| `ares.security.trustedpackages`, `ares.maven.ignore`, `ares.gradle.ignore`, `ares.maven.pom`, `ares.gradle.build` | **None** | Nothing; delete these system properties |

Notes on the rows that need them:

**`@StrictTimeout` and `regardingTimeouts`.** Keep the annotation. `regardingTimeouts` is parsed and validated into the policy model, but timeouts belong to the **Phobos** test-case family, which Ares 2.2.1 generates without yet dispatching it from the in-process execution path. That stage of the pipeline has not been migrated across, so a timeout expressed in the policy does not bound a test today. The list must still be present in the file, because the schema requires all six; `regardingTimeouts: [ ]` is the clearest form unless you want to record an intended value for a later release. Use `@StrictTimeout` wherever a test needs a deadline.

**`@WhitelistPath` and path types.** Only the `STARTS_WITH` path type maps naturally onto `onThisPathAndAllPathsBelow`, which is prefix-shaped by construction. `PathType.GLOB` and regular-expression variants have no counterpart. A glob such as `@WhitelistPath(value = "../course1920xyz**", type = PathType.GLOB)` must be re-expressed as one or more concrete path prefixes, and the result is usually narrower than the original, which is the safe direction.

**`PathActionLevel`.** Ares 1 levels are ordinal and cumulative, `READ < READLINK < WRITE < DELETE < EXECUTE`. For a whitelist rule, an operation is permitted when the requested level is at or below the configured level. Ares 2 instead has five independent booleans. Convert as follows, then verify against the exercise's actual behaviour:

| Ares 1 level | Ares 2 booleans to set `true` |
|---|---|
| `READ` | `readAllFiles` |
| `READLINK` | No exact equivalent; treat as `readAllFiles` and check symbolic-link behaviour explicitly |
| `WRITE` | `readAllFiles`, `overwriteAllFiles`, `createAllFiles` |
| `DELETE` | the `WRITE` set plus `deleteAllFiles` |
| `EXECUTE` | all five |

**`@BlacklistPath`.** Ares 2 is allowlist-only. A blacklist that carved an exception out of a broader whitelist has no direct form. Sometimes you can reproduce the intent by granting several narrower paths instead of the parent, but "allow this directory except one file inside it" is not representable. The common Ares 1 idiom `@WhitelistPath("target")` with `@BlacklistPath("target/test-classes")` becomes: grant only the specific subdirectories the exercise legitimately needs.

**`@AllowLocalPort`.** A fixed port becomes one `regardingNetworkConnections` entry on `localhost`. Ares 1's range-with-exclusions form has no counterpart; enumerate the ports you need. One special case is worth knowing: in Ares 2, `onThePort: 0` is a **wildcard** matching every port, not port zero. That makes an unrestricted Ares 1 `allowPortsAbove = 0` representable, and it makes writing `0` for anything else dangerously broad.

**`@AllowThreads`.** Ares 1 capped the number of *concurrently active* threads. Ares 2 counts threads per thread class through `createTheFollowingNumberOfThreads` and `ofThisClass`. The accounting differs, so a translated limit is an approximation, not a rename. Re-derive the number the exercise needs rather than copying `maxActiveCount`.

**`@BlacklistPackage`.** No negative package rule exists. Note that the `java` prefix is always permitted as an essential package, so an Ares 1 blacklist that forbade a specific `java.*` package cannot be reproduced at all.

**`@WhitelistClass`.** Map it to `theFollowingClassesAreTestClasses` **only** when the class is instructor-owned test infrastructure that students cannot modify. An entry in that list is exempt from both the static and the runtime checks, so it is considerably stronger than an Ares 1 whitelist entry. The Ares 1 warning applies with more force here: never list a class that students can edit.

> **Do not map `@AddTrustedPackage`.** It is tempting to put its package name into `theFollowingClassesAreTestClasses`, and that is wrong in both directions at once. The step named in section 6.4 of this guide explains that the field matches exact class names, so a package name grants **no** class exemption. But it is not inert either: Ares derives a package permission from every entry by stripping the last dotted component, so an entry `"com.thirdparty.tool"` silently permits imports from `com.thirdparty`, and a two-part entry such as `"org.example"` permits the whole `org` prefix. You therefore get no exemption, plus a package allowance you did not intend. If a third-party library genuinely needs to perform a restricted operation on behalf of student code, express that as the specific resource permission it needs.

### Consolidating additive annotations

Ares 1's whitelist annotations were `@Repeatable` and **additive**: annotations on the test class and on the test method combined, and multiple annotations of the same kind accumulated. Ares 2 does not merge. Resolution runs from the method outwards through the enclosing classes, and the **nearest** `@Policy` wins outright.

So this Ares 1 arrangement:

```java
@WhitelistPath("target")
@WhitelistPath(value = "data", level = PathActionLevel.DELETE)
public class ExerciseTest {

    @WhitelistPath("config")
    @Public
    @Test
    void oneParticularTest() { }
}
```

does not become three policy fragments. Work out the *effective* permission set for each distinct scope, then write one complete policy file per scope. In practice most exercises need a single file for the whole test class. Where one test genuinely needs more than the others, write a second complete file, containing the class-level permissions **and** the extra one, and point the method-level `@Policy` at it.

### Naming your test classes correctly

`theFollowingClassesAreTestClasses` takes **exact fully qualified class names**. Nested classes are recognised, but only on the `$` boundary, so listing `org.example.ExerciseTest` covers `org.example.ExerciseTest$Inner`.

Package names and package prefixes do not exempt anything. `"org.example"` does not trust the classes in `org.example`; it matches a class literally named `org.example`, which does not exist. List every test class explicitly:

```yaml
theFollowingClassesAreTestClasses:
  - "org.example.PenguinTest"
  - "org.example.AdvancedPenguinTest"
  - "org.example.util.PenguinTestHelper"
```

This is the same rule at both enforcement layers, so a mistake here fails consistently rather than intermittently: your test class is treated as supervised code, and your own assertions start tripping the policy.

## Behaviour differences you will notice

**Enforcement is no longer stack-frame based.** Ares 1's rule was that a permission required *every* frame on the stack to be whitelisted, which is why whitelisting one class could unblock an entire call chain, and why `@WhitelistClass` was so powerful. Ares 2 decides from the policy and the supervised scope. Reasoning of the form "this works because I trusted the caller" does not carry over.

**The class loader no longer needs paths whitelisted.** In Ares 1, loading one student class from another could itself be denied, producing `BAD PATH ACCESS` warnings and the standing advice that "all test setups should have some whitelisting". That whole class of problem disappears: Ares 2 does not mediate class loading through the path policy.

**Violation messages are localised.** Ares 2 renders violations in the JVM's locale, so "blocked by Ares" appears as "von Ares blockiert" on a German JVM. Assert on locale-stable content, such as the file name or the offending method, never on the prose:

```java
SecurityException violation = assertThrows(SecurityException.class,
        () -> new Penguin("Julian").readForbiddenFile());
assertTrue(violation.getMessage().contains("secret.txt"),
        "expected an Ares file-system violation naming secret.txt, was: " + violation.getMessage());
```

**Some restrictions are not policy-controlled.** Ares 2 installs fixed restrictions covering reflection, native access, JVM termination, class loading, JNDI and related domains. A policy governs the five resource domains; it cannot grant these.

**`@Deadline` and the hidden-test annotations remain available** with updated imports, so the public/hidden workflow you have carries across unchanged in shape. The security policy can now set the deadline, its extension and the date before which hidden tests always run for the whole exercise, under [`regardingHiddenTests`](/contributor/subsystems/policy/security-policy-manual#77-hidden-tests); the annotations still win on a class or method.

## Glossary

| Term | Meaning |
|---|---|
| **Ares 1** | `de.tum.in.ase:artemis-java-test-sandbox`, the Artemis Java Test Sandbox. Enforces via a `SecurityManager` and per-test annotations. |
| **Ares 2** | `de.tum.cit.ase:ares`. Enforces via static analysis plus a bytecode-level runtime layer, configured by a policy file. |
| **Security policy** | The `SecurityPolicy.yaml` file naming the configuration, the supervised scope, the trusted test classes and the permitted resource accesses. |
| **Supervised code** | The student code subject to the policy, identified by `theSupervisedCodeUsesTheFollowingPackage`. |
| **AspectJ** | The compile-time aspect-oriented programming (AOP) framework used for one of the two runtime enforcement mechanisms. Requires the compiler plugin during the build and `aspectjrt` on the bootstrap classpath. |
| **Aspect path** | The set of JARs `ajc` reads binary aspects from. Distinct from the compile classpath: a JAR on the classpath alone contributes no aspects. |
| **Instrumentation** | The other runtime mechanism: class bytecode modified at load time by a ByteBuddy `-javaagent`. |
| **Reserved package** | A package prefix student code may not declare, because Ares trusts that identity by name. Enforced by the build, see the step named in section 8 of this guide. |
| **`withinPath`** | The path to compiled student bytecode, relative to the build output directory. Differs between Gradle and Maven. |
| **Phobos** | A test-case family covering the file-system, network and timeout domains. Ares 2.2.1 generates Phobos cases but does not yet dispatch them from the in-process execution path, so a policy timeout does not bound a test today. Use `@StrictTimeout` for a deadline. |
| **`@StrictTimeout`** | The annotation that bounds test execution. Applied to a test class or method, and unchanged from Ares 1 apart from its package. |
| **Positive / negative control** | The paired checks of the step named in section 10 of this guide: one permitted operation that must succeed, one forbidden operation that must be rejected. Neither alone demonstrates that enforcement works. |
