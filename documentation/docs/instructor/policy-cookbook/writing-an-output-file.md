---
title: "Writing an output file"
sidebar_position: 3
description: "Permitting creation and overwrite of one output path without granting read access."
---

:::tip[Simple Story]
The pupil has to produce one sheet of work. They should be able to create and replace that one sheet, and nothing else.
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

Permitting overwrite on a directory rather than a path, which allows replacing files the exercise depends on.

## Notes

- Check the recipe against both controls: the permitted operation must succeed, and the nearest
  forbidden neighbour must still be rejected.
