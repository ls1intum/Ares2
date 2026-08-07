---
title: "Timeout"
sidebar_position: 4
description: "Bounding wall-clock execution time from outside the supervised process."
---

:::tip[ELI5]
An endless loop never finishes and never fails. It just sits there.

So something outside the program holds a stopwatch, and when the time is up it stops the
program. Outside matters: a program stuck in a loop cannot be trusted to time itself.
:::

## What it is

`timeout` runs a command with a time limit and sends it a signal when the limit is
exceeded. It is part of GNU coreutils and needs no special privileges.

## How Phobos uses it

`phobos-timeout.sh` is the outermost of the three layers, applying the budget from
`regardingTimeouts` in the policy. It can be disabled with `--no-timeout`, as each layer
can be disabled independently.

The JVM-side `@StrictTimeout` bounds an individual test from inside the JVM. This layer
bounds the whole supervised command from outside it, and therefore still applies when the
JVM itself is wedged.

It is the simplest illustration of the [wrapper](./base-idea/wrapper.md) idea: enforcement
in a process that outlives the thing it supervises.

## Further reading

- [`timeout(1)`](https://man7.org/linux/man-pages/man1/timeout.1.html) — Linux manual page
- [`java.lang.Thread`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html) — Oracle, Java SE 21 API
- [Java Threads](https://www.w3schools.com/java/java_threads.asp) — W3Schools
