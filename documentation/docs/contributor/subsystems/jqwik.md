---
title: "Jqwik"
sidebar_position: 2
description: "The jqwik integration subsystem."
---

:::tip[ELI5]
This is the piece that lets Ares supervise property tests as well as ordinary ones.

Property tests are run by their own engine rather than by the usual one, so without this
piece they would slip past every check Ares makes.
:::

## What it does

This package does for jqwik what the [jupiter package](./jupiter.md) does for JUnit Jupiter.
It mirrors that structure deliberately, but binds to jqwik's own lifecycle hooks.

## Why it has to exist separately

jqwik is a JUnit **platform** engine, not a Jupiter extension. It discovers and runs its
property tests itself, so an integration that only hooked into Jupiter would never see them.
Without this package, a `@Property` test would run entirely unsupervised.

## What is in it

| Class | Purpose |
| --- | --- |
| `JqwikAresTest` | Meta-annotation registering all jqwik lifecycle hooks |
| `@Public` / `@Hidden` | Test visibility, as in the Jupiter integration |
| `JqwikSecurityExtension` | Activates and tears down the sandbox |
| `JqwikTestGuard` | Pre- and post-condition guard, including the deadline |
| `JqwikIOExtension` | Console IO redirection |
| `JqwikStrictTimeoutExtension` | Timeout enforcement |
| `JqwikLocaleExtension` | Applies `@UseLocale` around a container |
| `JqwikContext` | Adapter from jqwik's lifecycle context to the Ares `TestContext` |

:::warning[Import the annotations that match your engine]
The annotations are duplicated per engine on purpose. A test class must import
`de.tum.cit.ase.ares.api.jqwik.*` for `@Property` and `de.tum.cit.ase.ares.api.jupiter.*` for
`@Test`. Mixing them produces a test that compiles, runs, and is not supervised.
:::

## Further reading

- [Package Overview](./package-overview.md) — every package in one place
- [Jqwik](/contributor/technologies/jqwik) — the engine itself
