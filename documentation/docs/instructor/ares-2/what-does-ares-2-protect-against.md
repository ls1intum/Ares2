---
title: "What does Ares 2 protect against"
sidebar_position: 2
description: "The resources Ares 2 guards, and the two compile modes through which it enforces a policy."
---

Beyond its standard features, Ares 2 supports advanced blocking mechanisms using **AspectJ**,
**AOP (aspect-oriented programming)**, **instrumentation** and **architecture tests**. These
give fine-grained control over student submissions, allowing specific actions such as file
system access, networking and thread management to be blocked through customisable policies
defined in YAML. Architecture-based tests additionally enforce structural constraints on
student code.

The goal is to prevent unauthorised access to key system resources:

- file system operations (read, create, overwrite, execute, delete)
- networking
- thread creation and management
- command execution
- use of unsupported or dangerous methods

To make this possible, Ares 2 provides two complementary compile modes that determine *when*
these transformations are applied: precompile and postcompile.

## Precompile

Precompile mode generates security test cases and infrastructure files **before** the student
code is compiled or executed. From your security configuration file, Ares 2 automatically
creates:

- architecture test cases using ArchUnit or WALA
- AOP configuration files for AspectJ or instrumentation
- build system modifications (Maven and Gradle plugins and dependencies)
- test infrastructure for enforcing security policies

This mode runs once during exercise setup and produces test files that are committed to the
repository. When students submit their code, these pre-generated tests run automatically and
enforce the security policy.

**Key advantages:**

- tests are versioned and can be reviewed before deployment
- no runtime configuration parsing overhead
- clear separation between policy definition and enforcement
- exercise creators can validate the generated tests before release

See [Precompile Mode](../protect-a-java-project/precompile.md) for the integration steps.

## Postcompile

Postcompile mode enforces security policies at **runtime**, by instrumenting the compiled
bytecode during test execution. When a test is annotated with `@Policy`, Ares 2:

- loads the security configuration file
- instruments student code using Java agents (Byte Buddy)
- intercepts method calls to protected resources (files, network, threads, commands)
- validates each operation against the policy and blocks unauthorised access

This mode activates dynamically when tests run, and gives immediate, fine-grained control over
student code execution without requiring pre-generated test files.

**Key advantages:**

- dynamic enforcement at runtime
- no repository modifications needed
- easy policy updates without regenerating tests
- fine-grained control over individual test cases
- detailed security violation messages

See [Postcompile Mode](../protect-a-java-project/postcompile.md) for the integration steps.

## Both modes together

The two modes work together. Ares 2 provides both steps to achieve strong isolation and
predictable, student-friendly feedback.

Both rely on the same configuration file, which is introduced in
[Policy Configuration](../protect-a-java-project/policy-configuration.md).

:::info How it works internally
The developer guide describes the enforcement machinery itself: the
[enforcement model](/contributor/subsystems/policy/enforcement-model), the
[AOP layer](/contributor/subsystems/aop/block-file-system-access) and the
[architecture analysis layer](/contributor/subsystems/architecture/block-file-system-access).
:::
