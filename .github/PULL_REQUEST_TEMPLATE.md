<!-- markdownlint-disable-file MD041 -->
<!--
  Thanks for contributing to Ares 2.
  Fill in every section. Each section states what to write when it does not apply.
  Tick boxes as [x], not [ x] and not [x ].
  Write in British English.

  Each recurring instruction is repeated, in the same words, in every section where it
  applies, so that reading the one section you are filling in is enough. They close every
  section comment, after whatever that section says for itself, and always in this order:
  1. "This section is always required" says so, and then says what to write when it does
     not apply to your change. Every section is required, so a section that does not
     apply is answered rather than deleted.
  2. "Limit" says how long the section may be, where there is a limit.
  3. "Simple words" says who has to be able to follow the section.
-->

## Summary

<!--
  What changes, and why it matters. No implementation detail.

  This section is always required. There is no change it does not apply to.

  Limit: 500 characters, counted over the text left once every instruction comment such
  as this one is removed, so keeping the comment costs nothing.

  Simple words: write this so that an instructor who does not know the inside of Ares can
  follow it. Spell out any Ares term you cannot avoid. Say less, not more: a reviewer who
  cannot follow a short answer will ask, and the detail belongs in the code or in the
  linked issue.
-->

## Linked issues

<!--
  For example "Closes #123" or "Relates to #456".

  This section is always required. If this pull request relates to no issue, write
  "No linked issues".

  Limit: 1000 characters, counted over the text left once every instruction comment such
  as this one is removed, so keeping the comment costs nothing.
-->

## 1. Problem

<!--
  What is wrong today?
  Useful to cover:
  - What did you see, and in which setup (Java version, Maven or Gradle, AspectJ or
    instrumentation, ArchUnit or WALA)?
  - What should have happened instead?
  - Which part of Ares is at fault? Name it. Examples, not a complete list: reading the
    security policy, writing the security test out of it, blocking a forbidden call while
    the code runs (AspectJ or instrumentation), finding a forbidden call or package
    without running the code (ArchUnit or WALA), reading the shape of the source itself
    (the AST check, for example a forbidden statement or unwanted recursion), keeping
    student code out of the packages Ares reserves for itself, generating the files an
    external Phobos sandbox runs a submission with, checking that a submission has the
    classes, methods, fields and annotations it should (the structural check), the
    console input and output testing, the dynamic access API, the wording a student is
    shown (localisation), how Ares hooks into JUnit or jqwik, or how it is plugged into
    the build (Maven or Gradle). If the part you mean is not listed, name it in your own
    words. A reviewer of a security tool needs to know where to look, so name it even
    where the rest stays plain.
  - Why does it matter? If something is broken, say which way round it went: Ares let
    forbidden student code through, or it failed a correct submission.

  This section is always required. If nothing is broken, describe the gap or the extra
  work that made you open this pull request instead.

  Limit: 1000 characters, counted over the text left once every instruction comment such
  as this one is removed, so keeping the comment costs nothing.

  Simple words: write this so that an instructor who does not know the inside of Ares can
  follow it. Spell out any Ares term you cannot avoid. Say less, not more: a reviewer who
  cannot follow a short answer will ask, and the detail belongs in the code or in the
  linked issue.
-->

## 2. Improvement from the user's perspective

<!--
  Users are everyone who uses Ares: students whose submissions run under a security
  policy, tutors who have to understand the feedback Ares produces in order to help those
  students, and editors and instructors who write those policies and ship Ares inside an
  exercise test repository.
  Say what gets better for them, for example a clearer failure message, fewer correct
  submissions failed by mistake, a rule that could not be written before, a faster test
  run, or a newly supported language or build tool.

  This section is always required. If this side gains nothing from this pull request,
  write "No Improvement from the user's perspective".

  Limit: 1000 characters, counted over the text left once every instruction comment such
  as this one is removed, so keeping the comment costs nothing.

  Simple words: write this so that an instructor who does not know the inside of Ares can
  follow it. Spell out any Ares term you cannot avoid. Say less, not more: a reviewer who
  cannot follow a short answer will ask, and the detail belongs in the code or in the
  linked issue.
-->

## 3. Improvement from the maintainer's perspective

<!--
  Maintainers are everyone who works on Ares itself: contributors who change the code,
  reviewers who read those changes and have to judge whether the security boundary still
  holds, and whoever publishes the release an exercise then depends on.
  Say what gets better for them, for example less duplicated code, a clearer structure, a
  flaky test removed, better error output, less manual release work, or a simpler
  dependency or CI setup.

  This section is always required. If this side gains nothing from this pull request,
  write "No Improvement from the maintainer's perspective".

  Limit: 1000 characters, counted over the text left once every instruction comment such
  as this one is removed, so keeping the comment costs nothing.

  Simple words: write this so that an instructor who does not know the inside of Ares can
  follow it. Spell out any Ares term you cannot avoid. Say less, not more: a reviewer who
  cannot follow a short answer will ask, and the detail belongs in the code or in the
  linked issue.
-->

## 4. Testing manual

<!--
  Write these steps so that a reviewer who did not write the code can follow them from a
  cold start. Where possible, begin from the perspective "I have an Ares exercise".

  Fewest steps, fewest tools. Count what you are asking for before you ask: every install,
  every account, every command line is a reason the manual goes untried, and a change
  nobody tested is a change nobody reviewed.

  Prefer a manual a reviewer can finish in the browser alone. Where that is possible it is
  the whole manual, and a terminal route belongs below it as an optional extra rather than
  as a step. Where a terminal cannot be avoided, ask for one tool rather than three, name
  every tool the steps assume, and give each command in full, including how to produce
  whatever it needs: a reviewer who has to invent a file, a number or a flag you left out
  is writing the manual for you.

  Describe the action, not the controls it happens to use today: "edit the description and
  save it" outlives "press the three dots, choose Edit, then press Update comment", and so
  does every other sentence that names what a reviewer is doing rather than what the
  interface currently calls it. Before sending a reviewer to a result, check what they can
  see with the sign-in state and the access they are likely to have, which is not yours.

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

  Steps: numbered, one observable result per step, with the exact commands where there are
  commands. A step a reviewer cannot check the outcome of is setup, not a step.

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

  Have the reviewer look at the result, not at an exit code. A command that exits zero
  says the command ran. It does not say that what it produced is right, and a manual made
  of green commands asks a reviewer to review your exit codes rather than your change.
  Every claim in section 2 needs a step where the thing itself is in front of them: the
  page as a reader gets it, the message a student is shown, the generated file, the row in
  the report. Name what they must see there, and name what would be wrong.

  This matters most where a build cannot see the defect. A site builds cleanly while a box
  renders as plain text, a policy is read without complaint while the rule it was meant to
  express is not enforced, and a test passes while asserting nothing. Where a suite in this
  repository already looks at such a result for you, run it as a step and say what it
  covers, rather than leaving a reviewer to assume the build covered it.

  Negative case: equally important for a security tool. State what must still be
  rejected, and how a reviewer confirms that Ares has not become more permissive.

  A step nobody can follow is a step nobody runs.

  The limit below covers this whole section, the modes at the end of it included.

  This section is always required. If the change cannot be exercised from an exercise
  (for example a CI workflow, build or documentation change), write "Not reproducible
  from an exercise" under Steps and describe instead how a reviewer verifies the change,
  for example which workflow run to inspect.

  Limit: 5000 characters, counted over the text left once every instruction comment such
  as this one is removed, so keeping the comment costs nothing.

  Simple words: write this so that an instructor who does not know the inside of Ares can
  follow it. Spell out any Ares term you cannot avoid. Say less, not more: a reviewer who
  cannot follow a short answer will ask, and the detail belongs in the code or in the
  linked issue.
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

  This part is always required. If the change cannot alter mode-specific behaviour, tick
  nothing and write "No mode-specific behaviour changed".
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

  This section is always required. If this pull request changes no production Java code
  (documentation, CI, build configuration or tests only), replace the table with "No
  production Java code changed".
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

  This is the section an instructor reads before upgrading.

  This section is always required. If the change is fully backwards compatible, write
  "No breaking changes or migration".

  Limit: 1000 characters, counted over the text left once every instruction comment such
  as this one is removed, so keeping the comment costs nothing.

  Simple words: write this so that an instructor who does not know the inside of Ares can
  follow it. Spell out any Ares term you cannot avoid. Say less, not more: a reviewer who
  cannot follow a short answer will ask, and the detail belongs in the code or in the
  linked issue.
-->

No breaking changes or migration.

## Checklist

<!--
  Tick what you have actually done, not what you intend to do. Each box is a claim a
  reviewer may check, and an untrue tick costs more trust than an untidy pull request.
  A task that applies but is not done stays unticked, with the reason said out loud in
  the section it belongs to, rather than ticked to make the list look finished.

  This section is always required. If a task does not apply, wrap its line in an HTML
  comment and state the reason inside the comment, rather than deleting or unticking it.
-->

- [ ] The title of this pull request describes the change, not the implementation.
- [ ] I followed the [guidelines for inclusive, diversity-sensitive and appreciative language](https://docs.artemis.tum.de/developer/guidelines/language).
- [ ] I have self-reviewed the diff of this pull request.
- [ ] Tests were added or updated for the behaviour changed here.
- [ ] Javadoc follows the [AGENTS.md](AGENTS.md#documenting-java) conventions.
- [ ] Documentation (`documentation/`, `README.md`, Javadoc) was updated where the change is user-facing.
- [ ] CI is green, or every remaining failure is explained above.
- [ ] No secrets, tokens or absolute local paths are contained in the diff.

## Review progress

<!--
  Reviewers tick what they have reviewed. Both boxes should be ticked before merge.

  This section is always required. If a category does not apply, wrap its line in an HTML
  comment and state the reason inside the comment, rather than deleting it.
-->

- [ ] Code review
- [ ] Manual test
