---
title: "New programming languages"
sidebar_position: 2
description: "Adding a language beyond Java: the reader, the director, the name rules and the test-case factory."
---

:::tip[Simple Story]
Today every pupil answers in one language. This page is about pupils who answer in another one.

The rule sheet stays the same, and so does the reading of it. What changes is everything that
looks at the pupils' work: turning the rule sheet into checks, clearing the desks and watching.
A language that reaches only some of them gives a rule sheet that promises more than anyone
keeps. This page is still an outline; nobody has written it out.

[Who is who](/instructor/who-is-who)
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written.
:::

## Where the language is decided

`ProgrammingLanguageConfiguration` and how `SecurityPolicyDirector.selectSecurityPolicyDirector`
dispatches on it.

## What a language must supply

The director, the name rules, the project scanner, the essential-packages and essential-classes
data, and the test-case factory and builder.

## What can be reused

The policy model, the reader, the enforcement model and the `TestCaseAbstractFactoryAndBuilder`
contract are language independent and must not be forked.

## Precompile and Postcompile obligations

Which of the two modes a new language must support, and what "supported" means for each: the
copy and edit configuration for Precompile, the runtime settings channel for Postcompile.

## Verification

Which of the four continuous integration (CI) combinations apply, and the positive and negative controls a new language
must ship.

## Notes

- A language added to the enum but not wired into every enforcement layer produces a policy that
  claims a guarantee the code does not deliver.
