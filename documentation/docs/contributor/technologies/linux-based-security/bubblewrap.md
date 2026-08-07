---
title: "Bubblewrap"
sidebar_position: 2
description: "Filesystem isolation through mount namespaces, without requiring root."
---

:::tip[ELI5]
Bubblewrap builds the program a private view of the file system before letting it start.

Folders you did not hand it simply are not there. Not forbidden, not protected: absent.
A program cannot break a rule about a file it cannot see.
:::

## What it is

Bubblewrap (`bwrap`) is an unprivileged sandboxing tool. It uses Linux namespaces to give a
process a different view of the system, most importantly a **mount namespace**, in which
the visible file system is assembled from scratch.

It needs no root, which is what makes it usable on an ordinary CI runner.

## How Phobos uses it

`phobos-filesystem.sh` builds the argument list from the policy:

| Argument | Effect |
| --- | --- |
| `--ro-bind <path> <path>` | the path is visible but read-only |
| `--bind <path> <path>` | the path is visible and writable |
| `--tmpfs <path>` | the path is replaced by an empty temporary filesystem, hiding what was there |

The three map onto the file policy: readable paths are bound read-only, writable paths are
bound writable, and hidden paths get a `tmpfs` over them.

This is [interception](./base-idea/interception.md) at the kernel level and an
[allow list](./base-idea/allow-list.md) of paths, applied by a
[wrapper](./base-idea/wrapper.md) process that execs the build command inside the
namespace it has just built.

## Further reading

- [containers/bubblewrap](https://github.com/containers/bubblewrap) — source repository
- [`namespaces(7)`](https://man7.org/linux/man-pages/man7/namespaces.7.html) — Linux manual page
- [`mount_namespaces(7)`](https://man7.org/linux/man-pages/man7/mount_namespaces.7.html) — Linux manual page
