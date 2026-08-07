---
title: "Further Important Options"
sidebar_position: 9
description: "Path access, testing before release, deadline extensions, threads, console interaction, networking and locale."
---

The basics are covered by [Setup](setup.md) and [Test Annotations](test-annotations.md),
but there is more you need to know about testing with Ares 2. The earlier example used a single
class and very little testing. Without the knowledge below, you may not get Ares 2 to work and
will get rather annoyed, so please read on.

## Path Access and Class Loading

File access is default-deny, and is granted only through `regardingFileSystemInteractions` in
the YAML policy referenced by `@Policy`. Generated files are confined to the explicit canonical
project root. See the [Security Policy Manual](/contributor/subsystems/policy/security-policy-manual).

## Testing the Exercise before Release

Hidden tests are executed by Ares 2 only after the deadline. That raises the question of how
exercise creators are meant to work on the tasks, tests and sample solution. One option would
be to change the deadline temporarily, but then it is quite likely someone forgets to change it
back, and the protection of the hidden tests fails.

Use `@ActivateHiddenBefore` just like `@Deadline` to state the `LocalDateTime` before which
hidden tests should be executed. This date should of course lie before the release of the
exercise on Artemis.

## Extending a Deadline and Disability Compensation

Use `@ExtendedDeadline` together with a duration such as `1d` or `2d 12h 30m` to extend the
deadline by that amount. `@ExtendedDeadline("1d")`, for example, extends the deadline by one
day.

If you use the annotation at several levels (for example class and method) without stating a
new deadline (for example a deadline only at class level), the extensions add up.

## Threads and Concurrency

Thread creation is configured only through `regardingThreadCreations` in the active policy.
Specify the permitted class and the maximum count; omission means denial.

## Testing Console Interaction

One example showing some of the possibilities:

```java
void testSquareCorrect(IOTester tester) { // (1)
    tester.provideInputLines("5"); // (2)

    InputOutputPenguin.calculateSquare(); // (3)

    tester.err().assertThat().isEmpty(); // (4)
    tester.out().assertThat().isEqualTo("""
                Enter Number:
                Answer:
                25"""); // (5)
}
```

1. Declare `IOTester` as a parameter.
2. Provide input lines before calling the student code. This content is used for reading lines
   from `System.in`.
3. Call the student code to process the input and produce output.
4. Assert that nothing was printed to `System.err`.
5. Assert that the standard output (here excluding the final line break) equals the given text.
   If you use text blocks, be aware of their newline handling.

Ares 2 normalises line breaks to `\n`, and
[`OutputTester`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/io/OutputTester.java)
offers many different ways of checking output (single string, list of strings, and more).

If students read more lines than were provided, they get the following feedback:

```text
java.lang.IllegalStateException: no further console input request after the last(number 1: "5") expected.
```

See also `IOTester` and, for more examples, the
[`InputOutputUser`](https://github.com/ls1intum/Ares2/blob/main/src/test/java/de/tum/cit/ase/ares/integration/testuser/InputOutputUser.java)
test.

:::tip[Custom IO managers]
If the default `IOTester` does not meet your requirements, provide a custom implementation by
applying `@WithIOManager(MyCustomOne.class)` to, for example, the test class or individual
methods. This also lets you register a custom parameter to control IO testing conveniently
inside the test method. Have a look at the test class linked above, or read the documentation
of
[`IOManager`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/io/IOManager.java).
:::

## Networking

Network access is default-deny. Configure each host, explicit port and operation boolean under
`regardingNetworkConnections`; port `0` is the sole any-port wildcard. Narrow allowances remain
narrow at runtime, even though argument-insensitive static analysis cannot represent them.

## Locale

You can set a locale for Ares 2, and for the rest of Java, by adding the `@UseLocale` JUnit
extension to classes or methods. It sets the Java default locale via
`Locale.setDefault(Locale)`, which Ares 2 also uses. The locale is changed only for the scope
where the annotation is applied.

Ares 2 is currently localised in German (`de_DE`) and English (`en_US`), where `en_US` is the
fallback for any other locale.

See also the
[`LocaleUser`](https://github.com/ls1intum/Ares2/blob/main/src/test/java/de/tum/cit/ase/ares/integration/testuser/LocaleUser.java)
test for more examples.
