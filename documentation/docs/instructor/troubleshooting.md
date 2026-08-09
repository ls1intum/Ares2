---
title: "Troubleshooting"
sidebar_position: 6
description: "The failures instructors actually hit when protecting or migrating an exercise, and what each one means."
---

:::tip[ELI5]
When something goes wrong, the error message usually names the mechanism rather than the cause.

This page translates the messages back into what you actually have to change.
:::

:::note[Still being consolidated]
The symptom table below came from the setup manual. The Ares 1 migration guide has a second
table that has not been merged in yet; until it is, check that page as well.
:::

## Symptom table

| Problem | Possible Cause | Solution |
|---------|---------------|----------|
| `ClassNotFoundException: de.tum.cit.ase.ares.api.Policy` | Ares not on the test classpath | Verify the `testImplementation` dependency is present |
| `The artifact de.tum.cit.ase:ares referenced in aspectj plugin as an aspect library, is not found the project dependencies` | Maven only: Ares is `<scope>test</scope>`, but the aspect library is resolved against the compile-visible dependencies | Change the scope to `provided` (the dependency step above) |
| A test asserting on the violation text fails, although the operation was blocked | Ares localises its violation messages, so "blocked by Ares" reads "von Ares blockiert" on a German JVM | Assert on locale-stable content, such as the file name or the offending method, rather than on the prose |
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
| The reserved-package check never runs under `gradlew test` | A boundary version 1 snippet hooked `check` alone | Migrate to boundary version 2, which also gates every `Test` task (the Gradle step above) |
| Policy seems to have no effect | Wrong `withinPath` | Gradle: `classes/java/main/<package/path>`, Maven: `classes/<package/path>` |

## The build succeeds but nothing is enforced

Missing agent attachment, a `@Policy` that never applied, or a reserved-package check that was
never hooked onto the test task.

## A permitted operation is rejected

Path normalisation, an allow-list entry that names a directory where a file was meant, or a call
that reaches the forbidden operation through a library rather than directly.

## A forbidden operation is not rejected

The most serious case. Which of the four analysis and weaving combinations was actually active,
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
