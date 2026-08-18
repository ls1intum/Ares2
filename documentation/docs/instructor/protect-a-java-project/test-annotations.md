---
title: "Test Annotations"
sidebar_position: 4
description: "The @Public, @Hidden, @PublicTest, @HiddenTest and @Deadline annotations."
---

:::tip[Simple Story]
These are the labels you put on a question to say which kind it is.

Public means the mark is read out at once. Hidden means it waits, and a hidden question has to
be told **when** to wake up, because Ares refuses to guess a deadline.
:::

## What puts a test under supervision

An Ares test annotation is what does it. `@Public`, `@Hidden`, `@PublicTest` and `@HiddenTest`
carry the JUnit extensions that load the policy, arm the runtime interception and apply the
deadline. A plain JUnit `@Test` carries none of them.

:::danger[`@Policy` activates nothing on its own]
`@Policy` selects **which** policy applies. It registers no extension, so a method annotated
with a plain `@Test` and a `@Policy` runs completely unsupervised, and nothing reports it: the
test passes and every resource domain stays open. Dropping an `@Public` while editing a test
class therefore loses all enforcement without an error, which is the failure mode this page
exists to prevent.
:::

## Combined annotations

Besides using `@Public` and `@Hidden` together with JUnit's `@Test`, Ares 2 also provides two
combined annotations, `@PublicTest` and `@HiddenTest`. These include the JUnit `@Test`
annotation internally and therefore offer a shorter, method-level notation. `@PublicTest`
corresponds to using `@Public` and `@Test` together, and the same applies to `@HiddenTest`.

```java
import static org.junit.jupiter.api.Assertions.*;

// IMPORTANT: make sure to use the "jupiter" ones (if you are not using jqwik)
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

// This example will not work just like that, see below why
public class PenguinTest {

    @PublicTest
    void testPenguinPublic() {
        Penguin pingu = new Penguin("Julian");
        assertEquals("Julian", pingu.getName(), "getName() does not return the name supplied to the constructor");
    }

    @HiddenTest
    void testPenguinHidden() {
        assertThrows(NullPointerException.class, () -> new Penguin(null));
    }
}
```

The combined annotations are functionally equivalent, and can be used where a more concise
syntax is preferred.

## Deadlines

Hidden tests require a deadline. If a test method is marked `@Hidden` (or `@HiddenTest`) and
no deadline is given on the class or on the method itself, JUnit reports:

```text
java.lang.annotation.AnnotationFormatError: cannot find a deadline for hidden test testPenguinHidden()
```

Tell Ares 2 the deadline with another annotation:

```java
// Format must be ISO_LOCAL_DATE(T| )ISO_LOCAL_TIME( ZONE_ID)?
@Deadline("2020-06-09 03:14 Europe/Berlin")
public class PenguinTest {
    // ...
}
```

Like most Ares 2 annotations, `@Deadline` can also be placed on the test method (and on
nested classes). When several are present, the one closest to the test method wins.

That is enough to make the example work. Try varying the deadline: if the given
`LocalDateTime` lies in the past, the test method is executed and, together with the student
code shown in [Setup](setup.md), passes. If the deadline has not passed, the test does not
pass either. It fails with

```text
org.opentest4j.AssertionFailedError: hidden tests will be executed after the deadline.
```

and the test was not executed, because the deadline is always checked before any hidden test
method runs.

:::warning[Always specify the time zone]
The annotation parser permits leaving the time zone unspecified, but doing so risks running
the tests at the wrong time when the build agent's time zone differs from the one on your own
machine, or from the one you assume. Ares 2 warns in the logs when it runs tests whose time
zone was not set.
:::

## Related options

- [`@ActivateHiddenBefore`](further-options.md#testing-the-exercise-before-release) — run hidden
  tests while authoring the exercise
- [`@ExtendedDeadline`](further-options.md#extending-a-deadline-and-disability-compensation) —
  extend a deadline by a duration
