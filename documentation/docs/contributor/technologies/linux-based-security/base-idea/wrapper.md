---
title: "Wrapper"
sidebar_position: 3
description: "Why enforcement lives in a process that wraps the supervised command rather than inside it."
---

:::tip[Simple Story]
Whoever is holding the door stands outside the room, not inside it.

That sounds like a detail and it is the whole point. A pupil who has taken the room over can
talk round anyone who is in there with them. They cannot talk round someone who arrived before
they did and is holding the door from the other side.
:::

## What it is

All three layers are wrappers: a process that configures its restriction and then executes the
next layer, with the supervised build command innermost.

This is the property that distinguishes Linux-based security from the
[AOP](/contributor/technologies/aop-tests/base-idea/aspect) and
[architecture](/contributor/technologies/architecture-tests/base-idea/rule) layers. Those run
*inside* the Java Virtual Machine (JVM) they protect. A wrapper runs outside it, so the supervised process cannot
switch the restriction off, however much control it gains over its own JVM.

The layers are composed by `phobos.sh` and each can be disabled independently with
`--no-timeout`, `--no-network` and `--no-filesystem`.

## Further reading

- [`namespaces(7)`](https://man7.org/linux/man-pages/man7/namespaces.7.html) — Linux manual page
- [`timeout(1)`](https://man7.org/linux/man-pages/man1/timeout.1.html) — Linux manual page
- [containers/bubblewrap](https://github.com/containers/bubblewrap) — source repository
