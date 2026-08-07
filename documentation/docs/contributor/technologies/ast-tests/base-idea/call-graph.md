---
title: "Call Graph"
sidebar_position: 2
description: "Deriving call relationships from the syntax tree, and how that differs from a bytecode call graph."
---

:::tip ELI5
You can work out from the source which method calls which, and draw the same kind of map
the bytecode analysis draws.

The catch is that the source only shows you the code you have. Anything from a library is
a name on the page with nothing behind it.
:::

## What it is

A call graph derived from an AST is built by walking the tree and recording each call
expression. It answers questions such as *does this method call itself*, which is how
recursion is detected.

## How it differs from the bytecode call graph

| | AST call graph | Bytecode call graph ([WALA](/contributor/technologies/architecture-tests/wala)) |
| --- | --- | --- |
| Built from | source | compiled classes |
| Sees library internals | no | yes |
| Sees synthetic methods | no | yes |
| Resolving a call target | needs symbol solving | resolved by the compiler |
| Suited to | structural requirements | reachability of forbidden operations |

Resolving *which* method a call refers to needs more than the tree, which is why Ares
depends on `javaparser-symbol-solver-core` and not only on `javaparser-core`.

:::warning Not a security boundary
The AST layer answers questions about how code is written. It is not an enforcement
mechanism: source-level analysis cannot bind what a program does at runtime.
:::

## Further reading

- [JavaParser](https://javaparser.org/) — project site
- [javaparser/javaparser](https://github.com/javaparser/javaparser) — source repository
- [Graphs in Java](https://www.baeldung.com/java-graphs) — Baeldung
