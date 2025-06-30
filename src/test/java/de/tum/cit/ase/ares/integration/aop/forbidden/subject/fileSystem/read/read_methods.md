# Java File Reading Methods

| Origin   | API (constructor / method / class)                     | Package                         | I/O kind                       | Purpose                                                                                              |
| -------- | ------------------------------------------------------ | ------------------------------- | ------------------------------ | ---------------------------------------------------------------------------------------------------- |
| Lecture  | `FileInputStream(String path)`                         | `java.io.FileInputStream`       | **Byte stream**                | Open a file and obtain a raw byte stream                                                             |
| Lecture  | `InputStream.read()`                                   | `java.io.InputStream`           | **Byte stream**                | Read one byte; returns `-1` at EOF                                                                   |
| Lecture  | `InputStream.available()`                              | `java.io.InputStream`           | **Byte stream**                | Bytes that can be read without blocking                                                              |
| Lecture  | `InputStream.readAllBytes()`                           | `java.io.InputStream`           | **Whole-file convenience**     | Slurp the whole file into a `byte[]`                                                                 |
| Lecture  | `DataInputStream(InputStream in)` (+ `readInt()` etc.) | `java.io.DataInputStream`       | **Byte stream**                | Add typed reads on top of any byte stream                                                            |
| Lecture  | `InputStreamReader(InputStream in[, Charset])`         | `java.io.InputStreamReader`     | **Character reader**           | Decode raw bytes → Unicode text                                                                      |
| Lecture  | `FileReader(String path)`                              | `java.io.FileReader`            | **Character reader**           | Text-file reader using the platform charset                                                          |
| Lecture  | `Reader.read()`                                        | `java.io.Reader`                | **Character reader**           | Read one Unicode code unit; `-1` at EOF                                                              |
| Lecture  | `BufferedReader.readLine()`                            | `java.io.BufferedReader`        | **Character reader**           | Efficiently read an entire text line                                                                 |
| Lecture  | `Scanner`                                              | `java.util.Scanner`             | **Line / token reader**        | Token- or line-oriented reader (file or console)                                                     |
| Internet | `Files.readAllBytes(Path)`                             | `java.nio.file.Files`           | **Whole-file convenience**     | Read whole file into a `byte[]` ([docs.oracle.com][1])                                               |
| Internet | `Files.readString(Path[, Charset])`                    | `java.nio.file.Files`           | **Whole-file convenience**     | Java 11+ shortcut to get the entire file as one `String` ([geeksforgeeks.org][2])                    |
| Internet | `Files.readAllLines(Path)`                             | `java.nio.file.Files`           | **Whole-file convenience**     | Load all lines into `List<String>` ([docs.oracle.com][1])                                            |
| Internet | `Files.lines(Path)`                                    | `java.nio.file.Files`           | **Line / token reader**        | Stream the file lazily as a `Stream<String>` ([stackoverflow.com][3])                                |
| Internet | `Files.newBufferedReader(Path, Charset)`               | `java.nio.file.Files`           | **Character reader**           | Open a `BufferedReader` directly from a `Path` ([docs.oracle.com][4])                                |
| Internet | `Files.newInputStream(Path)`                           | `java.nio.file.Files`           | **Byte stream**                | Low-level byte stream (respects `OpenOption`s) ([baeldung.com][5])                                   |
| Internet | `FileChannel.read(ByteBuffer)`                         | `java.nio.channels.FileChannel` | **Byte stream (seekable)**     | High-throughput read into a buffer; supports random access ([docs.oracle.com][6], [baeldung.com][7]) |
| Internet | `RandomAccessFile.read(byte[])`                        | `java.io.RandomAccessFile`      | **Byte stream (seekable)**     | Seekable reads anywhere in the file ([docs.oracle.com][8])                                           |
| Internet | `Files.newByteChannel(Path, OpenOption…)`              | `java.nio.file.Files`           | **Byte stream (seekable)**     | Open a `SeekableByteChannel` for scatter/gather I/O ([baeldung.com][5])                              |
| Internet | `ClassLoader.getResourceAsStream(String)`              | `java.lang.ClassLoader`         | **Class-path resource stream** | Read a file bundled on the class-path or inside a JAR ([docs.oracle.com][9])                         |

[1]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/nio/file/Files.html?utm_source=chatgpt.com "Files (Java SE 11 & JDK 11 ) - Oracle Help Center"
[2]: https://www.geeksforgeeks.org/java/files-class-readstring-method-in-java-with-examples/?utm_source=chatgpt.com "Files Class readString() Method in Java with Examples"
[3]: https://stackoverflow.com/questions/65653533/how-can-i-read-all-files-line-by-line-from-a-directory-by-using-streams?utm_source=chatgpt.com "How can I read all files line by line from a directory by using Streams?"
[4]: https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html?utm_source=chatgpt.com "Files (Java Platform SE 8 ) - Oracle Help Center"
[5]: https://www.baeldung.com/java-file-options?utm_source=chatgpt.com "Java Files Open Options | Baeldung"
[6]: https://docs.oracle.com/javase/8/docs/api/java/nio/channels/FileChannel.html?utm_source=chatgpt.com "FileChannel (Java Platform SE 8 ) - Oracle Help Center"
[7]: https://www.baeldung.com/java-filechannel?utm_source=chatgpt.com "Guide to Java FileChannel | Baeldung"
[8]: https://docs.oracle.com/javase/8/docs/api/java/io/RandomAccessFile.html?utm_source=chatgpt.com "RandomAccessFile (Java Platform SE 8 ) - Oracle Help Center"
[9]: https://docs.oracle.com/javase/8/docs/api/java/lang/ClassLoader.html?utm_source=chatgpt.com "ClassLoader (Java Platform SE 8 ) - Oracle Help Center"
