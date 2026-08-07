# Ares 2

**The Artemis Java Test Sandbox**

[![Java CI with Maven](https://github.com/ls1intum/Ares2/actions/workflows/maven.yml/badge.svg?event=push)](https://github.com/ls1intum/Ares2/actions/workflows/maven.yml)
[![CodeQL](https://github.com/ls1intum/Ares2/actions/workflows/codeql.yml/badge.svg?event=push)](https://github.com/ls1intum/Ares2/actions/workflows/codeql.yml)
[![Maven Central](https://img.shields.io/maven-central/v/de.tum.cit.ase/ares)](https://central.sonatype.com/artifact/de.tum.cit.ase/ares)
[![License: MIT](https://img.shields.io/github/license/ls1intum/Ares2)](LICENSE)
![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)

Ares 2 is a framework for the easy and secure remote execution of student submissions on an
interactive learning platform. It is the second Java-based implementation of the Secure COder
Remote Execution (SCORE) framework, and the first to support Java 25 and later.

- **Policy-based sandboxing** through static analysis and runtime instrumentation, to prevent
  unsafe operations and reduce cheating
- **More robust tests and builds**, through limits on time, threads and IO
- **Public and hidden tests**, where hidden tests obey a custom deadline
- **Better feedback**, including multiline error message processing and likely fault locations
- **Console interaction testing** for exercises using `System.out` and `System.in`

## Installation

Ares 2 requires at least **Java 17**.

```xml
<dependency>
    <groupId>de.tum.cit.ase</groupId>
    <artifactId>ares</artifactId>
    <version>2.1.2</version>
</dependency>
```

```groovy
implementation("de.tum.cit.ase:ares:2.1.2")
```

## Documentation

📖 **<https://ls1intum.github.io/Ares2/>**

- [Instructor Documentation](https://ls1intum.github.io/Ares2/instructor/ares-2/what-is-ares-2) —
  install Ares 2, wire it into a Maven or Gradle build, write a security policy, and understand
  what it does and does not protect against
- [Maintainer Documentation](https://ls1intum.github.io/Ares2/maintainer/how-can-you-contribute) —
  the technologies Ares 2 is built on, the policy model and the subsystems

The documentation source lives in [`documentation/`](documentation/).

## Examples

Complete, runnable exercises are in [`examples/`](examples/):
[Maven](examples/ares-exercise-maven) and [Gradle](examples/ares-exercise-gradle).

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) for the build
commands, the four enforcement mode combinations CI exercises, and the pull request process.
Participation is governed by our [Code of Conduct](CODE_OF_CONDUCT.md).

Found a security vulnerability? Please do **not** open a public issue; follow
[SECURITY.md](SECURITY.md).

## Citing

If you use Ares 2 in your work, please cite it using the metadata in
[CITATION.cff](CITATION.cff).

## Licence

Ares 2 is licensed under the MIT Licence. See [LICENSE](LICENSE) for details.

Ares 2 is the successor to Ares 1, created by Christian Femers and likewise MIT
licensed. Parts of that original work live on in Ares 2, so both copyright notices are
retained; see [NOTICE](NOTICE) for the attribution.
