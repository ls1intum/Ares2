---
title: "Binding"
sidebar_position: 5
description: "How the arguments and context of the intercepted operation reach the advice."
---

:::tip[Simple Story]
Knowing that a pupil reaches for a worksheet is not enough. The teacher needs to know which
worksheet.

While watching during the examination, the teacher sees the actual worksheet in the pupil's
hand. That lets the rule sheet allow one worksheet and forbid another. Clearing the desks before
the start cannot tell the two apart. So once the rule sheet allows anything with worksheets,
only the watching still covers them.

[Who is who](/instructor/who-is-who)
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
