import eslint from '@eslint/js';
import typescriptParser from '@typescript-eslint/parser';
import tsPlugin from '@typescript-eslint/eslint-plugin';

export default [
    {
        ignores: ['build/**', '.docusaurus/**', 'node_modules/**', '.plantuml-cache/**'],
    },
    eslint.configs.recommended,
    {
        // The diagram renderer runs under Node, not in a browser, so it gets the Node globals
        // and none of the DOM ones.
        files: ['scripts/**/*.mjs'],
        languageOptions: {
            parserOptions: {
                ecmaVersion: 'latest',
                sourceType: 'module',
            },
            globals: {
                console: 'readonly',
                process: 'readonly',
                fetch: 'readonly',
                Buffer: 'readonly',
                URL: 'readonly',
            },
        },
        rules: {
            'no-undef': 'error',
            'no-unused-vars': 'error',
        },
    },
    {
        files: ['src/**/*.js'],
        languageOptions: {
            parserOptions: {
                ecmaVersion: 'latest',
                sourceType: 'module',
                ecmaFeatures: {
                    jsx: true,
                },
            },
            globals: {
                console: 'readonly',
                document: 'readonly',
                window: 'readonly',
                Element: 'readonly',
                URL: 'readonly',
            },
        },
        rules: {
            'no-undef': 'error',
            'no-unused-vars': 'error',
        },
    },
    {
        files: ['src/**/*.{ts,tsx}', 'tests/**/*.ts', '*.ts'],
        languageOptions: {
            parser: typescriptParser,
            parserOptions: {
                ecmaVersion: 'latest',
                sourceType: 'module',
                ecmaFeatures: {
                    jsx: true,
                },
            },
            globals: {
                console: 'readonly',
                document: 'readonly',
                window: 'readonly',
                Element: 'readonly',
                URL: 'readonly',
                // Docusaurus config runs in Node and uses both of these.
                require: 'readonly',
                __dirname: 'readonly',
            },
        },
        plugins: {
            '@typescript-eslint': tsPlugin,
        },
        rules: {
            'no-undef': 'off',
            'no-unused-vars': 'off',
            '@typescript-eslint/no-unused-vars': 'error',
        },
    },
];
