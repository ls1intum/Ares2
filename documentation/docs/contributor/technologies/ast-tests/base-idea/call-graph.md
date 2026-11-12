---
title: "Call Graph"
sidebar_position: 2
description: "Deriving call relationships from the syntax tree, and how that differs from a bytecode call graph."
---

:::tip[Simple Story]
While marking, the teacher can work out from what the pupil wrote which part of an answer calls
on which. That shows, for example, whether an answer calls on itself.

It shows only what the pupil wrote. A permitted aid appears as a name with nothing behind it.
This serves the instructions that forbid certain ways of answering, and it stops nobody from
doing anything forbidden. Clearing the desks looks at what the pupil's work turned into instead.

[Who is who](/instructor/who-is-who)
:::

## What it is

A call graph derived from an abstract syntax tree (AST) is built by walking the tree and recording each call
expression. It answers questions such as *does this method call itself*, which is how
recursion is detected.

## How it differs from the bytecode call graph

| | AST call graph | Bytecode call graph, built by the [T. J. Watson Libraries for Analysis (WALA)](/contributor/technologies/architecture-tests/wala) |
| --- | --- | --- |
| Built from | source | compiled classes |
| Sees library internals | no | yes |
| Sees synthetic methods | no | yes |
| Resolving a call target | needs symbol solving | resolved by the compiler |
| Suited to | structural requirements | reachability of forbidden operations |

Resolving *which* method a call refers to needs more than the tree, which is why Ares
depends on `javaparser-symbol-solver-core` and not only on `javaparser-core`.

:::warning[Not a security boundary]
The AST layer answers questions about how code is written. It is not an enforcement
mechanism: source-level analysis cannot bind what a program does at runtime.
:::

## Further reading

- [JavaParser](https://javaparser.org/) — project site
- [javaparser/javaparser](https://github.com/javaparser/javaparser) — source repository
- [Graphs in Java](https://www.baeldung.com/java-graphs) — Baeldung
