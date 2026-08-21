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
import { mkdtemp, readFile, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import test, { after, before, describe } from 'node:test';

import { RULE_IDS, levelsById } from './rules.mjs';
import { DOCS, scanCategory, scanPage, scanSource } from './scan.mjs';

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

/**
 * The writing-rules page and the rules it describes, held to each other.
 *
 * The page is the standard and `rules.mjs` is the implementation of the decidable part of it.
 * Nothing else connects them: the scanner never reads the page, so a rule promoted in the code
 * and left in the advisory table would leave the two disagreeing with nobody the wiser. These
 * fixtures are that connection.
 */
describe('the page and the rules agree', () => {
    const PAGE = path.join(DOCS, 'contributor', 'writing-rules.md');

    /** The `## ` sections of the page, by heading. */
    async function sections() {
        const source = await readFile(PAGE, 'utf8');
        const found = new Map();
        let heading = null;
        let lines = [];
        for (const line of source.split(/\r?\n/)) {
            const match = /^## (.+)$/.exec(line);
            if (match === null) {
                lines.push(line);
                continue;
            }
            if (heading !== null) {
                found.set(heading, lines);
            }
            heading = match[1].trim();
            lines = [];
        }
        found.set(heading, lines);
        return found;
    }

    /**
     * The body rows of the first table in a section.
     *
     * The first contiguous run of pipe-prefixed lines, not every such line in the section: a
     * section with two tables would otherwise read as one, and this page has sections that do.
     */
    function firstTable(lines) {
        const start = lines.findIndex((line) => line.startsWith('|'));
        if (start === -1) {
            return [];
        }
        const rows = [];
        for (let index = start; index < lines.length && lines[index].startsWith('|'); index += 1) {
            rows.push(lines[index]);
        }
        return rows.filter((row) => !/^\|[\s-]+\|/.test(row)).slice(1);
    }

    /** The first cell of every body row of the first table in a section. */
    function firstCells(lines) {
        return firstTable(lines).map((row) => row.split('|')[1].trim());
    }

    /** The rule identifiers a section's table names, which are the cells written as code. */
    function idsIn(lines) {
        return firstCells(lines)
            .filter((cell) => /^`[a-z0-9-]+`$/.test(cell))
            .map((cell) => cell.slice(1, -1));
    }

    /** Every backticked token anywhere in a section's first table. */
    function codeSpansIn(lines) {
        return firstTable(lines).flatMap((row) => [...row.matchAll(/`([^`]+)`/g)]
            .map((match) => match[1]));
    }

    test('every rule the enforced table names is enforced in the code', async () => {
        const levels = levelsById();
        for (const id of idsIn((await sections()).get('Enforced rules'))) {
            assert.equal(levels.get(id), 'enforced', `${id} is in the enforced table`);
        }
    });

    test('every rule the advisory table names is advisory in the code', async () => {
        const levels = levelsById();
        for (const id of idsIn((await sections()).get('Advisory rules'))) {
            assert.equal(levels.get(id), 'advisory', `${id} is in the advisory table`);
        }
    });

    test('the two tables together name every rule, and no rule twice', async () => {
        const found = await sections();
        const listed = [
            ...idsIn(found.get('Enforced rules')),
            ...idsIn(found.get('Advisory rules')),
        ];
        assert.equal(new Set(listed).size, listed.length, 'a rule is listed twice');
        assert.deepEqual([...listed].sort(), [...RULE_IDS].sort());
    });

    test('the unchecked table exists and names no rule identifier', async () => {
        const lines = (await sections()).get('Rules with no check, and why');
        assert.ok(lines !== undefined, 'the page records the rules nothing checks');
        // Read every code span in the table, not the first cell alone: those rows are written
        // in words rather than as identifiers, so a first-cell check would look at nothing and
        // pass whatever the page said. This catches a rule identifier written into the
        // unchecked table. It cannot catch a rule described in words here and implemented
        // under some other identifier, which is why the section above says outright that a
        // change of level is a change to this page.
        const spans = codeSpansIn(lines);
        assert.ok(spans.length > 0, 'the unchecked table has rows');
        for (const span of spans) {
            assert.ok(!RULE_IDS.has(span), `${span} is listed as unchecked but the code has it`);
        }
    });
});

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

/**
 * The site's own TypeScript, which is read by parsing rather than by matching.
 *
 * Every fixture here is a case a regex over the raw source got wrong: a comment that looks
 * like a property, a comparison that looks like a tag, and a code sample that looks like a
 * paragraph. They are the reason the scanner asks TypeScript instead.
 */
describe('the site source', () => {
    /** Scans a source file written on the fly, returning the rule identifiers it reported. */
    async function sourceRules(body, extension = '.tsx') {
        const file = path.join(workspace, `source${extension}`);
        await writeFile(file, body, 'utf8');
        return (await scanSource(file, `source${extension}`)).findings.map((item) => item.rule);
    }

    test('a named property holding a string is prose', async () => {
        assert.deepEqual(await sourceRules("const a = { label: 'The color of it' };\n"),
            ['no-american-spellings']);
    });

    test('a property holding an identifier is not read', async () => {
        assert.deepEqual(await sourceRules('const a = { title: PAGE_TITLE };\n'), []);
    });

    test('a comment is not prose', async () => {
        assert.deepEqual(await sourceRules("// label: 'also'\nconst x = 1;\n"), []);
    });

    test('a comparison is not a tag', async () => {
        assert.deepEqual(await sourceRules('const x = left > also < right;\n', '.ts'), []);
    });

    test('a code sample inside a template is not prose', async () => {
        assert.deepEqual(await sourceRules('const a = <Code>{`<p>also</p>`}</Code>;\n'), []);
    });

    test('the children of a code component are not prose', async () => {
        assert.deepEqual(await sourceRules('const a = <CodeBlock>also</CodeBlock>;\n'), []);
    });

    test('a quoted property name is still a name', async () => {
        assert.deepEqual(await sourceRules("const a = { 'label': 'The color' };\n"),
            ['no-american-spellings']);
    });

    test('a literal inside JSX braces is prose', async () => {
        assert.deepEqual(await sourceRules("const a = <p>{'The color'}</p>;\n"),
            ['no-american-spellings']);
    });

    test('an array of visible strings in a page is prose', async () => {
        assert.deepEqual(await sourceRules("const items = ['The color of it'];\n"),
            ['no-american-spellings']);
    });

    test('an array in a configuration file is left to its named keys', async () => {
        assert.deepEqual(await sourceRules("const items = ['The color of it'];\n", '.ts'), []);
    });

    test('a finding is reported at the text, not at its key', async () => {
        const file = path.join(workspace, 'placed.ts');
        await writeFile(file, 'const a = {\n    label: `Prefix ${name} also`,\n};\n', 'utf8');
        const findings = (await scanSource(file, 'placed.ts')).findings;
        assert.equal(findings.length, 1);
        assert.equal(findings[0].line, 2);
        assert.equal(findings[0].column, 12);
    });

    test('text on either side of an interpolation is prose', async () => {
        assert.deepEqual(await sourceRules('const a = <p>The color {value} is also here.</p>;\n'),
            ['no-american-spellings', 'no-always-filler']);
    });

    test('a JSX attribute the reader sees is prose', async () => {
        assert.deepEqual(await sourceRules('const a = <img alt="The color of it" />;\n'),
            ['no-american-spellings']);
    });

    test('an escape is read as the character a reader sees', async () => {
        assert.deepEqual(await sourceRules("const a = { label: 'It doesn\\'t work' };\n"),
            ['no-contractions']);
    });

    test('an interpolation does not join the words on either side', async () => {
        assert.deepEqual(await sourceRules('const a = { label: `col${x}or here` };\n'), []);
    });

    test('a long string is a long sentence', async () => {
        const words = Array.from({ length: 40 }, (_, index) => `word${index}`).join(' ');
        assert.deepEqual(await sourceRules(`const a = { description: '${words}.' };\n`),
            ['long-sentence']);
    });
});

describe('sentences a person should look at', () => {
    test('a passive construction is reported, and only as advisory', async () => {
        const rules = await rulesFor('The call is blocked at the boundary.\n');
        assert.deepEqual(rules, ['active-voice']);
    });

    test('a participle used as an adjective is left alone', async () => {
        const rules = await rulesFor('The path is forbidden and the port is allowed.\n');
        assert.deepEqual(rules, []);
    });

    test('a participle after a modal is not matched', async () => {
        const rules = await rulesFor('The call may be blocked at the boundary.\n');
        assert.deepEqual(rules, ['prefer-must']);
    });

    test('a sentence past the limit is reported once', async () => {
        const words = Array.from({ length: 40 }, (_, index) => `word${index}`).join(' ');
        const rules = await rulesFor(`${words}.\n`);
        assert.deepEqual(rules, ['long-sentence']);
    });

    test('a sentence at the limit is not reported', async () => {
        const words = Array.from({ length: 35 }, (_, index) => `word${index}`).join(' ');
        const rules = await rulesFor(`${words}.\n`);
        assert.deepEqual(rules, []);
    });

    test('a long fenced block is not a long sentence', async () => {
        const words = Array.from({ length: 60 }, (_, index) => `word${index}`).join(' ');
        const rules = await rulesFor(`Text.\n\n\`\`\`\n${words}\n\`\`\`\n`);
        assert.deepEqual(rules, []);
    });
});

describe('sentence openers', () => {
    test('a sentence opening with Because is reported', async () => {
        const rules = await rulesFor('Because the aspect wove it, Ares catches the call.\n');
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
