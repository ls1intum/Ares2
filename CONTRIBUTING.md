# Contributing to Ares 2

Thank you for considering a contribution. Ares 2 is the security boundary around student
code on an interactive learning platform, so changes here carry more weight than in a
typical library: a false negative lets forbidden student code through, and a false positive
fails a correct submission. The guidelines below exist to keep both from happening.

Start with the [documentation](https://ls1intum.github.io/Ares2/), in particular the
[contributor documentation](https://ls1intum.github.io/Ares2/contributor/how-can-you-contribute).

## Identity and transparency

### Members of the organisation

1. **Real names required.** Use your full real name in your GitHub profile. This is a
   prerequisite for joining the organisation, and it is what makes accountability and open
   collaboration possible.
2. **Authentic profile picture.** Use a clear, professional photograph. Avoid comic-style
   pictures, memojis and other non-authentic styles.
3. **Branch directly in the repository.** Members create branches and pull requests here
   rather than in a fork.

### External contributors

1. **Identity verification.** External contributions are considered only when the
   contributor uses their real name and an authentic profile picture.
2. **Fork the repository** and work on a branch in your fork.
3. **Open a pull request** against `main` once the work is complete, with your branch up to
   date with `main`.

We align these expectations with the
[GitHub Acceptable Use Policies](https://docs.github.com/en/site-policy/acceptable-use-policies).
For general background on contributing to open source, see the
[Open Source Guides](https://opensource.guide/).

## Prerequisites

- **JDK 21** (Temurin) to build, matching CI. Note that Ares 2 *targets* Java 17
  (`maven.compiler.source` / `maven.compiler.target` in `pom.xml`), so it stays consumable
  by exercises running on Java 17.
- **Maven 3.9+**.
- For the network tests, an **external echo server on loopback port 25565**. When it is
  absent those tests skip rather than fail; see [AGENTS.md](AGENTS.md) for why the fixture
  must live outside the sandbox.

## Building and checking locally

```bash
mvn clean package -DskipTests    # build without running the suite
mvn spotless:apply               # format (tabs, 4 spaces per tab, Eclipse rules)
mvn spotless:check               # verify formatting, as CI does
mvn checkstyle:check             # Checkstyle gate
mvn pmd:check                    # PMD and CPD gate
mvn spotbugs:check               # SpotBugs gate
```

The static-analysis rule sets live in `.settings/` next to the Eclipse formatter rules.
They are deliberately trimmed so that no rule contradicts the formatter and no rule objects
to a deliberate security idiom. Everything they still report is a build failure, not a
warning, so run them before pushing.

## Running the tests

```bash
mvn test                                        # the whole suite
mvn test -Punit-core-tests                      # unit tests
mvn test -Punit-architecture-tests              # architecture unit tests
mvn test -Pintegration-core-tests               # core integration tests
mvn test -Dtest=SomeTest#someMethod             # a single test
```

Ares 2 enforces through two independent analysis layers and two independent weaving
mechanisms, and CI exercises **all four combinations**: ArchUnit or WALA for the
architecture side, AspectJ or instrumentation for the AOP side. A change to either layer
must be verified against every combination it can affect. The pull request template asks
you to record which ones you exercised.

## Documentation

User-facing and architectural documentation lives in `documentation/` and is published with
Docusaurus to <https://ls1intum.github.io/Ares2/>.

```bash
cd documentation
corepack enable                  # one-time: activate the pinned pnpm version
pnpm install --frozen-lockfile
pnpm start                       # local dev server with hot reload
pnpm run lint                    # ESLint over the site sources
pnpm run typecheck               # TypeScript check
pnpm run build                   # production build, as CI runs it
pnpm run test                    # Playwright integration tests against the built site
pnpm run update                  # interactive dependency update (npm-check-updates)
```

`pnpm run test` needs a browser once: `pnpm run test:install`.

The site is built with `onBrokenLinks` and `onBrokenAnchors` set to `throw`, so a dangling
cross-reference fails the build. Run `pnpm run build` before pushing documentation changes.

PlantUML diagrams are rendered at build time and their SVG output is committed alongside the
`.puml` source, the same way `.drawio` sources sit next to their rendered `.png`. After
editing a `.puml` file, regenerate and commit the SVG:

```bash
cd documentation
pnpm run diagrams
```

CI verifies that the committed SVGs match their sources and fails if they have drifted.

Update the documentation in the same pull request as the change it describes. A user-facing
change with stale documentation is an incomplete change.

## Pull requests

- Fill in every section of the [pull request template](.github/PULL_REQUEST_TEMPLATE.md). It
  is long on purpose: for a security tool, "what must still be rejected" matters as much as
  "what now works".
- Write the title to describe the change, not the implementation.
- Keep pull requests focused. A refactor bundled with a behaviour change is hard to review
  and harder to revert.
- Rebase onto `main` rather than merging it in, to keep the history readable.
- Ensure CI is green, or explain each remaining failure in the description.
- Never include secrets, tokens or absolute local paths in the diff.

## Reporting problems

- **Bugs and feature requests:** open an [issue](https://github.com/ls1intum/Ares2/issues)
  using the appropriate template.
- **Security vulnerabilities:** do **not** open a public issue. Follow
  [SECURITY.md](SECURITY.md), which asks you to use GitHub's Private Vulnerability Reporting
  or to write to <paulsenm@in.tum.de>.

## Code of conduct

Participation in this project is governed by the
[Code of Conduct](CODE_OF_CONDUCT.md). By taking part you are expected to uphold it.
