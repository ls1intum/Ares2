---
title: "LD-Preload Firewall"
sidebar_position: 3
description: "Blocking network access by interposing a shared library ahead of libc."
---

:::tip[Simple Story]
The special examination room has new features against cheating. One of them sits in the
telephone. Each time a pupil rings out, it checks the number and either puts the call through or
refuses it.

It only catches calls made the usual way, through that telephone. A pupil who reaches the line
some other way goes round it, so this feature is one of several. Nobody uses this room yet when
the teacher picks up the rule sheet for each question. This page tells how an earlier version of
the room worked.

[Who is who](/instructor/who-is-who)
:::

:::note[Historical design]
Where this page names a Phobos layer or script, it describes the scripts Ares used to bundle,
which it no longer ships. Current Phobos works differently; see the
[Phobos documentation](https://ls1intum.github.io/phobos/).
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
