---
title: "Command system access"
sidebar_position: 5
description: "How permitted executables and arguments are enforced, and why this domain has no Phobos section."
---

:::tip[Simple Story]
Sometimes a pupil is allowed to ask a friend outside the room for help. The rule sheet names
the friend and the exact words the pupil can say to them. Being allowed to ask the friend does
not make any words fine.

This is the smallest part of the rule sheet, and the one that does the most harm when the head
teacher gets it wrong, because a friend can fetch almost anything. Once the friend is helping,
the rule sheet no longer follows them. Even the special examination room, when the school uses
it, limits only the friend's worksheets and telephone.

[Who is who](/instructor/who-is-who)
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
