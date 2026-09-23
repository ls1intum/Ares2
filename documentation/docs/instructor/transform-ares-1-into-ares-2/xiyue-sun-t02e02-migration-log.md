---
title: "Migration log: T02E02 - Seal Telegraph Decoder (Xiyue Sun)"
sidebar_position: 6
description: "A second worked Postcompile Gradle migration, done after T01E02, where the lessons from the first attempt paid off and the real friction turned out to be tooling rather than Ares 2."
---

:::tip[Simple Story]
[The T01E02 log](./xiyue-sun-t01e02-migration-log.md) was the first trip, with every wrong turn
kept in. This is the second, taken straight afterward on a different exercise, carrying what the
first trip taught. Nothing in Ares 2 itself surprised this migration; what did was the tool used
to type it in.
:::

**Exercise:** T02E02 - Seal Telegraph Decoder, ITP Ares 2 Test Course. Three production classes
(`SealCitizen`, `CitizenRegistry`, `SheriffOffice`) plus a `Deed` value type, checked by a
structure oracle (`test.json`, via `AttributeTest`/`ConstructorTest`/`MethodTest`) and by three
reflection-driven behaviour test classes (`SealCitizenTest`, `CitizenRegistryTest`,
`SheriffOfficeTest`).

**Outcome:** 100% on the SOLUTION participation on the first build, reserved-package boundary v2
installed and its rejection verified, nothing left temporary in the shipped exercise.

## What T01E02 already answered

Every failure recorded in the T01E02 log was a question about Ares 2 itself: where the merged
assignment code lands in continuous integration (CI), why the structure oracle needs to be a declared resource and not
just a compiled source, why three structural-test classes needed exempting, and the
`assignmentSrcDir` property the structural tests read independently of `withinPath`. T02E02's
`build.gradle` already carried `assignmentSrcDir = "assignment/src"` and the matching
`sourceSets` block, inherited unchanged from its own Ares 1 setup, which is itself evidence: this
course's build template already used that name for its own purposes before Ares 2 gave it a
second one. None of T01E02's four failures needed rediscovering here.

## What was different about this exercise

**The Ares 1 security configuration was not applied uniformly.** T01E02's test classes each
carried their own annotations. T02E02's did not: `AttributeTest` and `SealCitizenTest` carried a
single composed annotation, `@T02E02`, that bundled `@WhitelistPath`, `@WhitelistClass`,
`@BlacklistPath`, `@StrictTimeout(3)` and `@MirrorOutput` into one reusable declaration.
`ConstructorTest` and `MethodTest` carried their own `@WhitelistPath("target")` and
`@BlacklistPath("target/test-classes")` directly, without `@T02E02` and without the timeout or
output-mirroring it would have added. `CitizenRegistryTest` and `SheriffOfficeTest` carried no
security annotation at all. Migrating this correctly meant reading each class individually rather
than assuming the exercise's pattern was uniform; a single "the guard is `@T02E02`" assumption
would have left two classes ungoverned.

Two of Ares 1's per-class annotations translated directly rather than through the policy:
`@StrictTimeout(10)` sat directly on the `generateTestsForAllClasses()` method in all three
structural test classes, overriding `@T02E02`'s class-level 3-second default for that one method,
and was kept exactly as an Ares 2 `@StrictTimeout(10)` in the same place. `SealCitizenTest` had no
such override and kept `@T02E02`'s class-level default, which became `@StrictTimeout(3)` applied
directly, since the composed-annotation approach was not carried forward (see below).

**Two more Ares 1 utility classes needed their Ares 2 counterparts confirmed, not just their
security annotations.** `de.tum.in.test.api.util.ReflectionTestUtils`, used throughout every
behaviour test for constructing and invoking student classes by reflection, has a drop-in
replacement at `de.tum.cit.ase.ares.api.util.ReflectionTestUtils` with the same method
signatures. `de.tum.in.test.api.io.IOTester`, injected as a parameter into `SheriffOfficeTest`'s
methods to capture `System.out`, has the same replacement at
`de.tum.cit.ase.ares.api.io.IOTester`, confirmed by reading both classes' source rather than
assuming the package rename was the whole story.

**The composed `@T02E02` annotation was not carried forward.** Rather than porting the Ares 1
pattern of one meta-annotation bundling `@Policy`, `@Public`, `@StrictTimeout` and `@MirrorOutput`
into an equivalent Ares 2 one, each class was given its own direct annotations. This was a
deliberate, conservative choice made without yet knowing whether Ares 2's JUnit 5 integration
resolves annotations composed this way; the [Sample Solution comparison](#comparing-against-the-sample-solution)
below confirms afterward that it does, for whoever migrates the next exercise and wants the
shorter form.

`theFollowingClassesAreTestClasses` in the resulting policy is the union of Ares 1's
`@WhitelistClass` entries (`AttributeHelper`, `HelperClass`, and the three behaviour test classes)
and the three structural-test-provider classes T01E02 already showed need the same exemption
(`AttributeTest`, `ConstructorTest`, `MethodTest`), applied uniformly through one shared
`SecurityPolicy.yaml` rather than replicating each class's original, uneven scope exactly.

One file needed deleting outright: `T02E02.java`, the Ares 1 `@interface` declaration itself,
once nothing referenced it any more. One more class, `AttributeHelper`, carried a bare `@T02E02`
with no `@Test`-family method beneath it, likely dead weight even under Ares 1; it was dropped
rather than translated, since `AttributeHelper` is exempted through
`theFollowingClassesAreTestClasses` regardless.

## The actual friction: the browser editor, not Ares 2

T01E02 was migrated entirely through Artemis's web code editor, whose Monaco-based editing
surface repeatedly auto-closed brackets in ways that left orphaned `}` and `]` characters behind,
particularly in longer Groovy files with several sibling blocks. Every one of that migration's
`build.gradle` and `AresReservedPackages.gradle` edits needed a manual scroll-to-end-of-file
cleanup pass afterward.

T02E02 started the same way and hit the same failure mode again while rebuilding
`AresReservedPackages.gradle`: a `doLast` block was, at one point, inserted a full block too
early, ahead of the `def` statements its own closure depends on, which would not have compiled;
separately, the same stray-bracket accumulation from T01E02 reappeared. Rather than continue
fighting the editor, the remaining work was redone by cloning the TESTS and SOLUTION
repositories' git URLs (available from each repository's **Code** button in Artemis) into a local
working copy, editing with ordinary file-editing tools, and pushing directly. Every file involved
in this migration was rewritten that way, and the resulting push built and passed at 100% on the
first attempt.

**The lesson this log exists to record:** for a Postcompile Gradle exercise with more than a
one- or two-line change to `build.gradle` or a generated-boundary script, cloning the repository
locally is faster and more reliable than the web editor, which has a real, repeatable bug in how
it handles nested bracket auto-closing in longer Groovy files. This is a tooling observation about
the Artemis code editor, not about Ares 2 or about anything documented on the migration pages.

## Verifying the migration

[Postcompile: Gradle § Replace the class-shadowing guard](./postcompile/gradle.md#replace-the-class-shadowing-guard)
was followed exactly, reusing the same `AresReservedPackages.gradle` snippet already proven in the
T01E02 migration. Verification here was narrower than T01E02's, since the underlying mechanism
(the boundary script, the AspectJ weaving it depends on) was already exercised end-to-end there:
a class declaring `package de.tum.cit.ase.ares.api;` was pushed to the SOLUTION repository, the
build failed with

```
Ares reserved-package validation 2 rejected student output: [de/tum/cit/ase/ares/api/EvilClass.class]. No bypass flag is supported.
```

and the class was then removed, restoring the build to 100%. The full positive/negative-control
pair and the deliberate AspectJ-weaving break that T01E02's log describes were not repeated here,
since they establish that the *mechanism* works and this migration changed nothing about how that
mechanism is wired.

## Comparing against the Sample Solution

T02E02 has a Sample Solution (exercise 21020) in the same course, read only after this
migration was pushed and passing, matching [the common page's](./index.md) instruction not to let
a reference answer shape the attempt.

**Agrees:** `assignmentSrcDir = "assignment/src"` is present, the same confirmation T01E02's
Sample Solution gave.

**Diverges, not just style, same gap as T01E02:** the Sample Solution's class-shadowing guard is
still the Ares 1 `forbiddenPackageFolders`/`doFirst` shape, checked against Ares 1-era prefixes.
Between this and [the T01E02 comparison](./xiyue-sun-t01e02-migration-log.md#comparing-against-the-sample-solution),
both Sample Solutions in this course share the gap, which is worth someone maintaining them
resolving once rather than twice.

**A confirmation this migration's log needed:** the Sample Solution kept the Ares 1 pattern of one
composed annotation (`@T02E02`, its `@interface` rewritten to Ares 2's own `@Policy`, `@Public`,
`@StrictTimeout` and `@MirrorOutput`) applied to every structural and behaviour test class alike.
It builds. That confirms Ares 2's JUnit 5 integration does resolve annotations composed this way,
which this migration deliberately left unconfirmed by not attempting it; a future migration of an
exercise with the same one-annotation-many-classes shape can use the composed form directly rather
than repeating each class's annotations individually, as this one did.

**Two points worth recording rather than copying:** the Sample Solution's
`regardingFileSystemInteractions` is an empty list, dropping the `readAllFiles` permission on
`target` that two of this exercise's own Ares 1 test classes declared directly
(`@WhitelistPath("target") // mainly for Artemis`). This migration kept that permission, since it
traces to an explicit Ares 1 declaration rather than an assumption; nothing here shows which
choice is required, only that they differ, and no test in either version depends on it either way.
Separately, the Sample Solution's policy declares `regardingTimeouts: - timeout: 3000`. Per
[Postcompile: Gradle § The alternative: no policy file at
all](./postcompile/gradle.md#the-alternative-no-policy-file-at-all), Ares 2.1.3 constructs a
timeout from Phobos but does not yet dispatch it in-process for Postcompile, so this entry is not
what is enforcing the three-second limit; the `@StrictTimeout` annotations both versions
carry are what do that. The entry is not wrong, only decorative under the current release, and a
reader comparing the two policies should not conclude the timeout domain is what to reach for.
