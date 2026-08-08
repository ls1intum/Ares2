---
title: "Policy Cookbook"
sidebar_position: 1
description: "Short, complete recipes for the policy situations that come up most often when writing an exercise."
---

:::tip[ELI5]
The [Policy Reference](/instructor/policy-reference/) tells you what every field means.

This section tells you what to write when you have an actual exercise in front of you and a
concrete thing the student's code needs to be allowed to do.
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
