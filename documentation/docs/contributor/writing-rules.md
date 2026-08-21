---
title: "Writing rules"
sidebar_position: 8
description: "The language rules this documentation is held to, which of them a check enforces, and why the rest are left to a person."
---

:::tip[ELI5]
Everything on this site has to read as one document written by one person.

These are the language rules that achieve that. A check enforces the ones a machine can
decide from the words alone. The rest are reported and left to a person, because deciding
them needs to know what the sentence means, and getting that wrong in documentation for a
security tool is worse than the style problem it fixes.
:::

The rules themselves are the
[AET general writing rules](https://outline.aet.cit.tum.de/doc/general-writing-rules-Wkl6baytWT).
They were written for exercise text that students read. This site is a reference work with
two audiences, so the section below records how each rule was translated. Where this page and
the Outline page disagree, this page is what the check implements.

## How to run it

```bash
cd documentation
pnpm run lint:prose      # fails on an enforced finding; this is what CI runs
pnpm run report:prose    # every finding, enforced and advisory, as JSON; never fails
pnpm run test:prose      # the scanner's own fixtures
```

`report:prose` is what regenerates any count quoted about this documentation. Quoting a
number from anywhere else means quoting a number nobody can reproduce.

## What the check reads

The scanner reads the Markdown syntax tree, not the lines. Text a reader reads is eligible:
paragraphs, headings and table cells wherever they sit, the `title` and `description` of the
front matter, the `alt` text of an image, and the `label` of a `_category_.json`.

Code is never eligible, whether fenced or between backticks, and neither is the target of a
link. A method named `initialize` and a query string reading `?initialize=true` are both left
alone, while the label of that same link is read as prose.

## Enforced rules

These fail the build. A rule earns a place here only when its forbidden form is decidable
from the text alone, without knowing what the author meant.

| Rule | What it forbids |
| --- | --- |
| `no-contractions` | `doesn't`, `it's`, `they're` and the rest of a closed list |
| `no-american-spellings` | A closed list of spellings with one British form in every context |
| `no-always-filler` | `additionally` and `of course` |
| `no-back-loaded-opener` | A sentence opening with `As`, `Since`, `To`, `In order to` or `Because` |
| `abbreviation-first-use` | An abbreviation the page never spells out |
| `canonical-terms` | A second name for a concept that already has one |

The spelling rule is deliberately narrower than "British English", which is not decidable:
`licence` and `license` are both British and differ by part of speech, so do `practice` and
`practise`, and `program` is the correct British spelling for software.

## Advisory rules

These are reported and never fail. Each one needs a person to read the sentence.

| Rule | Why a machine must not decide it |
| --- | --- |
| `prefer-must` | `may` is permission, possibility or uncertainty depending on the sentence |
| `present-tense` | `would` carries counterfactuals, as in "what a fourth mechanism would have to provide" |
| `context-filler` | `also`, `actually`, `just`. "The namespace it has just built" is temporal, not filler |
| `address-the-reader` | `we` sometimes means the Ares project rather than the reader |
| `no-intensifiers` | The corpus holds `best-effort`, which is a term of art rather than a superlative |

"The static analysis `may` report false positives" is correct English. A rule that rewrote it
to `must` would state a guarantee the code does not make. That is the reason these five are
reported rather than enforced, and moving one of them into the table above is a decision
about the standard that belongs on this page before it belongs in a check.

## Decisions

**Who "you" means.** The reader of the guide it appears in: the instructor in the instructor
guide, a maintainer in the contributor guide. No reader of this site is a student, so the
original rule cannot apply literally.

**Pointing back is allowed.** The Outline page exempts guides from the ban on referring to
earlier text, because a reference work needs cross-references. This site takes that
exemption.

**One name per concept.** Two concepts had two names each. The canonical names are:

| Concept | Write | Never write |
| --- | --- | --- |
| Code that is subject to security checks | `student code` | `restricted code` |
| Code that is exempt from them | `test code` | `trusted code` |

`supervised code` is a third thing, not a synonym for either. It names the package boundary
Ares reserves for itself, and it keeps its own name.

The configuration field is called `restrictedPackage` and keeps that name. Prose and the
field deliberately differ, under the exemption for names in code.

**Abbreviations.** Every abbreviation is spelled out once per page, at its first use in body
text. Headings neither trigger the rule nor satisfy it: requiring the expansion in a heading
would push authors to reword it, which changes its anchor and breaks every link pointing at
it.

Agree an expansion once and reuse it, rather than inventing a new wording per page. The list
lives in `documentation/scripts/prose/rules.mjs`. `WALA` had no expansion anywhere on this
site before the list existed, so it is written as "T. J. Watson Libraries for Analysis
(WALA)".

`ELI5` is exempt. It is the label of the admonition every page opens with, so it is an
interface element rather than prose.

**Surfaces.** Front matter, sidebar labels and image alt text are all read, and `.mdx` is
parsed rather than forbidden.

## Suppressing a finding

A suppression names the rule and gives a reason, and applies to the block directly after it:

```markdown
<!-- prose-allow no-american-spellings: quoting the name of the Gradle feature -->

The version catalog holds it.
```

A suppression naming no rule fails, and so does one whose block breaks nothing, because both
mean the text below moved and the suppression did not.

Never suppress a whole file or a whole rule. A named phrase belongs in the `allow` list of
that rule in `rules.mjs`, beside the reason it is there.

## Changing a rule

The rules live in `documentation/scripts/prose/rules.mjs`, one object each, with the level
and the exceptions beside them. The scanner's fixtures live next to it in `prose.test.mjs`,
and a new rule or a new exception needs a fixture in the same commit.

A change that moves a rule between the two tables above changes this page too. The check does
not read this page, so nothing detects the two drifting apart.
