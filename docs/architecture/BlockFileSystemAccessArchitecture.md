<a id="file-system-security-mechanism"></a>
# Ares 2 Architecture File System Access Control: File System Security Mechanism (Architecture Analysis)

<a id="table-of-contents"></a>
## Table of Contents

1. [Ares 2 Architecture File System Access Control: High-Level Overview](#1-ares-2-architecture-file-system-access-control-high-level-overview)
   - [1.1 How Does The UML Activity Diagram look like?](#11-how-does-the-uml-activity-diagram-look-like)
   - [1.2 What Is Architecture Testing?](#12-what-is-architecture-testing)
   - [1.3 Which Architecture Modes / Implementations Are There?](#13-which-architecture-modes--implementations-are-there)
   - [1.4 What Are The Internal Configuration Settings?](#14-what-are-the-internal-configuration-settings)
   - [1.5 When Is File Access Generally Blocked?](#15-when-is-file-access-generally-blocked)
2. [Ares 2 Architecture File System Access Control: Ares Monitors FileSystem Methods](#2-ares-2-architecture-file-system-access-control-ares-monitors-filesystem-methods)
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
- Whether the accessed classes are in allowed packages

---

## Summary for Programming Instructors (TL;DR)

**What does Architecture Testing do?**
- ✅ Analyzes **compiled bytecode** to detect forbidden file system operations
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
| **Detection** | Analyzes code structure | Intercepts method calls |
| **Granularity** | Binary (allowed/forbidden) | Path-based permissions |
| **Performance Impact** | Analysis overhead only | Runtime overhead on every call |
| **False Positives** | Possible (unreachable code) | None (only executed code checked) |
| **Coverage** | All code paths | Only executed paths |
| **Configuration** | Package-level permissions | Path-level permissions |
| **Use Case** | Pre-submission validation | Runtime security enforcement |
| **Error Timing** | Test phase | Production execution |

---

<a id="11-how-does-the-uml-activity-diagram-look-like"></a>
## 1.1 How Does The UML Activity Diagram look like?

Below is a general overview of the process for deciding whether to allow or block file access as an UML activity diagram. Throughout this document, you will find the following symbols:
- **🔴 Red** = File access blocked (security policy violation detected)
- **🌕 Yellow** = Intermediate condition met → continue to the next verification step
- **🟢 Green** = File access permitted (no security policy violation detected)

![File System Security Validation Flow](BlockFileSystemAccessArchitecture.drawio.png)

---

<a id="12-what-is-architecture-testing"></a>
## 1.2 What Is Architecture Testing?

Architecture Testing is a technique that validates code follows specific structural rules by analyzing compiled bytecode **before** execution. Think of it like a building inspector reviewing building plans before construction to ensure doors don't open into forbidden areas - the code doesn't run, but the structure gets checked automatically.

**Concrete Example:**

**Without Architecture Testing:** You would have to manually review student code for file system calls.
```java
public void readFile(String path) {
    Files.readString(Path.of(path));  // Would this be caught? Manual review needed!
}
```

**With Architecture Testing:** Ares automatically analyzes bytecode and detects ALL file system method calls, no execution required.
```java
public void readFile(String path) {
    Files.readString(Path.of(path));  // Detected at compile/test time! ArchUnit/WALA finds this.
}
```

**Key Difference from AOP:**
- **AOP (Runtime)**: Monitors method calls during program execution and blocks forbidden operations in real-time
- **Architecture (Static)**: Analyzes compiled bytecode before execution to detect potential security violations in the code structure

---

<a id="13-which-architecture-modes--implementations-are-there"></a>
## 1.3 Which Architecture Modes / Implementations Are There?

Ares automatically detects file system operations by analyzing compiled bytecode using one of two Architecture implementations:

- **ArchUnit (Static Analysis)**: Pure static analysis using the ArchUnit framework. Fast analysis without call graph construction.
- **WALA (Call Graph Analysis)**: Static analysis with dynamic modeling using IBM WALA framework. Precise call path detection with false positive filtering.

Both implementations analyze the **code structure** to find forbidden method calls, but differ in precision and performance:

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
| **restrictedPackage** | `String` | The package containing the student code (the code to be analyzed) | `"de.student."` |
| **allowedPackages** | `PackagePermission[]` | Packages allowed to be imported/used | `[new PackagePermission("java.io")]` |
| **classPath** | `String` | Path to compiled student code | `"target/classes"` |

**Architecture-Specific Configuration:**
- No path-based permissions (no `pathsAllowedToBeRead`, etc.) - Architecture testing detects ANY file system access attempt
- No AOP mode selection - Uses `architectureMode` instead
- Package-level permissions instead of path-level permissions

---

<a id="15-when-is-file-access-generally-blocked"></a>
## 1.5 When Is File Access Generally Blocked?

**Access is BLOCKED 🔴 if ALL of the following conditions apply:**

1. **Architecture Mode Enabled**: `architectureMode` is set to `"ARCHUNIT"` or `"WALA"`
2. **Student Code Contains File System Calls**: Analysis detects method calls to file system APIs
3. **Calls Are Reachable**: The forbidden methods can be reached from student code (directly or transitively)
4. **Not in Allowed Packages**: The accessed classes are not in the `allowedPackages` list

**Access is ALLOWED 🟢 if ANY of the aforementioned conditions do not apply**

**Key Differences from AOP:**
- 🔴 Detected at analysis time (before execution), not at runtime
- 🔴 Blocks ALL file system access attempts, not just specific paths
- 🔴 Reports potential violations, even if the code path is never executed

**Legend:**
- 🔴 AssertionError thrown → Security violation detected in code structure
- 🟢 No violations found → Code passes architecture analysis

Summarising this, Ares trusts code when: 
- It is located outside of the `restrictedPackage`
- It accesses classes that are in the `allowedPackages` list
- It is listed as Ares internal code

**Security Assumptions:** 
- Student code is compiled and available as `.class` files
- Class files have not been tampered with after compilation
- Call graph accurately represents possible execution paths (WALA mode)

---

<a id="2-ares-2-architecture-file-system-access-control-ares-monitors-filesystem-methods"></a>
# 2. Ares 2 Architecture File System Access Control: Ares Monitors FileSystem Methods

<a id="21-which-operations-does-ares-2-architecture-file-system-access-control-monitor"></a>
## 2.1 Which Operations Does Ares 2 Architecture File System Access Control Monitor?

Ares classifies file system interactions into five action types. These labels determine which method patterns are checked during static analysis.

- **READ**: Accessing file contents or metadata without modifying them (streams, read APIs, attribute queries).
- **OVERWRITE**: Writing or mutating existing content/attributes (write/append/truncate, metadata setters).
- **CREATE**: Creating new files, directories, or links (create* APIs, file system creation/open).
- **DELETE**: Removing files or scheduling deletion/trash operations.
- **EXECUTE**: In Ares, 'Execute' is a broad category covering execution of binaries, opening files in external applications, and complex file manipulations like moving/copying (for example, `Desktop.open(...)` or process-related operations).

Some APIs can appear under multiple actions because they imply more than one permission (for example, `copy`/`move` or methods that create and write simultaneously).

---

Both ArchUnit and WALA modes monitor the same set of file system methods, loaded from template files:

**Template Locations:**
- **ArchUnit**: `src/main/resources/templates/architecture/java/archunit/methods/file-system-access-methods.txt`
- **WALA**: `src/main/resources/templates/architecture/java/wala/methods/file-system-access-methods.txt`

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
| java.io.BufferedInputStream | `<new>` | ✅ | ✅ | ✅ |
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
| java.nio.file.Files | newBufferedReader | ✅ | ✅ | ✅ |
| java.nio.file.Files | readString | ✅ | ✅ | ✅ |
| java.nio.file.Files | lines | ✅ | ✅ | ✅ |
| java.nio.file.Files | readAllLines | ✅ | ✅ | ✅ |
| java.util.Scanner | `<new>` | ✅ | ✅ | ✅ |

**Reads only specifically formatted files fully**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.DataInputStream | `<new>` | ✅ | ✅ | ❌ |
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
| java.io.InputStream | read | ✅ | ✅ | ❌ |
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

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.DataOutputStream | `<new>` | ✅ | ✅ | ❌ |
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
| java.awt.Desktop | moveToTrash | ✅ | ✅ | ❌ |
| java.io.File | deleteOnExit | ✅ | ✅ | ✅ |

**Delete folders**

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.io.File | delete | ✅ | ✅ | ✅ |
| java.nio.file.Files | delete | ✅ | ✅ | ✅ |
| java.nio.file.Files | deleteIfExists | ✅ | ✅ | ✅ |
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

**What does "Execute" mean?** File system actions that trigger execution-like behavior such as launching processes or opening files with their default programs (e.g., `Runtime.exec(...)` or `ProcessBuilder.start(...)`).

**Security Component:** Execute operation monitor

**Monitored APIs:**

Execute APIs listed below trigger execution-like behavior on files.

**Executes the file on the console (command line execution)**

> **Note:** `ProcessBuilder.start()`, `ProcessBuilder.startPipeline()`, and `Runtime.exec()` are handled by the **Command System** rather than the File System in architecture testing, as they execute commands rather than individual files. The File System detection for execute only covers library loading (`load`/`loadLibrary`).

| Class (fully qualified) | Method | Detected by ArchUnit | Detected by WALA | Tested by RP |
| --- | --- | --- | --- | --- |
| java.lang.Runtime | load | ✅ | ✅ | ❌ |
| java.lang.Runtime | loadLibrary | ✅ | ✅ | ❌ |
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
2. Analyzes that `readSecretFile()` method calls `Files.readString()`
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

```java
JavaClasses javaClasses = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .withImportOption(location -> {
        String path = location.toString().replace("\\", "/");
        return !path.contains("/ares/api/");  // Exclude Ares internal classes
    })
    .importPackages("de.student");  // Import student package
```

**What happens:**
1. ClassFileImporter scans the classpath for `.class` files
2. Loads class metadata (methods, fields, dependencies)
3. Excludes test classes and Ares internal classes
4. Creates `JavaClasses` object containing all analyzed classes

**Analysis Process:**
1. **Import Classes**: Load `.class` files using `ClassFileImporter`
2. **Analyze Dependencies**: Build class dependency graph
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

```java
// Read exclusions file (to ignore JDK internals)
File exclusionsFile = FileTools.readFile(
    FileTools.resolveFileOnSourceDirectory("templates", "architecture", "java", "exclusions.txt")
);

// Create analysis scope
AnalysisScope scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
    classPath + File.pathSeparator + System.getProperty("java.class.path"),
    exclusionsFile
);

// Build class hierarchy
ClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);
```

**Exclusions File Example:**
```
# Exclude JDK internals to improve performance
sun/.*
com/sun/.*
jdk/internal/.*
```

**Step 2: Define Entry Points**

```java
// Find all methods in student code as potential entry points
List<DefaultEntrypoint> entryPoints = new ArrayList<>();

for (IClass iClass : classHierarchy) {
    if (iClass.getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
        for (IMethod method : iClass.getDeclaredMethods()) {
            if (!method.getName().toString().equals("main")) {
                entryPoints.add(new DefaultEntrypoint(method.getReference(), classHierarchy));
            }
        }
    }
}
```

**Entry Point Selection:**

Entry points are methods where analysis should start. WALA uses all methods in student code (except `main`) as entry points to ensure comprehensive coverage.

**Why exclude `main`?**
- In test scenarios, tests invoke student methods directly
- `main` is typically not the actual entry point for student code
- Including `main` may introduce unnecessary analysis paths

**Entry Point Representation:**
```java
DefaultEntrypoint(
    methodReference = "Lde/student/StudentCode.readFile()V",
    classHierarchy = <ClassHierarchy>
)
```

**Step 3: Build Call Graph**

```java
// Configure analysis options
AnalysisOptions options = new AnalysisOptions();
options.setEntrypoints(entryPoints);

// Create call graph builder (0-CFA algorithm)
CallGraphBuilder<?> builder = Util.makeZeroCFABuilder(
    Language.JAVA,
    options,
    new AnalysisCacheImpl(),
    classHierarchy
);

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
3. **Construct Call Graph**: Map all possible method call relationships
4. **Find Entry Points**: Identify starting methods in student code
5. **Reachability Analysis**: Use DFS to find paths to forbidden methods
6. **Filter False Positives**: Remove JDK helper paths (e.g., internal thread creation)

**Strengths:**
- ✅ Very precise call path detection
- ✅ Understands complex call patterns (lambdas, method references, reflection)
- ✅ Can filter out JDK internal calls (false positive reduction)
- ✅ Provides exact call paths with line numbers
- ✅ Models runtime behavior more accurately

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
// Define rule
ArchRule rule = noClasses()
    .should(new TransitivelyAccessesMethodsCondition(
        javaAccess -> forbiddenMethods.contains(javaAccess.getTarget().getFullName())
    ))
    .as("No class should access file system");

// Execute rule
rule.check(javaClasses);  // Throws AssertionError if violated
```

**Step 2: Create Architecture Rule**

```java
// Load forbidden methods from template file
Set<String> forbiddenMethods = FileTools.readMethodsFile(
    FileTools.readFile(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS)
);

// Create custom condition that checks method access
DescribedPredicate<JavaAccess<?>> checkForbiddenAccess = 
    new DescribedPredicate<>("accesses forbidden file system method") {
        @Override
        public boolean test(JavaAccess<?> javaAccess) {
            return forbiddenMethods.stream()
                .anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
        }
    };

// Create rule that no class should access forbidden methods
ArchRule rule = ArchRuleDefinition.noClasses()
    .should(new TransitivelyAccessesMethodsCondition(checkForbiddenAccess))
    .as("No class should access file system");
```

**Rule Components:**
- **DescribedPredicate**: Tests if a method access is forbidden
- **TransitivelyAccessesMethodsCondition**: Finds transitive access paths
- **ArchRule**: Complete rule that can be checked against JavaClasses

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
Architecture Violation [Priority: MEDIUM] - Rule 'No class should access file system' was violated (1 times):
Method <de.student.StudentCode.readFile()> transitively accesses method <java.nio.file.Files.readString(Ljava/nio/file/Path;)Ljava/lang/String;> by 
  [de.student.StudentCode.readFile() -> de.student.Helper.loadFile() -> java.nio.file.Files.readString()]
```

**Error is converted to:**
```java
throw new SecurityException(
    "Ares Security Error (Reason: Student-Code; Stage: Execution): " +
    "Illegal file system access: StudentCode.readFile transitively calls Files.readString"
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
// Reports: "StudentCode transitively accesses Files.readString by [StudentCode.readFile→Helper.processFile]"
```

**Violation Example:**
```
Architecture Violation [Priority: MEDIUM] - Rule 'No class should access file system' was violated (1 times):
Method <de.student.StudentCode.exploit()> transitively accesses method <java.io.FileInputStream.<init>(Ljava/lang/String;)V> by 
  [de.student.StudentCode.exploit() -> de.student.Helper.readFile() -> java.io.FileInputStream.<init>()]
```

**Best For:**
- Quick validation during development
- Broad detection of file system usage patterns
- When exact call paths are not critical

---

<a id="52-wala-mode-call-graph-analysis"></a>
## 5.2 WALA Mode: Call Graph Analysis

**How it works:**
```java
// Build call graph
CallGraph cg = new CustomCallgraphBuilder(classPath).buildCallGraph(classPath);

// Find reachable forbidden methods
List<CGNode> path = ReachabilityChecker.findReachableMethods(
    cg, 
    cg.getEntrypointNodes().iterator(),
    node -> forbiddenMethods.contains(node.getMethod().getSignature())
);

if (path != null) {
    throw new AssertionError("Forbidden method reachable: " + path);
}
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
// Reports: Method call at StudentCode.java:12 reaches forbidden FileInputStream.<init>
```

**Violation Example:**
```
Forbidden method 'java.io.FileInputStream.<init>(Ljava/lang/String;)V' is reachable from 'de.student.StudentCode.exploit()V' 
in class 'de.student.StudentCode' at line 42
Called by test method: 'org.example.TestClass.testExploit()V'
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
        for (JavaAccess<?> access : clazz.getAccessesFromSelf()) {
            // Find transitive path to forbidden method
            List<JavaAccess<?>> path = findPathToViolatingMethod(access);
            
            if (!path.isEmpty()) {
                // Report violation with complete call chain
                events.add(createViolationEvent(access, path));
            }
        }
    }
    
    private List<JavaAccess<?>> findPathToViolatingMethod(JavaAccess<?> access) {
        // Use DFS to find path from current access to forbidden method
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

```java
// Load forbidden methods
Set<String> forbiddenMethods = FileTools.readMethodsFile(
    FileTools.readFile(FileHandlerConstants.WALA_FILESYSTEM_METHODS)
);

// Find reachable forbidden methods
List<CGNode> path = ReachabilityChecker.findReachableMethods(
    callGraph,
    callGraph.getEntrypointNodes().iterator(),
    node -> forbiddenMethods.stream()
        .anyMatch(sig -> node.getMethod().getSignature().startsWith(sig))
);

if (path != null && !path.isEmpty()) {
    // Extract source position
    IMethod.SourcePosition sourcePos = path.get(path.size() - 1)
        .getMethod()
        .getSourcePosition(0);
    
    throw new AssertionError(String.format(
        "Forbidden method '%s' reachable from '%s' at line %d",
        path.get(path.size() - 1).getMethod().getSignature(),
        path.get(0).getMethod().getSignature(),
        sourcePos.getFirstLine()
    ));
}
```

**DFS Path Finding Algorithm:**

```java
public class CustomDFSPathFinder {
    List<CGNode> find() {
        Set<CGNode> visited = new HashSet<>();
        Stack<CGNode> currentPath = new Stack<>();
        
        for (CGNode entryPoint : entryPoints) {
            List<CGNode> result = dfs(entryPoint, visited, currentPath);
            if (result != null) return result;
        }
        return null;
    }
    
    List<CGNode> dfs(CGNode node, Set<CGNode> visited, Stack<CGNode> path) {
        if (visited.contains(node)) return null;
        visited.add(node);
        path.push(node);
        
        // Check if this node is a target
        if (isTargetNode(node)) {
            return new ArrayList<>(path);  // Found path!
        }
        
        // Recursively check successors
        for (CGNode successor : callGraph.getSuccNodes(node)) {
            List<CGNode> result = dfs(successor, visited, path);
            if (result != null) return result;
        }
        
        path.pop();
        return null;
    }
}
```

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

**Challenge:** JDK classes legitimately use file system operations internally (e.g., `Files.list()` creates threads internally for directory traversal).

**Solution:** Filter out paths that only go through JDK helper classes.

```java
private static final List<String> JDK_THREAD_HELPERS = List.of(
    "Lsun/nio/fs/", "sun/nio/fs/",
    "Lsun/nio/ch/", "sun/nio/ch/",
    "Ljava/nio/file/Files", "java/nio/file/Files",
    "Ljava/lang/ClassLoader", "java/lang/ClassLoader"
);

private static final List<String> ALLOWED_HELPER_APIS = List.of(
    "java.lang.ClassLoader.getSystemClassLoader",
    "java.io.File.getName",
    "java.io.File.<init>"
);

// Check if path should be ignored
boolean isHelperPath(List<CGNode> path) {
    CGNode forbidden = path.get(path.size() - 1);
    
    // Is the forbidden API actually an allowed helper API?
    boolean helperApi = ALLOWED_HELPER_APIS.stream()
        .anyMatch(sig -> forbidden.getMethod().getSignature().startsWith(sig));
    
    // Does the path contain JDK helper classes?
    boolean helperSeen = path.stream().anyMatch(node -> {
        String cls = node.getMethod().getDeclaringClass().getName().toString();
        return JDK_THREAD_HELPERS.stream().anyMatch(cls::startsWith);
    });
    
    // Ignore if it's a helper API called through helper classes
    return helperApi && helperSeen;
}
```

**Example Filtering:**

**Path 1 (Ignored):**
```
StudentCode.listFiles()
  ↓
Files.list()  (JDK helper)
  ↓
UnixDirectoryStream.<init>()  (internal JDK class)
  ↓
FileInputStream.<init>()  (allowed helper API)

Result: IGNORED (legitimate JDK internal use)
```

**Path 2 (Detected):**
```
StudentCode.readSecret()
  ↓
FileInputStream.<init>()  (forbidden, no helpers in path)

Result: VIOLATION (direct file access by student)
```

**Special WALA Features:**

**JDK Helper Filtering:**
WALA mode includes intelligent filtering of JDK-internal helper classes that legitimately use file/thread operations:
```java
private static final List<String> JDK_THREAD_HELPERS = List.of(
    "sun/nio/fs/", "sun/nio/ch/", "java/nio/file/Files",
    "java/lang/ClassLoader", "jdk/internal/loader/NativeLibraries"
);

// Ignores paths like: StudentCode → Files.list → UnixDirectoryStream (internal helper)
// Detects real violations: StudentCode → Files.readString → (no internal helper)
```

<a id="6-ares-2-architecture-file-system-access-control-operation-type-classification"></a>
# 6. Ares 2 Architecture File System Access Control: Operation Type Classification

This section explains how architecture testing classifies operations differently from AOP-based runtime analysis. Understanding these differences is essential for correctly interpreting analysis results.

**Key Difference from AOP:**

Unlike AOP which can analyze runtime parameters (like `StandardOpenOption` values), architecture testing classifies operations based on **method signatures** in the forbidden methods list. This means:

- **No parameter inspection**: Cannot distinguish `Files.newByteChannel(path, READ)` from `Files.newByteChannel(path, WRITE)` at analysis time
- **Method-level classification**: Each method is pre-classified as READ, WRITE, CREATE, DELETE, or EXECUTE based on its typical usage
- **Conservative approach**: Methods that could perform multiple operations are often classified under the most restrictive category

**Classification Strategy:**

| Method Pattern | Classified As | Reason |
|----------------|---------------|--------|
| `Files.readString`, `Files.readAllBytes` | READ | Primary purpose is reading |
| `Files.writeString`, `Files.write` | WRITE | Primary purpose is writing |
| `Files.createFile`, `Files.createDirectory` | CREATE | Primary purpose is creation |
| `Files.delete`, `Files.deleteIfExists` | DELETE | Primary purpose is deletion |
| `Desktop.open`, `Desktop.edit` | EXECUTE | Launches external programs |
| `Files.newByteChannel`, `FileChannel.open` | Multiple | Listed under multiple categories |

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
