# Runnable Ares 2 exercises

Two complete, minimal exercises that build and run as they stand. They exist so that setting up Ares does not start from a blank file, and so that the snippets in [the setup manual](../documentation/docs/user/make-a-project-an-ares-project.md) are demonstrably correct rather than merely plausible.

| Example | Build tool | Configuration |
|---|---|---|
| [`ares-exercise-gradle`](ares-exercise-gradle) | Gradle (wrapper included) | `JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ` |
| [`ares-exercise-maven`](ares-exercise-maven) | Maven | `JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ` |

Both contain the same scenario, so the two build files can be compared line by line.

## What the scenario demonstrates

`Penguin` is the supervised class, standing in for a student submission. `PenguinTest` is the trusted test class, named in the policy's `theFollowingClassesAreTestClasses` and therefore exempt from enforcement itself.

The policy permits **exactly one** file, `allowed.txt`, and nothing else. That single allowance is the point of the design:

- **Positive control**, `readsThePermittedFile`: supervised code reads `allowed.txt` and succeeds.
- **Negative control**, `rejectsTheForbiddenFile`: supervised code reads `secret.txt` and the test asserts that Ares rejects it.

Both reads happen in `Penguin`, never in the test. A test class named in the policy is exempt, so a read performed by the test itself would legitimately succeed and would demonstrate nothing.

The one allowance is equally deliberate. Ares adds a static deny-all rule only while a domain has **no** allowance ([Enforcement Model](../documentation/docs/developer/policy/enforcement-model.md)). Under a fully restrictive file policy, ArchUnit or WALA would reject the forbidden read before any runtime mechanism was consulted, so the negative control would still pass with the agent detached and the weaving switched off. Permitting one file makes the runtime layer authoritative for the file domain, and only then does the negative control actually exercise it.

## Expected result

A correct run is **green** and contains an asserted rejection. A failed build is not the expected outcome.

```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

The three tests are `name()`, `readsThePermittedFile()` and `rejectsTheForbiddenFile()`.

For Gradle, the task list must also show `verifyAresReservedPackagesV2` running **before** `test`:

```
> Task :verifyAresReservedPackagesV2
> Task :test
```

If that task is missing from a `gradlew test` run, the reserved-package boundary is not active and the exercise is carrying a superseded boundary version 1 snippet.

## Prerequisites

Java 17. The Gradle example ships the wrapper, so no Gradle installation is needed. The Maven example needs Maven 3.8 or newer.

Both resolve `de.tum.cit.ase:ares` from Maven Central. To try them against a locally built Ares instead, install it and add `mavenLocal()` to the Gradle `repositories` block; Maven consults the local repository automatically.

## Verifying that enforcement is real

Each break below must be detected. Make them in a **copy**, not in the example itself.

| Break | Expected consequence |
|---|---|
| Delete the `aspect` dependency (Gradle) or `<aspectLibraries>` (Maven) | `rejectsTheForbiddenFile` fails: nothing is woven, so nothing is enforced |
| Add a class in package `de.tum.cit.ase.ares.api` to `src/main/java` | The build fails with `Ares reserved-package validation 2 rejected student output` |
| Remove the permitted-file entry from the policy | `readsThePermittedFile` fails, and the negative control stops proving anything, because static deny-all now covers the whole domain |

Removing `-javaagent` changes nothing in these two examples, and that is correct: both use an `_ASPECTJ` configuration, where the aspects are woven at compile time. Removing the agent is only a meaningful break under an `_INSTRUMENTATION` configuration.

## Gradle wrapper provenance

Generated with `gradle wrapper --gradle-version 9.6.1 --distribution-type bin` using Gradle 9.6.1. `gradle/wrapper/gradle-wrapper.properties` pins `distributionSha256Sum`, so the wrapper refuses a distribution that does not match.
