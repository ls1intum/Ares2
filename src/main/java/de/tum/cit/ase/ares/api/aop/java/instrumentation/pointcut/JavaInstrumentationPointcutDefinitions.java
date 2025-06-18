package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class contains the pointcut definitions for the Java instrumentation agent.
 * These pointcuts are used to identify methods that perform file system operations
 * (e.g., read, write, execute, delete) and apply security checks using bytecode modification.
 * The methods in this class define which file operations will be instrumented and monitored to
 * enforce security policies at runtime.
 */
public class JavaInstrumentationPointcutDefinitions {

    //<editor-fold desc="Constructor">

    /**
     * This constructor is private to prevent instantiation of this utility class.
     */
    private JavaInstrumentationPointcutDefinitions() {
        throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.general.utility.initialization"));
    }
    //</editor-fold>

    //<editor-fold desc="Tools">

    /**
     * Creates a type matcher that selects exactly the classes (and their subtypes) named
     * in the given pointcut map.
     *
     * <p>The map keys represent fully qualified class names whose methods or constructors
     * are candidates for instrumentation. This matcher will match any class whose
     * {@link TypeDescription#getName() name} equals one of these keys, or which has a
     * supertype matching one of these keys.</p>
     *
     * <p>Internally, this is built by starting from {@code ElementMatchers.none()} (always false)
     * and OR’ing in for each target class:
     * <ul>
     *   <li>{@code named(target)}</li>
     *   <li>{@code hasSuperType(named(target))}</li>
     * </ul>
     * The resulting matcher is effectively:</p>
     * <pre>
     *   named(key1) or hasSuperType(named(key1))
     *   or named(key2) or hasSuperType(named(key2))
     *   … etc.
     * </pre>
     *
     * @param methodsMap
     *         A map whose keys are the fully qualified names of classes to match,
     *         and whose values are the pointcut method names (ignored here).
     * @return A Byte-Buddy {@code ElementMatcher<TypeDescription>} that matches exactly
     *         those classes and any of their subtypes.
     * @see net.bytebuddy.matcher.ElementMatchers#named(String)
     * @see net.bytebuddy.matcher.ElementMatchers#hasSuperType(ElementMatcher)
     */
    public static ElementMatcher<TypeDescription> getClassesMatcher(
            Map<String, List<String>> methodsMap
    ) {
        String[] targets = methodsMap
                .keySet()
                .stream()
                .distinct()
                .toArray(String[]::new);

        if (targets.length == 0) {
            return ElementMatchers.none();
        }

        ElementMatcher.Junction<TypeDescription> matcher = ElementMatchers.none();
        for (String target : targets) {
            matcher = matcher
                    .or(ElementMatchers.named(target))
                    .or(ElementMatchers.hasSuperType(ElementMatchers.named(target)));
        }

        return matcher;
    }

    /**
     * Creates a constructor matcher for the given type description and pointcut map.
     *
     * <p>This matcher will select only the constructors of classes that appear in
     * {@code methodsMap} and for which the list of method names contains {@code "<init>"}.
     * Concretely:
     * <ul>
     *   <li>We build a hierarchy matcher that OR’s together {@code named(key)} and
     *       {@code hasSuperType(named(key))} for each key.</li>
     *   <li>We check whether, for that key, the associated list contains the
     *       special pointcut name {@code "<init>"}. If not, that class is skipped.</li>
     *   <li>The final matcher is then {@code isConstructor().and(isDeclaredBy(hierarchyMatcher))}.</li>
     * </ul>
     *
     * @param typeDescription
     *         The Byte-Buddy description of the class being evaluated.
     * @param methodsMap
     *         A map with class names as keys and lists of method names as values;
     *         here we only consider entries whose list contains {@code "<init>"}.
     * @return A {@code ElementMatcher<MethodDescription>} matching only the constructors
     *         declared by the matched classes (or their subtypes) that are in the pointcut.
     * @see net.bytebuddy.matcher.ElementMatchers#isConstructor()
     * @see net.bytebuddy.matcher.ElementMatchers#isDeclaredBy(ElementMatcher)
     */
    static ElementMatcher<MethodDescription> getConstructorsMatcher(
            TypeDescription typeDescription,
            Map<String, List<String>> methodsMap
    ) {
        ElementMatcher.Junction<TypeDescription> hierarchyMatcher = ElementMatchers.none();
        boolean hasConstructorPointcut = false;
        for (String key : methodsMap.keySet()) {
            ElementMatcher.Junction<TypeDescription> keyMatcher = ElementMatchers.named(key);
            ElementMatcher.Junction<TypeDescription> subTypeMatcher = ElementMatchers.hasSuperType(keyMatcher);
            ElementMatcher.Junction<TypeDescription> typeMatcher = keyMatcher.or(subTypeMatcher);

            if (typeMatcher.matches(typeDescription)) {
                hierarchyMatcher = hierarchyMatcher.or(typeMatcher);
                List<String> names = methodsMap.get(key);
                if (names != null && names.contains("<init>")) {
                    hasConstructorPointcut = true;
                }
            }
        }
        if (!hasConstructorPointcut) {
            return ElementMatchers.none();
        }
        return ElementMatchers
                .isConstructor()
                .and(ElementMatchers.isDeclaredBy(hierarchyMatcher));
    }

    /**
     * Creates a method matcher for the given type description and pointcut map.
     *
     * <p>This matcher will select methods whose names appear in the pointcut list for
     * any class matching the hierarchy. The steps are:
     * <ol>
     *   <li>Build a hierarchy matcher by OR’ing {@code named(key)} and
     *       {@code hasSuperType(named(key))} for each key in {@code methodsMap} that
     *       matches {@code typeDescription}.</li>
     *   <li>Accumulate all method names (excluding {@code "<init>"}) into a set.</li>
     *   <li>Create two sub‐matchers:
     *       <ul>
     *         <li>{@code declaredMatcher = namedOneOf(names).and(isDeclaredBy(hierarchyMatcher))}</li>
     *         <li>{@code overrideMatcher = namedOneOf(names).and(isOverriddenFrom(namedOneOf(names)))}</li>
     *       </ul>
     *   </li>
     *   <li>Return the union {@code declaredMatcher.or(overrideMatcher)}.</li>
     * </ol>
     *
     * @param typeDescription
     *         The Byte-Buddy description of the class whose methods are under consideration.
     * @param methodsMap
     *         A map from fully qualified class names to lists of method names that
     *         should be instrumented (excluding constructors).
     * @return A {@code ElementMatcher<MethodDescription>} that matches any method
     *         declared by or overriding one of the listed pointcut names in the matching classes.
     * @see net.bytebuddy.matcher.ElementMatchers#namedOneOf(String...)
     * @see net.bytebuddy.matcher.ElementMatchers#isDeclaredBy(ElementMatcher)
     * @see net.bytebuddy.matcher.ElementMatchers#isOverriddenFrom(ElementMatcher)
     */
    static ElementMatcher<MethodDescription> getMethodsMatcher(
            TypeDescription typeDescription,
            Map<String, List<String>> methodsMap
    ) {
        // Start with an empty hierarchy matcher
        ElementMatcher.Junction<TypeDescription> hierarchyMatcher = ElementMatchers.none();
        // Initialize a set to collect merthod-pointcut names
        Set<String> pointcutNames = new TreeSet<>();
        // Iterate over the methods map to build the hierarchy matcher
        for (Map.Entry<String, List<String>> entry : methodsMap.entrySet()) {
            // Get the class name
            String key = entry.getKey();
            // Get the list of method names
            List<String> values = entry.getValue();

            // Matcher for the case when the class is the key
            ElementMatcher.Junction<TypeDescription> keyClassMatcher = ElementMatchers.named(key);
            // Matcher for the case when a subclass is the key
            ElementMatcher.Junction<TypeDescription> keySuperClassMatcher = ElementMatchers.hasSuperType(keyClassMatcher);
            // Combine both matchers
            ElementMatcher.Junction<TypeDescription> keyMatcher = keyClassMatcher.or(keySuperClassMatcher);
            // Check if typeDescription matches the key matcher
            if (keyMatcher.matches(typeDescription)) {
                // Add the key matcher to the hierarchy matcher
                hierarchyMatcher = hierarchyMatcher.or(keyMatcher);
                for (String name : values) {
                    if (!"<init>".equals(name)) {
                        pointcutNames.add(name);
                    }
                }
            }
        }
        if (pointcutNames.isEmpty()) {
            return ElementMatchers.none();
        } else {
            String[] namesArray = pointcutNames.toArray(new String[0]);
            ElementMatcher.Junction<NamedElement> nameMatcher = ElementMatchers.namedOneOf(namesArray);
            ElementMatcher.Junction<MethodDescription> declaredMatcher = nameMatcher.and(ElementMatchers.isDeclaredBy(hierarchyMatcher));
            ElementMatcher.Junction<MethodDescription> overrideMatcher = nameMatcher.and(ElementMatchers.isOverriddenFrom(hierarchyMatcher));
            return declaredMatcher.or(overrideMatcher);
        }
    }

    //</editor-fold>

    //<editor-fold desc="Read Path">
    /**
     * This map contains the methods which can read files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file read operations.
     */
    public static final Map<String, List<String>> methodsWhichCanReadFiles = Map.ofEntries(
            // java.io
            Map.entry("java.io.Reader", List.of("read")),
            Map.entry("java.io.InputStream", List.of("read")),
            Map.entry("java.io.DataInput", List.of("read", "readBoolean", "readByte", "readChar", "readDouble", "readFloat", "readFully", "readInt", "readLine", "readLong", "readShort", " readUnsignedByte", " readUnsignedShort",  "readUTF")),
            Map.entry("java.io.ObjectInput", List.of("read", "readObject")),
            Map.entry("java.io.File", List.of("canExecute", "canRead", "canWrite", "compareTo", "createLink", "getAbsoluteFile", "getAbsolutePath", "getCanonicalFile", "getCanonicalPath", "getFreeSpace", "getName", "getParent", "getParentFile", "getTotalSpace", "getUsableSpace", "isAbsolute", "isDirectory", "isFile", "isHidden", "lastModified", "length", "list", "listFiles", "listRoots", "renameTo", "toPath", "toURI", "toURL")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("copy", "createLink", "createSymbolicLink", "find", "getAttribute", "getFileAttributeView", "getFileStore", "getLastModifiedTime", "getOwner", "getPosixFilePermissions", "isDirectory", "isExecutable", "isHidden", "isReadable", "isRegularFile", "isSameFile", "isSymbolicLink", "isWritable", "list", "move", "newBufferedReader", "newByteChannel", "newDirectoryStream", "newFileSystem", "newInputStream", "notExists", "probeContentType", "readAllBytes", "readAllLines", "readAttributes", "readString", "readSymbolicLink", "size", "walk", "walkFileTree")),
            Map.entry("java.nio.channels.FileChannel", List.of("map", "position", "read", "size", "transferFrom", "transferTo")),
            Map.entry("java.nio.channels.AsynchronousFileChannel", List.of("read", "size")),
            Map.entry("java.nio.file.spi.FileSystemProvider", List.of("checkAccess", "getFileAttributeView", "getFileStore", "getFileSystem", "getScheme", "isHidden", "isSameFile", "newByteChannel", "newDirectoryStream", "newFileChannel", "newFileSystem", "newInputStream", "readAttributes", "readSymbolicLink")),
            // java.util
            Map.entry("java.util.Scanner", List.of("findInLine", "findWithinHorizon", "next", "nextBigDecimal", "nextBigInteger", "nextBoolean", "nextByte", "nextDouble", "nextFloat", "nextInt", "nextLine", "nextLong", "nextShort", "skip")),
            // java.net
            Map.entry("java.net.URL", List.of("openConnection", "openStream")),
            Map.entry("java.net.URLConnection", List.of("connect", "getContent", "getInputStream")),
            Map.entry("java.net.JarURLConnection", List.of("getInputStream")),
            Map.entry("java.net.URLClassLoader", List.of("getResourceAsStream")),
            // java.lang
            Map.entry("java.lang.ClassLoader", List.of("getResource", "getResourceAsStream", "getResources")),
            // java.awt
            Map.entry("java.awt.Toolkit", List.of("createImage", "getImage")),
            Map.entry("java.awt.image.PixelGrabber", List.of("grabPixels")),
            // javax.imageio
            Map.entry("javax.imageio.ImageIO", List.of("createImageInputStream", "getImageReaders", "read")),
            // javax.xml
            Map.entry("javax.xml.parsers.DocumentBuilder", List.of("parse")),
            // javax.sound
            Map.entry("javax.sound.sampled.AudioSystem", List.of("getAudioInputStream"))
    );
    //</editor-fold>

    //<editor-fold desc="Overwrite Path">
    /**
     * This map contains the methods which can overwrite files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file overwrite operations.
     */
    public static final Map<String, List<String>> methodsWhichCanOverwriteFiles = Map.ofEntries(
            // java.io
            Map.entry("java.io.Writer", List.of("append", "flush", "write")),
            Map.entry("java.io.OutputStream", List.of("flush", "write")),
            Map.entry("java.io.RandomAccessFile", List.of("write", "writeBoolean", "writeByte", "writeBytes", "writeChar", "writeChars", "writeDouble", "writeFloat", "writeInt", "writeLong", "writeShort", "writeUTF")),
            Map.entry("java.io.File", List.of("createNewFile", "createTempFile", "mkdir", "mkdirs", "renameTo", "setExecutable", "setLastModified", "setReadable", "setReadOnly", "setWritable")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("copy", "createDirectories", "createFile", "createLink", "createTempDirectory", "createTempFile", "createSymbolicLink", "lines", "move", "newByteChannel", "newOutputStream", "setAttribute", "setLastModifiedTime", "setOwner", "setPosixFilePermissions", "write")),
            Map.entry("java.nio.file.FileSystems", List.of("newFileSystem")),
            Map.entry("java.nio.file.attribute.UserDefinedFileAttributeView", List.of("write")),
            Map.entry("java.nio.channels.FileChannel", List.of("force", "map", "transferFrom", "transferTo", "truncate", "write")),
            Map.entry("java.nio.channels.AsynchronousFileChannel", List.of("force", "truncate", "write")),
            Map.entry("java.nio.file.spi.FileSystemProvider", List.of("copy", "createDirectory", "createLink", "createSymbolicLink", "move", "newAsynchronousFileChannel", "newByteChannel", "newOutputStream", "setAttribute")),
            // com.sun
            Map.entry("com.sun.management.HotSpotDiagnosticMXBean", List.of("dumpHeap")),
            // jdk.jfr
            Map.entry("jdk.jfr.Recording", List.of("dump")),
            // javax.print
            Map.entry("javax.print.DocPrintJob", List.of("print")),
            // javax.imageio
            Map.entry("javax.imageio.ImageIO", List.of("write")),
            // javax.sound
            Map.entry("javax.sound.sampled.AudioSystem", List.of("write")),
            // javax.xml
            Map.entry("javax.xml.transform.Transformer", List.of("transform")),
            Map.entry("javax.xml.bind.Marshaller", List.of("marshal"))
    );
    //</editor-fold>

    //<editor-fold desc="Execute Path">
    /**
     * This map contains the methods which can execute files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file execute operations.
     */
    public static final Map<String, List<String>> methodsWhichCanExecuteFiles = Map.ofEntries(
            Map.entry("java.io.File", List.of("renameTo")),
            Map.entry("java.nio.file.Files", List.of("move", "copy", "createSymbolicLink")),
            Map.entry("java.nio.channels.FileChannel", List.of("transferFrom", "transferTo")),
            // java.awt
            Map.entry("java.awt.Desktop", List.of("browse", "edit", "mail", "open", "print")),
            // java.lang
            Map.entry("java.lang.Runtime", List.of("exec")),
            Map.entry("java.lang.ProcessBuilder", List.of("start")),
            // org.eclipse
            Map.entry("org.eclipse.swt.program.Program", List.of("launch")),
            // com.apple
            Map.entry("com.apple.eio.FileManager", List.of("openURL", "runApplication"))
    );
    //</editor-fold>

    //<editor-fold desc="Delete Path">
    /**
     * This map contains the methods which can delete files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file delete operations.
     */
    public static final Map<String, List<String>> methodsWhichCanDeleteFiles = Map.ofEntries(
            // java.awt
            Map.entry("java.awt.Desktop", List.of("moveToTrash")),
            // java.io
            Map.entry("java.io.File", List.of("delete", "deleteOnExit", "renameTo")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("delete", "deleteIfExists", "move", "copy", "createSymbolicLink")),
            Map.entry("java.nio.channels.FileChannel", List.of("transferFrom", "transferTo")),
            Map.entry("java.nio.file.spi.FileSystemProvider", List.of("delete", "deleteIfExists"))
    );
    //</editor-fold>

    //<editor-fold desc="Create Thread">
    /**
     * This map contains the methods which can create threads. The map keys represent class names,
     * and the values are lists of method names that are considered to be thread create operations.
     */
    public static final Map<String, List<String>> methodsWhichCanCreateThreads = Map.ofEntries(
            // java.util
            Map.entry("java.util.concurrent.AbstractExecutorService", List.of("submit")),
            Map.entry("java.util.concurrent.ExecutorService", List.of("submit")),
            Map.entry("java.util.concurrent.ThreadPoolExecutor", List.of("submit"))
    );
    //</editor-fold>

}

