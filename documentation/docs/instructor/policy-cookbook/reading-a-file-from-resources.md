---
title: "Reading a file from resources"
sidebar_position: 2
description: "Permitting one read-only classpath resource without opening the working directory."
---

:::tip[ELI5]
The exercise ships a data file and the student's code has to read it. Nothing else on disk should become readable.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written. Until it is, use the
[Policy Reference](/instructor/policy-reference/) for the individual fields.
:::

## The situation

A test fixture or the exercise itself needs one file from `src/main/resources` or `src/test/resources`, and the policy must permit exactly that path.

## The policy fragment

The smallest `security-policy.yaml` addition that solves it, shown in full so it can be pasted
into an existing policy.

## What this still forbids

The operations the recipe deliberately does not enable, so the boundary stays visible.

## The tempting wrong version

Granting read access to the whole resources directory, or to `.`, which makes every other exercise file readable too.

## Notes

- Check the recipe against both controls: the permitted operation must succeed, and the nearest
  forbidden neighbour must still be rejected.
