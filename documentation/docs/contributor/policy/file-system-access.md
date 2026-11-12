---
title: "File system access"
sidebar_position: 3
description: "How read, overwrite, execute and delete permissions are enforced by the architecture layer, the advice and Phobos."
---

:::tip[Simple Story]
A pupil can be allowed to read, change, make, use or throw away a worksheet. Three things can
stop them: the teacher clearing the desks, the teacher watching, and the special examination
room. They do not all behave alike, and when the teacher picks up the rule sheet for each
question, nobody uses the room.

The trap: while the rule sheet allows no worksheet at all, the desk clearing stops every
attempt before the start, and the watching never gets its turn. Anyone who wants to see the
watching at work has to allow at least one worksheet first.

[Who is who](/instructor/who-is-who)
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
