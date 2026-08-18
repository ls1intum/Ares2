---
title: "Using threads"
sidebar_position: 4
description: "Permitting a bounded number of threads, and what the bound does and does not cover."
---

:::tip[Simple Story]
The examination is about doing two things at once, so helpers have to be allowed, but not without limit.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written. Until it is, use the
[Policy Reference](/instructor/policy-reference/) for the individual fields.
:::

## The situation

A concurrency exercise needs thread creation permitted, with a bound that still fails a runaway submission.

## The policy fragment

The smallest `security-policy.yaml` addition that solves it, shown in full so it can be pasted
into an existing policy.

## What this still forbids

The operations the recipe deliberately does not enable, so the boundary stays visible.

## The tempting wrong version

Permitting threads without a bound, which lets a submission exhaust the runner rather than failing its own test.

## Notes

- Check the recipe against both controls: the permitted operation must succeed, and the nearest
  forbidden neighbour must still be rejected.
