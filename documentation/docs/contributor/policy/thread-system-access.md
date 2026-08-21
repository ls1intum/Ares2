---
title: "Thread system access"
sidebar_position: 6
description: "How thread creation is enforced, and why Object monitor methods need call-site rewriting."
---

:::tip[ELI5]
Threads are the domain where enforcement has to touch code Ares does not own.

That is why one part of it is switched on after everything else.
:::

For the fields an exercise author writes, see
[Thread system access](/instructor/policy-reference/thread-system-access) in the instructor guide. This page is
about how the domain is enforced.

## Model

`ThreadPermission`, carrying the number of threads permitted and the class permitted to create
them.

## Validation and normalisation

The count is bounded; a policy that permits threads without a bound lets a runaway
submission exhaust the runner rather than failing its own test.

## What it generates

Architecture and aspect-oriented programming (AOP) test cases.

## Static enforcement

Matched against `thread-manipulation-methods.txt`.

## Runtime enforcement

`JavaInstrumentationAdviceThreadSystemToolbox`. `Object`'s final monitor methods cannot be
intercepted at the callee, so they need application call-site rewriting:
`JavaInstrumentationAgent.registerThreadMonitorRestrictedPackage` is called **last** in
`JavaExecuter.executeTestCases`, after the complete policy is installed, so a retransformed
class can never observe a partially configured policy.

## Where the code lives

- `policy/policySubComponents/ThreadPermission.java`
- `aop/java/instrumentation/advice/JavaInstrumentationAdviceThreadSystemToolbox.java`
- `aop/java/instrumentation/JavaInstrumentationAgent.registerThreadMonitorRestrictedPackage`

## Known gaps

The call-site rewriting applies to the restricted package only, so monitor operations reached
through a permitted library are not rewritten.
