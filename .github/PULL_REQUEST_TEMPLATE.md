<!--
  Thanks for contributing to Ares 2.
  Fill in every section. Each section states what to write when it does not apply.
  Tick boxes as [x], not [ x] and not [x ].
  If a checklist task does not apply, wrap that line in an HTML comment and state the
  reason inside the comment, so the diff still records that the task was considered.
-->

## Summary

<!--
  At most three lines: what changes, and why it matters. No implementation detail.
  This section is always required.
-->

## Linked issues

<!--
  For example "Closes #123" or "Relates to #456".
  If this pull request does not relate to any issue, write "None".
-->

## 1. Problem

<!--
  Describe in depth the problem that led to this pull request.
  Useful to cover:
  - What behaviour was observed, and under which configuration
    (Java version, build tool, AOP mode: AspectJ or instrumentation,
    architecture mode: ArchUnit or WALA, operating system)?
  - What was the expected behaviour instead?
  - What is the root cause? Ares is itself the security boundary, so state whether the
    cause sits in the policy layer, the generated security test, the enforcement
    (AOP or architecture) layer, or the build integration.
  - Why does it matter? A false negative lets forbidden student code through, a false
    positive fails a correct submission. Say which of the two this is.

  This section is always required. If this pull request is not driven by a defect,
  describe the gap, limitation or maintenance burden that motivated it instead.
-->

## 2. Improvement from the user's perspective

<!--
  Users are everyone who consumes Ares: students whose submissions run under a security
  policy, and instructors who author policies and ship Ares inside an exercise test
  repository.
  Describe the concrete benefit, for example clearer failure messages, fewer false
  positives, a policy option that was previously impossible to express, a faster test
  run, or a newly supported language or build tool.

  If this side does not benefit from this pull request, write "No Improvement".
-->

## 3. Improvement from the maintainer's perspective

<!--
  Maintainers are those who develop Ares itself.
  Describe the benefit for them, for example reduced duplication, a clearer abstraction,
  a flaky test removed, better diagnostics, less manual release work, or a dependency
  or CI simplification.

  If this side does not benefit from this pull request, write "No Improvement".
-->

## 4. Testing manual

<!--
  Write these steps so that a reviewer who did not write the code can follow them from a
  cold start. Where possible, begin from the perspective "I have an Ares exercise".

  Start from a runnable exercise, do not make the reviewer build one. `examples/`
  contains `ares-exercise-gradle` and `ares-exercise-maven`; point at one of them and
  describe only the delta. A reviewer who has to guess how to wire up Ares is testing
  their own setup rather than your change.

  Name the build tool explicitly, and prefer the one the affected users have. Most
  Artemis Java exercises are Gradle; a Maven-only manual leaves a Gradle reviewer
  translating as they go. If the change is build-tool independent, say so.

  Prerequisites: which Ares version or branch to build and install, which exercise or
  test repository and which security policy file to use, and any environment requirement
  (JDK, Maven or Gradle, the echo server on port 25565 for network tests, see AGENTS.md).

  Steps: numbered, one action per line, with the exact commands.

  Use only platform-independent paths. `/etc/hosts` does not exist on Windows, and
  neither do `/tmp` or `~/.bashrc` in the form you expect. Create a file such as
  `secret.txt` in the project directory instead.

  Expected result: state it per step, not once for the whole scenario. "Run `mvn test`
  again" tells a reviewer nothing about what they are looking for; "run `mvn test` again
  and confirm that PenguinTest.name() still executes, while Spoof.grab() is neither
  recognised nor executed as a test method" does. Say what must be observable, and where
  it is observable: name the log line, the report file or the build output the reviewer
  should read. If a claim cannot be observed anywhere, either add the diagnostic that
  makes it observable, or do not ask for it.

  Negative case: equally important for a security tool. State what must still be
  rejected, and how a reviewer confirms that Ares has not become more permissive.

  If the change cannot be exercised from an exercise (for example a CI workflow, build
  or documentation change), write "Not reproducible from an exercise" under Steps and
  describe instead how a reviewer verifies the change, for example which workflow run
  to inspect.
-->

**Prerequisites**

1.

**Steps**

1.

**Expected result**

**Negative case (what must still be rejected)**

**Modes exercised**

<!--
  Ares runs four combinations in CI. Tick the ones you verified, and say below why a
  subset is sufficient if you did not verify all four.
  If the change cannot alter mode-specific behaviour, tick nothing and write
  "No mode-specific behaviour changed".
-->

- [ ] ArchUnit + AspectJ
- [ ] ArchUnit + instrumentation
- [ ] WALA + AspectJ
- [ ] WALA + instrumentation

## 5. Test case coverage regarding this PR

<!--
  Coverage is produced by the "Coverage Report" job of the Maven workflow. It merges the
  JaCoCo execution data of every test job, publishes an aggregated table in the job
  summary and uploads a "coverage-report" artefact containing the HTML and CSV report.
  List every class this pull request adds or changes non-trivially, and leave out rows
  for purely cosmetic changes.

  Report every counter JaCoCo produces per class, not lines alone. Read them from
  `site/jacoco/jacoco.csv` inside the artefact, where each counter is a MISSED/COVERED
  column pair: INSTRUCTION, BRANCH, LINE, COMPLEXITY and METHOD. Give each as a
  percentage with the raw counts behind it, and write those counts as COVERED out of
  MISSED plus COVERED rather than as the raw pair, for example `81.0% (272/336)` for a
  class whose LINE_COVERED is 272 and whose LINE_MISSED is 64, so a reviewer can
  recompute the row. Where a counter has no total at all, for example a class without
  branches, write `n/a (0/0)` rather than 100%.

  Lines alone hide what matters here. Ares is itself the security boundary, so an
  untaken branch is a decision that was never enforced under test, and the advice and
  rule classes are mostly branches. A class can read as well covered by line and still
  have half of its denial paths never taken; branch coverage is what says so. Method
  coverage shows how many methods nothing reached at all, and complexity summarises how
  much of the remaining execution-path space is still untested.

  The last column confirms that the covered lines are backed by meaningful assertions,
  not merely executed.

  Note that the aggregated figures are repository-wide and that the JaCoCo thresholds
  are currently advisory, so they do not fail the build. Only `src/main/java` is
  reported on, so test classes never appear in the report and do not belong in the
  table, even though the agent does instrument the ones under `de.tum.cit.ase.ares.api`.

  If this pull request changes no production Java code (documentation, CI, build
  configuration or tests only), replace the table with "No production Java code
  changed".
-->

| Class | Instruction coverage | Branch coverage | Line coverage | Complexity coverage | Method coverage | Confirmation (meaningful assertions) |
| --- | ---: | ---: | ---: | ---: | ---: | :---: |
|  |  |  |  |  |  |  |

## Breaking changes and migration

<!--
  Ares is consumed as a released Maven artefact, so state explicitly whether this changes
  any of:
  - the public API under de.tum.cit.ase.ares.api
  - the security policy file format or its schema
  - the generated security test code that exercise repositories rely on
  - the minimum JDK, Maven or Gradle version
  If it does, describe what an instructor has to do to upgrade an existing exercise.

  If the change is fully backwards compatible, write "None".
-->

None.

## Checklist

- [ ] The title of this pull request describes the change, not the implementation.
- [ ] I followed the [guidelines for inclusive, diversity-sensitive and appreciative language](https://docs.artemis.tum.de/developer/guidelines/language).
- [ ] I have self-reviewed the diff of this pull request.
- [ ] Tests were added or updated for the behaviour changed here.
- [ ] Documentation (`documentation/`, `README.md`, Javadoc) was updated where the change is user-facing.
- [ ] CI is green, or every remaining failure is explained above.
- [ ] No secrets, tokens or absolute local paths are contained in the diff.

## Review progress

<!--
  Reviewers tick what they have reviewed. Both boxes should be ticked before merge.
  When new commits are pushed, the affected box is unchecked again. Not every change
  requires a full re-review.
  If a category does not apply, wrap its line in an HTML comment and state the reason
  inside the comment.
-->

- [ ] Code review
- [ ] Manual test
