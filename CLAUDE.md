# CLAUDE.md

@AGENTS.md

That import is this file's content. `AGENTS.md` holds the conventions this repository is
held to, written for whoever is doing the work, and a rule restated here would be a second
copy free to drift from the first. What this file adds below the reminders is orientation
rather than convention: what the project is, how it is built, and where its parts live.

What follows are deliberately abbreviated reminders of the two conventions an agent gets
wrong most often. Both are stated in full above, and where the short form and the full one
disagree, the full one is right.

- `gh pr create --body` bypasses `.github/PULL_REQUEST_TEMPLATE.md` silently, so read that
  file before writing a body, and check the body before opening the pull request with
  `PR_BODY="$(cat body.md)" java .github/scripts/CheckPullRequestTemplate.java`.
- A required heading, a character limit or a whole-section phrase changed in the template
  has to change in `.github/scripts/CheckPullRequestTemplate.java` in the same commit.
  Nothing detects the two files drifting apart.

## Project Overview

Ares 2 is a framework for the secure remote execution of student submissions on interactive
learning platforms such as Artemis. It is the second Java implementation of the Secure COder
Remote Execution (SCORE) framework.

Ares 2 is **itself the security boundary**. That single fact drives most of the conventions
in `AGENTS.md`. A false negative lets forbidden student code through; a false positive fails
a correct submission. Both are serious, and neither is caught by a test that only checks the
happy path, so a change to enforcement needs a positive test (an allowed operation still
works) and a negative one (a forbidden operation is still rejected).

Its main features are policy-based sandboxing (static analysis plus runtime instrumentation),
limits on time, threads and IO, support for hidden tests obeying a custom deadline, and
utilities for improved feedback.

## Tech Stack

- **Language**: Java, built with **JDK 21**, targeting **Java 17**
  (`maven.compiler.source` / `maven.compiler.target`), so exercises on Java 17 stay supported
- **Build**: Maven 3.9+
- **Testing**: JUnit 5, jqwik (property-based)
- **Enforcement**: AspectJ and Java instrumentation (AOP layer), ArchUnit and WALA
  (architecture layer)
- **Formatting**: Spotless with Eclipse rules; **tabs, 4 spaces per tab**
- **Static analysis**: Checkstyle, PMD, CPD, SpotBugs, all as failing gates
- **Documentation**: Docusaurus in `documentation/`, published to GitHub Pages

## Build & Development Commands

```bash
mvn clean package -DskipTests    # build without the suite
mvn spotless:apply               # format
mvn spotless:check               # verify formatting, as CI does
mvn checkstyle:check             # Checkstyle gate
mvn pmd:check                    # PMD and CPD gate
mvn spotbugs:check               # SpotBugs gate
```

The rule sets live in `.settings/`. They are deliberately trimmed so no rule contradicts the
formatter and no rule objects to a deliberate security idiom. Every finding is a build
failure, not a warning.

### Testing

```bash
mvn test                                  # whole suite
mvn test -Punit-core-tests                # unit tests
mvn test -Punit-architecture-tests        # architecture unit tests
mvn test -Pintegration-core-tests         # core integration tests
mvn test -Dtest=SomeTest#someMethod       # a single test
```

**Four mode combinations must be considered.** CI runs the integration suite across
ArchUnit + AspectJ, ArchUnit + instrumentation, WALA + AspectJ and WALA + instrumentation. A
change to the architecture or AOP layer is not verified until it has been checked against
every combination it can affect.

Pipe test output to a file rather than re-running to inspect a failure:

```bash
mvn test 2>&1 | tee /tmp/ares_test_output.txt | tail -30
```

### Documentation

```bash
cd documentation
corepack enable                  # one-time: activate the pinned pnpm version
pnpm install --frozen-lockfile
pnpm start                       # dev server
pnpm run build                   # production build, as CI runs it
pnpm run diagrams                # re-render PlantUML sources to committed SVGs
```

The site builds with `onBrokenLinks` and `onBrokenAnchors` set to `throw`. A dangling
cross-reference fails the build, so run `pnpm run build` before pushing documentation
changes. Rendered PlantUML SVGs are committed next to their `.puml` sources (mirroring the
`.drawio` / `.drawio.png` convention) and CI fails if they have drifted.

### The Simple Story storyline

Every documentation page opens with a `:::tip[Simple Story]` box, and all of them share one story. A
page should lean on the cast below rather than inventing a metaphor of its own.

- **Pupils**: the methods the student wrote.
- **The board of education**: the instructor who set the exercise. Writes the paper, the mark
  scheme and the checklist. The teacher decides none of it.
- **The paper and the mark scheme**: the test methods, including the AST structural
  requirements ("solve it without a loop"). AST is explicitly not a security boundary, so it
  belongs here and never on the checklist.
- **The teachers**: JUnit Jupiter and jqwik. One asks prepared questions. The other invents
  hundreds on the spot, then shrinks to the simplest question the pupil still fails.
- **The checklist**: Ares, as configured by `security-policy.yaml`. One document in three
  parts, matching the three lists the director produces:
  1. **The room**, arranged before anyone is let in: Phobos. A desk with dividers, so forbidden
     paths are absent rather than merely refused; someone outside the door vetting every
     message that leaves; a clock in the corridor. None of it reachable from the chair, which
     is the point.
  2. **Reach**, at each desk before the question is put: the architecture layer. It establishes
     that a route exists but cannot see what waits at the end of it, so any route fails
     outright. There is no permitted version of this one. It reads **compiled bytecode, not
     source**, so it sees what the compiler produced, including methods nobody typed. Only the
     mark scheme (AST) reads what the pupil actually wrote; never describe the reach check as
     reading what they wrote.
  3. **Use**, while the pupil works: the AOP layer. It sees the actual thing being reached for,
     so the verdict is conditional on what the checklist allows.
- **A desk visit**: one run of one test method. **In Postcompile** the question and the
  checklist arrive together, because Ares prepares that visit's checks beforehand and clears
  the active settings away afterwards, on the failure path too. Never state one mechanism as
  though it were universal: Jupiter uses `BeforeTestExecutionCallback` and `afterTestExecution`,
  jqwik hooks the same lifecycle through `AroundPropertyHook`, and Precompile generates one
  project-wide set outside the run altogether, so there is nothing to prepare per visit. A
  visit is also not one pupil: a test method may exercise several student methods. What Ares
  generates is a **security test case**; what the instructor wrote is a **test method**. Keep
  those two apart.
- **The examination**: the whole round of desk visits, that is, the test run.
- **The teacher's keys**: the test classes named in `theFollowingClassesAreTestClasses`. Ares
  combines them with its own essential classes and turns the result into the derived
  `ClassPermission` exemptions. There is no `classPermission` field to write.
- **Marks read out now, or kept back**: `@PublicTest` and `@HiddenTest`.
- **Vouching for the checklist itself**: the build. Ares cannot guard the door it stands
  behind.

Keep the honest limits inside the story instead of papering over them. Phobos covers filesystem,
network and timeout only, and in Postcompile its cases are generated and never dispatched.

Write "they", never "he/she". Every box belongs to the frame, but the weight varies: the
conceptual pages get a full scene, while the procedural walkthroughs (`installation.md`, the
build-tool paths, `github-packages.md`, the migration pages) get one framing sentence and then
get on with the steps. No box may depend on another having been read: search drops readers on
arbitrary pages, so each has to stand alone.

## Project Structure

Server code lives under `src/main/java/de/tum/cit/ase/ares/`:

- `api/` — the public API consumed by exercise test repositories (annotations such as
  `@Policy`, `@Deadline`, `@StrictTimeout`, `@MirrorOutput`, plus the test context)
- `api/aop/` — aspect-oriented enforcement: AspectJ and instrumentation advice for the file,
  command and thread subsystems
- `api/architecture/` — static architecture analysis via ArchUnit and WALA
- `api/ast/` — AST-based code analysis
- `api/policy/` — security policy model, readers and directors
- `api/securitytest/` — the test-case factory and builder that turns a policy into generated
  security tests
- `api/buildtoolconfiguration/` — Maven and Gradle integration
- `api/io/` — console IO testing utilities
- `api/jqwik/`, `api/jupiter/` — test-framework integration
- `api/localization/` — internationalisation
- `api/internal/` — internal infrastructure

Tests are in `src/test/java/`, examples in `examples/`, documentation in `documentation/`.

## Coding Conventions

- **Tabs for indentation**, 4 spaces per tab, in Java and XML (Spotless enforces this)
- No wildcard imports; import order comes from `.settings/eclipse-rules.importorder`
- PascalCase for classes, camelCase for fields and methods
- Never declare more than one attribute or method per line
- Prefer records and pattern matching where Java 17 allows
- LF line endings, UTF-8, final newline (see `.editorconfig`)

Javadoc is not a matter of taste here; the rule is in `AGENTS.md` under Documenting Java.
