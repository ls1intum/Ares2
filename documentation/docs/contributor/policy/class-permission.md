---
title: "Class permission"
sidebar_position: 2
description: "How the derived ClassPermission set is built and how it reaches both enforcement layers."
---

:::tip[Simple Story]
Some people in the room are trusted rather than checked, and this is where that set comes from.

It is the only line of the checklist nobody writes by hand.
:::

For the fields an exercise author writes, see
[Class permission](/instructor/policy-reference/class-permission) in the instructor guide. This page is
about how the domain is enforced.

## Model

`ClassPermission` is a record, but unlike every other domain it has **no field of its own in
the policy file**. It is derived. What an author writes is
`theFollowingClassesAreTestClasses`; see
[Test class exemptions](/instructor/policy-reference/class-permission) for the field itself.

## Validation and normalisation

Each declared test class is validated against `JAVA_CLASS_PATH_PATTERN`, a fully qualified
Java class name. Entries that are `null` or blank are filtered out **before** a
`ClassPermission` is constructed, because the constructor throws on both and a single
malformed entry from a scanned project would otherwise abort the creation of every security test case.

## What it generates

`JavaCreator.prepareAllowedClasses` concatenates two streams into one set: the **essential
classes**, which are Ares' own infrastructure, and the **declared test classes**.

## Static enforcement

The set reaches `JavaArchitectureTestCase` as `allowedClasses`, so a call made from a trusted
class is not reported.

## Runtime enforcement

The same set reaches `JavaAOPTestCase` as `allowedClasses`, and is published by
`JavaExecuter.executeTestCases` as `allowedListedClasses` **before** `restrictedPackage` arms
call-stack enforcement. The ordering is deliberate: an instrumented class-loading operation
must never observe a partially armed policy.

## Where the code lives

- `securitytest/java/creator/JavaCreator.prepareAllowedClasses`, the derivation
- `policy/policySubComponents/ClassPermission.java`, the record
- `securitytest/java/executer/JavaExecuter.executeTestCases`, the publication order

## Known gaps

Trust is granted by **name**. A class that manages to declare a trusted name inherits the
trust, which is why the reserved-package build boundary is a deployment prerequisite rather
than an optional extra. Ares generates that boundary in neither Precompile nor Postcompile.
