---
title: "Visitor"
sidebar_position: 3
description: "The traversal pattern used to walk the syntax tree and collect findings."
---

:::tip[ELI5]
You want to do something at every node of a big tree, but only actually care about a few
kinds of node.

Rather than writing the walking logic yourself and getting it wrong, you hand over an
object that says "when you reach a loop, call me". The tree walks itself and calls you
back at the parts you asked about.
:::

## What it is

The visitor pattern separates *traversing* a structure from *what to do* at each element.
The tree knows how to walk itself; the visitor supplies one method per node type, and only
the interesting ones are overridden.

It fits ASTs particularly well because a tree has many node types and any given analysis
cares about few of them.

## In Ares 2

The [`api/ast`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/ast) package uses JavaParser's visitors to collect the
information an assertion needs, keeping the traversal generic and the question specific.

## Further reading

- [Visitor Design Pattern in Java](https://www.baeldung.com/java-visitor-pattern) — Baeldung
- [JavaParser](https://javaparser.org/) — project site
- [Introduction to JavaParser](https://www.baeldung.com/javaparser) — Baeldung
