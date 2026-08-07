# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

Ares 2 is a framework for the secure remote execution of student submissions on interactive
learning platforms such as Artemis. It is the second Java implementation of the Secure COder
Remote Execution (SCORE) framework.

Ares 2 is **itself the security boundary**. That single fact drives most of the conventions
below. A false negative lets forbidden student code through; a false positive fails a correct
submission. Both are serious, and neither is caught by a test that only checks the happy path.

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
- Javadoc on public API, since instructors read it directly from their IDE
- LF line endings, UTF-8, final newline (see `.editorconfig`)

## Security-Specific Rules

- **A sandboxed test JVM must never start its own server** to test incoming or outgoing
  connections. Ares is the boundary under test, so a fixture inside the same JVM is subject
  to the active policy and its failure cannot be attributed. See [AGENTS.md](AGENTS.md) for
  the full rule.
- Outgoing-connection tests target an **external echo server** on loopback port `25565`, and
  **skip** (`Assumptions.abort`) when it is unreachable. CI provides the server.
- An Ares `SecurityException` on an explicitly allowed operation is always a real failure and
  must propagate. Never swallow or skip it.
- When adding enforcement, add both a positive test (allowed operation still works) and a
  negative test (forbidden operation is still rejected). A security change verified only by
  its positive case is unverified.

## Commit & PR Guidelines

- Concise, imperative commit messages describing the change rather than the implementation
- Fill in every section of `.github/PULL_REQUEST_TEMPLATE.md`, including the negative case
  and the mode combinations exercised
- Target `main`; rebase rather than merge to keep history readable
- Update documentation in the same pull request as the user-facing change it describes
- See [CONTRIBUTING.md](CONTRIBUTING.md) for the full process
