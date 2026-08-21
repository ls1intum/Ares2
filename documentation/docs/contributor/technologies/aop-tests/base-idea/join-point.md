---
title: "Join point"
sidebar_position: 2
description: "A point in the execution of the program where behaviour can be attached."
---

:::tip[ELI5]
A join point is a moment during the run where you could interrupt: this method is about to be
called, this constructor is about to run.

It is a *candidate* moment, not a chosen one. Choosing which of them you actually care about
is a separate step.
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
