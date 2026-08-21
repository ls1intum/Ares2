---
title: "Policy Reference"
sidebar_position: 1
description: "Field-by-field reference for every domain a security policy can express, one page per domain."
---

:::tip[Simple Story]
This is the dictionary for the checklist itself, `security-policy.yaml`, the file the board
fills in.

Every field, what type it takes, what it accepts, and where it sits in the file.
:::

## How this section is organised

One page per policy domain. Each shows the identical example `security-policy.yaml` with its own
section marked in red, then a field table with datatype, explanation, example and the accepted
range or regular expression, then notes on the traps specific to that domain.

Read in sidebar order, the eight pages walk the example file from top to bottom, so the section
doubles as an annotated tour of a complete policy. That property is asserted by
`PolicyDocumentationStructureTest`, which pins the shared page shape.

## What is not here

How each domain is **enforced** is in the contributor guide under
[Policy](/contributor/policy/file-system-access): which architecture rule inspects it, which advice intercepts it,
which Phobos configuration section it reaches, and where the code lives. The split is deliberate,
so that a field's meaning and its implementation cannot drift apart in two half-updated copies.

## The eight domains

| Page | What you write |
| --- | --- |
| [Programming Language Configuration](./programming-language-configuration.md) | Language, build tool, analyser and weaving mechanism |
| [Test class exemptions](./class-permission.md) | Which classes are trusted rather than sandboxed |
| [File System Access](./file-system-access.md) | Read, overwrite, execute and delete permissions per path |
| [Network System Access](./network-system-access.md) | Connect, send and receive permissions per host and port |
| [Command System Access](./command-system-access.md) | Which executables may be started, with which arguments |
| [Thread System Access](./thread-system-access.md) | Thread creation and the bounds on it |
| [Package Permission](./package-permission.md) | Which packages may be imported |
| [Resource Limits](./resource-limits.md) | The execution budget, in milliseconds |

## Notes

- A field that parses is not automatically a field that is enforced in the mode you are using.
  Where the two differ, the domain page says so. Resource limits are the current example: they
  are generated but not yet dispatched in Postcompile, so `@StrictTimeout` is what bounds a test
  today.
- For worked situations rather than field definitions, see the
  [Policy Cookbook](/instructor/policy-cookbook/).
