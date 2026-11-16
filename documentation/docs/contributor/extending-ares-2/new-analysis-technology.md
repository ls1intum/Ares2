---
title: "New analysis technology"
sidebar_position: 5
description: "Adding a static analyser alongside ArchUnit and WALA."
---

:::tip[Simple Story]
Before the start, the teacher clears the desks by the rule sheet. Today they know two ways of
doing it. Both look at what the pupils' work turned into, not at what they wrote. This page is
about a third way.

A new way has to decide the same as the two old ones on the same desks. When it disagrees, that
is a mistake to find, not a setting to adjust. This page is still an outline; nobody has
written it out.

[Who is who](/instructor/who-is-who)
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
