---
title: "How can you contribute"
sidebar_position: 1
description: "How to get involved with Ares 2: the workflow, the build, and what a good pull request looks like."
---

:::tip[Simple Story]
Ares is the checklist. Change it carelessly and one of two things happens: a pupil gets away
with something, or an honest pupil is failed for nothing.

Neither is a cosmetic bug, which is why the rules for changing Ares are stricter than for an
ordinary library. Every change to enforcement has to prove both halves: the permitted thing
still permitted,
the forbidden thing still refused. This page is the short version of those rules.
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
