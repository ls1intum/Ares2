---
title: "Installation"
sidebar_position: 3
description: "Adding the Ares 2 dependency to a Maven or Gradle project."
---

:::note[Requirements]
Ares 2 requires at least **Java 17**.
:::

Ares 2 is published to Maven Central as a Maven and Gradle dependency.

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
