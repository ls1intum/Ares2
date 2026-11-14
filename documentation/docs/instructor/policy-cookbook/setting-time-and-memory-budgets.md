---
title: "Setting time and memory budgets"
sidebar_position: 7
description: "Resource limits, which mode enforces them today, and what to use in the meantime."
---

:::tip[Simple Story]
A pupil who never finishes has to fail their own question, not hold up the whole
examination. The rule sheet has a line for a time limit, but that limit belongs to the clock
of the special examination room, and that clock stops nobody yet. The teacher's stopwatch is
what stops a pupil today.

Nobody has written this recipe yet.

[Who is who](/instructor/who-is-who)
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written. Until it is, use the
[Policy Reference](/instructor/policy-reference/) for the individual fields.
:::

## The situation

The exercise needs a wall-clock and a memory ceiling, and it matters which of the two modes enforces them.

## The policy fragment

The smallest `security-policy.yaml` addition that solves it, shown in full so it can be pasted
into an existing policy.

## What this still forbids

The operations the recipe deliberately does not enable, so the boundary stays visible.

## The tempting wrong version

Assuming the policy's resource limits bound a Postcompile run. They are generated but not yet dispatched there; `@StrictTimeout` is what applies.

## Notes

- Check the recipe against both controls: the permitted operation must succeed, and the nearest
  forbidden neighbour must still be rejected.
