---
title: "Gradle"
sidebar_position: 2
description: "Protecting a Gradle-based Java project with Ares 2, from dependency to enforced policy."
---

:::note This page is a stub
The end-to-end Gradle walkthrough has not been written as a single page yet. Every step already
exists in the reference pages linked below, and the
[Complete Setup Manual](./complete-setup-manual.md) covers the whole path for both build tools.
:::

## The path, in order

1. **[Installation](./installation.md)** — add the `de.tum.cit.ase:ares` dependency to
   `build.gradle`.
2. **[Setup](./setup.md)** — understand the public/hidden test model.
3. **[Test Annotations](./test-annotations.md)** — mark tests with `@PublicTest` /
   `@HiddenTest` and give hidden tests a `@Deadline`.
4. **[Policy Configuration](./policy-configuration.md)** — write `security-policy.yaml`,
   choosing one of the four `JAVA_USING_GRADLE_*` configurations.
5. **[Precompile Mode](./precompile.md)** or **[Postcompile Mode](./postcompile.md)** — generate
   the enforcement artefacts and wire up the agent JARs, or enforce at runtime with `@Policy`.
6. **The build-side reserved-package check** — see
   [What does Ares 2 not protect against](../ares-2/what-does-ares-2-not-protect-against.md).
   For Gradle this is the `forbiddenPackageFolders` assertion described in
   [Postcompile Mode](./postcompile.md#gradle). It is **not optional**.

:::warning Hook the check onto every `Test` task, not onto `check`
Gradle defines `check.dependsOn test` and not the reverse, so a `check`-only hook never runs for
`gradlew test`.
:::

## Working example

A complete, runnable Gradle exercise is in the repository at
[`examples/ares-exercise-gradle`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-gradle).
It is built and run by CI, so it cannot rot silently. Start from it rather than from a blank
`build.gradle`.
