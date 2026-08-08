# AGENTS.md

Repository conventions for automated agents and contributors working on Ares.

## Testing network access (incoming and outgoing)

A sandboxed test JVM must **never spin up its own server** (echo server, socket
listener, etc.) to test incoming or outgoing connections.

**Why:** Ares is the security boundary under test. Any server started inside the
same JVM as the student code is itself subject to the active security policy, so
Ares intercepts its thread, its `ServerSocket` bind and its `accept()`. A
failure then cannot be attributed to the behaviour being tested (the student's
client connection, or the student's own server) versus the test fixture failing
to start. A fixture must live **outside** the boundary it helps test.

**Rule:**

- Outgoing-connection tests connect to an **external echo server** at a
  configurable endpoint. The server runs as a separate process or CI service on
  the loopback at the agreed port (currently `25565`). The test exercises only
  the student's client behaviour.
- If the external echo server is not reachable, the test **skips** (JUnit
  `Assumptions.abort`) rather than fails. "Missing echo server" is an expected
  environmental condition locally; CI provides the server.
- An Ares `SecurityException` on an explicitly allowed connection is always a
  real failure and must propagate (never skipped).
- Do not hard-code a self-hosted listener as the connection counterpart. Port
  `25565` (Minecraft's default) collides easily; an external service avoids the
  in-JVM `BindException`/thread/lifecycle flakiness entirely.

`NetworkUser` follows this rule: it no longer starts an in-process echo server;
`connectLocallyAllowed` targets the external echo server and skips when it is
absent.

## Opening a pull request

Every pull request body must follow `.github/PULL_REQUEST_TEMPLATE.md`. **Read that
file before writing the body**, do not reconstruct it from memory or from another
repository's conventions.

**Why:** GitHub inserts the template only as a prefill in the web UI. Creating a pull
request from the command line with `gh pr create --body` or `--body-file` bypasses it
entirely and GitHub never validates the result, so an agent that has not read the
template will silently submit a body in the wrong shape. This is the single most common
way an otherwise correct contribution arrives unreviewable.

**Rule:**

- Build the body from the template. The reliable command is
  `gh pr create --body-file .github/PULL_REQUEST_TEMPLATE.md` followed by
  `gh pr edit --body-file <filled-in copy>`, or simply fill in a copy of the template
  and pass that as `--body-file`.
- Fill in every section. The template states what to write when a section does not
  apply; use those documented escape hatches (`No Improvement`, `None`,
  `No production Java code changed`, `Not reproducible from an exercise`,
  `No mode-specific behaviour changed`) rather than deleting the section.
- Do not delete, rename or reorder the `##` headings. The `pr-template` check reads the
  required headings out of the template itself, so a renamed heading fails the check.
- Tick boxes as `[x]`. When a checklist item does not apply, wrap that line in an HTML
  comment stating the reason, so the diff still records that it was considered.

The `pr-template` job in `.github/workflows/pullrequest-template.yml` enforces the shape
of the body and is a required status check. It verifies that every section exists, that
none is empty and that no unfilled stub survived. It deliberately does not require
checklist boxes to be ticked. It re-runs when the description is edited, so a failure is
fixed by editing the body rather than by pushing a commit.
