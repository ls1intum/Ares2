---
title: "GitHub Packages"
sidebar_position: 11
description: "Consuming Ares 2 from GitHub Packages instead of Maven Central."
---

:::tip[ELI5]
There are two shops you can get Ares from. This page is about the one that asks for ID at the
door.

Most people should use the other one, which does not. This page exists for the cases where you
cannot.
:::

:::tip[Maven Central is recommended]
Maven Central requires no authentication and is the simpler choice for most users. See
[Installation](installation.md). Use GitHub Packages only if you specifically need it.
:::

GitHub Packages does not currently allow unregistered, public access to packages. You therefore
need to authenticate to GitHub if you use GitHub Packages as a repository source.

## Maven configuration

Add the following to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub ls1intum Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/ls1intum/Ares2</url>
    </repository>
</repositories>
```

Then add authentication to your `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

## Gradle configuration

Add the following to your `build.gradle`:

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/ls1intum/Ares2")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

## Generating a GitHub token

1. Go to GitHub Settings → Developer settings → Personal access tokens.
2. Generate a new token with the `read:packages` scope.
3. Store the token securely in your settings.

:::warning[Keep the token out of the repository]
Read it from an environment variable or from `~/.gradle/gradle.properties`, never from a file
committed alongside the exercise.
:::
