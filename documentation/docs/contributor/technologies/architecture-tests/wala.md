---
title: "WALA"
sidebar_position: 3
description: "Inter-procedural call-graph analysis: reachability, sinks and false-positive filtering."
---

:::tip[ELI5]
WALA builds the full map of which method can call which, then asks whether any route leads
from the student's code to something forbidden.

Where the simpler engine asks "does this class mention that one?", WALA asks "can I get
there from here, however many steps it takes?"
:::

## What it is

WALA (T. J. Watson Libraries for Analysis) is a static analysis framework from IBM
Research. It builds inter-procedural call graphs and supports data-flow analysis over them.

## How Ares 2 uses it

WALA is the second architecture engine, selected by the `WALA` half of the
[programming language configuration](/contributor/policy/programming-language-configuration).
It builds a [call graph](./base-idea/call-graph.md) from the entry point named in
`theMainClassInsideThisPackageIs` and searches for [paths](./base-idea/dfs-path.md) from
student code to forbidden sinks.

This finds indirect access that dependency analysis misses: a student method that calls a
helper that calls a library method that opens a file has no direct dependency on the file
application programming interface (API), but there is a path.

`WalaPathClassification` decides which parts of a discovered path are student code and
which are trusted infrastructure. Its `RESERVED_PACKAGE_PREFIX_VERSION` is the versioned
prefix list that the build-side reserved-package boundary must stay aligned with.

## The cost

Whole-program call-graph construction is far more expensive than dependency analysis, and
reachability over an over-approximated graph produces false positives that have to be
filtered. That trade is the reason both engines exist rather than one.

## Further reading

- [wala/WALA](https://github.com/wala/WALA) — source repository
- [WALA Wiki](https://github.com/wala/WALA/wiki) — project documentation
- [Graphs in Java](https://www.baeldung.com/java-graphs) — Baeldung
