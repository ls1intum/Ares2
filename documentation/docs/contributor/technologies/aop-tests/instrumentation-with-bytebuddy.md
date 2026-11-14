---
title: "Instrumentation with ByteBuddy"
sidebar_position: 3
description: "Load-time bytecode rewriting through a Java agent and Byte Buddy."
---

:::tip[Simple Story]
This is the other way the teacher watches by the rule sheet during the examination. Here the teacher stands
beside the things a pupil can reach for, such as the worksheets and the telephone. They take up
that place as each thing comes into the room, before anyone uses it.

So it does not matter who reaches out: a pupil, or one of the permitted aids working for them.
This way catches most of what watching the pupils' hands misses. The two ways still do not catch exactly
the same things.

[Who is who](/instructor/who-is-who)
:::

## What it is

The `java.lang.instrument` application programming interface (API) lets an agent, attached with `-javaagent`, transform class
bytes as classes are loaded. Byte Buddy is the library Ares uses to express those
transformations without writing bytecode by hand.

## How Ares 2 uses it

This is the second enforcement backend, selected by the `INSTRUMENTATION` half of the
[programming language configuration](/contributor/policy/programming-language-configuration).
`JavaInstrumentationAgent` is the `Premain-Class` of the published `agent` classifier, so
an exercise consumes a JAR that already carries the right manifest.

The advice classes go on the **bootstrap append path**. Advice woven into a bootstrap class
has to be reachable from a class loaded by the bootstrap loader, which the ordinary
application classpath is not.

Several `--add-opens` and `--add-exports` flags are required for the same reason: the agent
reaches into Java Development Kit (JDK) internals that the module system closes by default.

## Why it reaches further than call-site weaving

Rewriting the target rather than the caller means every route to the method is covered at
once, including reflective and library-mediated ones. Coverage becomes a property of the
*target*, not an enumeration of call sites.

## Further reading

- [Byte Buddy](https://bytebuddy.net/) — project site
- [raphw/byte-buddy](https://github.com/raphw/byte-buddy) — source repository
- [A Guide to Byte Buddy](https://www.baeldung.com/byte-buddy) — Baeldung
- [Guide to Java Instrumentation](https://www.baeldung.com/java-instrumentation) — Baeldung
- [`java.lang.instrument`](https://docs.oracle.com/en/java/javase/21/docs/api/java.instrument/java/lang/instrument/package-summary.html) — Oracle, Java SE 21 API
- [Applying "Java Aspects" at Load Time: Java Instrumentation API](https://medium.com/javarevisited/applying-java-aspects-at-load-time-java-instrumentation-api-232b39622b32) — Hrishabh Purohit, Medium (freely readable)
