---
title: "JUnit Jupiter"
sidebar_position: 1
description: "The JUnit 5 engine Ares 2 builds on, and the extension points it hooks into."
---

:::tip[Simple Story]
JUnit is the teacher. It decides what a question is, puts it to the pupil, and says whether the
answer was right.

Ares does not replace it. Ares is the checklist the teacher works **alongside**: JUnit still
owns the question and when it is asked, and Ares decides whether that desk may be visited at
all, and what the pupil may do while answering.
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
  deadline. `@PublicTest` and `@HiddenTest` are the combined forms that also carry `@Test`.
- The deadline check runs **before** a hidden test executes, so a hidden test that is not
  yet due never runs, rather than running and having its result suppressed. That is the
  whole point: a test that runs can leak through a file or a static field.
- `@Policy` attaches a security policy to a test, and the sandbox is active for that test.
- Parameter resolution supplies `IOTester` to a test that declares it, which is how console
  interaction is tested without the test touching `System.in` directly.

Ares also ships a jqwik integration for property-based tests; see [Jqwik](./jqwik.md).

## Further reading

- [JUnit 5 User Guide](https://docs.junit.org/current/user-guide/) — JUnit team
- [junit-team/junit-framework](https://github.com/junit-team/junit-framework) — source repository
- [A Guide to JUnit 5](https://www.baeldung.com/junit-5) — Baeldung
- [The Java Tutorials](https://docs.oracle.com/javase/tutorial/) — Oracle
- [Java Tutorial](https://www.w3schools.com/java/) — W3Schools
