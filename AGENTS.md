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
  apply; use that section's documented phrase (`No linked issues`, `No Improvement from
  the user's perspective`, `No Improvement from the maintainer's perspective`,
  `No breaking changes or migration`, `No production Java code changed`,
  `Not reproducible from an exercise`, `No mode-specific behaviour changed`) rather than
  deleting the section. Each phrase belongs to the section that documents it, so the wrong
  one does not answer a section, and neither does a shortened one.
- Five of those phrases answer a whole section: `No linked issues`, the two
  `No Improvement from the ...'s perspective` phrases, `No breaking changes or migration`
  and `No production Java code changed`. Written as the section's whole answer, apart from
  a trailing full stop, they finish it and nothing further is checked in it. The phrase
  left standing above the empty table or the numbered stub does not because that section
  was not finished.
- The other two answer a part of section 4 rather than the section: `Not reproducible from
  an exercise` belongs under Steps, where the template also asks how a reviewer verifies
  the change instead, and `No mode-specific behaviour changed` belongs to the modes. They
  are what to write, but they do not finish section 4, and its limit and its stubs are
  checked either way.
- Do not delete, rename or reorder the `##` headings. The `pr-template` check knows them
  by name, so a renamed heading fails the check.
- Tick boxes as `[x]`. When a checklist item does not apply, wrap that line in an HTML
  comment stating the reason, so the diff still records that it was considered.
- Read the section you are filling in, not this list. Each recurring instruction is
  repeated, in the same words, in every section where it applies. They close every section
  comment, after whatever that section says for itself, always in this order: `This
  section is always required`, which also says what to write when the section does not
  apply to your change, then `Limit`, then `Simple words`.
- Respect the character limit a section declares. Summary carries `Limit: 500
  characters`; `Linked issues`, sections 1 to 3 and `Breaking changes and migration` carry
  `Limit: 1000 characters`; section 4 carries `Limit: 5000 characters`, counted over the
  whole section including the modes below it. The count is in code points over the text left once every
  instruction comment the checker recognises is removed, so a comment kept in the body does
  not count towards it.
- Write for an instructor who does not know the inside of Ares. Every section that
  carries the `Simple words` block asks for this, section 1 while still naming the part of
  Ares a defect sits in.

The `pr-template` job in `.github/workflows/pullrequest-template.yml` enforces the shape
of the body and is a required status check. It verifies that every section exists exactly
once and in the order the checker lists them, that none is empty, that none runs past its
limit, and that no unfilled stub survived in a section that was not answered with one of
its own documented phrases. A heading inside a
comment it recognises does not count as a section, and a heading or a comment marker shown inside a
fenced block or a code span is text rather than markup, and so is a comment marker on a
line indented four columns, counting a tab as up to four, which is read as code. What is not
read is a fence indented against its container rather than the margin, inside a list or a
block quote, so a comment marker written there still counts as a comment. The four-column
rule is an approximation in the other direction too, since four columns under a paragraph
continue that paragraph in Markdown; the Javadoc of the checker says why the line is drawn
where it is.

One shape pays for that four-column rule, knowingly: an indented example showing the
template's own skeleton, a bare `1.` inside a comment, is read as a blank somebody forgot.
That is wrong about a body written in good faith, and is the accepted cost of closing a
hole where text hid from the length count.

What is not prose is found in one left-to-right walk rather than by searching for comments,
fenced blocks and code spans separately. That walk is what settles the overlaps between the
three: a fence opening a line beats a code span left open above it because Markdown decides
blocks first, and a span covers what it holds, so a comment marker inside one is text rather
than the start of a comment. It deliberately does not require checklist boxes to be ticked. It
re-runs when the description is edited, so a failure is fixed by editing the body rather
than by pushing a commit.

Pull requests opened by `renovate[bot]` or `dependabot[bot]` are exempt: their bodies are
generated by the tool and cannot follow the template. The job still runs for them and still
reports a green check, it simply skips the steps that would fail.

Check a body before opening the pull request, from the repository root:

```
PR_BODY="$(cat body.md)" java .github/scripts/CheckPullRequestTemplate.java
```

The checker is a single-file Java program, run through the source-code launcher of JDK 11 or
newer, so it needs no build step and adds no language to the repository. CI runs it on 21.

**Changing the template is two edits, not one.** The required headings, the character
limits and the phrases that answer a section live in one ordered map, `SECTIONS`, at the
top of `.github/scripts/CheckPullRequestTemplate.java`, in the order the template puts them
in. Each entry is a heading and exactly two strings: the character limit, then the phrase
that answers that whole section when it does not apply, with an empty string in either
place meaning the section has none of that. The two phrases that answer only a part of
section 4 are not in it, so changing one of those changes the template alone. `No mode-specific
behaviour changed` is not among them on purpose because it answers the modes rather than
the testing manual they sit inside, and a phrase excuses its whole section from the
leftover-stub scan. The template states the same rules in prose for whoever is filling it
in. The checker does not read the template, so a section renamed, added, removed, given a
different limit or given a different whole-section phrase has to be changed in both files
in the same commit. Nothing detects the drift: the template
would keep promising 1000 characters while the check went on enforcing 500. The checker
does verify itself, but only against itself, refusing to run if it requires no sections at
all, spells a heading so that it could never match one, states anything other than a limit
and a phrase for a section, gives a limit that is not a whole number of at least one that
fits in an `int`, or gives a phrase of whitespace, which is neither a phrase nor the empty
string that says there is none.

## Documenting Java

Every method and every field carries Javadoc, and each one stays under 500 characters.
The class comment stays under 500 characters too.

**Why:** the two failure modes are a file that explains nothing and a file that explains
its hardest idea three times. A cap forces the choice of what the reader has to know, and
a comment on everything means the reader never wonders whether the silence was deliberate.
Long comments also rot fastest, because nobody rereads a page of prose when changing a
line of code.

**Rule:**

- Write it in simple words, so that an instructor who does not know the inside of Ares can
  follow it. Spell out any Ares term you cannot avoid. Say less, not more: a reader who
  cannot follow a short answer will ask.
- Say what the thing is and what it is for. Where the reasoning behind how it does that
  will not fit, keep the part a future editor could break unknowingly and drop the rest.
- No comments inside a method or a static block. A method that needs one is a method that
  should be two, each named after the question it answers, and a step explained in prose
  is a step whose name was not chosen carefully enough. A comment beside a field's value,
  at class level, is allowed, since a value cannot be split into smaller values.
- Never put a line comment between a Javadoc comment and the declaration it documents. It
  detaches the two, and the Javadoc then documents nothing.
- Claim only what the code does. Shortening is where documentation turns into fiction:
  "what a reader sees" claims a Markdown renderer, "every code block" claims the ones that
  are deliberately not read, "collects every complaint" claims there is no early exit. If a
  sentence would need a paragraph of exceptions, say the narrow true thing instead.
- Existing Javadoc that predates this rule is not rewritten wholesale. Bring a comment up
  to this shape when you change the code it documents.

The same applies to any code an agent writes here, not only to Java: `.github/scripts` and
the workflows are held to the same standard, in whatever comment syntax they have.
