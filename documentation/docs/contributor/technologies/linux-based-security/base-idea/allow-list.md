---
title: "Allow list"
sidebar_position: 2
description: "The declarative, fail-closed rule set each layer reads."
---

:::tip[ELI5]
Each layer carries a short list of what is permitted, and nothing else gets through.

The important half of that sentence is the second one. The list does not say what is
forbidden, because there is no way to write down everything a program might try. It says
what is allowed, and everything absent from it is refused by default.
:::

## What it is

Each layer is driven by a declarative rule set derived from the security policy, and each is
**fail-closed**: anything not listed is denied, and Phobos aborts with `PHB-EBASE` when no base
configuration is present at all.

| Layer | Rule set |
| --- | --- |
| [Bubblewrap](../bubblewrap.md) | The set of paths bound read-only, bound writable, or hidden behind a `tmpfs`. |
| [LD-Preload Firewall](../ld-preload-firewall.md) | A table of host, CIDR and port rules consulted on each connection attempt. |
| [Timeout](../timeout.md) | The wall-clock budget. |

## Further reading

- [`mount_namespaces(7)`](https://man7.org/linux/man-pages/man7/mount_namespaces.7.html) — Linux manual page
- [containers/bubblewrap](https://github.com/containers/bubblewrap) — source repository
- [`timeout(1)`](https://man7.org/linux/man-pages/man1/timeout.1.html) — Linux manual page
