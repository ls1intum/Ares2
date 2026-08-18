---
title: "New enforcement mechanism"
sidebar_position: 6
description: "Adding a weaving or sandboxing mechanism alongside AspectJ, instrumentation and Phobos."
---

:::tip[Simple Story]
Once the checklist says a thing is forbidden, somebody has to actually stop it while the pupil
is working.

Ares has three ways of doing that. This is what a fourth would have to provide.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written.
:::

## The three that exist

AspectJ weaving at compile time, Byte Buddy instrumentation at runtime, and the Phobos
out-of-process sandbox. Their trade-offs are compared under
[AspectJ versus instrumentation weaknesses](../subsystems/aop/aspectj-vs-instrumentation-weaknesses.md).

## Where the mechanism is decided

`AOPMode` for the in-JVM mechanisms, and the Phobos wrapper for the out-of-process one. Note that
this axis is independent of Precompile and Postcompile.

## The settings channel

How a mechanism receives its configuration, and the ordering constraint: allow-lists first,
`restrictedPackage` last, so that no supervised class observes a partially armed policy.

## The advice contract

What an advice must do on interception: which toolbox it consults, what it throws, and how the
message is localised.

## Coverage obligations

Which domains the mechanism must cover before it may be offered in a
`ProgrammingLanguageConfiguration`, and how a partial mechanism is declared.

## Verification

All four existing continuous integration (CI) combinations remain green, and the new mechanism ships its own positive and
negative controls.

## Notes

- A mechanism that can be disabled from inside the supervised code is not an enforcement
  mechanism. Establish the trust boundary before writing any advice.
