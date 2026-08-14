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
