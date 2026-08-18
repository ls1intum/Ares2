---
title: "What does Ares 2 protect against"
sidebar_position: 2
description: "The resources Ares 2 guards, and the two compile modes through which it enforces a policy."
---

:::tip[Simple Story]
The checklist names what a pupil may not do: open papers that are not theirs, telephone out,
send someone off on an errand, or fill the room with helpers.

It also sets out the two moments at which the school can act. Either the checks are printed
into the examination in advance, before anybody sits down, or the teacher carries the checklist
in and works through it while the examination runs. Ares does both rather than choosing one,
and this page is why.
:::

Beyond its standard features, Ares 2 supports advanced blocking mechanisms using **AspectJ**,
**AOP (aspect-oriented programming)**, **instrumentation** and **architecture tests**. These
give fine-grained control over student submissions, allowing specific actions such as file
system access, networking and thread management to be blocked through customisable policies
defined in YAML. Architecture-based tests enforce structural constraints on
student code.

The goal is to prevent unauthorised access to key system resources:

- file system operations (read, create, overwrite, execute, delete)
- networking
- thread creation and management
- command execution
- use of unsupported or dangerous methods

Ares 2 provides two complementary compile modes that determine *when*
these transformations are applied: precompile and postcompile.

## Precompile

Precompile mode generates security test cases and infrastructure files **before** the student
code is compiled or executed. From your security configuration file, Ares 2 automatically
creates:

- architecture test cases using ArchUnit or T. J. Watson Libraries for Analysis (WALA)
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

See [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md) for the integration steps.

## Postcompile

Postcompile mode enforces security policies at **runtime**, without writing any generated
files into the repository. When a test carries an Ares test annotation and a `@Policy`,
Ares 2:

- loads the security configuration file
- arms the runtime interception the policy asks for
- intercepts method calls to protected resources (files, network, threads, commands)
- validates each operation against the policy and blocks unauthorised access

This mode activates dynamically when tests run, and gives immediate, fine-grained control over
student code execution without requiring pre-generated test files.

:::danger[`@Policy` alone supervises nothing]
The Ares test annotation is what registers the extensions; `@Policy` only names the policy they
apply. A method carrying a plain JUnit `@Test` and a `@Policy` runs unsupervised and passes,
with nothing to indicate that no domain was enforced. See
[Test Annotations](../protect-a-java-project/test-annotations.md).
:::

:::warning[The mode does not decide the interception mechanism]
The interception mechanism comes from the policy's
`theFollowingProgrammingLanguageConfigurationIsUsed`, not from the mode. An `_ASPECTJ`
configuration needs the aspects woven into the bytecode by the AspectJ compiler during the
build; an `_INSTRUMENTATION` configuration needs the Byte Buddy agent attached to the test Java Virtual Machine (JVM).
Both work in Postcompile, and the two shipped examples run Postcompile with AspectJ. Setting up
only the agent leaves an `_ASPECTJ` policy with nothing to intercept with, and the run stays
green while enforcing nothing. [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md)
sets out the same distinction and lists which combination needs which build step.
:::

**Key advantages:**

- dynamic enforcement at runtime
- no repository modifications needed
- easy policy updates without regenerating tests
- fine-grained control over individual test methods
- detailed security violation messages

See [Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md) for the integration steps.

## Both modes together

The two modes work together. Ares 2 provides both steps to achieve strong isolation and
predictable, student-friendly feedback.

Both rely on the same configuration file, which is introduced in
[Policy Configuration](../protect-a-java-project/policy-configuration.md).

:::info[How it works internally]
The developer guide describes the enforcement machinery itself: the
[enforcement model](/contributor/subsystems/policy/enforcement-model), the
[AOP layer](/contributor/subsystems/aop/block-file-system-access) and the
[architecture analysis layer](/contributor/subsystems/architecture/block-file-system-access).
:::
