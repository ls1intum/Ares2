---
title: "Jupiter"
sidebar_position: 2
description: "The JUnit Jupiter integration subsystem."
---

:::tip[Simple Story]
This is the piece that puts the checklist into the hands of the teacher who runs the
examination.

It is what makes a question public or hidden, what refuses to ask a hidden question before its
deadline, and what puts a desk under supervision when the question asks for it.
:::

## What it does

This is the primary test-framework binding. It is the package that defines the annotations an
exercise author writes, and it hooks into the JUnit Jupiter lifecycle so that Ares
gets a say before, around and after every test.

## What is in it

| Class | Purpose |
| --- | --- |
| `@Public` / `@Hidden` | Marks whether a test is public or hidden; a hidden test waits until its deadline and then reports only its status |
| `@PublicTest` / `@HiddenTest` | The same, combined with JUnit's own `@Test` |
| `JupiterAresTest` | Internal meta-annotation that registers all four extensions at once |
| `JupiterSecurityExtension` | Reads the `@Policy` configuration and applies the sandbox around each test and around the constructor and setup and teardown methods, resetting it afterwards. A phase that runs before any method policy is known needs a class `@Policy` |
| `JupiterTestGuard` | Applies the deadline check and removes hidden output and failure details |
| `JupiterIOExtension` | Redirects `System.in`, `System.out` and `System.err` |
| `JupiterStrictTimeoutExtension` | Enforces `@StrictTimeout` |
| `JupiterLocaleExtension` | Applies `@UseLocale` around a class |
| `JupiterContext` | Adapts JUnit's `ExtensionContext` to the Ares `TestContext` |
| `UnifiedInvocationInterceptor` | Collapses JUnit's many `InvocationInterceptor` callbacks into one generic method |

An Ares test annotation is what registers these extensions: `@PublicTest` and `@HiddenTest`, or
`@Public` and `@Hidden` combined with JUnit's `@Test`, all carry `JupiterAresTest`. `@Policy` only
selects and configures the policy, so a plain JUnit `@Test` that carries a `@Policy` but no Ares test
annotation runs unsupervised.

## Why it is shaped this way

`JupiterContext` is an adapter. It exists so that the logic behind the guards and the sandbox
is written against the framework-agnostic `TestContext` rather than against JUnit, which keeps that logic independent of the test framework.

The deadline check runs in the guard **before** the test body executes. Before that deadline,
Ares skips the body and shows the scheduling message. After the deadline, a hidden failure
keeps its grading status, but Ares withholds its failure reason, stack and console output.

Sometimes JUnit constructs a test and runs setup before Ares checks its method deadline. Ares hides
output from these phases. A failure in either phase produces a failed result with no details instead
of the scheduling message; Ares still skips the test body.

## Further reading

- [Package Overview](./package-overview.md) — every package in one place
- [JUnit Jupiter](/contributor/technologies/junit-jupiter) — the framework itself
