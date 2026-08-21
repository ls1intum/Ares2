---
title: "Interception"
sidebar_position: 1
description: "Where each layer places itself between the supervised program and the resource it asks for."
---

:::tip[ELI5]
Every one of these three layers works by standing in the doorway.

The program asks for something, and instead of reaching the thing it asked for, it reaches
the guard first. What differs between the three is *which* doorway they stand in.
:::

## What it is

Every Linux-based layer works by putting itself between the supervised program and the resource
it requests, but each does so at a different level:

| Layer | Interception point |
| --- | --- |
| [Bubblewrap](../bubblewrap.md) | The kernel. A mount namespace gives the process a different view of the file system. |
| [LD-Preload Firewall](../ld-preload-firewall.md) | The dynamic linker. Socket symbols resolve to `libnetblocker.so` before they reach libc. |
| [Timeout](../timeout.md) | Signal delivery. The wrapper sends a signal once the budget is spent. |

This is the same idea as a [pointcut](/contributor/technologies/aop-tests/base-idea/pointcut) in
the aspect-oriented programming (AOP) layer, moved outside the Java Virtual Machine (JVM).

## Further reading

- [`ld.so(8)`](https://man7.org/linux/man-pages/man8/ld.so.8.html) — Linux manual page
- [`mount_namespaces(7)`](https://man7.org/linux/man-pages/man7/mount_namespaces.7.html) — Linux manual page
- [containers/bubblewrap](https://github.com/containers/bubblewrap) — source repository
