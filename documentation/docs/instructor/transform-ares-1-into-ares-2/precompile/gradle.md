---
title: "Gradle"
sidebar_position: 1
description: "Migrating an Ares 1 Gradle exercise straight onto Ares 2 in Precompile mode. Not yet written."
---

:::tip[ELI5]
This would take an Ares 1 Gradle exercise directly to the generated-artefact model, skipping the
dependency-based one.

Nobody has written that path down yet, so this page says what it would have to cover.
:::

:::note[This page is a stub]
The Ares 1 migration guide this section was built from describes exactly one target, and it is
the Postcompile one: replace the dependency, rewrite the imports, translate the annotations, apply
`@Policy`. It contains no Precompile material at all, so there is nothing here to relocate and
this page is new documentation rather than moved prose.

Until it is written, migrate onto [Postcompile: Gradle](../postcompile/gradle.md) first.
That path is documented and verified, and a Precompile conversion afterwards is then an ordinary
[Precompile setup](../../protect-a-java-project/precompile/gradle.md) rather than a
migration.
:::

## What this page will cover

Going from an Ares 1 exercise to a generated Precompile exercise in one step: removing the Ares 1
dependency without adding an Ares 2 one, translating the annotations into the policy the generator
reads, running the generator, and wiring the generated artefacts into `build.gradle`.

## What already applies unchanged

Two parts of the migration do not depend on the mode and are documented today:

- the [import rewrite and the annotation-to-policy translation](../index.md);
- the class-shadowing guard. Ares generates the reserved-package boundary in **neither** mode, so
  the Gradle snippet from [Postcompile: Gradle](../postcompile/gradle.md) applies here
  without change.

## What has to be established first

Whether a one-step migration is worth documenting at all, or whether migrating to Postcompile and
converting afterwards should stay the supported route. That decision belongs in the
[Precompile setup pages](../../protect-a-java-project/precompile/gradle.md), which are
themselves still being written.

## Notes

- There is no runnable Precompile example in the repository yet, so this page cannot be verified
  against one.
