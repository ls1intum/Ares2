# Ares 2 exercise, Maven

A complete, runnable exercise. The scenario and the reasoning behind it are described once in [`examples/README.md`](../README.md); this file gives the command and the expected output.

## Run it

```bash
mvn test
```

Requires Java 17 and Maven 3.8 or newer.

## Expected output

```
[INFO] --- antrun:3.2.0:run (verify-ares-reserved-packages-v2) @ ares-exercise-maven ---
...
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The three tests are `name()`, `readsThePermittedFile()` and `rejectsTheForbiddenFile()`. The last one asserts that Ares blocks the forbidden read, so a green run **is** the demonstration that enforcement works.

The AspectJ compiler prints a number of `[Xlint:adviceDidNotMatch]` warnings during `process-classes`. These are expected: the Ares JAR carries advice for the file, network, command and thread domains, and this exercise only touches files.

## What is where

| Path | Purpose |
|---|---|
| `pom.xml` | The full setup: dependencies, weaving, reserved-package boundary, agent wiring |
| `src/main/java/org/example/Penguin.java` | Supervised code, standing in for a student submission |
| `src/test/java/org/example/PenguinTest.java` | Trusted test class, exempt through the policy |
| `src/test/resources/SecurityPolicy.yaml` | Permits exactly one file, `allowed.txt` |
| `allowed.txt` / `secret.txt` | The permitted and the forbidden file, both created in the project directory so the exercise behaves the same on Windows, Linux and macOS |

## Two details worth copying

**Ares is `provided`, not `test`.** The `aspectj-maven-plugin` resolves `<aspectLibraries>` against the compile-visible dependencies and weaves the main classes during `process-classes`, where a test-scoped artefact is invisible. With `<scope>test</scope>` the build fails with `The artifact de.tum.cit.ase:ares referenced in aspectj plugin as an aspect library, is not found the project dependencies`. `provided` is compile- and test-visible while staying out of the packaged artefact.

**The Surefire `argLine` starts with `@{argLine}`,** and an empty `<argLine></argLine>` property is declared. Without the first, adding JaCoCo later silently produces empty coverage; without the second, a run in which JaCoCo does not participate fails on an unresolved `@{argLine}`.

`withinPath` is `classes/org/example` here. Gradle uses `classes/java/main/org/example`.
