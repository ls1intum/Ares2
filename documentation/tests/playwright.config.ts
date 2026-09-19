import { defineConfig } from '@playwright/test';
import { resolve } from 'node:path';

// The site is served by Docusaurus itself rather than by a bundler, because the documentation
// is a standalone project with no other build tooling to borrow. `docusaurus serve` serves
// exactly what `docusaurus build` produced, so these tests exercise the artefact that is
// actually deployed rather than a dev-server approximation of it.
//
// baseURL carries the /Ares2/ prefix because that is the configured baseUrl: the site is
// served from a GitHub project page, not from a domain root. Testing without the prefix would
// pass against a dev server and fail in production.
const PORT = 6107;

export default defineConfig({
    testDir: '.',
    forbidOnly: true,
    retries: 0,
    reporter: 'list',
    outputDir: resolve(__dirname, '../../target/test-results/documentation'),
    use: {
        baseURL: `http://127.0.0.1:${PORT}/Ares2/`,
        trace: 'retain-on-failure',
    },
    webServer: {
        command: `pnpm exec docusaurus serve --host 127.0.0.1 --port ${PORT} --no-open`,
        cwd: resolve(__dirname, '..'),
        url: `http://127.0.0.1:${PORT}/Ares2/`,
        reuseExistingServer: false,
        timeout: 120_000,
    },
});
