---
title: "Phobos"
sidebar_position: 9
description: "The out-of-process sandbox subsystem: bubblewrap, the LD_PRELOAD firewall and the timeout wrapper."
---

:::tip[Simple Story]
Everything else in Ares works inside the room. Phobos **is** the room.

The desk with its dividers, the door, and the clock out in the corridor, all put in place by
the operating system before the pupil walked in. It hides papers they should not see, refuses
calls they should not make, and stops them when they have run too long. Being outside matters:
a pupil can talk a teacher round, but cannot unbuild a divider.
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

## Further reading

- [Package Overview](./package-overview.md) — every package in one place
- [Linux-based security](/contributor/technologies/linux-based-security/base-idea/wrapper) — the ideas behind the three layers
