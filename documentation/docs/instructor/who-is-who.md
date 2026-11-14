---
title: "Who is who"
sidebar_position: 8
description: "The people, places and things in the Simple Story boxes, and the part of Ares each one stands for."
---

:::tip[Simple Story]
Every story on this site takes place in one school, during one examination. The same people
turn up on every page, and each one always does the same job.

This page tells you who is who.
:::

Every page of this guide and of the contributor guide opens with a short story. The tables
below translate its figures back into the parts of Ares they stand for. Where Ares does only
part of what a figure suggests, the right-hand column says so.

## People and places

| In the story | In Ares |
| --- | --- |
| The head teacher, who writes the test sheet, the grading instructions and the rule sheet, and stays in their office during the examination | You, the instructor. You write the tests and the security policy, and you are not there when a submission runs. |
| The pupils | The methods the student wrote. |
| The teacher, who hands out one question at a time and marks each answer by the grading instructions | JUnit Jupiter, the test framework. It runs your test methods one by one and decides nothing about security. |
| The teacher clearing the desks before the start, by the rule sheet | The architecture layer, ArchUnit or the T. J. Watson Libraries for Analysis (WALA). It reads the compiled submission rather than its source, and rejects any route to something forbidden before the code runs. Once the policy grants anything in a category, this check stops covering that category. |
| The teacher watching during the examination, by the rule sheet, in one of two ways: watching the pupils' hands, or standing beside the things a pupil can reach for | The aspect-oriented programming (AOP) layer. AspectJ is woven into the student code, which is watching the hands; instrumentation is attached to the classes the code reaches for, which is standing beside the things. Either one intercepts the operations it can see while the code runs and compares them with what the policy allows. The two do not catch exactly the same operations. |
| The school itself, whose office makes sure the rule sheet on the desk is the head teacher's | Your build. Each kind of school is one build tool, Maven or Gradle. Ares cannot check from the inside that the policy it reads is yours, so the build has to guarantee it. |
| A name the school keeps for itself | The packages Ares reserves for itself. Student code that borrows one would be trusted like Ares, so the build has to refuse it. |
| A special examination room with new anti-cheating features | Phobos, a sandbox outside the Java Virtual Machine (JVM) for the file system, the network and timeouts. Ares writes settings for it, but in Postcompile nothing runs them, and the current Phobos rejects the file and network sections Ares writes. |
| The space between the privacy screens | The package of the student code, as `theSupervisedCodeUsesTheFollowingPackage` names it. |
| The language the pupils answer in | The programming language of the exercise. |

## The sheets

| In the story | In Ares |
| --- | --- |
| The questions on the test sheet | Your test methods. |
| The grading instructions | Your test classes. The classes named in `theFollowingClassesAreTestClasses` are exempt from the security policy, so they can do what the student code cannot, such as opening a file to check it. |
| Instructions forbidding certain words and ways of answering | The abstract syntax tree (AST) check, for example "solve it without a loop". It reads the source the student wrote and is not a security boundary. |
| The rule sheet | Ares, as configured by `security-policy.yaml`. |
| One check on the rule sheet | One security test case that Ares generates from the policy. |
| The old rules, written beside each question | Ares 1, whose restrictions were annotations on each test. |

## The examination

| In the story | In Ares |
| --- | --- |
| The examination | One test run. |
| One question | One run of one test method. A test method can call several student methods. |
| The head teacher prints the rules onto the grading instructions beforehand | Precompile: Ares generates the security test cases into your test repository once, before the build. |
| The teacher picks up the rule sheet for each question and puts it away afterwards | Postcompile: Ares reads the policy while the tests run, prepares the checks for each test method and clears them afterwards. |
| The teacher's stopwatch for one question | `@StrictTimeout`. This is the time limit that works today; a timeout written into the policy does not bound a test yet. |

## Marking

| In the story | In Ares |
| --- | --- |
| The teacher says after each answer whether it was right or wrong | `@PublicTest`. |
| The teacher holds a question back until the examination is over, and only then asks it and says whether the answer was right or wrong | `@HiddenTest`. Before the deadline the test fails at once without running, with the message that hidden tests run after the deadline, unless an `@ActivateHiddenBefore` date has not passed yet. After the deadline it runs like any other test. |
| When the examination is over | `@Deadline`. |
| Extra time | `@ExtendedDeadline`. |
| The head teacher trying the held-back questions before any pupil | `@ActivateHiddenBefore`, which lets hidden tests run before the given date, so you can check them before release. |
| The teacher tells the pupil off and names the rule they broke | The message Ares reports when a submission breaks the security policy. A wrong answer is a failing test, not this message. |

## What the rule sheet can allow

| In the story | In Ares |
| --- | --- |
| A worksheet, which a pupil can be allowed to read, change, make, use or throw away | A file. Read, overwrite, create, execute and delete are five separate permissions in `regardingFileSystemInteractions`. |
| A pile of worksheets | A directory and every path below it, as `onThisPathAndAllPathsBelow` covers it. |
| The telephone: the number, the line, dialling, talking and listening | A network connection: host and port, and separately connecting, sending and receiving, in `regardingNetworkConnections`. |
| A friend outside the room who helps, and the exact words the pupil is allowed to say to them | A command the code starts, with exactly the arguments it is allowed to pass, in `regardingCommandExecutions`. |
| Side jobs a pupil starts during the examination, and how many they can start | Threads, by class and by the number that can be created in total, in `regardingThreadCreations`. The limit counts threads created, not threads running at the same time. |
| The permitted aids | Packages the student code is allowed to depend on, in `regardingPackageImports`. Ares always permits some of them, such as the Java standard library. |
| Aids the head teacher put out | Libraries you add to the exercise. Ares cannot vouch for what they do. |

## What nobody is allowed to do, and what Ares cannot see

| In the story | In Ares |
| --- | --- |
| Things nobody is allowed to do: pull the fire alarm, look inside the teacher's desk, let a stranger in | The categories Ares refuses outright, such as ending the JVM, reflection and class loading. Only the architecture layer checks them as such; an operation that falls under a grantable category as well can still be caught while the code runs. |
| A pupil answering out loud | Console input and output. |
| A pupil who never stops talking | Too much output, which fails the test. |
| An answer copied from someone else | Plagiarism. Ares does not detect it. |

## Getting Ares

| In the story | In Ares |
| --- | --- |
| A pad of blank rule sheets in the staff room cupboard | Ares as a dependency of your exercise. Adding it enforces nothing yet. |
| Two shops, one of which asks to see a staff card | Maven Central and GitHub Packages. GitHub Packages needs a token. |
