---
title: "LD-Preload Firewall"
sidebar_position: 3
description: "Blocking network access by interposing a shared library ahead of libc."
---

:::tip[Simple Story]
When a pupil wants to telephone out, they ask the system library to place the call.

Linux lets you slip your own library in front, so they ask *you* instead. You look at who they
are trying to reach, and either put the call through or refuse it. The pupil never knows the
difference.
:::

## What it is

The dynamic linker resolves a symbol to the first library that provides it. `LD_PRELOAD`
puts a chosen library at the front of that search, so a function defined there is found
before the one in libc. The original remains reachable through `dlsym` with `RTLD_NEXT`,
which is how an interposed function can still do the real work after deciding to allow it.

## How Phobos uses it

`netblocker.c` is compiled to `libnetblocker.so` and preloaded. It carries a rule table of
host, network and port entries, supporting literal hosts, wildcards, and CIDR ranges held
as IPv6-mapped bases, and consults it on each connection attempt.

`phobos-filesystem.sh` binds the library into the sandbox read-only, because a preloaded
library that is not visible inside the mount namespace cannot be loaded.

## The limit worth knowing

Interposition works on **dynamically linked** calls. A statically linked binary, or code
that issues the system call directly rather than through libc, is not affected. This layer
is therefore one of three, not a boundary on its own.

## Further reading

- [`ld.so(8)`](https://man7.org/linux/man-pages/man8/ld.so.8.html) — Linux manual page
- [`dlsym(3)`](https://man7.org/linux/man-pages/man3/dlsym.3.html) — Linux manual page
- [LD_PRELOAD in Linux: A Powerful Tool for Dynamic Library Interception](https://abhijit-pal.medium.com/ld-preload-in-linux-a-powerful-tool-for-dynamic-library-interception-7f681d0b6556) — Abhijit, Medium (freely readable)
- [containers/bubblewrap](https://github.com/containers/bubblewrap) — source repository
