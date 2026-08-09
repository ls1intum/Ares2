---
title: "Writing an output file"
sidebar_position: 3
description: "Permitting creation and overwrite of one output path without also granting read access."
---

:::tip[ELI5]
The student's code produces a file. It should be able to create and overwrite that one file, and nothing more.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written. Until it is, use the
[Policy Reference](/instructor/policy-reference/) for the individual fields.
:::

## The situation

The exercise asks for output to be written to a named path, and the policy must permit creation and overwrite there without turning the directory into a general-purpose scratch space.

## The policy fragment

The smallest `security-policy.yaml` addition that solves it, shown in full so it can be pasted
into an existing policy.

## What this still forbids

The operations the recipe deliberately does not enable, so the boundary stays visible.

## The tempting wrong version

Permitting overwrite on a directory rather than a path, which also allows replacing files the exercise depends on.

## Notes

- Check the recipe against both controls: the permitted operation must succeed, and the nearest
  forbidden neighbour must still be rejected.
