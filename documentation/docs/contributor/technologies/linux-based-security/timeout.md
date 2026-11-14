---
title: "Timeout"
sidebar_position: 4
description: "Bounding wall-clock execution time from outside the supervised process."
---

:::tip[Simple Story]
A pupil who never stops never finishes and never fails. They sit there forever.

The special examination room has a clock out of the pupil's reach. When the time is up, it stops
them, even when they are stuck and cannot stop themselves. Today that clock stops nobody. The
teacher's stopwatch for one question is what limits each question now. This page tells how an
earlier version of the room worked.

[Who is who](/instructor/who-is-who)
:::

:::note[Historical design]
Where this page names a Phobos layer or script, it describes the scripts Ares used to bundle,
which it no longer ships. Current Phobos works differently; see the
[Phobos documentation](https://ls1intum.github.io/phobos/).
:::

## What it is

`timeout` runs a command with a time limit and sends it a signal when the limit is
exceeded. It is part of GNU coreutils and needs no special privileges.

## How Phobos uses it

`phobos-timeout.sh` is the outermost of the three layers, applying the budget from
`regardingTimeouts` in the policy. It can be disabled with `--no-timeout`, as each layer
can be disabled independently.

The JVM-side `@StrictTimeout` bounds an individual test from inside the Java Virtual Machine (JVM). This layer
bounds the whole supervised command from outside it, and therefore still applies when the
JVM itself is wedged.

It is the simplest illustration of the [wrapper](./base-idea/wrapper.md) idea: enforcement
in a process that outlives the thing it supervises.

## Further reading

- [`timeout(1)`](https://man7.org/linux/man-pages/man1/timeout.1.html) — Linux manual page
- [`java.lang.Thread`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html) — Oracle, Java SE 21 application programming interface (API)
- [Java Threads](https://www.w3schools.com/java/java_threads.asp) — W3Schools
