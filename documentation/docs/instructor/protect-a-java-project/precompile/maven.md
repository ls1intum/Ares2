---
title: "Maven"
sidebar_position: 2
description: "Protecting a Maven project with Ares 2 in Precompile mode. Not yet written."
---

:::tip[ELI5]
This is the Maven counterpart to the Gradle Precompile walkthrough.

It does not exist yet, so this page says what it will cover rather than pretending otherwise.
:::

:::note[This page is a stub]
Ares supports the four `JAVA_USING_MAVEN_*` configurations, so Precompile generation works for
Maven. What does not exist is a written, verified walkthrough: the existing Precompile
documentation only ever covered Gradle, and there is no runnable Maven Precompile example to
derive one from. Until this page is written, follow the
[Gradle Precompile page](./gradle.md) for the generator steps, which are build-tool independent,
and the [Maven Postcompile page](../postcompile/maven.md) for the Maven-side wiring.
:::

## What this page will cover

The same six steps as the Gradle page, with the Maven wiring in place of the Gradle wiring:
defining the policy, running the generator, what lands in the project, the `pom.xml` changes,
the reserved-package boundary, and verification with both controls.

## What has to be established first

The generated artefacts have to be compiled and, for an `_ASPECTJ` configuration, woven by the
exercise's own build. The Maven equivalents of the Gradle steps therefore need to be written
against a working example rather than inferred from the Gradle page.

## What already applies

The reserved-package boundary is **not** generated in either mode, so the Maven snippet from the
[Maven Postcompile page](../postcompile/maven.md) applies unchanged here.

## Notes

- Do not assume the Gradle `build.gradle` changes translate one to one. They are written against
  the freefair AspectJ plugin, which has no Maven counterpart with the same behaviour.
