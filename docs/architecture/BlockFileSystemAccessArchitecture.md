<a id="file-system-security-mechanism"></a>
# Ares 2 Architecture File System Access Control: File System Security Mechanism (Architecture Analysis)

<a id="table-of-contents"></a>
## Table of Contents

1. [Ares 2 Architecture File System Access Control: High-Level Overview](#1-ares-2-architecture-file-system-access-control-high-level-overview)
   - [1.1 What Does the UML Activity Diagram Look Like?](#11-what-does-the-uml-activity-diagram-look-like)
   - [1.2 What Is Architecture Testing?](#12-what-is-architecture-testing)
   - [1.3 Which Architecture Modes / Implementations Are There?](#13-which-architecture-modes--implementations-are-there)
   - [1.4 What Are The Internal Configuration Settings?](#14-what-are-the-internal-configuration-settings)
   - [1.5 When Is File Access Generally Blocked?](#15-when-is-file-access-generally-blocked)
2. [Ares 2 Architecture File System Access Control: Ares Monitors FileSystem Methods](#2-ares-2-architecture-file-system-access-control-ares-monitors-file system-methods)
   - [2.1 Which Operations Does Ares 2 Architecture File System Access Control Monitor?](#21-which-operations-does-ares-2-architecture-file-system-access-control-monitor)
   - [2.2 What Are The Monitored READ Operations?](#22-what-are-the-monitored-read-operations)
   - [2.3 What Are The Monitored WRITE/OVERWRITE Operations?](#23-what-are-the-monitored-writeoverwrite-operations)
   - [2.4 What Are The Monitored CREATE Operations?](#24-what-are-the-monitored-create-operations)
   - [2.5 What Are The Monitored DELETE Operations?](#25-what-are-the-monitored-delete-operations)
   - [2.6 What Are The Monitored EXECUTE Operations?](#26-what-are-the-monitored-execute-operations)
3. [Ares 2 Architecture File System Access Control: Student Code Triggers Security Check](#3-ares-2-architecture-file-system-access-control-student-code-triggers-security-check)
4. [Ares 2 Architecture File System Access Control: Ares Collects Information About the File Access](#4-ares-2-architecture-file-system-access-control-ares-collects-information-about-the-file-access)
   - [4.1 Loading Java Classes (ArchUnit)](#41-loading-java-classes-archunit)
   - [4.2 Building the Call Graph (WALA)](#42-building-the-call-graph-wala)
5. [Ares 2 Architecture File System Access Control: Ares Validates the File Access](#5-ares-2-architecture-file-system-access-control-ares-validates-the-file-access)
   - [5.1 ArchUnit Mode: Static Analysis](#51-archunit-mode-static-analysis)
   - [5.2 WALA Mode: Call Graph Analysis](#52-wala-mode-call-graph-analysis)
   - [5.3 Transitive Access Detection](#53-transitive-access-detection)
   - [5.4 Reachability Analysis (WALA)](#54-reachability-analysis-wala)
   - [5.5 False Positive Filtering (WALA)](#55-false-positive-filtering-wala)
6. [Ares 2 Architecture File System Access Control: Operation Type Classification](#6-ares-2-architecture-file-system-access-control-operation-type-classification)
7. [Ares 2 Architecture File System Access Control: Conclusion](#7-ares-2-architecture-file-system-access-control-conclusion)
   - [7.1 Technical Details](#71-technical-details)

---

<a id="1-ares-2-architecture-file-system-access-control-high-level-overview"></a>
# 1. Ares 2 Architecture File System Access Control: High-Level Overview

This document explains how Ares 2 decides whether student code may access the file system through static code analysis. It checks:
- The code structure for file system method calls
- Which methods are reachable from student code
- Whether the calling classes are on the allow-list of exempt classes (`allowedClasses`)

---

## Summary for Programming Instructors (TL;DR)

**What does Architecture Testing do?**
- ✅ Analyses **compiled bytecode** to detect forbidden file system operations
- ✅ Works **before code execution** - catches violations during testing phase
- ✅ Provides **two analysis modes**: Fast (ArchUnit) and Precise (WALA)
- ✅ Detects **transitive calls** - finds violations even through helper methods
- ✅ Generates **clear error messages** with complete call chains

**When do you need this?**
- When you want to prevent students from using file system operations entirely
- For pre-submission checks (CI/CD pipelines)
- When runtime monitoring (AOP) is not feasible or desired
- For comprehensive code structure validation

**How does it work (simplified)?**
1. Compile student code to `.class` files
2. Architecture analysis scans bytecode for file system method calls
3. If forbidden patterns detected → Report violation with call chain
4. Unlike AOP, this happens during testing, not when code runs

---

## Comparison: Architecture vs. AOP

| Aspect | Architecture (ArchUnit/WALA) | AOP (Byte Buddy/AspectJ) |
|--------|------------------------------|---------------------------|
| **Analysis Time** | Before execution (static) | During execution (runtime) |
| **Detection** | Analyses code structure | Intercepts method calls |
| **Granularity** | Binary (allowed/forbidden) | Path-based permissions |
| **Performance Impact** | Analysis overhead only | Runtime overhead on every call |
| **False Positives** | Possible (unreachable code) | None (only executed code checked) |
| **Coverage** | All code paths | Only executed paths |
| **Configuration** | Class-level exemptions (`allowedClasses`); package permissions apply to the import rule only | Path-level permissions |
| **Use Case** | Pre-submission validation | Runtime security enforcement |
| **Error Timing** | Test phase | Production execution |

---

<a id="11-what-does-the-uml-activity-diagram-look-like"></a>
## 1.1 What Does the UML Activity Diagram Look Like?

Below is a general overview of the process for deciding whether to allow or block file access as a UML activity diagram. Throughout this document, you will find the following symbols:
- **🔴 Red** = File access blocked (security policy violation detected)
- **🌕 Yellow** = Intermediate condition met → continue to the next verification step
- **🟢 Green** = File access permitted (no security policy violation detected)

Diagram note: the rendered PNG is not committed in this repository snapshot. The textual flow in this section is authoritative.

---

<a id="12-what-is-architecture-testing"></a>
## 1.2 What Is Architecture Testing?

Architecture Testing is a technique that validates code follows specific structural rules by analysing compiled bytecode **before** execution. Think of it like a building inspector reviewing building plans before construction to ensure doors don't open into forbidden areas - the code doesn't run, but the structure gets checked automatically.

**Concrete Example:**

**Without Architecture Testing:** You would have to manually review student code for file system calls.
```java
public void readFile(String path) {
    Files.readString(Path.of(path));  // Would this be caught? Manual review needed!
}
```

**With Architecture Testing:** Ares automatically analyses bytecode and detects ALL file system method calls, no execution required.
```java
public void readFile(String path) {
    Files.readString(Path.of(path));  // Detected at compile/test time! ArchUnit/WALA finds this.
}
```

**Key Difference from AOP:**
- **AOP (Runtime)**: Monitors method calls during program execution and blocks forbidden operations in real-time
- **Architecture (Static)**: Analyses compiled bytecode before execution to detect potential security violations in the code structure

---

<a id="13-which-architecture-modes--implementations-are-there"></a>
## 1.3 Which Architecture Modes / Implementations Are There?

Ares automatically detects file system operations by analysing compiled bytecode using one of two Architecture implementations:

- **ArchUnit (Static Analysis)**: Pure static analysis using the ArchUnit framework. Fast analysis without call graph construction.
- **WALA (Call Graph Analysis)**: Static analysis with dynamic modelling using IBM WALA framework. Precise call path detection with false positive filtering.

Both implementations analyse the **code structure** to find forbidden method calls, but differ in precision and performance:

| Aspect | ArchUnit | WALA |
|--------|----------|------|
| **Speed** | Fast | Slower |
| **Precision** | Good | Very precise |
| **False Positives** | Possible | Filtered |
| **Call Graphs** | No | Yes |

---

<a id="14-what-are-the-internal-configuration-settings"></a>
## 1.4 What Are The Internal Configuration Settings?

Instructors define architecture policies, and Ares 2 translates them into the following analysis settings:

| Setting | Type | Description | Example |
|---------|------|-------------|---------|
| **architectureMode** | `String` | The used analysis framework | `"ARCHUNIT"` or `"WALA"` |
| **theSupervisedCodeUsesTheFollowingPackage** | `String` | The base package of the supervised (student) code, taken from the security policy. WALA mode uses it (together with a package prefix derived from the analysed classpath) to narrow the entry-point set; ArchUnit mode applies its rules to everything it imports | `"de.student"` |
| **allowedClasses** | `ClassPermission[]` | Classes exempt from the architecture rules (essential/test infrastructure). This is the exemption mechanism for the file-system rule | `[new ClassPermission("de.student.Helper")]` |
| **allowedPackages** | `PackagePermission[]` | Packages allowed to be imported; feeds ONLY the `PACKAGE_IMPORT` rule, not the file-system rule | `[new PackagePermission("java.io")]` |
| **classPath** | `String` | Path to compiled student code | `"target/classes"` |

**Architecture-Specific Configuration:**
- No path-based permissions (no `pathsAllowedToBeRead`, etc.) - Architecture testing detects ANY file system access attempt
- No AOP mode selection - Uses `architectureMode` instead
- Class-level exemptions (`allowedClasses`) instead of path-level permissions; `restrictedPackage` is an AOP setting and does not configure the architecture pipeline

**Note on scope per mode:** ArchUnit mode imports the whole analysed classpath (excluding Ares' own `de.tum.cit.ase.ares.api` classes) and applies the rules to every imported class. WALA mode narrows the analysis to entry points whose declaring class matches the package prefix derived from the analysed classpath (`CustomCallgraphBuilder.derivePackagePrefix`).

---

<a id="15-when-is-file-access-generally-blocked"></a>
## 1.5 When Is File Access Generally Blocked?

**Access is BLOCKED 🔴 if ALL of the following conditions apply:**

1. **Architecture Mode Enabled**: `architectureMode` is set to `"ARCHUNIT"` or `"WALA"`
2. **Student Code Contains File System Calls**: Analysis detects method calls to file system APIs
3. **Calls Are Reachable**: The forbidden methods can be reached from student code (directly or transitively)
4. **Not an Allow-Listed Class**: The calling class is not exempt via the `allowedClasses` list (`ClassPermission`); the `allowedPackages` list only affects the separate package-import rule

**Access is ALLOWED 🟢 if ANY of the aforementioned conditions do not apply**

**Key Differences from AOP:**
- 🔴 Detected at analysis time (before execution), not at runtime
- 🔴 Blocks ALL file system access attempts, not just specific paths
- 🔴 Reports potential violations, even if the code path is never executed

**Legend:**
- 🔴 AssertionError thrown → Security violation detected in code structure
- 🟢 No violations found → Code passes architecture analysis

In summary, Ares trusts code when: 
- Its class is on the `allowedClasses` allow-list (`ClassPermission`)
- It is infrastructure rather than student code (JDK, Ares internal code, test frameworks); in WALA mode this classification is done per call-graph frame, in ArchUnit mode Ares' own `de.tum.cit.ase.ares.api` classes are excluded from the import
- In WALA mode only: its class lies outside the package prefix derived for the entry points

**Security Assumptions:** 
- Student code is compiled and available as `.class` files
- Class files have not been tampered with after compilation
- Call graph accurately represents possible execution paths (WALA mode)

---

<a id="2-ares-2-architecture-file-system-access-control-ares-monitors-file system-methods"></a>
# 2. Ares 2 Architecture File System Access Control: Ares Monitors FileSystem Methods

<a id="21-which-operations-does-ares-2-architecture-file-system-access-control-monitor"></a>
## 2.1 Which Operations Does Ares 2 Architecture File System Access Control Monitor?

For documentation purposes, this chapter groups the monitored file system interactions into five action types. These labels are a documentation-level organisation only: at analysis time each mode reads ONE flat methods file and feeds it into ONE rule ("Accesses file system", the `NO_CLASS_MUST_ACCESS_FILE_SYSTEM` rule / the corresponding `WalaRule`), without any READ/WRITE/CREATE/DELETE/EXECUTE distinction.

- **READ**: Accessing file contents or metadata without modifying them (streams, read APIs, attribute queries).
- **OVERWRITE**: Writing or mutating existing content/attributes (write/append/truncate, metadata setters).
- **CREATE**: Creating new files, directories, or links (create* APIs, file system creation/open).
- **DELETE**: Removing files or scheduling deletion/trash operations.
- **EXECUTE**: In this documentation grouping, "execute" covers file-system entries that cause execution-like behaviour, such as `System.load(...)`, `System.loadLibrary(...)`, and Desktop launches (`Desktop.open(...)`, `Desktop.browse(...)`). Process-spawning APIs are documented under the Command System.

Some APIs can appear under multiple actions because they imply more than one permission (for example, `copy`/`move` or methods that create and write simultaneously).

---

ArchUnit and WALA modes monitor largely overlapping, but not identical, sets of file system methods, loaded from separate template files. The formats differ (ArchUnit uses source-form signatures like `java.io.FileWriter.<init>(java.io.File)`, WALA uses JVM-descriptor form like `java.io.FileWriter.<init>(Ljava/io/File;)`), and a few entries exist only in the ArchUnit list (for example `java.io.BufferedInputStream.<init>` and `java.io.InputStream.read`):

**Template Locations:**
- **ArchUnit**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/archunit/methods/file-system-access-methods.txt`
- **WALA**: `src/main/resources/de/tum/cit/ase/ares/api/templates/architecture/java/wala/methods/file-system-access-methods.txt`

---

<a id="22-what-are-the-monitored-read-operations"></a>
## 2.2 What Are The Monitored READ Operations?

**Security Component:** Read operation monitor

**Monitored APIs:**

Read APIs listed below access file contents or metadata without modifying them.

> **Note on "Tested by RP" column:** A ✅ means that this API is the **primary target** of a dedicated test in the Reproducibility Package. For example, if a test uses `BufferedInputStream` to wrap a `FileInputStream`, only the wrapper (`BufferedInputStream.<new>`) is marked as ✅, not the underlying `FileInputStream.<new>` which is merely a helper call in that context.

**Reads any formatted file fully**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.FileInputStream | `<new>` | ✅ | ✅ | ✅ |
| java.io.BufferedInputStream | `<new>` | ✅ | ❌ (only in the ArchUnit list) | ✅ |
| java.io.RandomAccessFile | `<new>` | ✅ | ✅ | ✅ |
| java.nio.channels.AsynchronousFileChannel | read | ✅ | ✅ | ❌ (triggers Thread security) |
| java.nio.channels.AsynchronousFileChannel | open | ✅ | ✅ | ❌ (triggers Thread security) |
| java.nio.channels.FileChannel | open | ✅ | ✅ | ✅ |
| java.nio.channels.FileChannel | map | ✅ | ✅ | ✅ |
| java.nio.file.Files | newByteChannel | ✅ | ✅ | ✅ |
| java.nio.file.Files | newInputStream | ✅ | ✅ | ✅ |
| java.nio.file.Files | readAllBytes | ✅ | ✅ | ✅ |
| java.lang.ClassLoader | getResourceAsStream | ✅ | ✅ | ❌ (triggers Reflection security) |

**Reads UTF-8 text/tokens fully**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.Reader | `<new>` | ✅ | ✅ | ✅ |
| java.io.FileReader | `<new>` | ✅ | ✅ | ❌ |
| java.nio.file.Files | newBufferedReader | ✅ | ✅ | ✅ |
| java.nio.file.Files | readString | ✅ | ✅ | ✅ |
| java.nio.file.Files | lines | ✅ | ✅ | ✅ |
| java.nio.file.Files | readAllLines | ✅ | ✅ | ✅ |
| java.util.Scanner | `<new>` | ✅ | ✅ | ✅ |

**Reads only specifically formatted files fully**

> **Note:** `java.io.DataInputStream.<new>` is in NEITHER methods file. Detection of data-stream reads happens via the `java.io.DataInput.read*` interface methods listed below; the WALA false-positives file even excludes `java.io.DataInputStream` explicitly to suppress JDK-internal usage.

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.DataInput | read | ✅ | ✅ | ❌ |
| java.io.DataInput | readBoolean | ✅ | ✅ | ❌ |
| java.io.DataInput | readByte | ✅ | ✅ | ❌ |
| java.io.DataInput | readChar | ✅ | ✅ | ❌ |
| java.io.DataInput | readDouble | ✅ | ✅ | ❌ |
| java.io.DataInput | readFloat | ✅ | ✅ | ❌ |
| java.io.DataInput | readFully | ✅ | ✅ | ❌ |
| java.io.DataInput | readInt | ✅ | ✅ | ❌ |
| java.io.DataInput | readLine | ✅ | ✅ | ❌ |
| java.io.DataInput | readLong | ✅ | ✅ | ❌ |
| java.io.DataInput | readShort | ✅ | ✅ | ❌ |
| java.io.DataInput | readUTF | ✅ | ✅ | ❌ |
| java.io.DataInput | readUnsignedByte | ✅ | ✅ | ❌ |
| java.io.DataInput | readUnsignedShort | ✅ | ✅ | ❌ |
| javax.imageio.ImageIO | createImageInputStream | ✅ | ✅ | ❌ |
| javax.imageio.ImageIO | read | ✅ | ✅ | ❌ |
| javax.sound.sampled.AudioSystem | getAudioInputStream | ✅ | ✅ | ❌ |
| javax.xml.bind.Unmarshaller | unmarshal | ✅ | ✅ | ❌ |
| javax.xml.parsers.DocumentBuilder | parse | ✅ | ✅ | ❌ |
| javax.xml.parsers.SAXParser | parse | ✅ | ✅ | ❌ |
| java.awt.Toolkit | createImage | ✅ | ✅ | ❌ |
| java.awt.Toolkit | getImage | ✅ | ✅ | ❌ |
| javax.imageio.ImageIO | getImageReaders | ✅ | ✅ | ❌ |
| javax.sound.midi.MidiSystem | getSoundbank | ✅ | ✅ | ❌ |
| java.awt.Font | createFont | ✅ | ✅ | ❌ |
| java.awt.Font | createFonts | ✅ | ✅ | ❌ |
| javax.imageio.stream.FileCacheImageInputStream | `<new>` | ✅ | ✅ | ❌ |
| javax.imageio.stream.FileImageInputStream | `<new>` | ✅ | ✅ | ❌ |

**Reads archive files (ZIP/JAR/GZIP)**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.util.zip.ZipInputStream | `<new>` | ✅ | ✅ | ❌ |
| java.util.zip.ZipInputStream | getNextEntry | ✅ | ✅ | ❌ |
| java.util.jar.JarInputStream | `<new>` | ✅ | ✅ | ❌ |
| java.util.jar.JarInputStream | getNextJarEntry | ✅ | ✅ | ❌ |
| java.util.zip.GZIPInputStream | `<new>` | ✅ | ✅ | ❌ |
| java.util.zip.ZipFile | `<new>` | ✅ | ✅ | ❌ |
| java.util.zip.ZipFile | entries | ✅ | ✅ | ❌ |
| java.util.zip.ZipFile | getInputStream | ✅ | ✅ | ❌ |
| java.util.jar.JarFile | `<new>` | ✅ | ✅ | ❌ |
| java.util.jar.JarFile | entries | ✅ | ✅ | ❌ |
| java.util.jar.JarFile | getInputStream | ✅ | ✅ | ❌ |

**Reads configuration/properties files**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.util.Properties | load | ✅ | ✅ | ❌ |
| java.util.Properties | loadFromXML | ✅ | ✅ | ❌ |

**Reads only specific parts of a file**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.RandomAccessFile | read | ✅ | ✅ | ❌ |
| java.io.InputStream | read | ✅ | ❌ (only in the ArchUnit list) | ❌ |
| java.io.Reader | read | ✅ | ✅ | ❌ |
| java.nio.channels.SeekableByteChannel | read | ✅ | ✅ | ❌ |

**Only reads the file hierarchy**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.File | normalizedList | ✅ | ✅ | ❌ |
| java.io.File | list | ✅ | ✅ | ❌ |
| java.io.File | listFiles | ✅ | ✅ | ❌ |
| java.io.File | listRoots | ✅ | ✅ | ❌ |
| java.nio.file.Files | find | ✅ | ✅ | ❌ |
| java.nio.file.Files | list | ✅ | ✅ | ❌ |
| java.nio.file.Files | newDirectoryStream | ✅ | ✅ | ❌ |
| java.nio.file.Files | walk | ✅ | ✅ | ❌ |
| java.nio.file.Files | walkFileTree | ✅ | ✅ | ❌ |
| java.nio.file.spi.FileSystemProvider | newDirectoryStream | ✅ | ✅ | ❌ |

---

<a id="23-what-are-the-monitored-writeoverwrite-operations"></a>
## 2.3 What Are The Monitored WRITE/OVERWRITE Operations?

**Security Component:** Write operation monitor

**Monitored APIs:**

Write APIs listed below modify existing content or attributes.

> **Note on "Tested by RP" column:** A ✅ means that this API is the **primary target** of a dedicated test in the Reproducibility Package. For example, if a test uses `BufferedWriter` to wrap a `FileWriter`, only the wrapper (`BufferedWriter.<new>`) is marked as ✅, not the underlying `FileWriter.<new>` which is merely a helper call in that context.

**Writes any format fully to a file**

> **Note:** Unlike AOP mode, architecture testing cannot inspect `OpenOption` or `MapMode` parameters at analysis time. Methods like `FileChannel.open` and `Files.newByteChannel` are detected regardless of the options used. The operation classification is based on method signatures, not runtime parameters.

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.FileOutputStream | `<new>` | ✅ | ✅ | ✅ |
| java.io.BufferedOutputStream | `<new>` | ✅ | ✅ | ✅ |
| java.io.RandomAccessFile | `<new>` | ✅ | ✅ | ✅ |
| java.nio.channels.AsynchronousFileChannel | write | ✅ | ✅ | ❌ |
| java.nio.channels.AsynchronousFileChannel | open | ✅ | ✅ | ❌ |
| java.nio.channels.FileChannel | open | ✅ | ✅ | ✅ |
| java.nio.channels.FileChannel | map | ✅ | ✅ | ✅ |
| java.nio.channels.FileChannel | write | ✅ | ✅ | ✅ |
| java.nio.file.Files | newByteChannel | ✅ | ✅ | ✅ |
| java.nio.file.Files | newOutputStream | ✅ | ✅ | ✅ |
| java.nio.file.Files | write | ✅ | ✅ | ✅ |

**Writes UTF-8 text/tokens fully**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.Writer | `<new>` | ✅ | ✅ | ✅ |
| java.nio.file.Files | newBufferedWriter | ✅ | ✅ | ✅ |
| java.nio.file.Files | writeString | ✅ | ✅ | ✅ |

**Writes only specifically formatted files fully**

> **Note:** `java.io.DataOutputStream.<new>` is in NEITHER methods file. Detection of data-stream writes happens via the `java.io.DataOutput.write*` interface methods listed below; the WALA false-positives file even excludes `java.io.DataOutputStream` explicitly to suppress JDK-internal usage.

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.DataOutput | writeBoolean | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeByte | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeBytes | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeChar | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeChars | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeDouble | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeFloat | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeInt | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeLong | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeShort | ✅ | ✅ | ❌ |
| java.io.DataOutput | writeUTF | ✅ | ✅ | ❌ |
| javax.imageio.ImageIO | write | ✅ | ✅ | ❌ |
| javax.imageio.ImageIO | createImageOutputStream | ✅ | ✅ | ❌ |
| javax.sound.sampled.AudioSystem | write | ✅ | ✅ | ❌ |
| javax.xml.bind.Marshaller | marshal | ✅ | ✅ | ❌ |
| javax.xml.transform.Transformer | transform | ✅ | ✅ | ❌ |
| java.io.PrintStream | `<new>` | ✅ | ✅ | ❌ |
| java.util.logging.FileHandler | `<new>` | ✅ | ✅ | ❌ |
| java.util.logging.FileHandler | publish | ✅ | ✅ | ❌ |
| java.util.logging.FileHandler | close | ✅ | ✅ | ❌ |
| java.util.zip.InflaterOutputStream | `<new>` | ✅ | ✅ | ❌ |
| javax.print.DocPrintJob | print | ✅ | ✅ | ❌ |

**Writes archive files (ZIP/JAR/GZIP)**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.util.zip.ZipOutputStream | `<new>` | ✅ | ✅ | ❌ |
| java.util.zip.ZipOutputStream | putNextEntry | ✅ | ✅ | ❌ |
| java.util.jar.JarOutputStream | `<new>` | ✅ | ✅ | ❌ |
| java.util.jar.JarOutputStream | putNextEntry | ✅ | ✅ | ❌ |
| java.util.zip.GZIPOutputStream | `<new>` | ✅ | ✅ | ❌ |
| java.util.zip.ZipOutputStream | closeEntry | ✅ | ✅ | ❌ |
| java.util.jar.JarOutputStream | closeEntry | ✅ | ✅ | ❌ |

**Writes configuration/properties files**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.util.Properties | store | ✅ | ✅ | ❌ |
| java.util.Properties | storeToXML | ✅ | ✅ | ❌ |
| java.util.Formatter | `<new>` | ✅ | ✅ | ❌ |

**Writes only specific parts to a file**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.nio.channels.AsynchronousFileChannel | truncate | ✅ | ✅ | ❌ |
| java.nio.channels.FileChannel | truncate | ✅ | ✅ | ❌ |
| java.nio.channels.FileChannel | transferTo | ✅ | ✅ | ❌ |
| java.nio.file.attribute.UserDefinedFileAttributeView | write | ✅ | ✅ | ❌ |

**Only writes the file hierarchy (metadata/attributes)**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.File | setExecutable | ✅ | ✅ | ❌ |
| java.io.File | setLastModified | ✅ | ✅ | ❌ |
| java.io.File | setReadOnly | ✅ | ✅ | ❌ |
| java.io.File | setReadable | ✅ | ✅ | ❌ |
| java.io.File | setWritable | ✅ | ✅ | ❌ |
| java.io.File | renameTo | ✅ | ✅ | ❌ |
| java.nio.file.Files | copy | ✅ | ✅ | ❌ |
| java.nio.file.Files | move | ✅ | ✅ | ❌ |
| java.nio.file.Files | setAttribute | ✅ | ✅ | ❌ |
| java.nio.file.Files | setLastModifiedTime | ✅ | ✅ | ❌ |
| java.nio.file.Files | setOwner | ✅ | ✅ | ❌ |
| java.nio.file.Files | setPosixFilePermissions | ✅ | ✅ | ❌ |
| java.nio.file.spi.FileSystemProvider | copy | ✅ | ✅ | ❌ |
| java.nio.file.spi.FileSystemProvider | move | ✅ | ✅ | ❌ |
| java.nio.file.spi.FileSystemProvider | setAttribute | ✅ | ✅ | ❌ |

---

<a id="24-what-are-the-monitored-create-operations"></a>
## 2.4 What Are The Monitored CREATE Operations?

**Security Component:** Create operation monitor

**Monitored APIs:**

Link creation APIs and conditional creates are listed under Creates files.

**Creates files**

> **Note:** Unlike AOP mode, architecture testing cannot inspect `OpenOption` parameters to determine if CREATE or CREATE_NEW is used. Methods like `FileChannel.open` are detected as potentially creating files regardless of the actual options passed.

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.File | createNewFile | ✅ | ✅ | ✅ |
| java.io.File | createTempFile | ✅ | ✅ | ✅ |
| java.nio.file.Files | createFile | ✅ | ✅ | ✅ |
| java.nio.file.Files | createTempFile | ✅ | ✅ | ✅ |
| java.nio.file.Files | createLink | ✅ | ✅ | ✅ |
| java.nio.file.Files | createSymbolicLink | ✅ | ✅ | ✅ |
| java.io.BufferedOutputStream | `<new>` | ✅ | ✅ | ✅ |
| java.io.BufferedWriter | `<new>` | ✅ | ✅ | ✅ |
| java.io.FileOutputStream | `<new>` | ✅ | ✅ | ✅ |
| java.io.FileWriter | `<new>` | ✅ | ✅ | ✅ |
| java.io.PrintWriter | `<new>` | ✅ | ✅ | ✅ |
| java.io.RandomAccessFile | `<new>` | ✅ | ✅ | ✅ |
| java.nio.file.Files | newBufferedWriter | ✅ | ✅ | ✅ |
| java.nio.file.Files | newOutputStream | ✅ | ✅ | ✅ |
| java.nio.channels.AsynchronousFileChannel | open | ✅ | ✅ | ❌ |
| java.nio.channels.FileChannel | open | ✅ | ✅ | ✅ |

**Creates folders**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.File | mkdir | ✅ | ✅ | ✅ |
| java.io.File | mkdirs | ✅ | ✅ | ✅ |
| java.nio.file.Files | createDirectories | ✅ | ✅ | ✅ |
| java.nio.file.Files | createDirectory | ✅ | ✅ | ✅ |
| java.nio.file.Files | createTempDirectory | ✅ | ✅ | ✅ |
| java.nio.file.spi.FileSystemProvider | createDirectory | ✅ | ✅ | ✅ |

---

<a id="25-what-are-the-monitored-delete-operations"></a>
## 2.5 What Are The Monitored DELETE Operations?

**Security Component:** Delete operation monitor

**Monitored APIs:**

Delete APIs listed below can remove files and empty directories.

**Delete files**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.File | delete | ✅ | ✅ | ✅ |
| java.nio.file.Files | delete | ✅ | ✅ | ✅ |
| java.nio.file.Files | deleteIfExists | ✅ | ✅ | ✅ |
| java.nio.file.spi.FileSystemProvider | delete | ✅ | ✅ | ❌ |
| org.apache.commons.io.FileUtils | forceDelete | ✅ | ✅ | ❌ |
| java.awt.Desktop | moveToTrash | ✅ | ✅ | ❌ |
| java.io.File | deleteOnExit | ✅ | ✅ | ✅ |

**Delete folders**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.File | delete | ✅ | ✅ | ✅ |
| java.nio.file.Files | delete | ✅ | ✅ | ✅ |
| java.nio.file.Files | deleteIfExists | ✅ | ✅ | ✅ |
| java.nio.file.spi.FileSystemProvider | delete | ✅ | ✅ | ❌ |
| org.apache.commons.io.FileUtils | forceDelete | ✅ | ✅ | ❌ |
| java.awt.Desktop | moveToTrash | ✅ | ✅ | ❌ |
| java.io.File | deleteOnExit | ✅ | ✅ | ✅ |

**Also monitored in delete analysis (can delete source file)**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.nio.file.Files | copy | ✅ | ✅ | ❌ |
| java.nio.file.Files | move | ✅ | ✅ | ❌ |

---

<a id="26-what-are-the-monitored-execute-operations"></a>
## 2.6 What Are The Monitored EXECUTE Operations?

**What does "Execute" mean?** File-system entries that trigger execution-like behaviour such as loading a native library or asking the desktop environment to open/browse/print a file or URI. Command-spawning APIs such as `Runtime.exec(...)`, `ProcessBuilder.start()`, and `ProcessBuilder.startPipeline()` are part of the Command System architecture rule, not this file-system rule.

**Security Component:** Execute operation monitor

**Monitored APIs:**

Execute APIs listed below trigger execution-like behaviour on files.

**Executes the file on the console (command line execution)**

> **Note:** `ProcessBuilder.start()`, `ProcessBuilder.startPipeline()`, and `Runtime.exec()` are handled by the **Command System** rather than the File System in architecture testing, as they execute commands rather than individual files. The File System detection for execute only covers library loading via `java.lang.System.load`/`loadLibrary`. The `java.lang.Runtime.load`/`loadLibrary` counterparts are NOT in the file-system methods lists; they are covered by the separate **NATIVE_CODE** rule (`native-code-access-methods.txt`) and the **COMMAND_EXECUTION** rule (`command-execution-methods.txt`).

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.lang.System | load | ✅ | ✅ | ❌ |
| java.lang.System | loadLibrary | ✅ | ✅ | ❌ |

**Opens files with default applications (Desktop integration)**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.awt.Desktop | open | ✅ | ✅ | ❌ |
| java.awt.Desktop | edit | ✅ | ✅ | ❌ |
| java.awt.Desktop | print | ✅ | ✅ | ❌ |
| java.awt.Desktop | browse | ✅ | ✅ | ❌ |
| java.awt.Desktop | browseFileDirectory | ✅ | ✅ | ❌ |
| java.awt.Desktop | mail | ✅ | ✅ | ❌ |
| java.awt.Desktop | openHelpViewer | ✅ | ✅ | ❌ |
| java.awt.Desktop | setDefaultMenuBar | ✅ | ✅ | ❌ |
| java.awt.Desktop | setOpenFileHandler | ✅ | ✅ | ❌ |
| java.awt.Desktop | setOpenURIHandler | ✅ | ✅ | ❌ |

---

<a id="3-ares-2-architecture-file-system-access-control-student-code-triggers-security-check"></a>
# 3. Ares 2 Architecture File System Access Control: Student Code Triggers Security Check

When student code (any code within the configured restricted package) attempts to use one of these file system methods, the architecture analysis will detect it during the test phase.

**Example:**
```java
// Student Code
package de.student.solution;

import java.nio.file.Files;
import java.nio.file.Path;

public class StudentSolution {
    public String readSecretFile() {
        // This will be detected by architecture analysis
        return Files.readString(Path.of("/etc/passwd"));
    }
}
```

**What happens during analysis:**

**ArchUnit Mode:**
1. `ClassFileImporter` loads `StudentSolution.class`
2. Analyses that `readSecretFile()` method calls `Files.readString()`
3. Checks if `Files.readString()` is in the forbidden methods list
4. Reports violation: "StudentSolution.readSecretFile transitively accesses Files.readString"

**WALA Mode:**
1. Builds call graph from student code entry points
2. Performs reachability analysis: Can `Files.readString()` be reached from student code?
3. Finds path: `StudentSolution.readSecretFile → Files.readString`
4. Reports violation with source line number

**Key Difference from AOP:**
- This happens during **test time**, not when the code actually runs
- Detects **all potential file accesses** in the code structure, even unreachable code
- No runtime overhead, but cannot check specific file paths

---

<a id="4-ares-2-architecture-file-system-access-control-ares-collects-information-about-the-file-access"></a>
# 4. Ares 2 Architecture File System Access Control: Ares Collects Information About the File Access

During architecture analysis, Ares collects information about the code structure to detect file system access patterns.

---

<a id="41-loading-java-classes-archunit"></a>
## 4.1 Loading Java Classes (ArchUnit)

**Framework:** TNGs ArchUnit (https://www.archunit.org/)

**Step 1: Import Compiled Classes**

At runtime, `ArchitectureMode.getJavaClasses(classPath)` imports the analysed classpath directly:

```java
// ArchitectureMode.getJavaClasses (runtime importer)
JavaClasses javaClasses = new ClassFileImporter()
    .withImportOption(
        location -> !location.toString().replace("\\", "/").contains("/de/tum/cit/ase/ares/api/"))
    .importPath(classPath);
```

A separate variant exists for the generated-template path (`JavaArchunitTestCase.javaClassesAsCode()`), which emits `ClassFileImporter` code using `ImportOption.Predefined.DO_NOT_INCLUDE_TESTS` and `importPackages(...)`. The runtime importer above uses neither; it imports the given path and only excludes Ares' own framework classes.

**What happens:**
1. ClassFileImporter scans the given class path for `.class` files
2. Loads class metadata (methods, fields, dependencies)
3. Excludes Ares internal classes (`/de/tum/cit/ase/ares/api/`)
4. Creates `JavaClasses` object containing all analysed classes

**Analysis Process:**
1. **Import Classes**: Load `.class` files using `ClassFileImporter`
2. **Analyse Dependencies**: Build class dependency graph
3. **Check Access Patterns**: Detect direct and transitive method calls
4. **Report Violations**: Throw `AssertionError` with call chain details

**Strengths:**
- ✅ Fast analysis (no call graph construction)
- ✅ Detects transitive dependencies (A→B→C where C is forbidden)
- ✅ Simple configuration
- ✅ Good error messages with class names

**Limitations:**
- ⚠️ Less precise than WALA for complex call patterns
- ⚠️ May have false positives for conditional code paths
- ⚠️ Cannot determine if code path is actually reachable at runtime

---

<a id="42-building-the-call-graph-wala"></a>
## 4.2 Building the Call Graph (WALA)

**Framework:** IBM WALA (T.J. Watson Libraries for Analysis)

**Step 1: Create Analysis Scope**

`CustomCallgraphBuilder` first filters the analysed classpath: every entry whose normalised path contains one of the `CLASSPATH_EXCLUDE_SUBSTRINGS` (test build outputs, JUnit/Mockito/AssertJ, JaCoCo, Gradle test infrastructure, static-analysis tooling, Ares itself, the WALA and AspectJ runtimes) is dropped (`filterClassPath`, `CustomCallgraphBuilder.java`). The remaining entries are used as-is; the runtime `java.class.path` is NOT appended.

```java
// CustomCallgraphBuilder.CachedAnalysis.build (simplified)
AnalysisScope scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
    filteredClassPath,
    FileTools.readFile(FileTools.resolveFileOnSourceDirectory(
        "templates", "architecture", "java", "exclusions.txt")));

// Build class hierarchy
ClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);
```

**Exclusions File (real excerpt):**
```
# The following are excluded from the classpath to make the analysis faster, as they are not relevant for the analysis.*
jdk/.*
sun/security/.*
sun/reflect/.*
sun/util/.*
java/util/Arrays.*
java/util/Collections.*
org/junit.*
org/mockito.*
com/tngtech.*
com/ibm/wala.*
```

The full file (166 lines) also excludes many further `java/util/*` classes, `java/time/*`, `java/text/*`, `java/math/*` and additional test-library patterns.

**Step 2: Define Entry Points**

`ReachabilityChecker.getEntryPointsFromStudentSubmission(classPath, classHierarchy)` adds ALL declared methods of every class loaded by the Application class loader as entry points (there is no exclusion of `main` or any other method):

```java
// ReachabilityChecker.getEntryPointsFromStudentSubmission (simplified)
List<DefaultEntrypoint> entryPoints = StreamSupport
    .stream(createClassHierarchy(classPath).spliterator(), false)
    .filter(iClass -> iClass.getClassLoader().getReference().equals(ClassLoaderReference.Application))
    .flatMap(iClass -> iClass.getDeclaredMethods().stream())
    .map(IMethod::getReference)
    .map(methodReference -> new DefaultEntrypoint(methodReference, applicationClassHierarchy))
    .collect(Collectors.toCollection(ArrayList::new));
```

**Entry Point Narrowing:**

Instead of excluding individual methods, `CustomCallgraphBuilder.buildCallGraph` narrows the entry-point set by package: `derivePackagePrefix(classPathToAnalyze)` converts a build-output package directory (e.g. `target/classes/de/student`) into a WALA type-name prefix (e.g. `Lde/student/`), and only entry points whose declaring class matches that prefix are kept. This keeps each architecture rule focused on the code under test instead of every class sharing the wider analysis scope.

**Entry Point Representation:**
```java
DefaultEntrypoint(
    methodReference = "Lde/student/StudentCode.readFile()V",
    classHierarchy = <ClassHierarchy>
)
```

**Step 3: Build Call Graph**

```java
// Configure analysis options (scope + entry points passed to the constructor)
AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);

// Create call graph builder (0-1-CFA algorithm)
CallGraphBuilder<?> builder = Util.makeZeroOneCFABuilder(
    Language.JAVA,
    options,
    new AnalysisCacheImpl(),
    classHierarchy
);

// Treat JDK methods as opaque: the call edge from student code into the JDK is
// still recorded (so forbidden-call checks keep working), but WALA does not
// descend into JDK bytecode.
options.setSelector(new JdkOpaqueMethodTargetSelector(options.getMethodTargetSelector(), classHierarchy));

// Build call graph
CallGraph callGraph = builder.makeCallGraph(options, null);
```

**Call Graph Structure:**
```
CallGraph:
  ├─ CGNode: StudentCode.main()
  │   ├─ successor: StudentCode.readFile()
  │   └─ successor: System.out.println()
  ├─ CGNode: StudentCode.readFile()
  │   ├─ successor: Files.readString()
  │   └─ successor: Path.of()
  └─ CGNode: Files.readString()
      └─ (implementation details...)
```

**Analysis Process:**
1. **Create Analysis Scope**: Define which classes to include/exclude
2. **Build Class Hierarchy**: Resolve all type relationships
3. **Find Entry Points**: Identify starting methods in student code
4. **Construct Call Graph**: Map all possible method call relationships
5. **Reachability Analysis**: Forward BFS from the entry points, then a per-sink reverse walk (`WalaRule.check`)
6. **Filter False Positives**: Classify each path with `WalaPathClassification` and suppress transitive-JDK false positives

**Strengths:**
- ✅ Very precise call path detection
- ✅ Understands complex call patterns (lambdas, method references, reflection)
- ✅ Can filter out JDK internal calls (false positive reduction)
- ✅ Provides exact call paths with line numbers
- ✅ Models runtime behaviour more accurately

**Limitations:**
- ⚠️ Slower analysis (call graph construction is expensive)
- ⚠️ Requires more memory
- ⚠️ More complex configuration (exclusions, entry points)
- ⚠️ May still have false positives for very dynamic code

---

<a id="5-ares-2-architecture-file-system-access-control-ares-validates-the-file-access"></a>
# 5. Ares 2 Architecture File System Access Control: Ares Validates the File Access

---

<a id="51-archunit-mode-static-analysis"></a>
## 5.1 ArchUnit Mode: Static Analysis

**How it works:**
```java
// Define rule (the rule name is the localised message for the key
// security.architecture.file.system.access, i.e. "Accesses file system")
ArchRule rule = ArchRuleDefinition.noClasses()
    .that(isNotAllowedClass(allowedClasses))  // exempt allow-listed classes
    .should(new TransitivelyAccessesMethodsCondition(
        javaAccess -> forbiddenMethods.stream()
            .anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method))
    ))
    .as(ruleName);

// Execute rule
rule.check(javaClasses);  // Throws AssertionError if violated
```

**Step 2: Create Architecture Rule**

```java
// JavaArchunitTestCaseCollection.createNoClassShouldHaveMethodRule (simplified)
return ArchRuleDefinition.noClasses().that(isNotAllowedClass(allowedClasses))
    .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>(ruleName) {
        private Set<String> forbiddenMethods;

        @Override
        public boolean test(JavaAccess<?> javaAccess) {
            if (forbiddenMethods == null) {
                // Lazily load the methods file and convert Java-style array notation
                // (e.g. "byte[]") into ArchUnit's JNI-style notation (e.g. "[B")
                forbiddenMethods = FileTools.readMethodsFile(FileTools.readFile(methodsFilePath)).stream()
                    .map(JavaArchunitTestCaseCollection::convertArrayNotation)
                    .collect(Collectors.toSet());
            }
            return forbiddenMethods.stream().filter(method -> !method.isEmpty())
                .anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
        }
    })).as(ruleName);
```

**Rule Components:**
- **DescribedPredicate**: Tests if a method access is forbidden; matching uses `startsWith` on the target's full name after `convertArrayNotation`
- **isNotAllowedClass**: `.that(...)` predicate excluding allow-listed classes (`ClassPermission`) from the rule
- **TransitivelyAccessesMethodsCondition**: Finds transitive access paths
- **ArchRule**: Complete rule that can be checked against JavaClasses; named via `.as(ruleName)` with the localised "Accesses file system"

**Step 3: Run Architecture Test**

```java
try {
    rule.check(javaClasses);  // Throws AssertionError if violated
} catch (AssertionError e) {
    // Parse error message and throw SecurityException
    JavaArchitectureTestCase.parseErrorMessage(e);
}
```

**When violation is found:**
```
Architecture Violation [Priority: MEDIUM] - Rule 'Accesses file system' was violated (1 times):
Method <de.student.StudentCode.readFile()> transitively accesses <java.nio.file.Files.readString(java.nio.file.Path)> by [de.student.StudentCode.readFile()->de.student.Helper.loadFile()] in (StudentCode.java:12)
```

**Error is converted to** (via `JavaArchitectureTestCase.parseErrorMessage`, using the `security.archunit.violation.error` message format `%s tried to illegally %s via %s (called by %s)`):
```java
throw new SecurityException(
    "Ares Security Error (Reason: Student-Code; Stage: Execution): " +
    "de.student.StudentCode.readFile() tried to illegally access the file system " +
    "via java.nio.file.Files.readString(java.nio.file.Path) " +
    "(called by de.student.StudentCode) but was blocked by Ares."
);
```

**Example Violation Detection:**
```java
// Student Code
class StudentCode {
    void readFile() {
        Helper.processFile();  // Indirect call
    }
}

class Helper {
    void processFile() {
        Files.readString(Path.of("file.txt"));  // Forbidden!
    }
}

// ArchUnit detects: StudentCode → Helper.processFile → Files.readString
// Reports: "... transitively accesses <java.nio.file.Files.readString(...)> by [StudentCode.readFile()->Helper.processFile()]"
```

**Violation Example:**
```
Architecture Violation [Priority: MEDIUM] - Rule 'Accesses file system' was violated (1 times):
Method <de.student.StudentCode.exploit()> transitively accesses <java.io.FileInputStream.<init>(java.lang.String)> by [de.student.StudentCode.exploit()->de.student.Helper.readFile()] in (StudentCode.java:42)
```

**Best For:**
- Quick validation during development
- Broad detection of file system usage patterns
- When exact call paths are not critical

---

<a id="52-wala-mode-call-graph-analysis"></a>
## 5.2 WALA Mode: Call Graph Analysis

**How it works:**

Production validation runs through `WalaRule.check(CallGraph, Set<ClassPermission>)` (`WalaRule.java`). The older forward-DFS API (`ReachabilityChecker.findReachableMethods` with `CustomDFSPathFinder`) is legacy: its global visited-set meant each forbidden sink was reported on exactly one path, so an allow-listed or false-positive caller discovered first could mask a genuine violation by a different caller of the same sink (see the `WalaRule.check` Javadoc).

```java
// Build call graph
CallGraph cg = new CustomCallgraphBuilder(classPath).buildCallGraph(classPath);

// WalaRule.check (simplified):
// 1. Collect every forbidden sink in the call graph (signature match against
//    the methods file, sorted for deterministic reporting).
// 2. Forward BFS from the entry points -> set of entry-reachable nodes
//    (fails closed with a SecurityException if the entry set is empty).
// 3. For each entry-reachable sink: reverse-walk through infrastructure
//    frames only, and evaluate each distinct nearest-student approach.
// 4. Each approach is classified with WalaPathClassification; a genuine,
//    non-exempt violation throws an AssertionError immediately.
new WalaRule(ruleName, forbiddenMethods).check(cg, allowedClasses);
```

**Example Violation Detection:**
```java
// Student Code
class StudentCode {
    void processData() {
        executor.submit(() -> readConfig());  // Lambda expression
    }
    
    void readConfig() {
        Properties props = new Properties();
        props.load(new FileInputStream("config.properties"));  // Forbidden!
    }
}

// WALA detects: 
// StudentCode.processData → Lambda$1.run → StudentCode.readConfig → FileInputStream.<init>
// Reports the nearest student caller, the forbidden sink, its source line and the entry point
```

**Violation Example** (message format from `messages.properties`, key `security.architecture.method.call.message`: `'%s'\n Method <%s> calls method <%s> in (%s.java:%d) accesses <%s>`; signatures are converted from WALA's JVM-descriptor form to source form, and the line number comes from `getSourcePosition`):
```
'Accesses file system'
 Method <de.student.StudentCode.readConfig()> calls method <java.io.FileInputStream.<init>(java.lang.String)> in (FileInputStream.java:42) accesses <de.student.StudentCode.processData()>
```

**Best For:**
- Precise violation detection
- Understanding exact call paths
- Production-grade security validation
- Reducing false positives

---

<a id="53-transitive-access-detection"></a>
## 5.3 Transitive Access Detection

**How TransitivelyAccessesMethodsCondition Works:**

```java
public class TransitivelyAccessesMethodsCondition extends ArchCondition<JavaClass> {
    
    @Override
    public void check(JavaClass clazz, ConditionEvents events) {
        // Get all method accesses from this class
        for (JavaAccess<?> accessInsideClass : clazz.getAccessesFromSelf()) {
            // Find transitive path to forbidden method
            List<JavaAccess<?>> path = transitiveAccessPath.findPathFromViolatingMethodTo(accessInsideClass);
            
            if (!path.isEmpty()) {
                // Report violation with complete call chain
                // (a SimpleConditionEvent.satisfied event carrying the path description)
                events.add(newTransitiveAccessPathFoundEvent(accessInsideClass, path));
            }
        }
    }
    
    // Inner class TransitiveAccessPath:
    List<JavaAccess<?>> findPathFromViolatingMethodTo(JavaAccess<?> access) {
        // Recursive DFS (recursivelyFindPathFromViolatingMethodTo) from the
        // current access to a forbidden method, tracking visited methods.
        // Returns: [access1 -> access2 -> ... -> forbiddenAccess]
    }
}
```

**Example Path Detection:**

```
StudentCode.main() calls:
  ├─ Helper.processData() ✓ (not forbidden, continue searching)
  │   └─ Files.readString() ✗ (FORBIDDEN! Report path)
  └─ System.out.println() ✓ (not forbidden, ignore)

Result: Path found = [StudentCode.main → Helper.processData → Files.readString]
```

---

<a id="54-reachability-analysis-wala"></a>
## 5.4 Reachability Analysis (WALA)

**Step 4: Find Paths to Forbidden Methods**

Production reachability analysis lives in `WalaRule.check`:

```java
// Load forbidden methods
Set<String> forbiddenMethods = FileTools.readMethodsFile(
    FileTools.readFile(FileHandlerConstants.WALA_FILESYSTEM_METHODS)
);

// WalaRule.check(cg, allowedClasses), simplified:
// 1. Sinks: every CGNode whose signature matches a forbidden method
//    (startsWith / equals after return-type stripping and descriptor formatting)
List<CGNode> sinks = /* collect + sort by signature for determinism */;

// 2. Forward BFS over successor edges from cg.getEntrypointNodes()
Set<CGNode> entryReachable = forwardReachableFromEntrypoints(cg);
// Fails closed: sinks present but no entry points -> SecurityException

// 3. Per sink (if entry-reachable): reverse walk over predecessor edges
//    through infrastructure frames only; every distinct nearest-student
//    approach [studentFrame, ...infra..., sink] is evaluated immediately
//    (bounded by a 64-approaches-per-sink backstop that is logged, never
//    a silent pass).
evaluateSink(cg, sink, allowedClasses, entryReachable);

// 4. evaluatePath classifies the reconstructed entry->...->sink path with
//    WalaPathClassification (nearestStudentFrame, isFalsePositiveTransitivePath),
//    applies the allowedClasses exemption, and returns the AssertionError to
//    throw for a genuine violation (source line via getSourcePosition).
```

**Legacy DFS Path Finding (`ReachabilityChecker` / `CustomDFSPathFinder`):**

The older API `ReachabilityChecker.findReachableMethods(callGraph, startNodes, targetNodeFilter)` delegates to `CustomDFSPathFinder`, which is an ITERATIVE depth-first search, not a recursive one: it keeps the current path on a `Deque<CGNode>` stack, tracks each node's unvisited successors in a `pendingChildren` map, iterates successors in signature-sorted order (`sortedSuccessors`, for run-to-run determinism), and skips children whose signature starts with an entry of the false-positives methods file (`FileHandlerConstants.FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS`, loaded lazily). `find()` returns the first root-to-target path or `null`.

This component is legacy for rule checking: because `pendingChildren` marks nodes globally, each sink is reported on at most one path, which is exactly the masking problem `WalaRule.check` was written to eliminate.

**Example Reachability Detection:**

```
Entry: StudentCode.processData()
  ↓ calls
Helper.loadConfig()
  ↓ calls
FileInputStream.<init>(String)  ← FORBIDDEN METHOD REACHED!

Path: [StudentCode.processData, Helper.loadConfig, FileInputStream.<init>]
```

---

<a id="55-false-positive-filtering-wala"></a>
## 5.5 False Positive Filtering (WALA)

**Challenge:** Permitted JDK and framework APIs can internally reach forbidden methods (for example, wrapping a stream in a `BufferedReader` routes through JDK internals). Reporting such paths would blame students for JDK implementation details.

**Solution:** Classify each call-graph path with `WalaPathClassification` (`WalaPathClassification.java`).

Two prefix lists drive the classification:

```java
// Package prefixes that belong to infrastructure and must not be attributed
// as student code (mirrors the exclusions in AspectJ's studentScope() pointcut)
public static final List<String> INFRA_PREFIXES = List.of("java.", "javax.", "sun.", "jdk.", "com.sun.",
    "de.tum.cit.ase.ares.api.", "net.bytebuddy.", "org.aspectj.", "com.ibm.wala.", "com.tngtech.archunit.",
    "anonymous.toolclasses.", "metatest.");

// Subset that genuinely indicates a transitive false positive; the project
// test-helper packages (anonymous.toolclasses., metatest.) are deliberately
// omitted so violations routed through such helpers are still reported
static final List<String> TRANSITIVE_FALSE_POSITIVE_PREFIXES = List.of("java.", "javax.", "sun.", "jdk.",
    "com.sun.", "de.tum.cit.ase.ares.", "net.bytebuddy.", "org.aspectj.", "com.ibm.wala.",
    "com.tngtech.archunit.");
```

`WalaRule.evaluatePath` uses two classification steps:

1. **`nearestStudentFrame(path)`**: scans from the sink back towards the entry point and returns the index of the first frame that is NOT infrastructure (not in `INFRA_PREFIXES` and not loaded by a JDK/primordial class loader). If the whole path is infrastructure, the path is dropped. The violation is attributed to this nearest student frame, not to an intermediate JDK method.
2. **`isFalsePositiveTransitivePath(path, studentIdx)`**: returns true when every frame strictly between the student frame and the forbidden sink is a transitive-false-positive frame. Such paths (student called a permitted JDK/framework API whose internals reach the forbidden method) are suppressed. Direct violations, where the student frame is immediately adjacent to the sink, are never suppressed.

In addition, the (legacy) `CustomDFSPathFinder` consumes an excluded-methods file (`wala/false-positives/false-positives-file.txt`, e.g. `java.io.BufferedReader`, `java.io.DataInputStream`, `java.io.DataOutputStream`, `sun.nio.cs.StreamEncoder`) and skips those methods during traversal.

**Example Filtering:**

**Path 1 (Suppressed as transitive-JDK false positive):**
```
StudentCode.readData()          (nearest student frame)
  ↓
java.io.BufferedReader.<init>() (infrastructure, prefix "java.")
  ↓
sun.nio.cs.StreamDecoder...     (infrastructure, prefix "sun.")
  ↓
FileInputStream.<init>()        (forbidden sink)

Result: SUPPRESSED (every intermediate frame is transitive infrastructure)
```

**Path 2 (Detected):**
```
StudentCode.readSecret()  (nearest student frame, adjacent to the sink)
  ↓
FileInputStream.<init>()  (forbidden sink)

Result: VIOLATION (direct call, never suppressed)
```

**Special WALA Features:**

**Infrastructure-Aware Attribution:**
Because `nearestStudentFrame` scans from the sink backwards, the reported caller is always the student method closest to the forbidden call, even when the actual invocation happens several JDK frames deeper. Paths routed through project test helpers (`anonymous.toolclasses.`, `metatest.`) are NOT treated as false positives: those helpers exist to exercise forbidden APIs, so suppressing them would silently disable detection.

<a id="6-ares-2-architecture-file-system-access-control-operation-type-classification"></a>
# 6. Ares 2 Architecture File System Access Control: Operation Type Classification

This section explains how the documentation groups file-system methods differently from AOP-based runtime analysis. Understanding this distinction is essential for correctly interpreting analysis results.

**Key Difference from AOP:**

Unlike AOP, which can analyse runtime parameters (like `StandardOpenOption` values), architecture testing does **not** derive an action at runtime. Each architecture backend loads one flat file-system methods list and checks one binary rule: the supervised code must not access any method in that list unless the relevant class is exempt. The READ/WRITE/CREATE/DELETE/EXECUTE labels in this document are therefore explanatory groupings only. This means:

- **No parameter inspection**: Cannot distinguish `Files.newByteChannel(path, READ)` from `Files.newByteChannel(path, WRITE)` at analysis time
- **No action-specific permissions**: Path permissions such as "read this folder" are not consulted by architecture tests; the architecture rule is allow/deny at the method-access level
- **Conservative documentation grouping**: Methods that could perform multiple operations may appear in more than one explanatory group, but the implementation still reads one flat forbidden-method list

**Documentation Grouping Examples:**

| Method Pattern | Classified As | Reason |
|----------------|---------------|--------|
| `Files.readString`, `Files.readAllBytes` | READ | Primary purpose is reading |
| `Files.writeString`, `Files.write` | WRITE | Primary purpose is writing |
| `Files.createFile`, `Files.createDirectory` | CREATE | Primary purpose is creation |
| `Files.delete`, `Files.deleteIfExists` | DELETE | Primary purpose is deletion |
| `Desktop.open`, `Desktop.edit` | EXECUTE | Launches external programs |
| `Files.newByteChannel`, `FileChannel.open` | Broad file access | Listed in the flat file-system methods file; AOP later derives the exact runtime action from open options |

**Consequence:** Architecture testing may flag more violations than AOP because it cannot distinguish read-only from write access when the same method is used for both.

---

<a id="7-ares-2-architecture-file-system-access-control-conclusion"></a>
# 7. Ares 2 Architecture File System Access Control: Conclusion

<a id="71-technical-details"></a>
## 7.1 Technical Details

The file system security mechanism provides **comprehensive protection** through:

1. **Extensive API Coverage**: Detection of all file system method calls in bytecode
2. **Transitive Analysis**: Finds violations through helper methods and indirect calls
3. **Call Graph Analysis**: WALA mode provides precise reachability information
4. **False Positive Filtering**: WALA mode filters out JDK-internal helper paths
5. **Clear Error Messages**: Detailed violation reports with complete call chains

The system operates **at compile/test time**, requiring no runtime overhead, and detects violations **before** dangerous code can execute.

> 💡 **ArchUnit vs. WALA:** For most use cases the detection is similar, but precision differs. ArchUnit is faster but may have more false positives; WALA is slower but filters JDK-internal paths.

**Implementation Differences:**

| Aspect | ArchUnit | WALA |
|--------|----------|------|
| **Analysis Type** | Static dependency analysis | Call graph construction |
| **Configuration** | `architectureMode = "ARCHUNIT"` | `architectureMode = "WALA"` |
| **Speed** | Fast | Slower |
| **Precision** | Good | Very precise |
| **False Positive Handling** | None | JDK helper filtering |
| **Memory Usage** | Low | Higher |
| **Use Case** | Quick validation | Production-grade analysis |

**Both implementations provide the same validation goal; the detected APIs and precision differ by mode.**

---

**The architecture testing approach provides comprehensive security validation at compile/test time, complementing the runtime AOP approach for defense-in-depth security.**
