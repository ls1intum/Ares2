/**
 * Fixtures for the prose scanner.
 *
 * The scanner is the part of the writing-rules programme most able to do damage. A rule that
 * reports what nobody wrote turns a required check into an obstacle, and a rule that misses
 * what a reader sees makes the whole exercise decorative. So every Markdown construct that
 * could confuse it, and every trap found while writing it, is pinned here.
 *
 * Run with `pnpm run test:prose`.
 */

import assert from 'node:assert/strict';
import { mkdtemp, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import test, { after, before, describe } from 'node:test';

import { scanCategory, scanPage } from './scan.mjs';

let workspace;

before(async () => {
    workspace = await mkdtemp(path.join(tmpdir(), 'prose-'));
});

after(async () => {
    await rm(workspace, { recursive: true, force: true });
});

/** Scans a page written on the fly, returning the rule identifiers it reported. */
async function rulesFor(body, extension = '.md') {
    const file = path.join(workspace, `page${extension}`);
    await writeFile(file, body, 'utf8');
    const state = await scanPage(file, `page${extension}`);
    return state.findings.map((item) => item.rule);
}

/** Scans a page and returns the full findings, for the cases that check a position. */
async function findingsFor(body) {
    const file = path.join(workspace, 'positioned.md');
    await writeFile(file, body, 'utf8');
    return (await scanPage(file, 'positioned.md')).findings;
}

describe('code is never prose', () => {
    test('a fenced block is not scanned', async () => {
        const rules = await rulesFor('Text.\n\n```java\nString color = "doesn\'t";\n```\n');
        assert.deepEqual(rules, []);
    });

    test('a tilde-fenced block holding a backtick fence is not scanned', async () => {
        const rules = await rulesFor('Text.\n\n~~~\n```\ncolor doesn\'t\n```\n~~~\n');
        assert.deepEqual(rules, []);
    });

    test('inline code is not scanned', async () => {
        const rules = await rulesFor('Call `initialize()` on it.\n');
        assert.deepEqual(rules, []);
    });

    test('an indented code block is not scanned', async () => {
        const rules = await rulesFor('Text.\n\n    String color = "x";\n');
        assert.deepEqual(rules, []);
    });

    test('inline code does not join the words on either side into one nobody wrote', async () => {
        const rules = await rulesFor('The col`x`or of it.\n');
        assert.deepEqual(rules, []);
    });
});

describe('links', () => {
    test('a link target is not prose', async () => {
        const rules = await rulesFor('See [the guide](https://example.invalid/?initialize=true&color=1).\n');
        assert.deepEqual(rules, []);
    });

    test('a link label is prose', async () => {
        const rules = await rulesFor('See [the color guide](https://example.invalid/).\n');
        assert.deepEqual(rules, ['no-american-spellings']);
    });

    test('a reference definition is not prose', async () => {
        const rules = await rulesFor('See [it][ref].\n\n[ref]: https://example.invalid/color\n');
        assert.deepEqual(rules, []);
    });
});

describe('terms of art keep their spelling', () => {
    test('best-effort is not a superlative', async () => {
        const rules = await rulesFor('Weaving is best-effort here.\n');
        assert.deepEqual(rules, []);
    });

    test('best practice is still reported', async () => {
        const rules = await rulesFor('This is best practice.\n');
        assert.deepEqual(rules, ['no-intensifiers']);
    });

    test('a Gradle version catalog keeps its name', async () => {
        const rules = await rulesFor('Declare it in the version catalog instead.\n');
        assert.deepEqual(rules, []);
    });

    test('the Unified Modeling Language keeps the standard spelling', async () => {
        const rules = await rulesFor('The Unified Modeling Language (UML) diagram shows it.\n');
        assert.deepEqual(rules, []);
    });

    test('supervised code is not a third name for student code', async () => {
        const rules = await rulesFor('Ares reserves the supervised code package for itself.\n');
        assert.deepEqual(rules, []);
    });
});

describe('words that only look like violations', () => {
    test('just meaning "a moment ago" is advisory, never enforced', async () => {
        const findings = await findingsFor('It tears down the namespace it has just built.\n');
        assert.deepEqual(findings.map((item) => item.rule), ['context-filler']);
    });

    test('just-in-time is one word, not filler', async () => {
        const rules = await rulesFor('The just-in-time compiler runs later.\n');
        assert.deepEqual(rules, []);
    });

    test('a possibility reads as advisory, not as a spelling or a ban', async () => {
        const findings = await findingsFor('The actual number may vary between releases.\n');
        assert.deepEqual(findings.map((item) => item.rule), ['prefer-must']);
        assert.equal(findings[0].rule, 'prefer-must', 'a modal is never enforced');
    });

    test('quoted error output is reported only as advisory', async () => {
        const findings = await findingsFor('The build prints "the run will be terminated" and stops.\n');
        assert.ok(findings.every((item) => item.rule === 'present-tense'));
    });

    test('a licence sentence is reported only as advisory', async () => {
        const findings = await findingsFor('You may obtain a copy of the Licence at the address below.\n');
        assert.ok(findings.every((item) => item.rule === 'prefer-must'));
    });
});

describe('surfaces a reader sees', () => {
    test('the front matter description is prose', async () => {
        const rules = await rulesFor('---\ntitle: Colors\ndescription: It doesn\'t matter.\n---\n\nBody.\n');
        assert.deepEqual(rules.sort(), ['no-american-spellings', 'no-contractions']);
    });

    test('a heading is prose', async () => {
        const rules = await rulesFor('## What\'s the state?\n');
        assert.deepEqual(rules, ['no-contractions']);
    });

    test('a table cell is prose', async () => {
        const rules = await rulesFor('| A | B |\n| --- | --- |\n| color | x |\n');
        assert.deepEqual(rules, ['no-american-spellings']);
    });

    test('image alt text is prose', async () => {
        const rules = await rulesFor('![The color chart](./x.png)\n');
        assert.deepEqual(rules, ['no-american-spellings']);
    });

    test('a block quotation is prose', async () => {
        const rules = await rulesFor('> The color of it.\n');
        assert.deepEqual(rules, ['no-american-spellings']);
    });

    test('a list item is prose', async () => {
        const rules = await rulesFor('- The color of it.\n');
        assert.deepEqual(rules, ['no-american-spellings']);
    });

    test('a sidebar label is prose', async () => {
        const file = path.join(workspace, '_category_.json');
        await writeFile(file, '{\n  "label": "Colors",\n  "position": 1\n}\n', 'utf8');
        const state = await scanCategory(file, '_category_.json');
        assert.deepEqual(state.findings.map((item) => item.rule), ['no-american-spellings']);
    });
});

describe('sentence openers', () => {
    test('a sentence opening with Because is reported', async () => {
        const rules = await rulesFor('Because it is woven, the call is caught.\n');
        assert.deepEqual(rules, ['no-back-loaded-opener']);
    });

    test('a second sentence opening with To is reported', async () => {
        const rules = await rulesFor('It runs first. To catch the call, weave it.\n');
        assert.deepEqual(rules, ['no-back-loaded-opener']);
    });

    test('e.g. does not start a new sentence', async () => {
        const rules = await rulesFor('Use a policy, e.g. To catch it early is wrong here.\n');
        assert.deepEqual(rules, []);
    });

    test('the word as inside a sentence is not an opener', async () => {
        const rules = await rulesFor('It behaves as the policy says.\n');
        assert.deepEqual(rules, []);
    });
});

describe('abbreviations', () => {
    test('an unexpanded abbreviation is reported once per page', async () => {
        const rules = await rulesFor('The JVM starts. The JVM then loads the agent.\n');
        assert.deepEqual(rules, ['abbreviation-first-use']);
    });

    test('an expanded abbreviation is accepted', async () => {
        const rules = await rulesFor('The Java Virtual Machine (JVM) starts. The JVM loads it.\n');
        assert.deepEqual(rules, []);
    });

    test('a heading neither triggers nor satisfies the rule, so no anchor has to change', async () => {
        const rules = await rulesFor('## JVM notes\n\nIt starts early.\n');
        assert.deepEqual(rules, []);
    });

    test('an abbreviation only ever in code is not reported', async () => {
        const rules = await rulesFor('Set `JVM_OPTS` in the file.\n');
        assert.deepEqual(rules, []);
    });
});

describe('suppressions', () => {
    test('a suppression silences the block directly after it', async () => {
        const rules = await rulesFor('<!-- prose-allow no-american-spellings: quoting the API name -->\n\nThe color of it.\n');
        assert.deepEqual(rules, []);
    });

    test('a suppression does not reach the block after next', async () => {
        const rules = await rulesFor('<!-- prose-allow no-american-spellings: reason -->\n\nThe color of it.\n\nThe color again.\n');
        assert.deepEqual(rules, ['no-american-spellings']);
    });

    test('a suppression naming no rule is a problem', async () => {
        const file = path.join(workspace, 'unknown.md');
        await writeFile(file, '<!-- prose-allow no-such-rule: reason -->\n\nText.\n', 'utf8');
        const state = await scanPage(file, 'unknown.md');
        assert.equal(state.declared.length, 1);
        assert.equal(state.used.size, 0);
    });

    test('a suppression without a reason does not parse as one', async () => {
        const rules = await rulesFor('<!-- prose-allow no-american-spellings -->\n\nThe color of it.\n');
        assert.deepEqual(rules, ['no-american-spellings']);
    });
});

describe('positions', () => {
    test('a finding reports the author\'s own line', async () => {
        const findings = await findingsFor('One.\n\nTwo.\n\nThe color of it.\n');
        assert.equal(findings.length, 1);
        assert.equal(findings[0].line, 5);
    });

    test('a match after a line break inside one paragraph keeps its line', async () => {
        const findings = await findingsFor('A paragraph that runs on\nand then says color here.\n');
        assert.equal(findings[0].line, 2);
    });
});

describe('MDX', () => {
    test('an MDX expression is not prose', async () => {
        const rules = await rulesFor('The value is {someColor} today.\n', '.mdx');
        assert.deepEqual(rules, []);
    });

    test('text around an MDX expression is prose', async () => {
        const rules = await rulesFor('The color is {value} today.\n', '.mdx');
        assert.deepEqual(rules, ['no-american-spellings']);
    });
});
