---
title: "Advice"
sidebar_position: 4
description: "The code that runs at a matched join point, and when it runs relative to it."
---

:::tip ELI5
Advice is what actually happens at the moment you interrupted.

You can act just before, just after, or wrap yourself around the moment so that you decide
whether the original thing happens at all. For a security check, that last one is the
important power: you can refuse.
:::

## What it is

Advice is the behaviour attached to a pointcut. The usual kinds are *before*, *after* (further
split by normal return or thrown exception) and *around*, which surrounds the join point and
controls whether the original code proceeds.

In Ares the advice is where a denial is produced. It reads the intended operation and its
arguments, compares them against the active policy, and either lets the call through or
throws a `SecurityException` naming what was attempted and what blocked it.

The advice classes are on the bootstrap append path rather than the ordinary classpath. They
have to be loadable from code that itself loads very early, and they must not be shadowable
by student code.

## Further reading

- [AspectJ Documentation and Resources](https://eclipse.dev/aspectj/doc/latest/index.html) — Eclipse Foundation
- [Intro to AspectJ](https://www.baeldung.com/aspectj) — Baeldung
- [Understanding Aspect-Oriented Programming: Java and AspectJ](https://aayush-shrivastav.medium.com/understanding-aspect-oriented-programming-java-and-aspectj-4da6dd0e4157) — Aayush Shrivastav, Medium (freely readable)
