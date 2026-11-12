---
title: "Phobos"
sidebar_position: 9
description: "The out-of-process sandbox subsystem: bubblewrap, the LD_PRELOAD firewall and the timeout wrapper."
---

:::tip[Simple Story]
The special examination room works from outside. It hides the worksheets a pupil must not see,
cuts the telephone lines they must not use, and ends the examination when time runs out. Its
walls do not depend on the pupil behaving.

This page describes an older plan of the room, which the school no longer follows. When the
teacher picks up the rule sheet for each question, nobody uses the room at all. And some of the
notes written for the room still use an old form, which today's room refuses.

[Who is who](/instructor/who-is-who)
:::

:::note[Historical design]
The three layers and `phobos.sh` below describe the scripts Ares used to bundle, which it no
longer ships. Current Phobos works differently; see the
[Phobos documentation](https://ls1intum.github.io/phobos/).
:::

## What it does

Phobos enforces from **outside** the Java Virtual Machine (JVM), using the operating system, rather than from inside
it. It is the one subsystem whose guarantees do not depend on the supervised process behaving.

It works by generating configuration and shell scripts that an external sandbox runtime
consumes, rather than by weaving anything into bytecode.

## The three layers

| Layer | Mechanism | What it bounds |
| --- | --- | --- |
| Filesystem | `bwrap`, through mount namespaces | which paths exist at all for the process |
| Network | `libnetblocker.so`, preloaded ahead of libc | which hosts and ports may be reached |
| Timeout | the `timeout` command | how long the whole thing may run |

`phobos.sh` composes them as nested wrappers, and each can be disabled independently with
`--no-filesystem`, `--no-network` and `--no-timeout`. With no base configuration present at
all it fails closed with `PHB-EBASE` rather than running unprotected.

## What is in it

| Class | Purpose |
| --- | --- |
| `Phobos` | Utility for the CSV-driven file copy and edit configuration |
| `PhobosTestCase` | Abstract base with the extractors for file, network and resource-limit permissions |
| `JavaPhobosTestCase` | Java implementation, producing the sandbox configuration from the policy |
| `JavaPhobosTestCaseSupported` | The supported domains: `FILESYSTEM_INTERACTION`, `NETWORK_CONNECTION`, `TIMEOUT` |

## Known gaps

**Nothing here bounds a Postcompile test today.** Ares creates the Phobos cases, but
`JavaTestCaseFactoryAndBuilder.executeTestCases` passes only the architecture and
aspect-oriented programming (AOP) cases to the executer, and Postcompile reaches Ares through
the test extensions, which call `executeTestCases` alone. This holds for all three layers, not
only the timeout, and is a pending migration rather than a defect. See
[Resource limits](/contributor/policy/resource-limits) for the same gap from the policy side.

Ares still writes `[readonly]` and `[network]` sections, which current Phobos rejects;
migrating the generated configuration format is separate work.

## Further reading

- [Package Overview](./package-overview.md) — every package in one place
- [Linux-based security](/contributor/technologies/linux-based-security/base-idea/wrapper) — the ideas behind the three layers
