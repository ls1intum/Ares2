---
title: "ArchUnit"
sidebar_position: 2
description: "Static dependency analysis over imported bytecode: what ArchUnit sees, and what it cannot."
---

:::tip[Simple Story]
This is one of the two ways the teacher clears the desks before the start. They look at what
the pupils' work turned into, not at what the pupils wrote, and follow where each part leads.
None of it runs while they do this.

They can see that a pupil's work leads to the worksheets. They cannot see which worksheet,
because that only shows once the pupil is working. So once the rule sheet allows anything with
worksheets, the desk clearing stops covering them and only the watching remains.

[Who is who](/instructor/who-is-who)
:::

## What it is

ArchUnit is a Java library for asserting architectural rules. It imports compiled classes
into its own model and evaluates fluent rules over them, as ordinary unit tests.

## How Ares 2 uses it

ArchUnit is one of the two interchangeable engines behind the architecture layer, selected
by the `ARCHUNIT` half of the
[programming language configuration](/contributor/policy/programming-language-configuration).
It analyses **dependencies**: which types the student's classes refer to.

## What it cannot see

Dependency analysis is coarse. It establishes that student code refers to a file-writing
application programming interface (API); it does not establish which path is written, because the path is a runtime value.

That is not a defect, it is the division of labour. The architecture layer rejects what can
be decided statically, and the aspect-oriented programming (AOP) layer decides the rest at the moment of the call, where
the arguments exist. Neither layer is sufficient alone, which is why Ares runs both and continuous integration (CI)
exercises all four combinations.

## Further reading

- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html) — TNG
- [TNG/ArchUnit](https://github.com/TNG/ArchUnit) — source repository
- [Introduction to ArchUnit](https://www.baeldung.com/java-archunit-intro) — Baeldung
