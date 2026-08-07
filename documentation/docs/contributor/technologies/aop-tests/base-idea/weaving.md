---
title: "Weaving"
sidebar_position: 6
description: "How advice is placed into the bytecode: at compile time, at load time or at runtime."
---

:::tip ELI5
At some point the checking code has to actually get *into* the program.

That can happen when the program is compiled, or when each class is loaded into memory, or
while it is already running. Later is more powerful, because by then you can reach code you
never had the source for.
:::

## What it is

Weaving is the act of inserting advice at matched join points. It can happen:

- at **compile time**, by a special compiler that emits already-woven bytecode;
- at **load time**, by an agent that rewrites each class as the JVM loads it;
- at **runtime**, by redefining classes that are already loaded.

The two backends Ares supports sit at different points on that scale.
[AspectJ](../aspectj.md) weaves the call sites it can see. Load-time
[instrumentation](../instrumentation-with-bytebuddy.md) rewrites classes as they are loaded,
including classes Ares never compiled.

This difference is the reason the two backends are not interchangeable in their guarantees,
and the reason CI runs both.

## Further reading

- [AspectJ Documentation and Resources](https://eclipse.dev/aspectj/doc/latest/index.html) — Eclipse Foundation
- [Comparing Spring AOP and AspectJ](https://www.baeldung.com/spring-aop-vs-aspectj) — Baeldung
- [Applying "Java Aspects" at Load Time: Java Instrumentation API](https://medium.com/javarevisited/applying-java-aspects-at-load-time-java-instrumentation-api-232b39622b32) — Hrishabh Purohit, Medium (freely readable)
- [Class Loaders in Java](https://www.baeldung.com/java-classloaders) — Baeldung
