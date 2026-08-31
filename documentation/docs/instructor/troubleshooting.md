---
title: "Troubleshooting"
sidebar_position: 6
description: "The failures instructors hit when protecting or migrating an exercise, and what each one means."
---

:::tip[Simple Story]
When something goes wrong, the report names the mechanism rather than the cause. It tells you
which part of the checklist objected, not what you did.

This page translates the reports back into what you have to change.
:::

The two tables below are the fastest route: find the message you saw. The sections
after them cover the failures that show up as wrong behaviour rather than as a message.

## Symptom table: setting Ares 2 up

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| `ClassNotFoundException: de.tum.cit.ase.ares.api.Policy` | Ares not on the test classpath | Verify the `testImplementation` dependency is present |
| `The artifact de.tum.cit.ase:ares referenced in aspectj plugin as an aspect library, is not found the project dependencies` | Maven only: Ares is `<scope>test</scope>`, but the aspect library is resolved against the compile-visible dependencies | Change the scope to `provided` (the dependency step above) |
| A test asserting on the violation text fails, although the operation was blocked | Ares localises its violation messages, so "blocked by Ares" reads "von Ares blockiert" on a German Java Virtual Machine (JVM) | Assert on locale-stable content, such as the file name or the offending method, rather than on the prose |
| `Could not find de.tum.cit.ase:ares:<version>` | The version does not exist on Maven Central, or a mirror has not synchronised it | Check the coordinate against Maven Central; only released versions resolve |
| `Failed to find premain agent`, or agent-related errors | Agent JAR not found, or wrong classifier | Ensure the dependency uses the `:agent` classifier (Gradle) or the `<classifier>agent</classifier>` artefact item (Maven) |
| Tests pass but student code is not restricted | `-javaagent` missing **and** no weaving | Check the argument provider (Gradle) or `<argLine>` (Maven), and that the `aspect` / `<aspectLibraries>` entry is present |
| **The build succeeds but nothing is woven** | The Ares JAR is on the compile classpath but not on the **aspect path** | Add `aspect "de.tum.cit.ase:ares:..."` (Gradle) or the `<aspectLibraries>` entry (Maven). `ajc` ignores binary aspects that are not on the aspect path, so this fails silently |
| `InaccessibleObjectException` at runtime | Missing `--add-opens` / `--add-exports` flags | Ensure the complete list from the agent step above / the agent step above is present. A partial list fails only once a policy exercises the corresponding advice |
| Coverage reports nothing after adding Ares | A plain `<argLine>` overwrote the property JaCoCo sets | Prefix the Surefire `<argLine>` with `@{argLine}` and declare an empty `<argLine>` property |
| `Could not resolve all files for configuration ':aresAgent'`, or "expected exactly one file" | The configuration is transitive, so it holds more than the agent JAR | Set `transitive = false` on the dedicated configurations, as in the agent-configuration step above |
| `IllegalStateException: Ambiguous project: both Maven and Gradle descriptors are active` | The project has both a `pom.xml` and a `build.gradle`, and the no-policy path has no explicitly selected build tool, so it cannot tell which is authoritative. Discovery fails before any enforcement is configured | Remove the descriptor you do not use, or supply a policy that names the configuration explicitly |
| `IllegalStateException: Unsupported project: no pom.xml, build.gradle or build.gradle.kts` | The directory the tests run from carries no supported build descriptor | Run from the project root that holds the build descriptor |
| `logback.xml occurs multiple times on the classpath` | The agent JAR and the ordinary Ares JAR each carry one | A warning only; enforcement is unaffected |
| `[Xlint:adviceDidNotMatch]` warnings during compilation | An `_ASPECTJ` build reports each Ares advice whose operation the exercise never performs | Expected and non-fatal; it confirms the weaver ran. A build that wove nothing looks the same, so do not suppress the category |
| The reserved-package check never runs under `gradlew test` | A boundary version 1 snippet hooked `check` alone | Migrate to boundary version 2, which gates every `Test` task too (the Gradle step above) |
| A Gradle file-system watcher (FSEvents) warning at build start | Gradle's native file watching cannot start in a sandboxed or container environment | A warning only; compilation and tests are unaffected. Pass `--no-watch-fs` to silence it |
| Policy seems to have no effect | Wrong `withinPath` | Gradle: `classes/java/main/<package/path>`, Maven: `classes/<package/path>` |

## Symptom table: migrating from Ares 1

| Problem | Cause | Solution |
|---|---|---|
| `The artifact de.tum.cit.ase:ares referenced in aspectj plugin as an aspect library, is not found the project dependencies` | Maven only: you carried `<scope>test</scope>` across from Ares 1 | Change the scope to `provided` (the step named in section 4.2 of this guide) |
| `package de.tum.in.test.api does not exist` | An Ares 1 import survived the rewrite | Search for `de.tum.in.test` across the test sources; any remaining hit is either a rename (the step named in section 5 of this guide) or a security annotation to translate (the step named in section 6 of this guide) |
| `cannot find symbol: class WhitelistPath` (and similar) | These annotations do not exist in Ares 2 | Translate them into the policy file, then delete them |
| The build succeeds but nothing is enforced | The Ares JAR is on the compile classpath but not on the **aspect path** | Add `aspect "de.tum.cit.ase:ares:..."` (Gradle) or the `<aspectLibraries>` entry (Maven). `ajc` ignores binary aspects that are not on the aspect path, so this fails silently |
| Tests pass, and nothing is restricted, and no error appears | The test carries a plain `@Test` and `@Policy` but no Ares test annotation, so the extension was never registered | Add `@Public`, `@Hidden`, `@PublicTest` or `@HiddenTest` (the step named in section 7 of this guide) |
| Your own test code is blocked by the policy | `theFollowingClassesAreTestClasses` names a package instead of exact class names | List every test class by its fully qualified name (the step named in section 6.4 of this guide) |
| A policy is rejected on load | `thisPolicyFileCompliesToThePolicyVersion` is missing or is not `1`, or one of the six lists is absent | All six lists must be present even when empty (the step named in section 6.1 of this guide) |
| A policy loads but the run fails with a supervised-code error | `theSupervisedCodeUsesTheFollowingPackage` is missing or blank. The schema tolerates that, but a **present** policy must name the package, and Ares fails closed rather than guessing it | Set the supervised package (the step named in section 6.1 of this guide) |
| A `timeout` value of `0` is rejected | `timeout` must be strictly positive | Use a positive value, or leave the list empty |
| Policy seems to have no effect | Wrong `withinPath` | Gradle: `classes/java/main/<package/path>`; Maven: `classes/<package/path>` |
| A `@StrictTimeout` was replaced by `regardingTimeouts` and no longer bounds anything | Timeouts are Phobos cases, and the Phobos stage is generated but not yet dispatched in-process in Ares 2.1.3 | Restore `@StrictTimeout` (the step named in section 6.2 of this guide) |
| `InaccessibleObjectException` at runtime | An incomplete list of module-access flags | Use the complete list from the step named in section 4.1 of this guide or the step named in section 4.2 of this guide |
| Coverage reports nothing after the migration | A plain `<argLine>` overwrote the property JaCoCo sets | Prefix Surefire's `<argLine>` with `@{argLine}` and declare an empty `<argLine>` property |
| `[Xlint:adviceDidNotMatch]` warnings during compilation | An `_ASPECTJ` build reports each Ares advice whose operation the exercise never performs | Expected and non-fatal; it confirms the weaver ran. A build that wove nothing looks the same, so do not suppress the category |
| The reserved-package check never runs under `gradlew test` | The snippet hooks `check` alone | Use boundary version 2, which gates every `Test` task too (the step named in section 8.1 of this guide) |
| `Ambiguous project: both Maven and Gradle descriptors are active` | The project has both a `pom.xml` and a `build.gradle`, and no policy names the build tool | Remove the descriptor you do not use, or supply a policy that names the configuration explicitly |

## The build succeeds but nothing is enforced

Missing agent attachment, a `@Policy` that never applied, or a reserved-package check that was
never hooked onto the test task.

## A permitted operation is rejected

Path normalisation, an allow-list entry that names a directory where a file was meant, or a call
that reaches the forbidden operation through a library rather than directly.

## A forbidden operation is not rejected

The most serious case. Which of the four analysis and weaving combinations was active,
and whether the supervised package was shadowed.

## The agent does not attach

`--add-opens` requirements, `useSystemClassLoader`, and the Surefire `argLine` trap: a plain
`<argLine>` replaces what other plugins contributed, silently dropping the JaCoCo agent.

## Gradle runs the tests but not the boundary check

`check.dependsOn test` and not the reverse, so a hook on `check` never runs for `gradlew test`.

## The policy file is rejected

Version gate, unknown fields, and values the enforcement layers cannot honour.

## Timeouts do not fire

Policy resource limits are generated but not dispatched in Postcompile. `@StrictTimeout` is the
mechanism that applies there.

## Notes

- Before filing a bug, record which of the four combinations you ran. A failure that appears in
  only one of them is a different problem from one that appears in all four.
