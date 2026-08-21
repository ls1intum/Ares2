---
title: "Using a library that reflects"
sidebar_position: 6
description: "Keeping a reflective dependency working without disabling the reflection rules wholesale."
---

:::tip[Simple Story]
A piece of equipment the examination depends on inspects its own internals, and the checklist refuses it.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written. Until it is, use the
[Policy Reference](/instructor/policy-reference/) for the individual fields.
:::

## The situation

A legitimate dependency trips the reflection rules, and the exercise must keep working without the student gaining reflective access.

## The policy fragment

The smallest `security-policy.yaml` addition that solves it, shown in full so it can be pasted
into an existing policy.

## What this still forbids

The operations the recipe deliberately does not enable, so the boundary stays visible.

## The tempting wrong version

Adding the reflecting package to the allow-list at a level that exempts the student's own code as well.

## Notes

- Check the recipe against both controls: the permitted operation must succeed, and the nearest
  forbidden neighbour must still be rejected.
