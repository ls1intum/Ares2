#!/usr/bin/env node
/**
 * Runs the writing rules over the documentation.
 *
 * Three modes, because the rules split into two kinds and a programme of clean-up needs a
 * third:
 *
 *   --lint      fails on an enforced finding that is not already accepted, and on a
 *               suppression that names no rule or silences nothing. This is what CI runs.
 *   --report    prints every finding, enforced and advisory, as JSON. This is what
 *               regenerates the figures in the plan, and what the advisory clean-up is
 *               worked down from. It never fails.
 *   --accept    rewrites the baseline and the advisory ceilings from what is on disk now. Run
 *               it deliberately, in the same commit as the change that earns it, never to make
 *               CI green.
 *
 * The baseline is a ratchet keyed on the identity of a finding, not on a count per file. A
 * count lets one violation replace another and stay green; an identity does not. The
 * identity is the rule, the file, the words matched and the words around them, never a line
 * number, because an unrelated edit above moves every line below it.
 *
 * Usage:
 *   node scripts/prose/cli.mjs --lint
 *   node scripts/prose/cli.mjs --report > prose-report.json
 *   node scripts/prose/cli.mjs --accept
 */

import { readFile, readdir, writeFile } from 'node:fs/promises';
import path from 'node:path';

import { isEnforced, levelsById } from './rules.mjs';
import {
    DOCS, SITE, identityOf, scanCategory, scanPage, scanSource, suppressionProblems,
} from './scan.mjs';

/** Where the accepted findings are recorded. */
const BASELINE = path.resolve(import.meta.dirname, 'baseline.json');

/** Where the advisory ceilings are recorded. */
const CEILING = path.resolve(import.meta.dirname, 'advisory-ceiling.json');

/**
 * The site's own TypeScript that carries text a reader sees: the navbar and footer labels, the
 * tagline and the copyright line, all of which appear on every page.
 */
const SOURCE_FILES = [
    'docusaurus.config.ts', 'sidebar-instructor.ts', 'sidebar-contributor.ts',
];

/**
 * Every file holding text a reader reads, as absolute and relative pairs.
 *
 * Three kinds, because a reader does not know which is which: the Markdown under `docs/`, the
 * standalone pages under `src/pages/` (`imprint` and `privacy` are Markdown, the landing page
 * is TSX), and the configuration above.
 */
async function documentationFiles() {
    const files = [];
    const add = (absolute) => files.push({
        absolute,
        relative: path.relative(SITE, absolute).replaceAll('\\', '/'),
    });
    const walk = async (directory) => {
        for (const entry of await readdir(directory, { withFileTypes: true })) {
            const absolute = path.join(directory, entry.name);
            if (entry.isDirectory()) {
                await walk(absolute);
            } else if (/\.(?:mdx?|tsx?)$/.test(entry.name) || entry.name === '_category_.json') {
                add(absolute);
            }
        }
    };
    await walk(DOCS);
    await walk(path.join(SITE, 'src', 'pages'));
    for (const name of SOURCE_FILES) {
        add(path.join(SITE, name));
    }
    return files.sort((left, right) => left.relative.localeCompare(right.relative));
}

/** The scanner that reads one file, chosen by what the file is. */
function scannerFor(name) {
    if (name.endsWith('_category_.json')) {
        return scanCategory;
    }
    return /\.tsx?$/.test(name) ? scanSource : scanPage;
}

/** Scans everything, returning the findings and the per-file suppression bookkeeping. */
async function scanEverything() {
    const states = [];
    for (const file of await documentationFiles()) {
        states.push(await scannerFor(file.absolute)(file.absolute, file.relative));
    }
    return {
        findings: states.flatMap((state) => state.findings),
        problems: suppressionProblems(states),
    };
}

/** The accepted identities, or an empty set the first time this runs. */
async function readBaseline() {
    try {
        return new Set(JSON.parse(await readFile(BASELINE, 'utf8')).accepted);
    } catch {
        return new Set();
    }
}

/**
 * The advisory ceilings, or an empty map the first time this runs.
 *
 * An advisory rule cannot fail on a finding, because deciding one needs the sentence read. It
 * can still fail on a count. The ceiling is what stops the 1058 findings this documentation
 * carries from quietly becoming 1200: a rule may sit under its number for as long as it likes
 * and may be lowered whenever the prose improves, but it may not go up without somebody
 * writing the higher number down.
 *
 * A rule with no entry has a ceiling of zero, so adding a rule and leaving it unrecorded fails
 * rather than passing unnoticed.
 */
async function readCeiling() {
    try {
        return new Map(Object.entries(JSON.parse(await readFile(CEILING, 'utf8')).ceiling));
    } catch {
        return new Map();
    }
}

/** The advisory findings each rule produced, including the rules that produced none. */
function advisoryCounts(findings) {
    const counts = new Map();
    for (const [id, level] of levelsById()) {
        if (level !== 'enforced') {
            counts.set(id, 0);
        }
    }
    for (const item of findings) {
        if (counts.has(item.rule)) {
            counts.set(item.rule, counts.get(item.rule) + 1);
        }
    }
    return counts;
}

/** Counts findings by rule, which is the only summary worth printing every run. */
function byRule(findings) {
    const counts = new Map();
    for (const item of findings) {
        counts.set(item.rule, (counts.get(item.rule) ?? 0) + 1);
    }
    return [...counts.entries()].sort((left, right) => right[1] - left[1]);
}

/** Prints the whole report as JSON, and never fails. */
async function report() {
    const { findings, problems } = await scanEverything();
    const enforced = findings.filter((item) => isEnforced(item.rule));
    process.stdout.write(`${JSON.stringify({
        generated: 'run --report to regenerate',
        totals: {
            findings: findings.length,
            enforced: enforced.length,
            advisory: findings.length - enforced.length,
            files: new Set(findings.map((item) => item.file)).size,
        },
        byRule: Object.fromEntries(byRule(findings)),
        suppressionProblems: problems,
        findings,
    }, null, 2)}\n`);
    return 0;
}

/** Fails on an enforced finding nobody has accepted, and on a broken suppression. */
async function lint() {
    const { findings, problems } = await scanEverything();
    const accepted = await readBaseline();
    const enforced = findings.filter((item) => isEnforced(item.rule));
    const fresh = enforced.filter((item) => !accepted.has(identityOf(item)));
    const identities = new Set(enforced.map(identityOf));
    const gone = [...accepted].filter((identity) => !identities.has(identity));

    for (const item of fresh) {
        process.stdout.write(`${item.file}:${item.line}:${item.column}  ${item.rule}  ${item.message}\n`);
    }
    for (const problem of problems) {
        process.stdout.write(`${problem}\n`);
    }

    if (gone.length > 0) {
        process.stdout.write(`\n${gone.length} accepted finding(s) are fixed. `
            + 'Run `pnpm run prose:accept` and commit the smaller baseline.\n');
    }
    // Both ratchets fail when they are looser than the truth, not only when they are tighter.
    // A ceiling that falls to 90 and stays recorded as 100 leaves 10 findings' worth of room
    // for the next change to spend, which is the drift the ceiling exists to prevent.

    const counts = advisoryCounts(findings);
    const ceiling = await readCeiling();
    const over = [];
    const under = [];
    for (const [rule, count] of counts) {
        const limit = ceiling.get(rule) ?? 0;
        if (count > limit) {
            over.push(`${rule} has ${count} advisory finding(s) and its ceiling is ${limit}. `
                + 'Fix the new ones, or raise the ceiling deliberately in advisory-ceiling.json.');
        } else if (count < limit) {
            under.push(`${rule} is down to ${count} from ${limit}`);
        }
        if (!Number.isInteger(limit) || limit < 0) {
            over.push(`${rule} has a ceiling of ${JSON.stringify(ceiling.get(rule))}, which is `
                + 'not a count. Every entry in advisory-ceiling.json is a whole number.');
        }
    }
    for (const rule of ceiling.keys()) {
        if (!counts.has(rule)) {
            over.push(`advisory-ceiling.json has an entry for "${rule}", which is not an `
                + 'advisory rule. Run `pnpm run prose:accept` and commit the smaller file.');
        }
    }
    // Printed after every problem is collected, so a stale entry names itself rather than
    // failing the run silently.
    for (const problem of over) {
        process.stdout.write(`${problem}\n`);
    }
    if (under.length > 0) {
        process.stdout.write(`\n${under.join(', ')}. `
            + 'Run `pnpm run prose:accept` and commit the lower ceiling.\n');
    }

    const advisory = findings.length - enforced.length;
    process.stdout.write(`\n${enforced.length} enforced (${accepted.size} accepted, ${fresh.length} new), `
        + `${advisory} advisory. An advisory finding never fails on its own; its rule fails when `
        + 'the count stops matching the ceiling. Run `pnpm run report:prose` to read them.\n');

    return fresh.length + problems.length + over.length + under.length + gone.length > 0
        ? 1 : 0;
}

/** Rewrites the baseline and the ceilings from what is on disk now. */
async function accept() {
    const { findings } = await scanEverything();
    const enforced = findings.filter((item) => isEnforced(item.rule));
    const identities = [...new Set(enforced.map(identityOf))].sort();
    await writeFile(BASELINE, `${JSON.stringify({
        why: 'Enforced findings accepted for now. This list may only shrink; see scripts/prose/cli.mjs.',
        accepted: identities,
    }, null, 2)}\n`, 'utf8');

    const counts = [...advisoryCounts(findings)].sort((left, right) => left[0].localeCompare(right[0]));
    await writeFile(CEILING, `${JSON.stringify({
        why: 'The advisory findings each rule is allowed. Lower one by improving the prose, never '
            + 'by editing it here. A number rises only where a merge brings in text written before '
            + 'these rules existed, and only in the commit that brings it; see scripts/prose/cli.mjs.',
        ceiling: Object.fromEntries(counts),
    }, null, 2)}\n`, 'utf8');

    process.stdout.write(`Accepted ${identities.length} enforced finding(s) into the baseline, `
        + `and ${counts.reduce((total, [, count]) => total + count, 0)} advisory finding(s) into `
        + 'the ceiling.\n');
    return 0;
}

const mode = process.argv[2] ?? '--lint';
const run = { '--lint': lint, '--report': report, '--accept': accept }[mode];
if (run === undefined) {
    process.stdout.write('Usage: node scripts/prose/cli.mjs [--lint|--report|--accept]\n');
    process.exitCode = 2;
} else {
    // The exit code is set rather than `process.exit` called, because `process.exit` does not
    // wait for stdout to drain. Writing to a pipe is asynchronous, and the report is a hundred
    // kilobytes of JSON, so exiting immediately truncated it at the pipe buffer and left the
    // documented `--report > file` producing a file that does not parse.
    process.exitCode = await run();
}
