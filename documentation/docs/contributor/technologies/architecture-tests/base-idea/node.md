---
title: "Node"
sidebar_position: 1
description: "The unit the architecture layer reasons about: a class, a method or a field of the analysed program."
---

:::tip[Simple Story]
While clearing the desks, the teacher looks at what each pupil's work turned into, not at what
the pupil wrote. That holds some pieces nobody typed.

This page is about one such piece, one stop on a route. The teacher sorts the pieces before judging them.
The rule sheet applies to the pieces of the pupils' work. The teacher trusts some other pieces,
such as those of the grading instructions, because of where they sit. A pupil's piece placed
there counts as trusted too, so the pupils' work must never sit there.

[Who is who](/instructor/who-is-who)
:::

## What it is

A node is one element of the analysed program. Which elements count as nodes depends on
the engine: ArchUnit reasons mostly about classes and their members, while T. J. Watson Libraries for Analysis (WALA) reasons
about individual methods, because a call graph joins methods rather than classes.

Nodes come from **compiled bytecode**, not from source. That matters twice over: the
analysis sees what the compiler produced, including synthetic and bridge methods
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
