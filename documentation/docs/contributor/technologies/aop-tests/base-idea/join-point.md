---
title: "Join point"
sidebar_position: 2
description: "A point in the execution of the program where behaviour can be attached."
---

:::tip[Simple Story]
This page is about a moment in the examination where the teacher could step in: a pupil is about
to pick up a worksheet, or about to start a side job.

It is only a moment the teacher could use. Which moments they watch is a separate choice. With
one way of watching, the moment sits where the pupil's own work reaches out. With the other, it
sits at the worksheet itself.

[Who is who](/instructor/who-is-who)
:::

## What it is

A join point is a well-defined point in program execution: a method call, a method execution,
a constructor call, a field access. The set of join points a system can express is its join
point model, and it differs between implementations.

The distinction that matters most in Ares is **call** versus **execution**:

- a *call* join point is at the caller, where the call is written;
- an *execution* join point is at the callee, inside the method being run.

This is not a detail. AspectJ weaves at call sites, which means it can only intercept calls it
can see and rewrite. A call made from inside the Java Development Kit (JDK), or reached reflectively, has no woven
call site. See
[AspectJ versus Instrumentation](/contributor/subsystems/aop/aspectj-vs-instrumentation-weaknesses)
for what follows from that.

## Further reading

- [AspectJ Documentation and Resources](https://eclipse.dev/aspectj/doc/latest/index.html) — Eclipse Foundation
- [Intro to AspectJ](https://www.baeldung.com/aspectj) — Baeldung
- [Understanding Aspect-Oriented Programming: Java and AspectJ](https://aayush-shrivastav.medium.com/understanding-aspect-oriented-programming-java-and-aspectj-4da6dd0e4157) — Aayush Shrivastav, Medium (freely readable)
