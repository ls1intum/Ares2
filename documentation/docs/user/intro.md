---
title: "Introduction"
sidebar_position: 1
description: "What Ares 2 is, what it protects against, and how the documentation is organised."
---

Ares 2 is a framework for the easy and secure remote execution of student submissions on an
interactive learning platform. It is the second Java-based implementation of the Secure COder
Remote Execution (SCORE) framework, and the first to support Java 25 and later.

Its main features are:

- policy-based sandboxing (static analysis plus runtime instrumentation) to prevent unsafe
  operations and reduce cheating
- more robust tests and builds, through limits on time, threads and IO
- support for public and hidden Artemis tests, where hidden ones obey a custom deadline
- utilities for improved feedback in Artemis, such as processing multiline error messages or
  pointing at a likely location that caused an exception
- utilities for comfortably testing exercises that use `System.out` and `System.in`

:::warning Read the basics before using Ares 2 in production
Ares 2 provides a high level of security, and that comes at the cost of usability. Several
steps are needed to make tests work properly, and it takes some time to understand what Ares 2
does. Please read at least [Setup](./setup.md), [Test Annotations](./test-annotations.md) and
[Security Overview](./security/overview.md) before using Ares 2 for a real exercise.
:::

## Where to go next

| If you want to | Read |
| --- | --- |
| Add Ares 2 to a project | [Installation](./installation.md) |
| Understand the public/hidden test model | [Setup](./setup.md) and [Test Annotations](./test-annotations.md) |
| Restrict what student code may do | [Security Overview](./security/overview.md) |
| Write a policy file | [Policy Configuration](./security/policy-configuration.md) and the [Security Policy Manual](./security/policy-manual.md) |
| Set up a complete exercise from scratch | [Making a Project an Ares Project](./make-a-project-an-ares-project.md) |
| Migrate an Ares 1 exercise | [Converting an Ares 1 Exercise](./convert-ares1-to-ares2.md) |
| Understand how Ares 2 works internally | The [Developer Guide](/developer/overview) |

## Licence

Ares 2 is licensed under the MIT Licence. See
[LICENSE](https://github.com/ls1intum/Ares2/blob/main/LICENSE) for details.
