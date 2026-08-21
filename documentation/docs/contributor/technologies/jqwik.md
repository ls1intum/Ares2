---
title: "Jqwik"
sidebar_position: 2
description: "Property-based testing on the JUnit platform, and how Ares 2 supervises it."
---

:::tip[ELI5]
An ordinary test checks one example: "if I pass 5, I should get 25".

A property test instead states a rule that should hold for *every* input, and then the
framework invents hundreds of inputs trying to break it. When it finds one that fails, it
shrinks it down to the smallest example that still fails, so you get "it breaks on 0"
rather than "it breaks on -2147483648".
:::

## What it is

jqwik is a property-based testing engine that runs on the JUnit platform. Instead of
`@Test` with fixed inputs, a `@Property` method declares a rule and jqwik generates inputs
to try to falsify it, then *shrinks* any failing input to a minimal counterexample.

It is a JUnit platform engine rather than a JUnit Jupiter extension, so it discovers
and runs its tests itself. An integration that only hooked into Jupiter would therefore
miss every property test.

## How Ares 2 uses it

The [`api/jqwik`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/jqwik) package mirrors the Jupiter integration for jqwik's own
lifecycle, so a property test gets the same treatment as an ordinary test: the public and
hidden distinction, the deadline check before a hidden property runs, and the active
security policy.

This is why the annotations are duplicated per engine. A test class must import the
`jupiter` annotations or the `jqwik` ones to match the engine that will actually run it;
mixing them silently produces a test that is not supervised.

:::warning[Import the annotations that match your engine]
`de.tum.cit.ase.ares.api.jupiter.*` for `@Test`, `de.tum.cit.ase.ares.api.jqwik.*` for
`@Property`. This is the single most common integration mistake.
:::

## Further reading

- [jqwik User Guide](https://jqwik.net/docs/current/user-guide.html) — jqwik team
- [jqwik-team/jqwik](https://github.com/jqwik-team/jqwik) — source repository
- [JUnit 5 User Guide](https://docs.junit.org/current/user-guide/) — JUnit team
