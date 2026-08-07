---
title: "Interception"
sidebar_position: 1
description: "Where each layer places itself between the supervised program and the resource it asks for."
---

Every Linux-based layer works by putting itself between the supervised program and the resource
it requests, but each does so at a different level:

| Layer | Interception point |
| --- | --- |
| [Bubblewrap](../bubblewrap.md) | The kernel. A mount namespace gives the process a different view of the file system. |
| [LD-Preload Firewall](../ld-preload-firewall.md) | The dynamic linker. Socket symbols resolve to `libnetblocker.so` before they reach libc. |
| [Timeout](../timeout.md) | Signal delivery. The wrapper sends a signal once the budget is spent. |

This is the same idea as a [pointcut](/maintainer/technologies/aop-tests/base-idea/pointcut) in
the AOP layer, moved outside the JVM.
