---
title: "Wrapper"
sidebar_position: 3
description: "Why enforcement lives in a process that wraps the supervised command rather than inside it."
---

All three layers are wrappers: a process that configures its restriction and then executes the
next layer, with the supervised build command innermost.

This is the property that distinguishes Linux-based security from the
[AOP](/maintainer/technologies/aop-tests/base-idea/aspect) and
[architecture](/maintainer/technologies/architecture-tests/base-idea/rule) layers. Those run
*inside* the JVM they protect. A wrapper runs outside it, so the supervised process cannot
switch the restriction off, however much control it gains over its own JVM.

The layers are composed by `phobos.sh` and each can be disabled independently with
`--no-timeout`, `--no-network` and `--no-filesystem`.
