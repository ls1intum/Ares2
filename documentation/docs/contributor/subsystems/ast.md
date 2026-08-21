---
title: "AST"
sidebar_position: 8
description: "The abstract syntax tree analysis subsystem."
---

:::tip[Simple Story]
Everything else in Ares asks what a pupil is allowed to **do**.

This part asks how they **answered**. Did they actually use a loop? Is there recursion? That is
a mark-scheme question rather than a checklist one, and it is settled by reading the source as
a tree of sentences rather than by watching the pupil work.
:::

## What it does

Every other part of Ares asks what a program is permitted to **do** at runtime. This package
asks how it was **written**, by parsing the source with JavaParser and inspecting the resulting
syntax tree.

That answers a different kind of exercise requirement: *did the student actually use
recursion*, rather than *may this code open that file*.

## What is in it

| Sub-package | Responsibility |
| --- | --- |
| `asserting/` | The fluent, AssertJ-style assertions: `UnwantedNodesAssert`, `UnwantedRecursionAssert` |
| `model/` | The representation: `JavaFile`, `RecursionCheck`, `MethodCallGraph` |
| `type/` | The categories a rule can name: `LoopType`, `ConditionalType`, `ExceptionHandlingType`, `ClassType` |

The assertions read as a specification of what should be **absent** from a submission, which
suits the usual requirement ("solve this without a loop") better than asserting presence.

:::warning[Not a security boundary]
This layer analyses source, so it can only see code it has the source for, and it says nothing
about what happens at runtime. It answers questions about style and structure. The sandbox is
the [aspect-oriented programming (AOP)](./aop/block-file-system-access.md) and
[architecture](./architecture/block-file-system-access.md) layers.
:::

## Further reading

- [Package Overview](./package-overview.md) — every package in one place
- [Abstract Syntax Tree Tests](/contributor/technologies/ast-tests/javaparser) — the technique
