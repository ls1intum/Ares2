---
title: "How can you contribute"
sidebar_position: 1
description: "How to get involved with Ares 2: the workflow, the build, and what a good pull request looks like."
---

:::tip[Simple Story]
Anyone who changes how the teacher clears the desks or watches the pupils can go wrong in two
ways. A pupil gets away with something forbidden, or an honest pupil fails for nothing.

So every such change shows both halves: what the rule sheet allows still works, and the teacher
still stops what it forbids. There are two ways of clearing the desks and two ways of watching.
A change has to hold in every pairing it touches. This page gives the short version of these
rules.

[Who is who](/instructor/who-is-who)
:::

Ares 2 is developed in the open at [ls1intum/Ares2](https://github.com/ls1intum/Ares2).

The full contribution guide lives in the repository at
[CONTRIBUTING.md](https://github.com/ls1intum/Ares2/blob/main/CONTRIBUTING.md). It covers the
identity expectations, the build and quality gates, how to run the tests, and the pull request
process.

A few things are worth repeating here, because they are specific to a security tool.

## Ares 2 is the boundary under test

A false negative lets forbidden student code through. A false positive fails a correct
submission. Both are serious, and neither is caught by a test that only checks the happy path.
Every change to enforcement needs a positive test (the allowed operation still works) **and** a
negative test (the forbidden operation is still rejected).

## Four mode combinations

Enforcement has two independent analysis layers and two independent weaving mechanisms, and continuous integration (CI)
exercises all four combinations: ArchUnit + AspectJ, ArchUnit + instrumentation, T. J. Watson Libraries for Analysis (WALA) + AspectJ
and WALA + instrumentation. A change to either layer is not verified until it has been checked
against every combination it can affect.

## Never start a server inside the sandbox

A sandboxed test Java Virtual Machine (JVM) must never spin up its own server to test a connection. Ares is the
boundary under test, so a fixture inside the same JVM is subject to the active policy and its
failure cannot be attributed. See
[AGENTS.md](https://github.com/ls1intum/Ares2/blob/main/AGENTS.md) for the full rule.
