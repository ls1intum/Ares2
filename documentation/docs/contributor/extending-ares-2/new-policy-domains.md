---
title: "New policy domains"
sidebar_position: 4
description: "Adding a permission domain beyond the current eight, from the YAML field to every enforcement layer."
---

:::tip[ELI5]
A policy domain is one category of thing student code might try to do, such as touching files or
opening sockets.

Adding one means changing every layer that has an opinion about it.
:::

:::note[This page is a stub]
The outline below is the intended structure and is not yet written.
:::

## The model

The record under `policy/policySubComponents`, its validation, and where it is normalised.

## The reader

Parsing, the version gate, and rejecting values the enforcement layers cannot honour.

## The creator

Which test-case families the domain produces: architecture, AOP, Phobos, or a combination.

## The enforcement layers

The architecture rule, the AOP advice and toolbox, and the Phobos configuration section. A
domain that is expressible but enforced by only some layers must say so explicitly in its
documentation.

## The documentation contract

One page under [Policy Reference](/instructor/policy-reference/) for what an instructor writes,
and one page in the contributor [Policy](../policy/file-system-access.md) section for how it is
enforced. Both are pinned by the documentation structure tests.

## Verification

Positive and negative controls in each layer that claims to enforce the domain.

## Notes

- The asymmetry to avoid: a field that parses and validates but is never dispatched. Timeouts
  are the current example of a domain that is generated but not yet executed in Postcompile.
