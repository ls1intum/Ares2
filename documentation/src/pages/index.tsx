import type { ReactNode } from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Heading from '@theme/Heading';
import Layout from '@theme/Layout';
import CodeBlock from '@theme/CodeBlock';

import styles from './index.module.css';

interface Destination {
    title: string;
    description: string;
    to: string;
}

const destinations: Destination[] = [
    {
        title: 'Instructor Documentation',
        description:
            'Add Ares 2 to an exercise, write a security policy and understand what it does and does not protect against.',
        to: '/instructor/ares-2/what-is-ares-2',
    },
    {
        title: 'Contributor Documentation',
        description:
            'The technologies Ares 2 is built on, the policy model and the subsystems. For people working on Ares 2 itself.',
        to: '/contributor/how-can-you-contribute',
    },
];

const features: string[] = [
    'Policy-based sandboxing through static analysis and runtime instrumentation',
    'More robust tests and builds through limits on time, threads and IO',
    'Public and hidden Artemis tests, where hidden tests obey a custom deadline',
    'Utilities for clearer feedback, including multiline error messages and fault locations',
    'Comfortable testing of exercises that use System.out and System.in',
];

function Hero(): ReactNode {
    const { siteConfig } = useDocusaurusContext();

    return (
        <header className={styles.hero}>
            <div className="container">
                <Heading as="h1" className={styles.heroTitle}>
                    Ares 2
                </Heading>
                <p className={styles.heroSubtitle}>{siteConfig.tagline}</p>
                <div className={styles.buttons}>
                    <Link className="button button--secondary button--lg" to="/instructor/protect-a-java-project/installation">
                        Get started
                    </Link>
                    <Link className="button button--outline button--secondary button--lg" to="/contributor/how-can-you-contribute">
                        Contributor documentation
                    </Link>
                </div>
            </div>
        </header>
    );
}

export default function Home(): ReactNode {
    return (
        <Layout
            title="Ares 2"
            description="Documentation for Ares 2, the Artemis Java Test Sandbox: secure remote execution of student submissions."
        >
            <Hero />
            <main className="container">
                <div className={styles.cards}>
                    {destinations.map((destination) => (
                        <Link key={destination.to} to={destination.to} className={styles.card}>
                            <Heading as="h2" className={styles.cardTitle}>
                                {destination.title}
                            </Heading>
                            <p className={styles.cardDescription}>{destination.description}</p>
                        </Link>
                    ))}
                </div>

                <div className={clsx('row', styles.features)}>
                    <div className="col col--6">
                        <Heading as="h2">What Ares 2 does</Heading>
                        <ul>
                            {features.map((feature) => (
                                <li key={feature}>{feature}</li>
                            ))}
                        </ul>
                    </div>
                    <div className="col col--6">
                        <Heading as="h2">Add it to an exercise</Heading>
                        <CodeBlock language="xml" title="pom.xml">
                            {`<dependency>
    <groupId>de.tum.cit.ase</groupId>
    <artifactId>ares</artifactId>
    <version>2.1.2</version>
</dependency>`}
                        </CodeBlock>
                        <CodeBlock language="groovy" title="build.gradle">
                            {`implementation("de.tum.cit.ase:ares:2.1.2")`}
                        </CodeBlock>
                    </div>
                </div>
            </main>
        </Layout>
    );
}
