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

## Where the rules come from

`phobos-common.sh` writes the allow-list an exercise's policy produced to `net.rules` in
that run's specification directory. `phobos-network.sh` then exports that file's path as
`NETBLOCKER_CONF`, creating it empty if the merge produced nothing, and the library reads
the file that variable names when it is loaded. Nothing else selects the rules: a library
that read a fixed path instead would apply one list to every exercise.

## Two copies of Phobos

Phobos is maintained in its own repository, [`ls1intum/phobos`](https://github.com/ls1intum/phobos).
That is the runtime an Artemis deployment installs, and it carries its own `netblocker.c` and
its own compiled library. Ares separately vendors a **snapshot** of the Phobos files under
`src/main/resources/.../templates/phobos` and packages it into the Ares jar.

Everything below concerns the vendored snapshot. Keeping it in step with upstream is manual:
nothing compares the two repositories, and nothing here changes the upstream one.

## Regenerating the vendored library

`libnetblocker.so` is checked in already compiled, next to the `netblocker.c` it is built
from, because no Maven build compiles C. The two can therefore fall out of step, and a
library that reads a configuration file other than the one `NETBLOCKER_CONF` names applies a
single rule list to every exercise.

`tools/netblocker/build-netblocker.sh` is the only supported way to produce the vendored
artefact. Run it with no arguments after changing the vendored `netblocker.c`: it rebuilds the
library and rewrites `netblocker.provenance` beside it, recording the source, the compiler,
the flags and the resulting digests.

`--check` verifies a checkout without changing it. It requires the recorded digests to match,
builds the source twice and requires those builds to agree, then puts the same rule sets to
the vendored library and to a fresh build and requires the same verdicts. `--check-jar` takes
the path of a built jar and checks the packaged side: that the library, the source and the
provenance each appear exactly once, that their bytes are the checkout's, and that the digests
the packaged provenance records recompute from the packaged entries. The
`Netblocker Digests And Behaviour` job runs both on every pull request.

**What this does and does not establish.** It establishes that the source and library match
their recorded digests, that the build is deterministic on one toolchain, that both libraries
agree on every rule set tested, and that the library reads `NETBLOCKER_CONF`. Byte equality is
deliberately not required, because a different compiler release or flag set arranges the same
code differently; a difference is reported without inferring its cause. Agreement on a finite
set of rule sets is not proof that two libraries are the same program.

**One architecture, one GLIBC compatibility ceiling.** The library is built and tested for
64-bit x86 alone, and the build script refuses any other host rather than producing an
unverified artefact, so a deployment on another architecture has no network layer and must not
be told otherwise. It also refuses a library whose newest required GLIBC symbol version is
past 2.34, since a build on a newer distribution behaves identically where it loads at all and
silently stops loading everywhere else.

## The limit worth knowing

Interposition works on **dynamically linked** calls. A statically linked binary, or code
that issues the system call directly rather than through libc, is not affected. This layer
is therefore one of three, not a boundary on its own.

## Further reading

- [`ld.so(8)`](https://man7.org/linux/man-pages/man8/ld.so.8.html) — Linux manual page
- [`dlsym(3)`](https://man7.org/linux/man-pages/man3/dlsym.3.html) — Linux manual page
- [LD_PRELOAD in Linux: A Powerful Tool for Dynamic Library Interception](https://abhijit-pal.medium.com/ld-preload-in-linux-a-powerful-tool-for-dynamic-library-interception-7f681d0b6556) — Abhijit, Medium (freely readable)
- [containers/bubblewrap](https://github.com/containers/bubblewrap) — source repository
