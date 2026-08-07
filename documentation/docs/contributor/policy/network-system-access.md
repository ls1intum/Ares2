---
title: "Network System Access"
sidebar_position: 4
description: "Which hosts and ports the supervised code may connect to, and what it may do on the connection."
---

:::tip ELI5
This is the phone book of numbers the student's program is allowed to call.

Each entry names who may be called and on which line, and then says whether the program may
dial at all, whether it may speak, and whether it may listen. A program can be permitted to
open a connection and still be forbidden from sending anything down it.
:::

## Position in the example policy file

The section documented on this page is marked in red. Every page in this section shows the
same example file, so reading them in order walks it from top to bottom.

```yaml title="security-policy.yaml"
regardingTheSupervisedCode:
  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_WALA_AND_ASPECTJ
  theSupervisedCodeUsesTheFollowingPackage: "org.example"
  theMainClassInsideThisPackageIs: "Main"

  theFollowingClassesAreTestClasses:
    - "org.example.PenguinTest"

  theFollowingResourceAccessesArePermitted:

    regardingFileSystemInteractions:
      - onThisPathAndAllPathsBelow: "something.txt"
        readAllFiles: true
        overwriteAllFiles: true
        createAllFiles: true
        executeAllFiles: false
        deleteAllFiles: false

# policy-focus-start
    regardingNetworkConnections:
      - onTheHost: "www.example.com"
        onThePort: 80
        openConnections: true
        sendData: true
        receiveData: true
# policy-focus-end

    regardingCommandExecutions:
      - executeTheCommand: "ls"
        withTheseArguments:
          - "-l"

    regardingThreadCreations:
      - createTheFollowingNumberOfThreads: 10
        ofThisClass: "org.example.Worker"

    regardingPackageImports:
      - importTheFollowingPackage: "java.util"

    regardingTimeouts:
      - timeout: 120
```

## Fields

Implemented by `NetworkPermission` in
[`policy/policySubComponents/NetworkPermission.java`](https://github.com/ls1intum/Ares2/blob/main/src/main/java/de/tum/cit/ase/ares/api/policy/policySubComponents/NetworkPermission.java).

| Field | Datatype | Explanation | Example | Regex or Range |
| --- | --- | --- | --- | --- |
| `onTheHost` | `String` | The host this entry governs. | `www.example.com` | `HOST_PATTERN`: `*`, `localhost`, an IPv4 address, an IPv6 address (including IPv4-mapped forms), or a DNS name of at most 253 characters whose labels are at most 63 characters. A bare four-part numeric string is rejected as a DNS name so that it must parse as an IP address. |
| `onThePort` | `int` | The port this entry governs. `0` is the any-port wildcard. | `80` | Range `0`–`65535` inclusive. Outside that range the constructor throws. |
| `openConnections` | `boolean` | Permits opening a connection to the host and port. | `true` | `true` or `false`. Absent means `false`. |
| `sendData` | `boolean` | Permits sending data on the connection. | `true` | `true` or `false`. Absent means `false`. |
| `receiveData` | `boolean` | Permits receiving data on the connection. | `true` | `true` or `false`. Absent means `false`. |

## Notes

Port `0` is the **only** any-port wildcard. There is no range syntax.

A narrow allowance stays narrow at runtime even though the architecture layer cannot represent it: static analysis is argument-insensitive, so it sees only that a connection may be opened, while the AOP layer checks the actual host and port of the call.
