---
title: "Command system access"
sidebar_position: 5
description: "How permitted executables and arguments are enforced, and why this domain has no Phobos section."
---

:::tip[Simple Story]
This part of the checklist governs sending a pupil out of the room on an errand.

It is the smallest surface there is, and the one with the widest blast radius when it is wrong.
:::

For the fields an exercise author writes, see
[Command system access](/instructor/policy-reference/command-system-access) in the instructor guide. This page is
about how the domain is enforced.

## Model

`CommandPermission`, naming an executable and the arguments it may be started with.

## Validation and normalisation

The executable and its argument list are validated separately, so a permitted executable
does not imply permitted arguments.

## What it generates

Architecture and aspect-oriented programming (AOP) test cases. Unlike the file-system and network domains, this one has
**no** Phobos section: `JavaPhobosTestCaseSupported` covers only filesystem, network and
timeout.

## Static enforcement

Matched against `command-execution-methods.txt`.

## Runtime enforcement

`JavaInstrumentationAdviceCommandSystemToolbox`, or the corresponding aspect.

## Where the code lives

- `policy/policySubComponents/CommandPermission.java`
- `aop/java/instrumentation/advice/JavaInstrumentationAdviceCommandSystemToolbox.java`
- `templates/architecture/java/archunit/methods/command-execution-methods.txt`

## Known gaps

No Phobos section exists for this domain, so a subprocess started outside the Java Virtual Machine (JVM) is not
constrained by the policy even when the Phobos wrapper is active. The filesystem and network
sandboxes still apply to it.
