---
title: "JavaParser"
sidebar_position: 2
description: "Parsing Java source into an AST, and resolving what its names refer to."
---

:::tip[Simple Story]
Some instructions forbid certain words and ways of answering, such as "solve it without a loop".
The teacher checks them while marking, by reading what the pupil wrote.

This tool turns the pupil's writing into parts the teacher can ask questions about. It can work
out what each name in the writing refers to. These instructions are about how the pupil wrote
the answer. They stop nobody from doing anything forbidden.

[Who is who](/instructor/who-is-who)
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
