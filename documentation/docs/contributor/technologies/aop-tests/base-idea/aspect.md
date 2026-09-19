---
title: "Aspect"
sidebar_position: 1
description: "A cross-cutting concern captured in one place instead of scattered through the code."
---

:::tip[Simple Story]
Some rules do not belong to any one pupil. "Check the checklist before picking up a paper"
applies wherever a paper is picked up.

Writing that into every desk would be hopeless: you would miss some, and a pupil's work is not
yours to edit anyway. An aspect is that rule written down **once**, in one place, and then
applied everywhere it is needed.
:::

## What it is

An aspect is the module that holds a cross-cutting concern. It brings together the places the
concern applies to (its [pointcuts](./pointcut.md)) and the behaviour to run there (its
[advice](./advice.md)).

In Ares the concern is the security check itself. The advice classes under
[`api/aop`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/aop) hold the logic that decides whether a file, command, thread or
network operation is permitted, written once and applied at every point where such an
operation can occur.

## Further reading

- [AspectJ Documentation and Resources](https://eclipse.dev/aspectj/doc/latest/index.html) — Eclipse Foundation
- [Intro to AspectJ](https://www.baeldung.com/aspectj) — Baeldung
- [Understanding Aspect-Oriented Programming: Java and AspectJ](https://aayush-shrivastav.medium.com/understanding-aspect-oriented-programming-java-and-aspectj-4da6dd0e4157) — Aayush Shrivastav, Medium (freely readable)
