---
title: "Postcompile or Precompile"
sidebar_position: 2
description: "Which mode to migrate an Ares 1 exercise onto, and what the choice costs you."
---

:::tip[ELI5]
Ares 1 worked one way only. Ares 2 gives you two, and you have to pick one before touching the
build.

For a migration the answer is almost always Postcompile.
:::

## The short answer for a migration

**Migrate onto Postcompile.** An Ares 1 exercise already carries Ares as a dependency and already
activates enforcement from its test classes, so Postcompile is the shape it is closest to: you
replace the dependency, rewrite the imports, translate the annotations into a policy, apply
`@Policy` and keep an Ares test annotation on every test that is to be supervised. The structure
of the exercise does not change.

`@Policy` is not itself a JUnit extension and registers nothing. What activates Ares is the
test-type annotation (`@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest`), so a test carrying a
plain JUnit `@Test` and a `@Policy` runs entirely unsupervised, and it does so silently.

Precompile is a different deployment model rather than a different setting. The exercise stops
depending on Ares and instead receives generated artefacts from an external run. That is a
worthwhile target, but it is a second migration on top of this one, not a variant of it.

## What the two modes are

The full comparison is on
[Precompile or Postcompile](../protect-a-java-project/precompile-or-postcompile.md). In short:

| | Postcompile | Precompile |
| --- | --- | --- |
| Ares in the exercise | a dependency | not needed after generation |
| Activated by | the test cases, through an Ares test annotation (`@Public`, `@Hidden`, `@PublicTest`, `@HiddenTest`); `@Policy` only selects and configures the policy | the exercise's own build |
| Policy granularity | per test method | one generated set per project |
| Changing the policy | edit and rerun | regenerate and rebuild |
| Closest to an Ares 1 exercise | **yes** | no |

## What does not depend on the choice

Three parts of the migration are the same either way, and all three are on
[the common page](./index.md): the import rewrite, the annotation-to-policy translation, and the
class-shadowing guard.

The guard is worth singling out. Ares 1 had a `forbiddenPackageFolders` assertion; Ares 2 replaces
it with the versioned reserved-package boundary. **Ares generates that boundary in neither mode**,
so you install it by hand from the shipped snippets whichever mode you migrate onto. It is not
optional: without it a student can declare a class in a package Ares trusts and be trusted along
with it.

## Then pick your leaf

- [Postcompile: Gradle](./postcompile/gradle.md) or [Postcompile: Maven](./postcompile/maven.md)
- [Precompile: Gradle](./precompile/gradle.md) or [Precompile: Maven](./precompile/maven.md)
