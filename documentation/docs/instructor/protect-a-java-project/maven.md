---
title: "Maven"
sidebar_position: 1
description: "Protecting a Maven-based Java project with Ares 2, from dependency to enforced policy."
---

:::note This page is a stub
The end-to-end Maven walkthrough has not been written as a single page yet. Every step already
exists in the reference pages linked below, and the
[Complete Setup Manual](./complete-setup-manual.md) covers the whole path for both build tools.
:::

## The path, in order

1. **[Installation](./installation.md)** — add the `de.tum.cit.ase:ares` dependency to
   `pom.xml`.
2. **[Setup](./setup.md)** — understand the public/hidden test model.
3. **[Test Annotations](./test-annotations.md)** — mark tests with `@PublicTest` /
   `@HiddenTest` and give hidden tests a `@Deadline`.
4. **[Policy Configuration](./policy-configuration.md)** — write `security-policy.yaml`,
   choosing one of the four `JAVA_USING_MAVEN_*` configurations.
5. **[Precompile Mode](./precompile.md)** or **[Postcompile Mode](./postcompile.md)** — generate
   the enforcement artefacts, or enforce at runtime with `@Policy`.
6. **The build-side reserved-package check** — see
   [What does Ares 2 not protect against](../ares-2/what-does-ares-2-not-protect-against.md).
   For Maven this is the `maven-enforcer-plugin` rule described in
   [Postcompile Mode](./postcompile.md#maven). It is **not optional**.

## Working example

A complete, runnable Maven exercise is in the repository at
[`examples/ares-exercise-maven`](https://github.com/ls1intum/Ares2/tree/main/examples/ares-exercise-maven).
It is built and run by CI, so it cannot rot silently. Start from it rather than from a blank
`pom.xml`.
