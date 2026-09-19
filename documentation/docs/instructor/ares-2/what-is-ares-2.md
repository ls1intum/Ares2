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

- policy-based sandboxing (static and dynamic analysis) to prevent unsafe operations, to
  reduce cheating, to protect academic integrity and fairness, and to protect the learning
  goal the exercise was set for
- more robust tests and builds, through limits on time, threads and IO
- support for public and hidden tests, where hidden ones obey a custom deadline
- utilities for improved feedback in Artemis, such as processing multiline error messages or
  pointing at a likely location that caused an exception
- utilities for comfortably testing exercises that use `System.out` and `System.in`

:::warning[Read the basics before using Ares 2 in production]
Ares 2 provides a high level of security, and that comes with a small downgrade in
simplicity. Several
steps are needed to make tests work properly, and it takes some time to understand what Ares 2
does. Please read at least [Setup](../protect-a-java-project/setup.md), [Test Annotations](../protect-a-java-project/test-annotations.md) and
[Security Overview](what-does-ares-2-protect-against.md) before using Ares 2 for a real exercise.
:::

## Where to go next

The rows follow the order of the sidebar, so reading straight down is the same as working
through the guide from top to bottom.

| If you want to | Read |
| --- | --- |
| Know what a submission is stopped from doing | [What does Ares 2 protect against](what-does-ares-2-protect-against.md) |
| Know where the boundary ends, and what your build still has to do | [What does Ares 2 not protect against](what-does-ares-2-not-protect-against.md) |
| Decide how Ares 2 enters your build, before touching anything else | [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md) |
| Add Ares 2 to a project | [Installation](../protect-a-java-project/installation.md) |
| Understand the public and hidden test model | [Setup](../protect-a-java-project/setup.md) and [Test Annotations](../protect-a-java-project/test-annotations.md) |
| Write a policy file | [Policy Configuration](../protect-a-java-project/policy-configuration.md), with every detail in the [Security Policy Manual](/contributor/subsystems/policy/security-policy-manual) |
| Migrate an Ares 1 exercise | [Converting an Ares 1 Exercise](../transform-ares-1-into-ares-2/index.md) |
| Write a policy for one concrete situation | [Policy Cookbook](/instructor/policy-cookbook/) |
| Look up a single policy field | [Policy Reference](/instructor/policy-reference/) |
| Work out what an error message is telling you | [Troubleshooting](../troubleshooting.md) |
| Understand how Ares 2 works internally | The [Contributor Guide](/contributor/subsystems/package-overview) |

## Licence

Ares 2 is licensed under the MIT Licence. See
[LICENSE](https://github.com/ls1intum/Ares2/blob/main/LICENSE) for details.
