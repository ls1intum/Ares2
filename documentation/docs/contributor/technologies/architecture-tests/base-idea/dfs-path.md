---
title: "DFS Path"
sidebar_position: 3
description: "Depth-first traversal of the call graph, used to find a route from student code to a forbidden operation."
---

:::tip[Simple Story]
While clearing the desks, the teacher wants to know whether any route leads from a pupil's work
to something forbidden.

They follow one route as far as it goes. At a dead end, they go back to the last fork and try
the next way. They remember every stop they have seen, because some routes go round in circles.
When they find a forbidden route, they name every step of it, so the pupil knows where to look.
Some routes they find are ones the work never takes when it runs, so this can fail an honest
pupil.

[Who is who](/instructor/who-is-who)
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

This is where false positives arise too. A path through the graph is a path that *may*
exist, and an over-approximated edge produces a reachable sink that no execution reaches.
The [T. J. Watson Libraries for Analysis (WALA)](../wala.md) filters some of these.

## Further reading

- [Introduction to Depth First Search (DFS)](https://www.baeldung.com/cs/depth-first-search-intro) — Baeldung on Computer Science
- [Graphs in Java](https://www.baeldung.com/java-graphs) — Baeldung
- [Introduction to Graph Theory](https://www.baeldung.com/cs/graph-theory-intro) — Baeldung on Computer Science
