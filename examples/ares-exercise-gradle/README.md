# Ares 2 exercise, Gradle

A complete, runnable exercise. The scenario and the reasoning behind it are described once in [`examples/README.md`](../README.md); this file gives the command and the expected output.

## Run it

```bash
./gradlew test
```

Requires Java 17. The Gradle wrapper is included, so no Gradle installation is needed.

## Expected output

The build succeeds, and the task list contains the reserved-package validation **before** the tests:

```
> Task :verifyAresReservedPackagesV2
> Task :test

BUILD SUCCESSFUL
```

All three tests pass: `name()`, `readsThePermittedFile()` and `rejectsTheForbiddenFile()`. Gradle hides test output by default; to see the individual results, open `build/reports/tests/test/index.html` or add `test { testLogging.showStandardStreams = true }`.

The AspectJ compiler prints a number of `[Xlint:adviceDidNotMatch]` warnings during `compileJava`. These are expected: the Ares JAR carries advice for the file, network, command and thread domains, and this exercise only touches files, so most of it matches nothing here.

## What is where

| Path | Purpose |
|---|---|
| `build.gradle` | The full setup: dependencies, aspect path, agent wiring, reserved-package boundary |
| `gradle/AresReservedPackages.gradle` | Copied verbatim from the snippet Ares ships at `configuration/reservedPackages/GradleReservedPackages.gradle` |
| `src/main/java/org/example/Penguin.java` | Supervised code, standing in for a student submission |
| `src/test/java/org/example/PenguinTest.java` | Trusted test class, exempt through the policy |
| `src/test/resources/SecurityPolicy.yaml` | Permits exactly one file, `allowed.txt` |
| `allowed.txt` / `secret.txt` | The permitted and the forbidden file, both created in the project directory so the exercise behaves the same on Windows, Linux and macOS |

`withinPath` is `classes/java/main/org/example` here. Maven uses `classes/org/example`, because the two build tools lay their output directories out differently.

## Configuration cache

The setup is configuration-cache compatible. The agent and AspectJ runtime paths are supplied through a `CommandLineArgumentProvider` with declared file inputs rather than being interpolated into `jvmArgs` while the build is configured:

```bash
./gradlew test --configuration-cache
```

Running that twice reports `Configuration cache entry reused`.
