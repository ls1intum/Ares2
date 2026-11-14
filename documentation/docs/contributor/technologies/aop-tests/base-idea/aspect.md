---
title: "Aspect"
sidebar_position: 1
description: "A cross-cutting concern captured in one place instead of scattered through the code."
---

:::tip[Simple Story]
Some tasks of the teacher belong to no single pupil. "Check the rule sheet whenever someone
reaches for a worksheet" holds for every pupil in the room.

Writing that task out again for every pupil is hopeless, because somebody always misses one.
This page is about such a task, set down once, in one place, and carried out everywhere it is needed. The
teacher keeps tasks like this for the worksheets, the telephone, the friend outside the room and
the side jobs.

[Who is who](/instructor/who-is-who)
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
