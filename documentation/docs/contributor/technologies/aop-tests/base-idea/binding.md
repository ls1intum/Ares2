---
title: "Binding"
sidebar_position: 5
description: "How the arguments and context of the intercepted operation reach the advice."
---

:::tip[ELI5]
Knowing that a file is about to be opened is not enough. You need to know **which** file.

Binding is how that information is handed to your check. Without it you could only ever
answer yes or no to "may this program open files at all", never "may it open *this* one".
:::

## What it is

Binding passes the context of the join point into the advice: the arguments, the target
object, the signature of the intercepted member.

Binding is exactly what the architecture layer lacks. Static analysis is
argument-insensitive: it sees that a file-opening method is reachable, not which path is
passed. The AOP layer has the actual argument at the moment of the call, which is why a
policy can permit `something.txt` and deny `secret.txt` and have that distinction mean
something at runtime.

It is also why a narrow allowance stays narrow even though the static layer cannot represent
it.

## Further reading

- [AspectJ Documentation and Resources](https://eclipse.dev/aspectj/doc/latest/index.html) — Eclipse Foundation
- [Intro to AspectJ](https://www.baeldung.com/aspectj) — Baeldung
- [`java.lang.instrument`](https://docs.oracle.com/en/java/javase/21/docs/api/java.instrument/java/lang/instrument/package-summary.html) — Oracle, Java SE 21 API
