---
title: "New build tools"
sidebar_position: 3
description: "Adding a build tool beyond Maven and Gradle: BuildMode, descriptor detection and generated wiring."
---

:::tip[ELI5]
Ares has to put its enforcement into someone else's build.

Adding a build tool means teaching it one more build's vocabulary.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written.
:::

## Where the build tool is decided

`BuildMode`, its `fileName()` descriptors, and the check in `JavaWriter.writeTestCases` that
refuses to write when no matching descriptor exists in the project root.

## Descriptor detection

How the build configuration is discovered and why a mismatch between the selected mode and the
discovered descriptor is a hard failure rather than a warning.

## What has to be generated

For Precompile: the copy and edit configuration entries. For Postcompile: the agent attachment,
the reserved-package check and the test-task hook.

## The reserved-package boundary

Every build tool needs its own way to assert that the supervised package is not shadowed. This
is not optional, and it is the part most easily forgotten.

## Verification

A runnable example exercise under `examples/`, with both a positive and a negative control.

## Notes

- The Gradle hook must attach to every `Test` task rather than to `check`, because Gradle
  defines `check.dependsOn test` and not the reverse.
