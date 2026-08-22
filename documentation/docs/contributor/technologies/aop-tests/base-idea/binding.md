---
title: "Binding"
sidebar_position: 5
description: "How the arguments and context of the intercepted operation reach the advice."
---

:::tip[Simple Story]
Knowing that a paper is about to be picked up is not enough. You need to know **which** paper.

Binding is how that gets handed to the teacher. Without it you could only ever answer yes or no
to "may this pupil pick up papers at all", never "may they pick up *this* one".
:::

## What it is

Binding passes the context of the join point into the advice: the arguments, the target
object, the signature of the intercepted member.

Binding is exactly what the architecture layer lacks. Static analysis is
argument-insensitive: it sees that a file-opening method is reachable, not which path is
passed. The aspect-oriented programming (AOP) layer has the actual argument at the moment of the call, which is why a
policy can permit `something.txt` and deny `secret.txt` and have that distinction mean
something at runtime.

It is why a narrow allowance stays narrow even though the static layer cannot represent
it.

## Further reading

- [AspectJ Documentation and Resources](https://eclipse.dev/aspectj/doc/latest/index.html) — Eclipse Foundation
- [Intro to AspectJ](https://www.baeldung.com/aspectj) — Baeldung
- [`java.lang.instrument`](https://docs.oracle.com/en/java/javase/21/docs/api/java.instrument/java/lang/instrument/package-summary.html) — Oracle, Java SE 21 application programming interface (API)
