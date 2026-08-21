---
title: "Network system access"
sidebar_position: 4
description: "How connect, send and receive permissions are enforced in the JVM and by the LD_PRELOAD firewall."
---

:::tip[ELI5]
Two very different mechanisms guard the network: one inside the Java Virtual Machine (JVM), one below it.

They see different things, which is why both exist.
:::

For the fields an exercise author writes, see
[Network system access](/instructor/policy-reference/network-system-access) in the instructor guide. This page is
about how the domain is enforced.

## Model

`NetworkPermission`, one record per host and port, with separate connect, send and receive
flags.

## Validation and normalisation

Host and port are validated separately. A wildcard host is accepted by the model but is
almost never what an exercise means.

## What it generates

Architecture, aspect-oriented programming (AOP) and Phobos test cases. The Phobos path produces the `network` section of
`SpecificExercise.cfg`.

## Static enforcement

Matched against `network-access-methods.txt`. In every domain, a policy with no network
allowance produces a static deny-all rule that fires before any runtime mechanism is
consulted.

## Runtime enforcement

`JavaInstrumentationAdviceNetworkSystemToolbox` inside the JVM. Outside it, the Phobos
`LD_PRELOAD` firewall (`libnetblocker.so`) applies the same allow-list to the whole process,
including anything the JVM shells out to.

## Where the code lives

- `policy/policySubComponents/NetworkPermission.java`
- `aop/java/instrumentation/advice/JavaInstrumentationAdviceNetworkSystemToolbox.java`
- `templates/phobos/phobos-network.sh` and the `NETBLOCKER_CONF` contract

## Known gaps

A test fixture that opens a socket inside the sandboxed JVM is itself subject to the policy, so
network tests must use an external echo server. See
[Testing conventions](../testing-conventions.md).
