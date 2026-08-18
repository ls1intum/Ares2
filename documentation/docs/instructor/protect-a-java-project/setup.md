---
title: "Setup"
sidebar_position: 3
description: "The problem Ares 2 solves, using a worked example with a public and a hidden test."
---

:::tip[Simple Story]
Some marks are read out as soon as the pupil has answered. Others stay sealed until the
deadline has passed.

Simply keeping the sealed ones quiet is not enough, because a question that was asked can leave
traces behind. This page shows the problem with a small worked example before fixing it.
:::

This page introduces the problem Ares 2 solves, using a small worked example. The
[next page](test-annotations.md) then covers the annotations in detail.

## A starting point

Assume a Maven project whose `pom.xml` looks like this:

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>

<dependencies>
    <dependency>
        <groupId>de.tum.cit.ase</groupId>
        <artifactId>ares</artifactId>
        <version>2.1.2</version>
    </dependency>
</dependencies>
```

Consider the following student class that needs to be tested:

```java
import java.util.Objects;

public final class Penguin {

    private final String name;

    public Penguin(String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public String getName() {
        return name;
    }
}
```

And a simple JUnit 5 test class for it:

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PenguinTest {

    @Test
    void testPenguinPublic() {
        Penguin pingu = new Penguin("Julian");
        assertEquals("Julian", pingu.getName(), "getName() does not return the name supplied to the constructor");
    }

    @Test
    void testPenguinHidden() {
        assertThrows(NullPointerException.class, () -> new Penguin(null));
    }
}
```

In this example:

- `testPenguinPublic()` is supposed to run after each push and give students immediate
  feedback, while
- `testPenguinHidden()` should run only after the exercise deadline, and its results should
  not be visible before then.

## Why marking a test hidden is not enough

Artemis has a feature to mark tests as hidden, but that alone does not prevent the
contents of the test method leaking through static variables, files and similar, whether
accidentally or on purpose. To prevent that, **the hidden test method must not be executed
before the deadline at all**.

The public test method does not need to be hidden, since its purpose is to give direct feedback.
There are still several possible problems, though, such as crashing the Maven build with
`System.exit(0)`, or an endless loop. Both harm the interactive learning experience, because
students are confronted with an incomprehensible log of a failed build. Such errors can be
explained, but that takes a lot of time, especially when it happens often, and it will once
the number of students is large enough.

It is also a security concern: students could try to read the `.java` files containing the
test classes.

## Integrating Ares 2

Ares 2 secures the tests and avoids unintelligible feedback. The most basic way to do this is
with the `@Public` and `@Hidden` annotations:

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

// IMPORTANT: make sure to use the "jupiter" ones (if you are not using jqwik)
import de.tum.cit.ase.ares.api.jupiter.Hidden;
import de.tum.cit.ase.ares.api.jupiter.Public;

// This example will not work just like that, see below why
public class PenguinTest {

    @Public
    @Test
    void testPenguinPublic() {
        Penguin pingu = new Penguin("Julian");
        assertEquals("Julian", pingu.getName(), "getName() does not return the name supplied to the constructor");
    }

    @Hidden
    @Test
    void testPenguinHidden() {
        assertThrows(NullPointerException.class, () -> new Penguin(null));
    }
}
```

This is not yet complete: a hidden test needs a deadline. Continue with
[Test Annotations](test-annotations.md).
