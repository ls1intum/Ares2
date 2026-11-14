---
title: "Allow list"
sidebar_position: 2
description: "The declarative, fail-closed rule set each layer reads."
---

:::tip[Simple Story]
The special examination room works only from what the rule sheet allows: which worksheets are
there, and which numbers the telephone can ring.

Nothing in it names what is forbidden, because nobody can think of everything a pupil might
try. The room refuses anything the rule sheet does not allow. Without any settings at all, the
room does not open. Nobody uses this room yet when the teacher picks up the rule sheet for each
question. This page tells how an earlier version of the room worked.

[Who is who](/instructor/who-is-who)
:::

:::note[Historical design]
Where this page names a Phobos layer or script, it describes the scripts Ares used to bundle,
which it no longer ships. Current Phobos works differently; see the
[Phobos documentation](https://ls1intum.github.io/phobos/).
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
