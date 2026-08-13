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
- Read the section you are filling in, not this list. Each recurring instruction is
  repeated, in the same words, in every section where it applies, so each section states
  its own rules:
  `Simple words` says who has to be able to follow it, `This section is always required`
  or a sentence naming what to write instead says what to do when it does not apply, and
  `Limit` says how long it may be.
- Respect the character limit a section declares. Sections 1 to 3 carry
  `Limit: 1000 characters`, counted in code points over the text left once every
  instruction comment is removed, so a comment kept in the body does not count towards it.
- Write for an instructor who does not know the inside of Ares. Every section that
  carries the `Simple words` block asks for this, section 1 while still naming the part of
  Ares a defect sits in.

The `pr-template` job in `.github/workflows/pullrequest-template.yml` enforces the shape
of the body and is a required status check. It verifies that every section exists exactly
once and in the order of the template, that none is empty, that none runs past the limit
its template section declares, and that no unfilled stub survived. A heading inside a
comment does not count as a section, and a heading or a comment marker shown inside a
fenced block or a code span is text rather than markup. Two code contexts are not read,
namely an indented code block and a fence nested inside a list or a block quote, so a
comment marker written there still counts as a comment; the Javadoc of the checker says
why that line was drawn. It deliberately does not require checklist boxes to be ticked. It
re-runs when the description is edited, so a failure is fixed by editing the body rather
than by pushing a commit.

Check a body before opening the pull request, from the repository root:

```
PR_BODY="$(cat body.md)" java .github/scripts/CheckPullRequestTemplate.java
```

The checker is a single-file Java program, run through the source-code launcher, so it
needs no build step and adds no language to the repository.
