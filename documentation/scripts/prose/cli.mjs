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
 *   --accept    rewrites the baseline from what is on disk now. Run it deliberately, in the
 *               same commit as the change that earns it, never to make CI green.
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

import { isEnforced } from './rules.mjs';
import {
    DOCS, identityOf, scanCategory, scanPage, suppressionProblems,
} from './scan.mjs';

/** Where the accepted findings are recorded. */
const BASELINE = path.resolve(import.meta.dirname, 'baseline.json');

/** Every page and category file under the documentation, as absolute and relative pairs. */
async function documentationFiles() {
    const files = [];
    const walk = async (directory) => {
        for (const entry of await readdir(directory, { withFileTypes: true })) {
            const absolute = path.join(directory, entry.name);
            if (entry.isDirectory()) {
                await walk(absolute);
            } else if (/\.mdx?$/.test(entry.name) || entry.name === '_category_.json') {
                files.push({ absolute, relative: path.relative(path.dirname(DOCS), absolute).replaceAll('\\', '/') });
            }
        }
    };
    await walk(DOCS);
    return files.sort((left, right) => left.relative.localeCompare(right.relative));
}

/** Scans everything, returning the findings and the per-file suppression bookkeeping. */
async function scanEverything() {
    const states = [];
    for (const file of await documentationFiles()) {
        states.push(file.absolute.endsWith('.json')
            ? await scanCategory(file.absolute, file.relative)
            : await scanPage(file.absolute, file.relative));
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

    const advisory = findings.length - enforced.length;
    process.stdout.write(`\n${enforced.length} enforced (${accepted.size} accepted, ${fresh.length} new), `
        + `${advisory} advisory. Advisory findings never fail; run \`pnpm run report:prose\` to read them.\n`);

    return fresh.length + problems.length > 0 ? 1 : 0;
}

/** Rewrites the baseline from what is on disk now. */
async function accept() {
    const { findings } = await scanEverything();
    const enforced = findings.filter((item) => isEnforced(item.rule));
    const identities = [...new Set(enforced.map(identityOf))].sort();
    await writeFile(BASELINE, `${JSON.stringify({
        why: 'Enforced findings accepted for now. This list may only shrink; see scripts/prose/cli.mjs.',
        accepted: identities,
    }, null, 2)}\n`, 'utf8');
    process.stdout.write(`Accepted ${identities.length} enforced finding(s) into the baseline.\n`);
    return 0;
}

const mode = process.argv[2] ?? '--lint';
const run = { '--lint': lint, '--report': report, '--accept': accept }[mode];
if (run === undefined) {
    process.stdout.write('Usage: node scripts/prose/cli.mjs [--lint|--report|--accept]\n');
    process.exit(2);
}
process.exit(await run());
