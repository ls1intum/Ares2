---
title: "DFS Path"
sidebar_position: 3
description: "Depth-first traversal of the call graph, used to find a route from student code to a forbidden operation."
---

:::tip ELI5
You are in a maze and want to know whether any route reaches the treasure.

Depth-first search means: always take the next unexplored corridor, keep going until you
hit a dead end, then back up to the last junction and try the next one. If a route exists,
this finds it. And the trail you followed *is* the answer: it shows exactly how you got there.
:::

## What it is

Depth-first search explores a graph by following one edge as far as it goes before
backtracking. Applied to a call graph, it answers reachability: is there a path from this
method to that one?

Two properties matter here:

1. **Visited nodes must be tracked.** Call graphs contain cycles, because methods recurse
   and call one another mutually. Without a visited set, the search does not terminate.
2. **The path is the diagnostic.** Knowing a sink is reachable is much less useful than
   knowing the chain of calls that reaches it. The route is what a student can act on.

## In Ares 2

The architecture layer reports the path it found, not merely the verdict. A denial that
says only "forbidden file access" leaves a student guessing; one that names the chain from
their method to the forbidden call tells them where to look.

This is also where false positives arise. A path through the graph is a path that *may*
exist, and an over-approximated edge produces a reachable sink that no execution reaches.
[WALA](../wala.md) filters some of these.

## Further reading

- [Introduction to Depth First Search (DFS)](https://www.baeldung.com/cs/depth-first-search-intro) — Baeldung on Computer Science
- [Graphs in Java](https://www.baeldung.com/java-graphs) — Baeldung
- [Introduction to Graph Theory](https://www.baeldung.com/cs/graph-theory-intro) — Baeldung on Computer Science
