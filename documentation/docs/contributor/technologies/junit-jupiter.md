---
title: "JUnit Jupiter"
sidebar_position: 1
description: "The JUnit 5 engine Ares 2 builds on, and the extension points it hooks into."
---

:::tip[Simple Story]
The teacher hands out one question at a time and marks each answer by the grading instructions.
They decide nothing about safety by those instructions. Clearing the desks and watching are jobs
they do by the rule sheet, a second sheet that sits beside the first.

When the teacher picks up the rule sheet for each question, they do it only for questions whose
answers they read out at once or keep back until the end. Any other question goes ahead with no
clearing and no watching, even with a rule sheet on the desk.

[Who is who](/instructor/who-is-who)
:::

## What it is

JUnit Jupiter is the programming and extension model of JUnit 5. A test is a method
annotated `@Test`; the Jupiter engine discovers it, runs it and reports the outcome.

What matters for Ares is the **extension model**. Jupiter lets a library hook into the
lifecycle of a test rather than wrapping it by hand: an extension can run before a test is
executed, decide that a test should not be executed at all, or supply a parameter to the
test method.

## How Ares 2 uses it

The [`api/jupiter`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/jupiter) package is the integration layer.

- `@Public` and `@Hidden` mark whether a test gives immediate feedback or waits for the
  deadline. `@PublicTest` and `@HiddenTest` are the combined forms that carry `@Test` as well.
- The deadline check runs **before** a hidden test executes, so a hidden test that is not
  yet due never runs, rather than running and having its result suppressed. That is the
  whole point: a test that runs can leak through a file or a static field.
- `@Policy` selects and configures the security policy, but does not activate anything on its
  own. The sandbox runs only when an Ares test annotation (`@Public`, `@Hidden`, `@PublicTest`,
  `@HiddenTest`) has registered the extensions, so a plain JUnit `@Test` carrying only `@Policy`
  runs unsupervised.
- Parameter resolution supplies `IOTester` to a test that declares it, which is how console
  interaction is tested without the test touching `System.in` directly.

## Further reading

- [JUnit 5 User Guide](https://docs.junit.org/current/user-guide/) — JUnit team
- [junit-team/junit-framework](https://github.com/junit-team/junit-framework) — source repository
- [A Guide to JUnit 5](https://www.baeldung.com/junit-5) — Baeldung
- [The Java Tutorials](https://docs.oracle.com/javase/tutorial/) — Oracle
- [Java Tutorial](https://www.w3schools.com/java/) — W3Schools
