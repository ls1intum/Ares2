---
title: "New enforcement mechanism"
sidebar_position: 6
description: "Adding a weaving or sandboxing mechanism alongside AspectJ, instrumentation and Phobos."
---

:::tip[Simple Story]
Today the teacher has two ways of watching during the examination, and the school has a special
examination room. Nobody uses that room when the teacher picks up the rule sheet for each
question. This page is about a fourth way to stop a pupil.

It has to say which things it covers, and it must not leave any of them half done. A way of
watching that a pupil can switch off from their own desk stops nobody. This page is still an
outline; nobody has written it out.

[Who is who](/instructor/who-is-who)
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
