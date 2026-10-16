---
title: "Verifying the Secure Baseline"
sidebar_position: 5
description: "How each operation the secure baseline allows without a policy entry is handled and proven on every backend."
---

:::tip[Simple Story]
The board of education does not have to write "may sharpen a pencil" on the checklist. Some
things every pupil does are fine without asking, and some things that look just as small never
are.

This page is the record that both ways of watching the pupil, the one at every desk and the one at
the pencil sharpener itself, wave the same small things through and stop the same things. Where the
two genuinely differ, the difference is written down by name, so a new one cannot slip in unseen.
:::

The secure baseline is what a policy with no file entries still allows. The instructor-facing list
is [What works without an entry](/instructor/policy-reference/file-system-access#what-works-without-an-entry).
This page records how each item is decided on each backend and which test proves it, so a change
to one backend that the other does not follow fails the build.

## The parity test

`BaselineExemptionParityTest` (`src/test/java/de/tum/cit/ase/ares/api/aop/java/`) sends the same
call, with the same arguments and the same allow-list, through the real file-system check of both
runtime backends, from a supervised frame (`example.student.BaselineAdviceProbe`). Each row states
the verdict expected from each backend. A row whose two verdicts differ must be named in
`INTENDED_ASYMMETRIES`, and every name there must still differ, so fixing a difference forces its
entry to be deleted. Today the list is empty: every verdict row agrees.

The same class compares the constants the backends duplicate:

- the native-library suffixes trusted under `java.home`, equal on both;
- the random-seed devices, named on instrumentation only (see below);
- no list of file names allowed wherever they lie, on either backend;
- the calls ArchUnit and WALA allow without an entry, equal once both are written in one notation.

## What each backend decides

| Operation | Instrumentation | AspectJ | Proven in all four modes by |
| --- | --- | --- | --- |
| Read under `java.home` | Allowed: trusted start-up root | Allowed: same root, from the JVM's start-up record | Parity test; `BaselineStandardAllowTest` (certificates, locale and charset data) |
| Create, replace or delete under `java.home` | Refused | Refused | Parity test |
| Load a JDK native library by name or under `java.home` | Allowed | Allowed | Parity test |
| Read a `.jar` in the start-up Maven repository | Allowed | Allowed | Parity test; `JceGeneratedExerciseTest` (start-up repository only) |
| Read an entry of an already-open `JarFile`/`ZipFile` | Allowed; opening it is checked | Allowed; opening it is checked | Parity test |
| `SecureRandom`, `UUID.randomUUID()` | Allowed while the JDK's own seed class reads the device | Nothing to allow: the JDK's read is not woven | `BaselineStandardAllowTest`; `BaselineStillDeniedTest` (direct read, provider callback) |
| System timezone | Nothing to allow: read natively or under `java.home` | Same | `BaselineStandardAllowTest`; `BaselineStillDeniedTest` (direct read) |
| Temporary file without a directory | Allowed in the directory fixed at start-up | Allowed in the start-up directory | Parity test; `BaselineStandardAllowTest`; `BaselineStillDeniedTest` (named directories, links, a planting file attribute) |
| `Files.createTempDirectory` | Refused | Refused | Parity test; `BaselineStillDeniedTest` |
| A file named like one of Ares's own files | Checked like any other file | Checked like any other file | Parity test |
| A student record | Rewritten for thread and monitor calls; its generated methods pass through | Woven like any class | `BaselineStandardAllowTest.test_useRecord…`, `ThreadSystemAccessTest.test_recordStartsThread…`, `JavaInstrumentationRecordCallSiteTest` |

## Named differences

- **Random-seed devices.** Instrumentation sees the JDK opening `/dev/urandom` while seeding and
  allows exactly that; AspectJ never sees it. A student opening the device directly is refused on
  both. The reason AspectJ cannot see the JDK's own calls is
  [section 2 of the weaknesses page](aspectj-vs-instrumentation-weaknesses.md#2-jdk-internal-call-sites-cannot-be-woven).
- **A thread started through a method reference.** Refused on instrumentation, missed by AspectJ;
  this is an AspectJ weakness outside the baseline, listed in
  [section 3 of the weaknesses page](aspectj-vs-instrumentation-weaknesses.md#3-indirect-reflective-and-library-mediated-calls-escape).

## Removed name-based exemptions

Two exemptions once matched a file by its name alone and were removed on both backends:

- the JDK's cryptography policy file names (`default_local.policy` and the like): genuine
  cryptography reads them under `java.home`;
- six Ares file names (`messages.properties`, `EssentialClasses.yaml` and others), matched by their
  ending for every action: Ares reads its own files itself, outside any student call, and the
  re-entrancy guard skips file access made while a check is running.

The parity test has a row for each, and `PrecompileTest` checks that a generated exercise's copy
of the advice carries neither list.

## Running the evidence

```bash
mvn test -Dtest=BaselineExemptionParityTest,JavaInstrumentationRecordCallSiteTest
mvn test -Dtest='BaselineStandardAllowTest,BaselineStillDeniedTest,ThreadSystemAccessTest'
mvn test -Dtest='PrecompileTest,JceGeneratedExerciseTest'
```

The integration tests carry one method per mode combination (`…MavenArchunitAspectJ`,
`…MavenArchunitInstrumentation`, `…MavenWalaAspectJ`, `…MavenWalaInstrumentation`).
