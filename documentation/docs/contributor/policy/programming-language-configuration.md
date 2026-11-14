---
title: "Programming language configuration"
sidebar_position: 1
description: "How the configuration value is dispatched into a director, an analyser and a weaving mechanism."
---

:::tip[Simple Story]
One line on the rule sheet picks the language the pupils answer in, the kind of school, and
which of two ways the teacher clears the desks and watches. It allows nothing by itself. Every
other line says what a pupil can do; this one says who checks it.

The way of watching does not decide how the rules reach the teacher. The head teacher can print
the rules onto the grading instructions beforehand, or the teacher can pick up the rule sheet
for each question, with either way of watching.

[Who is who](/instructor/who-is-who)
:::

For the fields an exercise author writes, see
[Programming language configuration](/instructor/policy-reference/programming-language-configuration) in the instructor guide. This page is
about how the domain is enforced.

## Model

`ProgrammingLanguageConfiguration` is an enum, not a record: eight values, each naming a
language, a build tool, an architecture analyser and a weaving mechanism. It is the only
policy field that selects code paths rather than permissions.

## Validation and normalisation

The value is parsed by the reader and must match an enum constant exactly. `getNameRules()`
maps every Java value onto `JavaNameRules.INSTANCE`, which is what makes the enum extensible
to a second language without touching the reader.

## What it generates

Nothing directly. It selects the director, and the director builds the
`TestCaseAbstractFactoryAndBuilder` that generates everything else.

## Static enforcement

Chooses between ArchUnit and T. J. Watson Libraries for Analysis (WALA) for every architecture test case in the run. The two are
meant to reach the same verdicts; a disagreement between them is a finding, not a tuning
parameter.

## Runtime enforcement

Chooses between AspectJ weaving and Byte Buddy instrumentation. Note that this axis is
**independent** of Precompile and Postcompile: `examples/ares-exercise-gradle` runs an
`_ASPECTJ` configuration in Postcompile, and the Precompile generator can emit
instrumentation.

## Where the code lives

- `policy/policySubComponents/ProgrammingLanguageConfiguration.java`, the enum and its name rules
- `policy/director/SecurityPolicyDirector.selectSecurityPolicyDirector`, which dispatches on it
- `policy/director/java/SecurityPolicyJavaDirector.generateFactoryAndBuilder`, which turns the
  value into a `BuildMode`, an `ArchitectureMode` and an `AOPMode`

## Known gaps

With no policy at all there is no explicitly selected build tool, so a project carrying both a
`pom.xml` and a `build.gradle` is rejected during discovery rather than guessed at.
