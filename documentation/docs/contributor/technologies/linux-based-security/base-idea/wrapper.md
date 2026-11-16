---
title: "Wrapper"
sidebar_position: 3
description: "Why enforcement lives in a process that wraps the supervised command rather than inside it."
---

:::tip[Simple Story]
The features of the special examination room sit in its walls and its door, out of the pupil's
reach.

That is the whole point. The teacher's checks work inside the room, beside the pupils. The
room's features work from outside, so even a pupil who gains control over everything inside
cannot switch them off. Nobody uses this room yet when the teacher picks up the rule sheet for
each question. This page tells how an earlier version of the room worked.

[Who is who](/instructor/who-is-who)
:::

:::note[Historical design]
Where this page names a Phobos layer or script, it describes the scripts Ares used to bundle,
which it no longer ships. Current Phobos works differently; see the
[Phobos documentation](https://ls1intum.github.io/phobos/).
:::

## What it is

All three layers are wrappers: a process that configures its restriction and then executes the
next layer, with the supervised build command innermost.

This is the property that distinguishes Linux-based security from the
[aspect-oriented programming (AOP)](/contributor/technologies/aop-tests/base-idea/aspect) and
[architecture](/contributor/technologies/architecture-tests/base-idea/rule) layers. Those run
*inside* the Java Virtual Machine (JVM) they protect. A wrapper runs outside it, so the supervised process cannot
switch the restriction off, however much control it gains over its own JVM.

The layers are composed by `phobos.sh` and each can be disabled independently with
`--no-timeout`, `--no-network` and `--no-filesystem`.

## Further reading

- [`namespaces(7)`](https://man7.org/linux/man-pages/man7/namespaces.7.html) — Linux manual page
- [`timeout(1)`](https://man7.org/linux/man-pages/man1/timeout.1.html) — Linux manual page
- [containers/bubblewrap](https://github.com/containers/bubblewrap) — source repository
