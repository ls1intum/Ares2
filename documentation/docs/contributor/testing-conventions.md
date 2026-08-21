---
title: "Testing conventions"
sidebar_position: 2
description: "The rules a test in this repository must follow, and why a security boundary needs stricter ones than an ordinary library."
---

:::tip[ELI5]
Ares is the thing being tested and the thing doing the testing at the same time.

That sounds circular, and it is exactly where tests go wrong here. These are the rules that keep
a test measuring the student's behaviour rather than the test's own scaffolding.
:::

## Every change is verified against all four combinations

Ares 2 enforces through two independent analysis layers and two independent weaving mechanisms:

| Axis | Options |
| --- | --- |
| Architecture analysis | ArchUnit, T. J. Watson Libraries for Analysis (WALA) |
| aspect-oriented programming (AOP) weaving | AspectJ, instrumentation |

continuous integration (CI) exercises **all four combinations**. A change to either layer must be verified against every
combination it can affect, and the
[pull request template](https://github.com/ls1intum/Ares2/blob/main/.github/PULL_REQUEST_TEMPLATE.md)
asks you to record which ones you exercised. "It passes locally" is not an answer unless you say
which of the four you ran.

## The profiles

```bash
mvn test                                        # the whole suite
mvn test -Punit-core-tests                      # unit tests
mvn test -Punit-architecture-tests              # architecture unit tests
mvn test -Pintegration-core-tests               # core integration tests
mvn test -Dtest=SomeTest#someMethod             # a single test
```

## A fixture must live outside the boundary it helps test

A sandboxed test Java Virtual Machine (JVM) must **never spin up its own server** to test incoming or outgoing
connections: no echo server, no socket listener, nothing.

The reason is that Ares is the security boundary under test. Any server started inside the same
JVM as the student code is itself subject to the active policy, so Ares intercepts its thread,
its `ServerSocket` bind and its `accept()`. When such a test fails, the failure cannot be
attributed: it may be the behaviour under test (the student's client connection, or the
student's own server), or it may be the fixture failing to start. The two are indistinguishable
from the outside.

The rule that follows:

- Outgoing-connection tests connect to an **external echo server** at a configurable endpoint,
  running as a separate process or CI service on loopback port `25565`. The test exercises only
  the student's client behaviour.
- If that server is unreachable, the test **skips** through `Assumptions.abort` rather than
  failing. A missing echo server is an expected local condition; CI provides one.
- An Ares `SecurityException` on an explicitly allowed connection is **always** a real failure
  and must propagate. It is never skipped.
- Do not hard-code a self-hosted listener as the counterpart. Port `25565` collides easily, and
  an external service avoids the in-JVM `BindException`, thread and lifecycle flakiness
  entirely.

`NetworkUser` follows this rule: it no longer starts an in-process echo server, and
`connectLocallyAllowed` targets the external server and skips when it is absent.

## Both directions of a security assertion

For a security tool, a green test that only proves "the allowed thing still works" is half a
test. Every enforcement change needs both:

- a **positive control**, proving the permitted operation is still permitted, so that an
  over-broad rule is caught;
- a **negative control**, proving the forbidden operation is still rejected, so that a
  regression in enforcement is caught.

The runnable examples under
[`examples/`](https://github.com/ls1intum/Ares2/tree/main/examples) are written this way: a
correct run is green **and** contains an asserted rejection. A red build there is not the
expected outcome.

## Structure tests for the documentation

The documentation has its own tests under
`src/test/java/de/tum/cit/ase/ares/documentation`. They read the Markdown directly, so they run
in the ordinary Maven build without Node:

- every page of both guides is enumerated by an expected path set, so a page cannot be added,
  removed, renamed or filed under the wrong audience unnoticed;
- every page declares its front matter, opens with an ELI5 box and carries no `h1`;
- every `_category_.json` is valid and collision-free against its siblings;
- the policy pages share one shape and together walk the example policy top to bottom.

What they cannot see is whether a page renders. The Playwright suite under
`documentation/tests` covers that.

## Notes

- Static analysis (`spotless`, `checkstyle`, `pmd`, `spotbugs`) is a build failure, not a
  warning. Run it before pushing; see [CONTRIBUTING.md](https://github.com/ls1intum/Ares2/blob/main/CONTRIBUTING.md).
- Build with Java Development Kit (JDK) 21 to match CI, even though Ares 2 targets Java 17 so that it stays consumable
  by exercises on Java 17.
