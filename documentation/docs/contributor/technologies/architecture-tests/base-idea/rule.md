---
title: "Rule"
sidebar_position: 4
description: "A condition asserted over the analysed program, and what it means for one to be violated."
---

:::tip[ELI5]
A rule is a sentence about the program that must be true, such as "nothing the student
wrote may reach the code that deletes files".

The tool checks the sentence against the map. If the sentence is false, it tells you which
part of the program made it false.
:::

## What it is

A rule is a condition asserted over the nodes and edges of the program. In ArchUnit it is
written in a fluent form that reads close to English, along the lines of *no class in this
package should depend on that one*, and it is evaluated over the imported classes.

A rule that holds produces nothing. A rule that fails must say **which** node broke it,
otherwise the result is unusable on a codebase of any size.

## In Ares 2

Rules are generated from the security policy rather than written by hand. Each permitted
domain in the policy becomes a rule about which operations may be reached from student
code, and every domain that is *not* permitted becomes a deny rule.

One consequence is worth knowing: a deny-all static rule is added only while a domain has
**no** allowance at all. As soon as a domain permits one entry, the static layer can no
longer decide the question by itself, because it is argument-insensitive and cannot tell
which file or which host a call refers to. The runtime layer becomes authoritative for
that domain instead. This is why the example exercises permit exactly one file: it is what
makes the negative control actually exercise the runtime layer.

## Further reading

- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html) — TNG
- [Introduction to ArchUnit](https://www.baeldung.com/java-archunit-intro) — Baeldung
- [TNG/ArchUnit](https://github.com/TNG/ArchUnit) — source repository
