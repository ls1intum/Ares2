---
title: "JavaParser"
sidebar_position: 2
description: "Parsing Java source into an AST, and resolving what its names refer to."
---

:::tip[Simple Story]
JavaParser turns a pupil's `.java` file into a tree you can ask questions about, and can also
work out what each name in that file actually refers to.
:::

## What it is

JavaParser parses Java source into an abstract syntax tree and offers visitors to walk it.
The companion symbol solver resolves names to their declarations, which the tree alone
cannot do.

## How Ares 2 uses it

Ares depends on both `javaparser-core` and `javaparser-symbol-solver-core`. The
[`api/ast`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/ast) package builds structural assertions on top: the `asserting`
subpackage holds the assertions, `model` the representation and `type` the supported
constructs.

This supports exercise requirements about *how* a solution is written, which no runtime
sandbox can express.

## Further reading

- [JavaParser](https://javaparser.org/) — project site
- [javaparser/javaparser](https://github.com/javaparser/javaparser) — source repository
- [Introduction to JavaParser](https://www.baeldung.com/javaparser) — Baeldung
- [Visitor Design Pattern in Java](https://www.baeldung.com/java-visitor-pattern) — Baeldung
