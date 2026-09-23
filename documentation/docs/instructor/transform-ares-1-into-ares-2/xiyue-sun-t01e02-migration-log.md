---
title: "Migration log: T01E02 - Autumn Leaves (Xiyue Sun)"
sidebar_position: 5
description: "A worked Postcompile Gradle migration of an existing Artemis exercise, with every failure hit along the way and how each was found and fixed."
---

:::tip[Simple Story]
The pages before this one are the map. This page is one trip: the T01E02 "Autumn Leaves"
exercise, migrated from Ares 1 to Ares 2 Postcompile on Gradle, with every wrong turn kept in.

Nothing here is a defect in the exercise. Every failure below was the migration in progress,
not the exercise misbehaving.
:::

**Exercise:** T01E02 - Autumn Leaves, ITP Ares 2 Test Course. Two production classes, `Sky` and
`Nature`, checked by behaviour tests (Jupiter and jqwik) and by a structure oracle (`test.json`)
read through `ConstructorTestProvider`, `AttributeTestProvider` and `MethodTestProvider`.

**Starting point:** an Ares 1 `security-policy.yaml`-free exercise, its enforcement configured
entirely through Ares 1's annotations, migrated onto Postcompile per
[the common page](./index.md) and [Postcompile: Gradle](./postcompile/gradle.md).

**Outcome:** 100% on the SOLUTION participation, the reserved-package guard installed and
proven, and a positive/negative control pair proving the runtime layer is what rejects the
forbidden operation, not static analysis alone.

## Failure 1: the merged assignment code was not where `sourceSets` said

**Manual step referenced:** [Postcompile: Gradle § Replace the dependency and wire up the
build](./postcompile/gradle.md#replace-the-dependency-and-wire-up-the-build).

**Expected:** `sourceSets.main.java.srcDir 'src'`, matching the exercise's Ares 1 layout, would
let `compileJava` find `Sky.java` and `Nature.java`.

**Actual:** the Gradle build failed outright:

```
java.lang.IllegalStateException: Configured source root is not a directory: /var/tmp/testing-dir/src
```

**What was tried:** the TESTS repository's own `.gitignore` was the clue. It ignores an
`assignment/` directory it never creates itself, which only makes sense if something else
creates it. Artemis's continuous integration (CI) checks the TESTS repository out at the
project root and merges the ASSIGNMENT or SOLUTION repository's content underneath it, at
`assignment/`, not flat at the root. That is a fact about the CI pipeline for an
already-existing Artemis exercise, not about a fresh Postcompile project, and it is not stated
on the migration pages because they are written from the fresh-project side.

**What worked:** `sourceSets.main.java.srcDir 'assignment/src'`.

**Final state:** the build compiled. This alone was not enough to pass the exercise; see
Failure 3.

## Failure 2: the structure oracle was not on the test classpath

**Manual step referenced:** the same section, for the `sourceSets` block generally; the
structure oracle itself is documented in
[Security Policy Manual](/contributor/subsystems/policy/security-policy-manual) and the
structural test providers in the package overview.

**Expected:** with `sourceSets.test.java.srcDirs = ['test']` set, `test.json` (checked into the
`test/` directory alongside the test sources) would be found on the test runtime classpath.

**Actual:** every structural test failed at `initializationError`, reporting that the structure
oracle was missing, even though `test.json` was present in the repository and the test sources
compiled.

**What was tried:** confirming with `jar tf` equivalent (browsing the built `test` output
directory through the code editor) that `build/resources/test/` was empty of `test.json`. Gradle
only copies a resource that is declared as one; `sourceSets.test.java.srcDirs` controls compiled
sources, not resources, and the two are independent even when they point at the same directory.

**What worked:** adding a `resources` block alongside `java` in the `test` source set:

```gradle
test {
    java {
        srcDirs = ['test']
    }
    resources {
        srcDirs = ['test']
    }
}
```

**Final state:** `test.json` reached `build/resources/test/`, and the "structure oracle is
present" failures cleared.

## Failure 3: three test classes needed exempting, not fixing

**Manual step referenced:** [the common page § the annotation-to-policy
translation](./index.md), and the `theFollowingClassesAreTestClasses` field described in
[Security Policy Manual](/contributor/subsystems/policy/security-policy-manual).

**Expected:** with `Sky` and `Nature` compiling under `assignment/src` and the oracle on the
classpath, `AttributeTest`, `ConstructorTest` and `MethodTest` (the three classes that drive the
structural providers) would run cleanly; they only read the oracle and reflect over the compiled
classes.

**Actual:**

```
java.lang.SecurityException: ... tried to illegally connect Network <unknown>:-1 via java.net.URL.openStream()
```

thrown from inside `AttributeTest`, `ConstructorTest` and `MethodTest` themselves, not from
`Sky` or `Nature`.

**What was tried:** reading what these three classes do: `ClassNameScanner`, which they
call into, resolves a class's package by walking source files and, on some code paths, resolving
a resource `URL`. That is enforcement infrastructure running inside the same Java Virtual Machine (JVM) as the policy
that governs it, and it was not on the exempt list, so Ares supervised it like any other network
access.

**What worked:** adding all three classes to `theFollowingClassesAreTestClasses` in
`SecurityPolicy.yaml`, alongside the behaviour test classes already there.

**Final state:** the three classes stopped being supervised, and the exception cleared. Score
rose from complete failure to 71.4% (15 of 21 tests), with the remaining six failures unrelated
to this fix (see Failure 4).

## Failure 4: the structural tests still could not find `Sky` and `Nature`

**Manual step referenced:** [Postcompile: Gradle § Point the build at your
sources](./postcompile/gradle.md#point-the-build-at-your-sources), which was written after this
migration and now carries both the `assignmentSrcDir` line and the way this failure presents.

**Expected:** with Failures 1 through 3 fixed, `testAttributes`, `testConstructors` and
`testMethods`, for both `Sky` and `Nature`, would find the classes the same way the fifteen
already-passing tests did.

**Actual:** all six failed identically:

```
The exercise expects a class with the name Sky in the package de.tum.cit.aet. You did not implement the class in the exercise.
```

despite `Sky.java` and `Nature.java` compiling without error and being found by every other test
in the suite through ordinary reflection.

**What was tried:** reading Ares 2's own source rather than guessing at the configuration.
`structural.scan.notFound` in `messages.properties` led to `ClassNameScanner`, whose
`findObservedClassesInProject()` calls `ProjectSourcesFinder.findProjectSourcesPath()` and then
walks the `.java`/`.kt` files under whatever path comes back, entirely independently of
`@Policy`'s `withinPath` and of compiled bytecode. `findProjectSourcesPath()` for a Gradle
project turned out not to read `sourceSets` at all: it is a legacy accessor that scans
`build.gradle` for one specific top-level property assignment, `assignmentSrcDir`, and returns
empty if it is absent. An empty result here does not make the structural check permissive; it
makes `observedClasses` empty, so every class in the exercise reports as unimplemented.

**What worked:** adding, alongside the existing `sourceSets` block:

```gradle
def assignmentSrcDir = "assignment/src"
```

**Final state:** all six structural tests found `Sky` and `Nature`. The SOLUTION participation
reached 100%.

## Verifying the migration

With the exercise green, [Postcompile: Gradle § Replace the class-shadowing
guard](./postcompile/gradle.md#replace-the-class-shadowing-guard) and
[§ Verify the migration](./postcompile/gradle.md#verify-the-migration) were carried out in full,
not skipped as already covered by the passing exercise:

- **Reserved-package boundary v2** was installed from the shipped `AresReservedPackages.gradle`
  snippet, applied from `build.gradle`, and its own build stayed green: the boundary accepted
  the legitimate solution code.
- **A positive/negative control pair** was added temporarily in the one domain the policy
  grants exactly one allowance in (`readAllFiles: true` under `target`, every other file
  operation denied): a permitted read under `target/`, asserted not to throw, and a forbidden
  write under the same path, asserted to throw `SecurityException`. Both passed, the run stayed
  green, and the rejection was asserted rather than merely absent of failure.
- **Breaking AspectJ weaving on purpose**, by deleting the `aspect
  "de.tum.cit.ase:ares:${aresVersion}"` dependency line, dropped the score from 100% to 95.7%:
  the negative control stopped rejecting, because nothing wove the advice into the compiled
  class any more. Restoring the line restored 100%, confirming the runtime layer, not static
  analysis, was what had been rejecting the forbidden write.
- **Declaring a class in the reserved package**, `de.tum.cit.ase.ares.api`, failed the build
  with the boundary's own diagnostic:

  ```
  Ares reserved-package validation 2 rejected student output: [de/tum/cit/ase/ares/api/EvilClass.class]. No bypass flag is supported.
  ```

  Removing the class restored the build.

The temporary control classes and the reserved-package violation were removed once each check
was confirmed; nothing from this verification step remains in the shipped exercise.

## Comparing against the Sample Solution

T01E02 has an official "Sample Solution" exercise in the same course (ITP Ares 2 Test Course,
exercise 21016), read only after this migration was finished and the log above written, per
[the common page's](./index.md) instruction not to let a reference answer shape the attempt.
Its TESTS repository's `build.gradle` agrees with this migration on the load-bearing point and
diverges from the current guide on two others worth recording here rather than copying.

**Agrees:** `def assignmentSrcDir = "assignment/src"` is present, confirming Failure 4 above is
a real, general requirement for a migrated Artemis exercise with structural tests, not an
artefact of this one.

**Diverges, style only:** the Sample Solution declares dependencies with hardcoded version
literals rather than the guide's `ext { aresVersion, aspectjVersion }` block, puts the AspectJ
runtime jar in the same `aresAgent` configuration as the agent jar rather than a separate
`aresAspectjRuntime` one, and attaches the module-access flags with a plain `jvmArgs +=` list
rather than the `CommandLineArgumentProvider` the guide uses for configuration-cache
compatibility. None of this affects correctness; it predates the guide's current recommended
form.

**Diverges, not just style:** the Sample Solution's class-shadowing guard is still the Ares 1
list-and-`doFirst` shape, checked against Ares 1-era prefixes such as `de/tum/in/test/api/`, not
the reserved-package boundary v2 this migration installed and proved in
["Verify the migration"](#verifying-the-migration) above. [Postcompile: Gradle § Replace the
class-shadowing guard](./postcompile/gradle.md#replace-the-class-shadowing-guard) is explicit that
the old guard "must be replaced, not kept," so this is a gap in the Sample Solution rather than
an equally-valid alternative. Separately, its `SecurityPolicy.yaml` is present but empty, while
the test classes' `@Policy(value = ...)` points at it rather than leaving `value` blank; the
guide's policy-free alternative is documented for a blank `value`, not a non-blank one pointing
at nothing, so this exercise was not built to confirm which behaviour that combination produces.

Neither point changes anything about the migration recorded above. They are reported so that
whoever maintains the Sample Solution can decide whether to bring it forward to boundary v2, not
because this migration needed to match it.
