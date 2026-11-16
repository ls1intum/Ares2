---
title: "Resource limits"
sidebar_position: 8
description: "How time and memory budgets are generated, which mechanism enforces them, and which one does not."
---

:::tip[Simple Story]
The rule sheet can set a time limit. Only the clock of the special examination room is meant to
keep it, and nobody else in the school looks at it.

The trap: when the teacher picks up the rule sheet for each question, nobody uses the room, so
the time limit bounds nothing. The teacher's stopwatch is what limits a question today.

[Who is who](/instructor/who-is-who)
:::

For the fields an exercise author writes, see
[Resource limits](/instructor/policy-reference/resource-limits) in the instructor guide. This page is
about how the domain is enforced.

## Model

`ResourceLimitsPermission`, carrying the time and memory budgets as a map of named limits.

## Validation and normalisation

`JavaResourceLimitsExtractor.collectResourceLimits` normalises the declared limits into
that map. Unit handling here has been a source of defects, so check the current parser
rather than assuming.

## What it generates

Phobos test cases only. `JavaPhobosTestCase.writePhobosSecurityTestCaseFile` turns the map
into the `limits` section of `SpecificExercise.cfg`, as seconds with three decimals, for the
Phobos runtime maintained in [ls1intum/phobos](https://github.com/ls1intum/phobos).

## Static enforcement

None.

## Runtime enforcement

Out of process only. The Phobos timeout layer wraps the build command with coreutils
`timeout`. Nothing inside the Java Virtual Machine (JVM) consults these values.

## Where the code lives

- `policy/policySubComponents/ResourceLimitsPermission.java`
- `phobos/JavaPhobosTestCase.writePhobosSecurityTestCaseFile`, the `limits` section
- `templates/phobos/PhobosCopyTool.sh`, copied into the exercise
- the timeout layer itself, in [ls1intum/phobos](https://github.com/ls1intum/phobos)

## Known gaps

**A policy timeout does not bound a Postcompile test today.** `JavaTestCaseFactoryAndBuilder.writeTestCases` passes the Phobos cases to the writer, but
`executeTestCases` passes only the architecture and aspect-oriented programming (AOP) cases to the executer, so the limits
are parsed, validated and then never dispatched. This is a pending migration
rather than a defect. `@StrictTimeout` is the mechanism that bounds a test.
