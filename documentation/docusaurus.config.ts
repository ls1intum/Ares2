import { themes as prismThemes } from 'prism-react-renderer';
import type { Config } from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)
const ARES_REPOSITORY_URL = 'https://github.com/ls1intum/Ares2';
const EDIT_URL = ARES_REPOSITORY_URL + '/tree/main/documentation/';
const PAGE_TITLE = 'Ares 2 Documentation';

const config: Config = {
    title: PAGE_TITLE,
    tagline: 'The Artemis Java Test Sandbox',
    favicon: 'img/tum-logo-blue.svg',

    // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
    future: {
        v4: true, // Improve compatibility with the upcoming Docusaurus v4
    },

    // GitHub project pages: the site is served from https://ls1intum.github.io/Ares2/,
    // so the repository name has to be part of baseUrl. It is case sensitive.
    url: 'https://ls1intum.github.io',
    baseUrl: '/Ares2/',

    organizationName: 'ls1intum',
    projectName: 'Ares2',

    // A dangling cross-reference is a documentation bug, and this documentation is the
    // reference an instructor follows while wiring a security policy into an exercise.
    // Fail the build rather than publish a broken link.
    onBrokenLinks: 'throw',
    onBrokenAnchors: 'throw',

    markdown: {
        // Docusaurus 3 parses .md as MDX by default. The manuals migrated from the former
        // docs/ folder are ordinary CommonMark full of bare `<` and `{` (Java generics such
        // as List<String> in running prose, and ${...} in Gradle snippets), all of which MDX
        // would reject. 'detect' keeps .md as CommonMark and reserves MDX for .mdx, so a page
        // opts into JSX by its extension.
        format: 'detect',
        hooks: {
            onBrokenMarkdownLinks: 'throw',
        },
    },

    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    presets: [
        [
            'classic',
            {
                // Both doc sets are declared as explicit plugin instances below.
                docs: false,
                blog: false,
                theme: {
                    customCss: './src/css/custom.css',
                },
            } satisfies Preset.Options,
        ],
    ],

    themes: [
        [
            require.resolve('@easyops-cn/docusaurus-search-local'),
            /** @type {import("@easyops-cn/docusaurus-search-local").PluginOptions} */
            {
                hashed: true,
                language: ['en'],
                indexDocs: true,
                indexBlog: false,
                docsRouteBasePath: ['instructor', 'contributor'],
                searchContextByPaths: [
                    {
                        label: 'Instructor Documentation',
                        path: 'instructor',
                    },
                    {
                        label: 'Contributor Documentation',
                        path: 'contributor',
                    },
                ],
                useAllContextsWithNoSearchContext: true,
            },
        ],
    ],

    plugins: [
        // The first content-docs instance intentionally carries no id and therefore uses the
        // reserved 'default' plugin id. Every further instance needs a unique id of its own.
        [
            '@docusaurus/plugin-content-docs',
            {
                path: 'docs/instructor',
                routeBasePath: 'instructor',
                sidebarPath: './sidebar-instructor.ts',
                editUrl: EDIT_URL,
                exclude: ['**/README.md'],
            },
        ],
        [
            '@docusaurus/plugin-content-docs',
            {
                id: 'contributor',
                path: 'docs/contributor',
                routeBasePath: 'contributor',
                sidebarPath: './sidebar-contributor.ts',
                editUrl: EDIT_URL,
                exclude: ['**/README.md'],
            },
        ],
    ],

    themeConfig: {
        image: 'img/tum-logo-blue.svg',
        colorMode: {
            respectPrefersColorScheme: true,
        },
        navbar: {
            title: 'Ares 2',
            logo: {
                alt: 'TUM Logo',
                src: 'img/tum-logo-blue.svg',
                srcDark: 'img/tum-logo-blue.svg',
            },
            items: [
                {
                    type: 'docSidebar',
                    sidebarId: 'sidebar',
                    docsPluginId: 'default',
                    position: 'left',
                    label: 'Instructor',
                },
                {
                    type: 'docSidebar',
                    sidebarId: 'sidebar',
                    docsPluginId: 'contributor',
                    position: 'left',
                    label: 'Contributor',
                },
                {
                    href: ARES_REPOSITORY_URL,
                    label: 'GitHub',
                    position: 'right',
                },
            ],
        },
        footer: {
            style: 'dark',
            links: [
                {
                    title: 'Documentation',
                    items: [
                        {
                            label: 'Instructor Documentation',
                            to: '/instructor/ares-2/what-is-ares-2',
                        },
                        {
                            label: 'Contributor Documentation',
                            to: '/contributor/how-can-you-contribute',
                        },
                    ],
                },
                {
                    title: 'Community',
                    items: [
                        {
                            label: 'AET Website',
                            href: 'https://aet.cit.tum.de',
                        },
                        {
                            label: 'GitHub - Ares 2',
                            href: ARES_REPOSITORY_URL,
                        },
                        {
                            label: 'GitHub - AET Projects',
                            href: 'https://github.com/ls1intum',
                        },
                    ],
                },
                {
                    title: 'Project',
                    items: [
                        {
                            label: 'Artemis',
                            href: 'https://github.com/ls1intum/Artemis',
                        },
                        {
                            label: 'Licence (MIT)',
                            href: ARES_REPOSITORY_URL + '/blob/main/LICENSE',
                        },
                        {
                            label: 'Security Policy',
                            href: ARES_REPOSITORY_URL + '/blob/main/SECURITY.md',
                        },
                    ],
                },
            ],
            copyright: `© ${new Date().getFullYear()} Technical University of Munich – Built with ❤️ by the Applied Education Technologies (AET) group`,
        },
        prism: {
            theme: prismThemes.github,
            darkTheme: prismThemes.dracula,
            additionalLanguages: ['java', 'groovy', 'gradle', 'yaml', 'bash', 'json'],
        },
    },
} as Config;

export default config;
