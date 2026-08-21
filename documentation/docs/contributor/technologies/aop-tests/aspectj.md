---
title: "AspectJ"
sidebar_position: 2
description: "Compile-time weaving with AspectJ, and the consequences of intercepting at the call site."
---

:::tip[ELI5]
AspectJ edits the program's compiled code so that your check sits at every place a
forbidden operation is *written*.

That works well for code you compiled. It cannot help where the call is written somewhere
you never compiled, such as deep inside Java itself.
:::

## What it is

AspectJ is the aspect-oriented extension of Java: a language for aspects, plus a compiler
and a weaver that place advice into bytecode.

## How Ares 2 uses it

AspectJ is one of the two runtime enforcement backends, selected by the `ASPECTJ` half of
the [programming language configuration](/contributor/policy/programming-language-configuration).
The Ares JAR goes on the aspect path so its aspects are woven into the exercise.

## What follows from call-site weaving

AspectJ intercepts at the **call**, not at the target. It rewrites the place where the call
is written, which means coverage is an enumerated list of call sites the weaver could see:

- a call made from inside the Java Development Kit (JDK) has no woven call site;
- a reflective or library-mediated call reaches the target without passing a woven site;
- coverage grows only by naming more call sites.

The repository documents this in detail, including the one case where AspectJ is *stronger*
than instrumentation, in
[AspectJ versus Instrumentation](/contributor/subsystems/aop/aspectj-vs-instrumentation-weaknesses).

## Further reading

- [AspectJ Documentation and Resources](https://eclipse.dev/aspectj/doc/latest/index.html) — Eclipse Foundation
- [eclipse-aspectj/aspectj](https://github.com/eclipse-aspectj/aspectj) — source repository
- [Intro to AspectJ](https://www.baeldung.com/aspectj) — Baeldung
- [Comparing Spring AOP and AspectJ](https://www.baeldung.com/spring-aop-vs-aspectj) — Baeldung
- [Understanding Aspect-Oriented Programming: Java and AspectJ](https://aayush-shrivastav.medium.com/understanding-aspect-oriented-programming-java-and-aspectj-4da6dd0e4157) — Aayush Shrivastav, Medium (freely readable)
