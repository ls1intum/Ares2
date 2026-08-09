---
title: "Policy Reference"
sidebar_position: 1
description: "Field-by-field reference for every domain a security policy can express, one page per domain."
---

:::tip[ELI5]
This is the dictionary for `security-policy.yaml`.

Every field, what type it takes, what it accepts, and where it sits in the file.
:::

:::note[The domain pages have not moved here yet]
The eight reference pages still live in the Policy section of the contributor guide. They are
being moved here, because their content is what an exercise author writes rather than how Ares
enforces it. Until the move lands, follow the links in the table below.
:::

## How this section is organised

One page per policy domain. Each shows the identical example `security-policy.yaml` with its own
section marked in red, then a field table with datatype, explanation, example and the accepted
range or regular expression, then notes on the traps specific to that domain.

Once a domain is enforced by more than one layer, the enforcement detail lives on the matching
contributor page rather than here, so that a field's meaning and its implementation cannot drift
apart in two half-updated copies.

## The eight domains

| Page | Status | Intended purpose |
| --- | --- | --- |
| [Programming Language Configuration](/contributor/policy/programming-language-configuration) | To be moved | Language, build tool, analyser and weaving mechanism |
| [File System Access](/contributor/policy/file-system-access) | To be moved | Read, overwrite, execute and delete permissions per path |
| [Network System Access](/contributor/policy/network-system-access) | To be moved | Connect, send and receive permissions per host and port |
| [Command System Access](/contributor/policy/command-system-access) | To be moved | Which executables may be started, with which arguments |
| [Thread System Access](/contributor/policy/thread-system-access) | To be moved | Thread creation and the bounds on it |
| [Resource Limits](/contributor/policy/resource-limits) | To be moved | Time and memory budgets |
| [Class Permission](/contributor/policy/class-permission) | To be moved, and reframed | Test class exemptions; the derived `ClassPermission` record moves to the contributor guide |
| [Package Permission](/contributor/policy/package-permission) | To be moved | Which packages may be imported |

## Notes

- A field that parses is not automatically a field that is enforced in the mode you are using.
  Where the two differ, the domain page says so explicitly. Resource limits are the current
  example: they are generated but not yet dispatched in Postcompile.
