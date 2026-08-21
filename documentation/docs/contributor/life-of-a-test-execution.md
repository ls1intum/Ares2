---
title: "Life of a test execution"
sidebar_position: 3
description: "One annotated test method, followed from JUnit discovery through policy installation to teardown."
---

:::tip[ELI5]
Follow one single test from the moment JUnit finds it to the moment it is cleaned up again.

Most questions about Ares turn out to be questions about *when* something happens. This page is
the timeline everything else hangs off.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written. The individual steps are
already documented on the [subsystem pages](./subsystems/package-overview.md); what is missing
is the single continuous narrative that connects them.
:::

## What this page will trace

A single `@PublicTest` method in a Postcompile project, from discovery to teardown, naming the
class and method responsible at every step. Precompile is traced separately, because there the
timeline spans two processes.

## 1. Discovery and extension registration

How `@Public` / `@Hidden` resolve through `@JupiterAresTest` to the four registered extensions,
and why `BeforeTestExecutionCallback` is used rather than `interceptTestMethod`.

## 2. Reading the policy

`SecurityPolicyReader.selectSecurityPolicyReader`, format selection, and what happens when no
`@Policy` is present at all.

## 3. Directing test-case creation

`SecurityPolicyDirector`, the `ProgrammingLanguageConfiguration`, and the three lists the
creator produces: architecture, aspect-oriented programming (AOP) and Phobos test cases.

## 4. Installing the policy

The ordering inside `JavaExecuter.executeTestCases` and why it is security-relevant: allow-lists
first, architecture and AOP cases next, `restrictedPackage` last, thread-monitor retransformation
after that.

## 5. The student code runs

Which interception path a forbidden call takes, and how the call stack is checked against the
allow-list.

## 6. Teardown

`afterTestExecution`, the reset in both the standard and the bootstrap class loader, and why the
reset must also happen on the failure path.

## 7. Where Precompile differs

The same seven steps, split across the generation process and the exercise's own build.

## Notes

- The static advice settings are JVM-global, which is why this timeline assumes sequential test
  execution.
