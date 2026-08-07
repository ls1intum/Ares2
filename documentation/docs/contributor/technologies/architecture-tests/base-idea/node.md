---
title: "Node"
sidebar_position: 1
description: "The unit the architecture layer reasons about: a class, a method or a field of the analysed program."
---

:::tip ELI5
Imagine the program drawn as a map of dots joined by arrows.

A node is one dot. Depending on how closely you look, a dot might be a whole class or a
single method. Everything the architecture layer does is answering questions about which
dots exist and which arrows lead where.
:::

## What it is

A node is one element of the analysed program. Which elements count as nodes depends on
the engine: ArchUnit reasons mostly about classes and their members, while WALA reasons
about individual methods, because a call graph joins methods rather than classes.

Nodes come from **compiled bytecode**, not from source. That matters twice over: the
analysis sees what the compiler actually produced, including synthetic and bridge methods
the author never wrote, and it can analyse a dependency for which no source is available.

## In Ares 2

Nodes are classified before they are judged. A node belonging to student code is subject
to the policy; a node belonging to Ares itself, to the test harness, or to a class named
in `theFollowingClassesAreTestClasses` is trusted. That classification is the whole reason
the trust boundary can be drawn at all, and it is why student code must never land in a
reserved package: a node in a reserved package would be classified as trusted.

## Further reading

- [Introduction to Graph Theory](https://www.baeldung.com/cs/graph-theory-intro) — Baeldung on Computer Science
- [Graphs in Java](https://www.baeldung.com/java-graphs) — Baeldung
- [The Java Language Specification, Java SE 21](https://docs.oracle.com/javase/specs/jls/se21/html/index.html) — Oracle
