---
title: "Installation"
sidebar_position: 2
description: "Adding the Ares 2 dependency to a Maven or Gradle project."
---

:::tip[Simple Story]
One dependency line, and the checklist is available in your project.

That is all this page is. Nothing is enforced yet; this only puts the pad of blank checklists
on the shelf.
:::

:::note[Requirements]
Ares 2 requires at least **Java 17**.
:::

Ares 2 is published to Maven Central as a Maven and Gradle dependency.

## Prerequisites

- **Java 17** or later
- **Gradle** in a version compatible with the chosen freefair AspectJ plugin (the freefair 9.x line used in this guide requires Gradle 9; older Gradle versions need an older freefair line) or **Maven 3.8+**
- **JUnit 5** (Jupiter) for test execution

## Maven

Add the following to the `dependencies` section of your `pom.xml`:

```xml
<dependency>
    <groupId>de.tum.cit.ase</groupId>
    <artifactId>ares</artifactId>
    <version>2.1.2</version>
</dependency>
```

## Gradle

Add the following to the `dependencies` section of your `build.gradle`:

```groovy
implementation("de.tum.cit.ase:ares:2.1.2")
```

## Related dependencies

You can remove explicit JUnit 5 dependencies, because Ares 2 already includes them. Keep or
add AssertJ and Hamcrest if your tests use them. If you want to use jqwik (1.2.4 or later) or
JUnit 4 (through the JUnit 5 vintage engine), include them in the dependencies section
yourself.

## Alternative: GitHub Packages

Maven Central is recommended, as it needs no authentication. Ares 2 is also published to
GitHub Packages, which does require a token; see [GitHub Packages](github-packages.md).
