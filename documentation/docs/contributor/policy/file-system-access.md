---
title: "File system access"
sidebar_position: 3
description: "How read, overwrite, execute and delete permissions are enforced by the architecture layer, the advice and Phobos."
---

:::tip[Simple Story]
This is the part of the checklist with the most ways to reach it, so it carries the most
enforcement.

Three independent layers can each stop a pupil picking up a paper, and they do not all behave
alike.
:::

For the fields an exercise author writes, see
[File system access](/instructor/policy-reference/file-system-access) in the instructor guide. This page is
about how the domain is enforced.

## Model

`FilePermission`, one record per path, carrying the four booleans for read, overwrite, execute
and delete.

## Validation and normalisation

Paths are normalised before comparison. The distinction that matters is create versus
overwrite: the advice reserves "create" for APIs that specifically create a new file.

## What it generates

Architecture test cases, aspect-oriented programming (AOP) test cases and Phobos test cases. The Phobos path turns the
permissions into the `readonly` and `write` sections of `SpecificExercise.cfg`.

## Static enforcement

`JavaArchunitTestCaseCollection` for ArchUnit, or the T. J. Watson Libraries for Analysis (WALA) call-graph equivalent, matched
against `templates/architecture/java/archunit/methods/file-system-access-methods.txt`. A
domain with **no** allowance at all gets a static deny-all rule, which is why a negative
control needs at least one permitted file to exercise the runtime layer.

## Runtime enforcement

`JavaInstrumentationAdviceFileSystemToolbox`, or the AspectJ aspect for the same join points.

## Where the code lives

- `policy/policySubComponents/FilePermission.java`
- `aop/java/instrumentation/advice/JavaInstrumentationAdviceFileSystemToolbox.java`
- `architecture/java/archunit/JavaArchunitTestCaseCollection.java`
- `phobos/JavaPhobosTestCase.writePhobosSecurityTestCaseFile`, the `readonly` and `write` sections

## Known gaps

The Phobos sections are written but only enforced when the Phobos wrapper runs the build. In
Postcompile the Phobos cases are generated and never dispatched.
