---
title: "Pointcut"
sidebar_position: 3
description: "The predicate that selects which join points an aspect applies to."
---

:::tip[Simple Story]
There are countless moments in the examination where the teacher could step in. Only a few of
them matter.

The teacher's choice of moments picks those out, for example every time someone reaches for a worksheet. It says
where the teacher watches, not what they do there. A moment it does not pick goes unwatched,
even when a pupil reaches for something forbidden in it.

[Who is who](/instructor/who-is-who)
:::

## What it is

A pointcut is a predicate over join points. It selects them by signature, by type, by
annotation, or by combinations of those.

A pointcut is only as good as its coverage, and this is the central weakness of the
call-site approach: the pointcut is effectively an enumerated list of call sites the weaver
could find and rewrite. An operation reachable by a route the pointcut does not name is not
intercepted at all.

In Ares the pointcut definitions live alongside the advice under
[`api/aop`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/aop), and the instrumentation backend uses matchers over types and
methods for the same purpose.

## Further reading

- [AspectJ Documentation and Resources](https://eclipse.dev/aspectj/doc/latest/index.html) — Eclipse Foundation
- [Intro to AspectJ](https://www.baeldung.com/aspectj) — Baeldung
- [Understanding Aspect-Oriented Programming: Java and AspectJ](https://aayush-shrivastav.medium.com/understanding-aspect-oriented-programming-java-and-aspectj-4da6dd0e4157) — Aayush Shrivastav, Medium (freely readable)
