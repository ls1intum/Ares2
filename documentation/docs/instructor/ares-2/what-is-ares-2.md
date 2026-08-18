---
title: "What is Ares 2"
sidebar_position: 1
description: "What Ares 2 is, what it is for, and how this documentation is organised."
---

:::tip[Simple Story]
When students submit programming homework, something has to run their code to see whether it
works. Running a stranger's code on your machine is exactly as risky as it sounds.

So picture an examination. The methods the student wrote are the pupils. The test framework,
JUnit or jqwik, is the teacher, putting the exercise's questions one at a time and marking each
answer on the spot. Ares is the checklist the teacher works through at every desk: what a pupil
may reach for, and what happens when they reach for anything else. The work still runs and
still gets marked, and what it may reach is what the checklist allows.
:::

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

:::warning[Read the basics before using Ares 2 in production]
Ares 2 provides a high level of security, and that comes at the cost of usability. Several
steps are needed to make tests work properly, and it takes some time to understand what Ares 2
does. Please read at least [Setup](../protect-a-java-project/setup.md), [Test Annotations](../protect-a-java-project/test-annotations.md) and
[Security Overview](what-does-ares-2-protect-against.md) before using Ares 2 for a real exercise.
:::

## Where to go next

| If you want to | Read |
| --- | --- |
| Add Ares 2 to a project | [Installation](../protect-a-java-project/installation.md) |
| Understand the public/hidden test model | [Setup](../protect-a-java-project/setup.md) and [Test Annotations](../protect-a-java-project/test-annotations.md) |
| Restrict what student code may do | [Security Overview](what-does-ares-2-protect-against.md) |
| Write a policy file | [Policy Configuration](../protect-a-java-project/policy-configuration.md) and the [Security Policy Manual](/contributor/subsystems/policy/security-policy-manual) |
| Set up a complete exercise from scratch | [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md) |
| Migrate an Ares 1 exercise | [Converting an Ares 1 Exercise](../transform-ares-1-into-ares-2/index.md) |
| Understand how Ares 2 works internally | The [Developer Guide](/contributor/subsystems/package-overview) |

## Licence

Ares 2 is licensed under the MIT Licence. See
[LICENSE](https://github.com/ls1intum/Ares2/blob/main/LICENSE) for details.
