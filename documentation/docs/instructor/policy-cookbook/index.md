---
title: "Policy Cookbook"
sidebar_position: 1
description: "Short, complete recipes for the policy situations that come up most often when writing an exercise."
---

:::tip[Simple Story]
The reference pages explain each line of the rule sheet. These recipes start from an
exercise instead: one thing a pupil has to be allowed to do, and the least the head teacher
can write on the rule sheet to allow it.

Allowing more than the exercise needs is a mistake, even when the teacher marks every answer
right. Nobody has written the recipes yet.

[Who is who](/instructor/who-is-who)
:::

## How a recipe is built

Each recipe states the situation, gives a complete `security-policy.yaml` fragment that can be
pasted in, names the narrowest permission that solves it, and says what stays forbidden. Where a
recipe has a common wrong version that looks equivalent but grants far more, the recipe shows
that too.

## The recipes

| Page | Status | Intended purpose |
| --- | --- | --- |
| [Reading a file from resources](./reading-a-file-from-resources.md) | Planned | Permit one classpath resource read-only, without opening the working directory |
| [Writing an output file](./writing-an-output-file.md) | Planned | Permit creation and overwrite of one output path, without granting read access |
| [Using threads](./using-threads.md) | Planned | Permit a bounded number of threads and say what the limit does and does not cover |
| [Allowing exactly one host](./allowing-exactly-one-host.md) | Planned | Permit one host and port, and why a wildcard host is almost never what you want |
| [Using a library that reflects](./using-a-library-that-reflects.md) | Planned | Keep a reflective dependency working without disabling the reflection rules wholesale |
| [Setting time and memory budgets](./setting-time-and-memory-budgets.md) | Planned | Resource limits, which mode enforces them today, and what to use in the meantime |

## Notes

- Every recipe assumes the narrowest permission that works. A policy is a security boundary, so
  a recipe that grants more than the exercise needs is a defect even when the tests pass.
