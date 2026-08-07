---
title: "Allow list"
sidebar_position: 2
description: "The declarative, fail-closed rule set each layer reads."
---

Each layer is driven by a declarative rule set derived from the security policy, and each is
**fail-closed**: anything not listed is denied, and Phobos aborts with `PHB-EBASE` when no base
configuration is present at all.

| Layer | Rule set |
| --- | --- |
| [Bubblewrap](../bubblewrap.md) | The set of paths bound read-only, bound writable, or hidden behind a `tmpfs`. |
| [LD-Preload Firewall](../ld-preload-firewall.md) | A table of host, CIDR and port rules consulted on each connection attempt. |
| [Timeout](../timeout.md) | The wall-clock budget. |
