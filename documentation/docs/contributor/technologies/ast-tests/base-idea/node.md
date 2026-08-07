---
title: "Node"
sidebar_position: 1
description: "A single syntactic construct in the parsed source: a class, a method, a loop, an expression."
---

:::tip[ELI5]
When a tool reads source code, it does not keep it as text. It turns it into a tree.

The whole file is the trunk, each class is a branch, each method a smaller branch, each
statement a twig. A node is any one of those. Working on a tree is far easier than working
on text, because the structure is already worked out.
:::

## What it is

A node in an abstract syntax tree is one syntactic construct. The tree is *abstract*
because it drops what does not affect meaning, such as whitespace, and keeps the structure.

The essential difference from the architecture layer: an AST node comes from **source**,
not bytecode. So it can see things the compiler discards, such as which loop construct was
written, and it cannot see anything for which no source is available.

## In Ares 2

The [`api/ast`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/ast) package uses this to make structural assertions about how
student code is written, rather than about what it may do at runtime. That answers a
different kind of exercise requirement: *did they actually use recursion*, rather than
*may they open this file*.

## Further reading

- [JavaParser](https://javaparser.org/) — project site
- [Introduction to JavaParser](https://www.baeldung.com/javaparser) — Baeldung
- [The Java Language Specification, Java SE 21](https://docs.oracle.com/javase/specs/jls/se21/html/index.html) — Oracle
