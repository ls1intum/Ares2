---
title: "Package permission"
sidebar_position: 7
description: "How import restrictions are enforced, and why this domain is architecture-only."
---

:::tip[ELI5]
This domain is about which packages student code may import at all.

It is decided by reading the bytecode, not by watching the program run.
:::

For the fields an exercise author writes, see
[Package permission](/instructor/policy-reference/package-permission) in the instructor guide. This page is
about how the domain is enforced.

## Model

`PackagePermission`, one record per permitted package prefix.

## Validation and normalisation

Prefix matching is boundary aware, so permitting `java.util` does not silently permit
`java.utilities`.

## What it generates

Architecture test cases only.

## Static enforcement

This is the whole of its enforcement. The rule inspects the imports of the supervised
bytecode; there is no runtime counterpart, because by the time a class runs its imports have
already been resolved.

## Runtime enforcement

None. A package restriction that a static analyser misses is not caught later.

## Where the code lives

- `policy/policySubComponents/PackagePermission.java`, which is also one of the classes copied
  into a Precompile-generated exercise (`ArchunitJavaCopyFiles.csv`)
- `architecture/java/archunit/JavaArchunitTestCaseCollection.java`

## Known gaps

Being architecture-only makes this the domain most sensitive to the ArchUnit-versus-WALA
choice, since the two build their view of the code differently.
