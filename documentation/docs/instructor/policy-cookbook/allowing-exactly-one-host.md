---
title: "Allowing exactly one host"
sidebar_position: 5
description: "Permitting one host and port, and why a wildcard host is almost never what you want."
---

:::tip[Simple Story]
The examination requires one telephone call. Exactly that number should be reachable, and no other.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written. Until it is, use the
[Policy Reference](/instructor/policy-reference/) for the individual fields.
:::

## The situation

A networking exercise needs one endpoint, and the policy must name the host and the port rather than opening a range.

## The policy fragment

The smallest `security-policy.yaml` addition that solves it, shown in full so it can be pasted
into an existing policy.

## What this still forbids

The operations the recipe deliberately does not enable, so the boundary stays visible.

## The tempting wrong version

A wildcard host or an open port range, which turns a networking exercise into unrestricted outbound access.

## Notes

- Check the recipe against both controls: the permitted operation must succeed, and the nearest
  forbidden neighbour must still be rejected.
