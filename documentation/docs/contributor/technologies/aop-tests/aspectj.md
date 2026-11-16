---
title: "AspectJ"
sidebar_position: 2
description: "Compile-time weaving with AspectJ, and the consequences of intercepting at the call site."
---

:::tip[Simple Story]
This is one of the two ways the teacher watches by the rule sheet during the examination. Here the teacher
watches the pupils' hands, in the places of the pupils' work where they know to look.

They miss a reach that starts inside one of the permitted aids, or inside an aid the head
teacher put out. When such an aid reaches for something forbidden for the pupil, this way of
watching does not see it. Some roundabout reaches by the pupil escape it too.

[Who is who](/instructor/who-is-who)
:::

## What it is

AspectJ is the aspect-oriented programming (AOP) extension of Java: a language for aspects, plus a compiler
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
