---
title: "Extending Ares 2"
sidebar_position: 1
description: "The five extension points of Ares 2, what each one requires, and which pages are written."
---

:::tip[Simple Story]
Ares 2 is built so that the next language, build tool or analysis technique can join without
rewriting the parts that already work.

This section says where those seams are, so a new kind of examination can be run in the same
school.
:::

## The five extension points

Each corresponds to one axis of the `ProgrammingLanguageConfiguration` enum or to one layer of
the enforcement pipeline.

| Page | Status | Intended purpose |
| --- | --- | --- |
| [New programming languages](./new-programming-languages.md) | Planned | Add a language beyond Java: reader, director, name rules, test-case factory |
| [New build tools](./new-build-tools.md) | Planned | Add a build tool beyond Maven and Gradle: `BuildMode`, descriptor detection, generated wiring |
| [New policy domains](./new-policy-domains.md) | Planned | Add a permission domain beyond the current eight, end to end |
| [New analysis technology](./new-analysis-technology.md) | Planned | Add a static analyser alongside ArchUnit and T. J. Watson Libraries for Analysis (WALA) |
| [New enforcement mechanism](./new-enforcement-mechanism.md) | Planned | Add a weaving or sandboxing mechanism alongside AspectJ, instrumentation and Phobos |

## How to read this section

Every page follows the same shape: which enum or interface you extend, which files the change
touches, which of the four continuous integration (CI) combinations must be re-run, and what a complete change looks like
when it is finished. None of them is a substitute for reading the
[subsystem pages](../subsystems/package-overview.md) first.

## Notes

- Adding a domain or a mechanism means adding to a **security boundary**. A partial extension
  that is silently ignored by one enforcement layer is worse than no extension at all, because
  the policy then claims a guarantee the code does not deliver.
