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
| `@Public` / `@Hidden` | Marks whether a test's result is shown immediately or held until the deadline |
| `@PublicTest` / `@HiddenTest` | The same, combined with JUnit's own `@Test` |
| `JupiterAresTest` | Internal meta-annotation that registers all four extensions at once |
| `JupiterSecurityExtension` | Reads `@Policy`, activates the sandbox, and resets it before and after each test |
| `JupiterTestGuard` | Applies the pre- and post-test guards, including the deadline check |
| `JupiterIOExtension` | Redirects `System.in`, `System.out` and `System.err` |
| `JupiterStrictTimeoutExtension` | Enforces `@StrictTimeout` |
| `JupiterLocaleExtension` | Applies `@UseLocale` around a class |
| `JupiterContext` | Adapts JUnit's `ExtensionContext` to the Ares `TestContext` |
| `UnifiedInvocationInterceptor` | Collapses JUnit's many `InvocationInterceptor` callbacks into one generic method |

## Why it is shaped this way

`JupiterContext` is an adapter. It exists so that the logic behind the guards and the sandbox
is written against the framework-agnostic `TestContext` rather than against JUnit, which is
what allows the [jqwik integration](./jqwik.md) to reuse it instead of duplicating it.

The deadline check runs in the guard **before** the test body executes. A hidden test that is
not yet due never runs at all, rather than running and having its result suppressed, because a
test that runs can leave traces in a file or a static field.

## Further reading

- [Package Overview](./package-overview.md) — every package in one place
- [JUnit Jupiter](/contributor/technologies/junit-jupiter) — the framework itself
