---
title: "Call Graph"
sidebar_position: 2
description: "The directed graph of which method may call which, and why it is the object of the analysis."
---

:::tip[Simple Story]
While clearing the desks, the teacher works out where each pupil's work can lead: this part can
call on that part, which can call on another. They look at what the work turned into, not at
what the pupil wrote, and nothing runs while they do it.

A forbidden route is seldom one step long. A pupil rarely throws a worksheet away themselves.
They call on a part that calls on another, and that one throws it away. Where the teacher cannot
tell whether a step exists, counting it can fail an honest pupil. Missing it lets a forbidden
route through.

[Who is who](/instructor/who-is-who)
:::

## What it is

A call graph is a directed graph whose nodes are methods and whose edges mean "may call".
*May* is the important word: it is built without running the program, so it describes what
could happen on some execution, not what did happen on one.

Building it exactly is impossible in general, so every call graph is an approximation, and
the interesting question is which way it errs:

- **Over-approximation** includes edges that no real execution takes. This is the safe
  direction for a security tool: it can produce a false positive, never a false negative.
- **Under-approximation** misses edges. For a security tool this is the dangerous
  direction, because a missed edge is forbidden behaviour that goes unnoticed.

Virtual dispatch, reflection and dynamically loaded classes are the hard cases, because the
target of the call is not fixed in the bytecode.

## In Ares 2

The call graph is what makes indirect access detectable. The policy names forbidden
operations as *sinks*; the analysis then asks whether any path leads from student code to a
sink. `theMainClassInsideThisPackageIs` supplies the entry point the graph is built from.

See [depth-first search (DFS) Path](./dfs-path.md) for how those paths are searched, and
the [T. J. Watson Libraries for Analysis (WALA)](../wala.md) for the engine that builds the
graph.

## Further reading

- [Graphs in Java](https://www.baeldung.com/java-graphs) — Baeldung
- [Introduction to Graph Theory](https://www.baeldung.com/cs/graph-theory-intro) — Baeldung on Computer Science
- [WALA Wiki](https://github.com/wala/WALA/wiki) — project documentation
- [Guide to Java Reflection](https://www.baeldung.com/java-reflection) — Baeldung
