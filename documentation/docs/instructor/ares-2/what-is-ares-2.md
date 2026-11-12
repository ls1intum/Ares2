---
title: "What is Ares 2"
sidebar_position: 1
description: "What Ares 2 is, what it is for, and how this documentation is organised."
---

:::tip[Simple Story]
When students submit programming homework, something has to run their code to see whether it
works. Running a stranger's code on your machine is exactly as risky as it sounds.

So picture an examination. The methods the student wrote are the pupils. The test framework,
JUnit Jupiter, is the teacher, putting the exercise's questions one at a time and marking each
answer on the spot. Ares is the checklist the teacher works through at every desk: what a pupil
may reach for, and what happens when they reach for anything else. The work still runs and
still gets marked, and what it may reach is what the checklist allows.
:::

Ares 2 is a framework for the easy and secure remote execution of student submissions on an
interactive learning platform. It is the second Java-based implementation of the Secure COder
Remote Execution (SCORE) framework, and the first to support Java 25 and later.

Its main features are:

1. **Allowable resource access:** your policy file names what a submission can use, such as a
   file path, a host and port, a command with fixed arguments, or the packages it can import.
   Ares 2 checks the submission against what you named, within the limits set out in
   [What does Ares 2 not protect against](what-does-ares-2-not-protect-against.md).
2. **Not-allowable resource access:** beyond a small built-in allowance, Ares 2 refuses
   whatever your policy does not name. Some categories, such as reflection, native code or
   `System.exit`, have no policy field at all, and Ares 2 rejects a submission whose compiled
   code contains a route to them. This prevents unsafe operations, reduces cheating, and
   protects academic integrity, fairness and the learning goal the exercise was set for.
3. **Runtime limitation:** `@StrictTimeout` stops a test that runs too long, and your policy
   caps how many threads a submission can start, which makes tests and builds more robust.
4. **I/O limitation:** utilities for comfortably testing exercises that use `System.out` and
   `System.in`. A test provides the console input up front, and reading past it fails with a
   clear message. Output beyond a set amount fails the test, and
   `@MirrorOutput(maxCharCount = ...)` changes that amount.
5. **Test case result visibility limitation:** public tests run and report straight away.
   Hidden tests do not run before the deadline you set with `@Deadline` and count as failed
   until then. Once they run, a failing hidden test tells the student only that it failed.
6. **Better feedback for students:** utilities for improved feedback in Artemis, such as
   processing multiline error messages.

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
| Add Ares 2 to an exercise, for Maven and for Gradle | [How to protect a Java project with Ares 2](/instructor/category/how-to-protect-a-java-project-with-ares-2) |
| Migrate an Ares 1 exercise | [How to transform an Ares 1 protected project into an Ares 2 protected project](../transform-ares-1-into-ares-2/index.md) |
| Write a policy for one concrete situation | [Policy Cookbook](/instructor/policy-cookbook/) |
| Look up a single policy field | [Policy Reference](/instructor/policy-reference/) |
| Work out what an error message is telling you | [Troubleshooting](../troubleshooting.md) |

## Licence

Ares 2 is licensed under the MIT Licence. See
[LICENSE](https://github.com/ls1intum/Ares2/blob/main/LICENSE) for details.
