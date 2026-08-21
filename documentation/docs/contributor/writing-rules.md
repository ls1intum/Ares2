---
title: "Writing rules"
sidebar_position: 8
description: "The language rules this documentation is held to, which of them a check enforces, and why the rest are left to a person."
---

:::tip[Simple Story]
One examination, one hand. Everything on this site has to read as though one person wrote it.

These are the language rules that achieve that. A check enforces the ones a machine can decide
from the words alone, and counts the ones it cannot, so the prose can drift in only one
direction. The rest are left to whoever reads the sentence, because getting them wrong in
documentation for a security tool is worse than the style problem it fixes.
:::

The rules themselves are the
[AET general writing rules](https://outline.aet.cit.tum.de/doc/general-writing-rules-Wkl6baytWT).
They were written for exercise text that students read. This site is a reference work with
two audiences, so the section below records how each rule was translated. Where this page and
the Outline page disagree, this page is what the check implements.

## How to run it

```bash
cd documentation
pnpm run lint:prose      # fails on an enforced finding, or on an advisory count that rose
pnpm run report:prose    # every finding, enforced and advisory, as JSON; never fails
pnpm run test:prose      # the scanner's own fixtures, and this page against the rules
pnpm run prose:accept    # rewrites the baseline and the ceilings from what is on disk
```

`lint:prose` is what continuous integration (CI) runs.

`report:prose` is what regenerates any count quoted about this documentation. Quoting a
number from anywhere else means quoting a number nobody can reproduce.

## What the check reads

The scanner reads the Markdown syntax tree, not the lines. Text a reader reads is eligible:
paragraphs, headings and table cells wherever they sit, the `title` and `description` of the
front matter, the `alt` text of an image, and the `label` of a `_category_.json`.

Code is never eligible, whether fenced or between backticks, and neither is the target of a
link. A method named `initialize` and a query string reading `?initialize=true` are both left
alone, while the label of that same link is read as prose.

Three kinds of file are read, because a reader does not know which is which:

| Where | What is read |
| --- | --- |
| `docs/**` | Every `.md` and `.mdx` page, and the `label` of every `_category_.json` |
| `src/pages/**` | The standalone pages: `imprint` and `privacy` as Markdown, the landing page as TSX |
| `docusaurus.config.ts`, `sidebar-*.ts` | The navbar and footer labels, the tagline and the copyright line, which appear on every page |

The TypeScript is read lexically rather than parsed: the value of a named key, and the text
between two JSX tags. Neither needs the grammar, and a parser here pulls a TypeScript
toolchain into a check that runs before the build.

## Enforced rules

These fail the build. A rule earns a place here only when its forbidden form is decidable
from the text alone, without knowing what the author meant.

| Rule | What it forbids |
| --- | --- |
| `no-contractions` | `doesn't`, `it's`, `they're` and the rest of a closed list |
| `no-american-spellings` | A closed list of spellings with one British form in every context |
| `no-always-filler` | The Outline filler list in full: `additional`, `additionally`, `of course`, `furthermore`, `moreover`, `also`, `actually`, `obviously`, `clearly` |
| `no-back-loaded-opener` | A sentence opening with `As`, `Since`, `To`, `In order to` or `Because` |
| `abbreviation-first-use` | An abbreviation the page never spells out |
| `canonical-terms` | A second name for a concept that already has one |

The spelling rule is deliberately narrower than "British English", which is not decidable:
`licence` and `license` are both British and differ by part of speech, so do `practice` and
`practise`, and `program` is the correct British spelling for software.

`also` and `actually` reached the enforced list late. The corpus used them in 139 places, and
enforcing a word that a hundred pages still carry means shipping a baseline rather than a rule,
so the clean-up came first and the rule afterwards. Where the word carried meaning the
sentence got a word that carries it plainly: `and` for another entry in a list, `further` for
one more of something, `as well` or `too` for an addition.

## Advisory rules

These are reported and never fail. Each one needs a person to read the sentence.

| Rule | Why a machine must not decide it |
| --- | --- |
| `prefer-must` | `may` is permission, possibility or uncertainty depending on the sentence |
| `present-tense` | `would` carries counterfactuals, as in "what a fourth mechanism would have to provide" |
| `context-filler` | `just`, `simply`, `in fact`. "The namespace it has just built" is temporal, not filler |
| `address-the-reader` | `we` sometimes means the Ares project rather than the reader |
| `no-intensifiers` | The corpus holds `best-effort`, which is a term of art rather than a superlative |
| `active-voice` | A form of "to be" plus a participle is passive far more often than not, but "is interested" is an adjective wearing the same clothes |
| `long-sentence` | Length is measurable; whether a long sentence earns its length is not |

"The static analysis `may` report false positives" is correct English. A rule that rewrote it
to `must` would state a guarantee the code does not make. That is the reason these seven are
reported rather than enforced, and moving one of them into the table above is a decision
about the standard that belongs on this page before it belongs in a check.

## The advisory ceiling

An advisory finding cannot fail the build on its own. Its rule fails when the count goes up.

`advisory-ceiling.json` records how many findings each advisory rule is allowed. A rule can sit
under its number for as long as it likes, and the number falls whenever the prose improves, but
it cannot rise without somebody writing the higher number down in a commit. A rule with no
entry has a ceiling of zero, so adding a rule and leaving it unrecorded fails rather than
passing unnoticed.

That is what stops 1,170 advisory findings quietly becoming 1,500. The counts are not a target to
drive to zero. `active-voice` in particular never reaches it, because a security reference
describes what happens to code and some of that is genuinely agentless. They are a direction.

Run `pnpm run prose:accept` to rewrite the ceilings from what is on disk, in the same commit as
the change that earns it. Never run it to make a red build green.

## Rules with no check, and why

Two of the Outline rules have no check and are not going to get one. Recording them here is the
point: an absent rule that nobody wrote down reads as an oversight.

| Rule | Why nothing checks it |
| --- | --- |
| Use `can` only where something is genuinely optional | `can` marks ability, permission and possibility with the same three letters. Deciding which one a sentence means is the whole of the rule, and a lexical check reports every use |
| Define a specialised term at its first appearance | The abbreviation list is the decidable half of this. Whether a word is jargon to the reader of this page is not something the page can be asked |

Both are still rules. They are checked by whoever reviews the pull request, which is what
happens to them on the Outline page too.

A check approximates the two Outline rules it can. `active-voice` and
`long-sentence` sit in the advisory table above, and the sentence-length limit is 35 words,
which is roughly two lines of this documentation.

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

The label of the admonition every page opens with, `Simple Story`, is two ordinary words and
needs no expansion.

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

A change that moves a rule between the tables above changes this page too, and `test:prose`
checks that it did: the fixtures parse the three tables on this page and compare them with the
levels in `rules.mjs`. A rule promoted in the code and left in the advisory table here fails,
and so does one listed here that the code does not define.
