---
title: "New analysis technology"
sidebar_position: 5
description: "Adding a static analyser alongside ArchUnit and WALA."
---

:::tip[Simple Story]
The reach check reads the pupil's compiled answer, without running any of it, looking for routes to
forbidden things.

Two libraries do that today. This is how a third would join them.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written.
:::

## The shared vocabulary

Node, call graph, depth-first search (DFS) path and rule, as described under
[Architecture Tests](../technologies/architecture-tests/base-idea/node.md). A new analyser must express all four
or explain what it substitutes.

## Where it plugs in

`ArchitectureMode`, the test-case collection, and the execution entry point that receives the
architecture and aspect-oriented programming (AOP) mode strings.

## The data it needs

The methods lists under `templates/architecture`, the exclusions file and the essential packages
and classes.

## Precompile output

The Java sources and templates copied into the exercise, and the package rewrite into
`<packageName>.ares.api...`.

## Verification

The same rule set must produce the same verdicts as the existing analysers on the shared
fixtures. A disagreement is a finding, not a tuning parameter.

## Notes

- The analyser runs before `restrictedPackage` is published, so it must not itself trip the
  enforcement it is installing.
