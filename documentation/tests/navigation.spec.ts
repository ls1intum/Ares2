import { expect, test } from '@playwright/test';

/**
 * Integration tests for the published documentation site.
 *
 * These run against the output of `docusaurus build`, served by `docusaurus serve`, so they
 * check the artefact that is actually deployed. They deliberately cover what the build itself
 * cannot: the build verifies that links resolve, but not that a reader can navigate between
 * the two guides, that the search index is wired to both, or that the site works under the
 * /Ares2/ path prefix it is deployed at.
 */

test.describe('landing page', () => {
    test('offers both audiences and reaches each guide', async ({ page }) => {
        await page.goto('./');

        await expect(page.getByRole('heading', { level: 1, name: 'Ares 2' })).toBeVisible();

        // Scoped to the main content: the footer links to both guides too, so an unscoped
        // lookup matches twice and fails Playwright's strict mode.
        const cards = page.getByRole('main');
        const instructor = cards.getByRole('link', { name: /Instructor Documentation/ });
        const contributor = cards.getByRole('link', { name: /Contributor Documentation/ });
        await expect(instructor).toBeVisible();
        await expect(contributor).toBeVisible();

        await instructor.click();
        await expect(page).toHaveURL(/\/Ares2\/instructor\//);
        await expect(page.getByRole('heading', { level: 1 })).toContainText('What is Ares 2');
    });

    test('the get-started button reaches the installation page', async ({ page }) => {
        await page.goto('./');
        await page.getByRole('link', { name: 'Get started' }).click();
        await expect(page).toHaveURL(/\/Ares2\/instructor\/protect-a-java-project\/installation/);
        await expect(page.getByRole('heading', { level: 1 })).toContainText('Installation');
    });
});

test.describe('the two guides', () => {
    test('the navbar switches between them', async ({ page }) => {
        await page.goto('./instructor/ares-2/what-is-ares-2');

        await page.getByRole('navigation').getByRole('link', { name: 'Contributor', exact: true }).click();
        await expect(page).toHaveURL(/\/Ares2\/contributor\//);

        await page.getByRole('navigation').getByRole('link', { name: 'Instructor', exact: true }).click();
        await expect(page).toHaveURL(/\/Ares2\/instructor\//);
    });

    test('each guide renders its own sidebar', async ({ page }) => {
        await page.goto('./instructor/ares-2/what-is-ares-2');
        const sidebar = page.getByRole('navigation', { name: 'Docs sidebar' });
        await expect(sidebar.getByText('How to protect a Java project with Ares 2')).toBeVisible();

        await page.goto('./contributor/how-can-you-contribute');
        await expect(sidebar.getByText('Technologies on which Ares 2 is built')).toBeVisible();
        await expect(sidebar.getByText('Subsystems')).toBeVisible();
    });

    test('a cross-guide link resolves', async ({ page }) => {
        // The two guides are separate plugin instances, so a link from one to the other is an
        // absolute route rather than a relative file path. That is easy to get wrong and the
        // build only checks that the target exists, not that it is reachable by clicking.
        await page.goto('./instructor/ares-2/what-does-ares-2-not-protect-against');
        await page.getByRole('link', { name: /enforcement model/i }).first().click();
        await expect(page).toHaveURL(/\/Ares2\/contributor\/subsystems\/policy\/enforcement-model/);
    });
});

test.describe('policy documentation', () => {
    test('every policy page marks a section of the example in red', async ({ page }) => {
        // The red marking is produced by a custom Prism magic comment plus a CSS class. Neither
        // the Docusaurus build nor the Java structure test can confirm that it actually renders,
        // because both look at the source rather than at the page.
        const pages = [
            'programming-language-configuration',
            'class-permission',
            'file-system-access',
            'network-system-access',
            'command-system-access',
            'thread-system-access',
            'package-permission',
            'resource-limits',
        ];

        for (const name of pages) {
            await page.goto(`./contributor/policy/${name}`);

            const marked = page.locator('.code-block-policy-focus');
            await expect(marked, `${name} must mark part of the example`).not.toHaveCount(0);

            // The magic comments themselves must never be visible to a reader.
            await expect(page.locator('pre')).not.toContainText('policy-focus-start');
            await expect(page.locator('pre')).not.toContainText('policy-focus-end');
        }
    });

    test('the marked region is visually distinct from the surrounding code', async ({ page }) => {
        await page.goto('./contributor/policy/file-system-access');
        const marked = page.locator('.code-block-policy-focus').first();
        const background = await marked.evaluate((element) => getComputedStyle(element).backgroundColor);

        // Transparent would mean the class is applied but the stylesheet never reached the page.
        expect(background).not.toBe('rgba(0, 0, 0, 0)');
        expect(background).not.toBe('transparent');
    });
});

test.describe('site behaviour', () => {
    test('search is available and indexes both guides', async ({ page, request }) => {
        await page.goto('./');
        await expect(page.getByRole('textbox', { name: /search/i })).toBeVisible();

        for (const index of ['search-index-instructor.json', 'search-index-contributor.json']) {
            const response = await request.get(`./${index}`);
            expect(response.status(), `${index} must be published`).toBe(200);
        }
    });

    test('an unknown path renders the 404 page rather than failing', async ({ page }) => {
        const response = await page.goto('./instructor/this-page-does-not-exist');
        expect(response?.status()).toBe(404);
        await expect(page.getByText(/Page Not Found/i)).toBeVisible();
    });

    test('the ELI5 boxes render as admonitions', async ({ page }) => {
        await page.goto('./contributor/policy/file-system-access');
        const eli5 = page.locator('.theme-admonition').filter({ hasText: 'ELI5' });
        await expect(eli5).toHaveCount(1);
        await expect(eli5).toBeVisible();
    });
});
