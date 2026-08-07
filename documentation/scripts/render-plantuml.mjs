#!/usr/bin/env node
/**
 * Renders every `.puml` file under `docs/` to an `.svg` next to its source.
 *
 * The rendered SVGs are committed, exactly as the `.drawio` sources in this repository sit
 * next to their committed `.drawio.png` renders. That choice is deliberate:
 *
 *   - No third party is contacted at page-view time. The obvious alternative,
 *     `remark-simple-plantuml`, rewrites diagram blocks into `<img>` tags pointing at
 *     plantuml.com, which sends the diagram source to an external server on every view. For a
 *     project whose diagrams describe its own sandbox-bypass surfaces, that is a poor trade.
 *   - Diagram changes show up in review as a rendered diff, not as an opaque source change.
 *   - The site builds offline and reproducibly, with no Java needed for a plain `pnpm build`.
 *
 * The cost is that a `.puml` edit must be followed by a re-render. `--check` enforces exactly
 * that in CI: it re-renders into a temporary directory and fails if the result differs from
 * what is committed.
 *
 * Usage:
 *   node scripts/render-plantuml.mjs           # render and write the SVGs
 *   node scripts/render-plantuml.mjs --check   # verify the committed SVGs are current
 *
 * Requires a JDK on PATH. The PlantUML JAR is downloaded once into `.plantuml-cache/` and
 * verified against a pinned SHA-256.
 */

import { createHash } from 'node:crypto';
import { spawn } from 'node:child_process';
import { mkdir, mkdtemp, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(HERE, '..');
const DOCS_DIR = path.join(ROOT, 'docs');
const CACHE_DIR = path.join(ROOT, '.plantuml-cache');

// The Apache-licensed distribution is used deliberately: the default plantuml.jar is GPL,
// and while running it as a build tool would not affect the licence of Ares 2, the ASL build
// is functionally identical and removes the question entirely.
// Update the checksum together with the version; it pins the exact release asset.
const PLANTUML_VERSION = '1.2026.6';
const PLANTUML_SHA256 = '695bcfc423172fff8c9eee6eb88eed4c256458784cbb30a07efe59994d069ee0';
const PLANTUML_URL = `https://github.com/plantuml/plantuml/releases/download/v${PLANTUML_VERSION}/plantuml-asl-${PLANTUML_VERSION}.jar`;
const JAR_PATH = path.join(CACHE_DIR, `plantuml-asl-${PLANTUML_VERSION}.jar`);

const check = process.argv.includes('--check');

/** Recursively collects every file under `dir` whose name ends with `suffix`. */
async function collect(dir, suffix) {
    if (!existsSync(dir)) {
        return [];
    }
    const entries = await readdir(dir, { withFileTypes: true });
    const found = await Promise.all(
        entries.map(async (entry) => {
            const full = path.join(dir, entry.name);
            if (entry.isDirectory()) {
                return collect(full, suffix);
            }
            return entry.name.endsWith(suffix) ? [full] : [];
        }),
    );
    return found.flat();
}

async function run(command, args, options = {}) {
    return new Promise((resolve, reject) => {
        const child = spawn(command, args, { stdio: 'inherit', ...options });
        child.on('error', reject);
        child.on('close', (code) =>
            code === 0 ? resolve() : reject(new Error(`${command} exited with code ${code}`)),
        );
    });
}

async function ensureJar() {
    if (existsSync(JAR_PATH)) {
        return;
    }
    await mkdir(CACHE_DIR, { recursive: true });
    console.log(`Downloading PlantUML ${PLANTUML_VERSION} ...`);
    const response = await fetch(PLANTUML_URL);
    if (!response.ok) {
        throw new Error(`Failed to download PlantUML: ${response.status} ${response.statusText}`);
    }
    const bytes = Buffer.from(await response.arrayBuffer());
    const digest = createHash('sha256').update(bytes).digest('hex');
    if (digest !== PLANTUML_SHA256) {
        throw new Error(
            `PlantUML checksum mismatch.\n  expected ${PLANTUML_SHA256}\n  actual   ${digest}\n` +
                'Refusing to use the download. Update PLANTUML_SHA256 only after verifying the release asset.',
        );
    }
    await writeFile(JAR_PATH, bytes);
}

// PlantUML renders an *error image* and still exits 0 when something goes wrong at layout
// time, so a plain exit-code check silently accepts a broken diagram. These markers appear in
// the SVG body of such an image.
const ERROR_MARKERS = [
    'dot not found',
    'Graphviz',
    'An error has occured',
    'Syntax Error',
    'data-diagram-type="ERROR"',
];

/**
 * Renders the given absolute `.puml` paths to `.svg` next to each source.
 *
 * `-Playout=smetana` selects PlantUML's built-in layout engine instead of shelling out to
 * Graphviz. Without it, a machine with no `dot` on PATH produces an SVG containing the words
 * "dot not found" rather than the diagram, and PlantUML still exits 0. Smetana removes the
 * native dependency, so a plain JDK is all that is needed here and in CI.
 */
async function render(sources) {
    await run('java', ['-jar', JAR_PATH, '-tsvg', '-nometadata', '-failfast2', '-Playout=smetana', ...sources]);

    // Defence in depth: confirm no output is an error image.
    for (const source of sources) {
        const rendered = source.replace(/\.puml$/, '.svg');
        if (!existsSync(rendered)) {
            throw new Error(`PlantUML produced no output for ${path.relative(ROOT, source)}`);
        }
        const svg = await readFile(rendered, 'utf8');
        const marker = ERROR_MARKERS.find((candidate) => svg.includes(candidate));
        if (marker !== undefined) {
            throw new Error(
                `PlantUML rendered an error image for ${path.relative(ROOT, source)} (found "${marker}").`,
            );
        }
    }
}

async function main() {
    const sources = await collect(DOCS_DIR, '.puml');
    if (sources.length === 0) {
        console.log('No .puml sources found under docs/, nothing to render.');
        return;
    }
    await ensureJar();

    if (!check) {
        await render(sources);
        console.log(`Rendered ${sources.length} diagram(s) to SVG.`);
        return;
    }

    const scratch = await mkdtemp(path.join(tmpdir(), 'ares-plantuml-'));
    try {
        const stale = [];
        for (const source of sources) {
            const copy = path.join(scratch, path.basename(source));
            await writeFile(copy, await readFile(source));
            await render([copy]);
            const expected = path.join(scratch, `${path.basename(source, '.puml')}.svg`);
            const committed = source.replace(/\.puml$/, '.svg');
            if (!existsSync(committed)) {
                stale.push(`${path.relative(ROOT, committed)} (missing)`);
                continue;
            }
            const [a, b] = await Promise.all([readFile(expected, 'utf8'), readFile(committed, 'utf8')]);
            if (a.trim() !== b.trim()) {
                stale.push(`${path.relative(ROOT, committed)} (out of date)`);
            }
        }
        if (stale.length > 0) {
            console.error('The committed PlantUML renders do not match their sources:\n');
            stale.forEach((entry) => console.error(`  - ${entry}`));
            console.error('\nRun `pnpm run diagrams` in documentation/ and commit the result.');
            process.exitCode = 1;
            return;
        }
        console.log(`All ${sources.length} committed diagram(s) are up to date.`);
    } finally {
        await rm(scratch, { recursive: true, force: true });
    }
}

await main();
