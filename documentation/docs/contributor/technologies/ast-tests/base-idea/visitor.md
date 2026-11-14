---
title: "Visitor"
sidebar_position: 3
description: "The traversal pattern used to walk the syntax tree and collect findings."
---

:::tip[Simple Story]
While marking, the teacher checks instructions such as "solve it without a loop". They want to
stop at every loop in the answer and nowhere else.

So the way through the answer stays the same every time, and only what the teacher checks at
each stop changes. They ask to be told at every loop, and the walk through the answer does the
rest. These instructions stop nobody from doing anything forbidden.

[Who is who](/instructor/who-is-who)
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
