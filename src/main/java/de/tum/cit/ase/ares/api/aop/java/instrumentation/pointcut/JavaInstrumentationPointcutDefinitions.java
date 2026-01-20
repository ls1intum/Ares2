package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.ArrayList;
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
        throw new SecurityException(
                JavaInstrumentationAdviceAbstractToolbox.localize(
                        "security.general.utility.initialization",
                        "JavaInstrumentationPointcutDefinitions"
                )
        );
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
     * <p>The resulting matcher is effectively:</p>
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

    private static final class MethodPointcutSpec {
        private final String name;
        private final List<String> parameterTypeNames;

        private MethodPointcutSpec(String name, List<String> parameterTypeNames) {
            this.name = name;
            this.parameterTypeNames = List.copyOf(parameterTypeNames);
        }

        static MethodPointcutSpec parse(String raw) {
            String trimmed = raw == null ? "" : raw.trim();
            if (trimmed.isEmpty()) {
                return new MethodPointcutSpec("", List.of());
            }

            int parenStart = trimmed.indexOf('(');
            if (parenStart < 0 || !trimmed.endsWith(")")) {
                return new MethodPointcutSpec(trimmed, List.of());
            }

            String name = trimmed.substring(0, parenStart).trim();
            String paramsPart = trimmed.substring(parenStart + 1, trimmed.length() - 1).trim();
            if (paramsPart.isEmpty()) {
                return new MethodPointcutSpec(name, List.of());
            }

            List<String> parameterTypes = new ArrayList<>();
            int depth = 0;
            int start = 0;

            for (int i = 0; i < paramsPart.length(); i++) {
                char ch = paramsPart.charAt(i);

                if (ch == '<') {
                    depth++;
                } else if (ch == '>') {
                    depth--;
                } else if (ch == ',' && depth == 0) {
                    String typeName = paramsPart.substring(start, i).trim();
                    if (!typeName.isEmpty()) {
                        parameterTypes.add(typeName);
                    }
                    start = i + 1;
                }
            }

            String lastType = paramsPart.substring(start).trim();
            if (!lastType.isEmpty()) {
                parameterTypes.add(lastType);
            }
            return new MethodPointcutSpec(name, parameterTypes);
        }

        String getName() {
            return name;
        }

        List<String> getParameterTypeNames() {
            return parameterTypeNames;
        }

        boolean hasParameters() {
            return !parameterTypeNames.isEmpty();
        }

        boolean isConstructor() {
            return "<init>".equals(name);
        }
    }

    /**
     * Creates a constructor matcher for the given type description and pointcut map.
     *
     * <p>This matcher selects constructors of classes that appear in {@code methodsMap}. Entries can
     * either specify {@code "<init>"} (to match all constructors) or {@code "<init>(...signature...)"} to
     * restrict instrumentation to specific parameter types.</p>
     *
     * @param typeDescription
     *         The Byte-Buddy description of the class being evaluated.
     * @param methodsMap
     *         A map with class names as keys and lists of constructor or method specifications as values.
     * @return A {@code ElementMatcher<MethodDescription>} matching the configured constructors declared by
     *         the matched classes (or their subtypes).
     * @see net.bytebuddy.matcher.ElementMatchers#isConstructor()
     * @see net.bytebuddy.matcher.ElementMatchers#isDeclaredBy(ElementMatcher)
     */
    static ElementMatcher<MethodDescription> getConstructorsMatcher(
            TypeDescription typeDescription,
            Map<String, List<String>> methodsMap
    ) {
        ElementMatcher.Junction<TypeDescription> hierarchyMatcher = ElementMatchers.none();
        List<MethodPointcutSpec> constructorSpecs = new ArrayList<>();
        for (String key : methodsMap.keySet()) {
            ElementMatcher.Junction<TypeDescription> keyMatcher = ElementMatchers.named(key);
            ElementMatcher.Junction<TypeDescription> subTypeMatcher = ElementMatchers.hasSuperType(keyMatcher);
            ElementMatcher.Junction<TypeDescription> typeMatcher = keyMatcher.or(subTypeMatcher);

            if (typeMatcher.matches(typeDescription)) {
                hierarchyMatcher = hierarchyMatcher.or(typeMatcher);
                List<String> names = methodsMap.get(key);
                if (names != null) {
                    for (String name : names) {
                        MethodPointcutSpec spec = MethodPointcutSpec.parse(name);
                        if (spec.isConstructor() && !spec.getName().isEmpty()) {
                            constructorSpecs.add(spec);
                        }
                    }
                }
            }
        }
        if (constructorSpecs.isEmpty()) {
            return ElementMatchers.none();
        }

        ElementMatcher.Junction<MethodDescription> matcher = ElementMatchers.none();
        for (MethodPointcutSpec spec : constructorSpecs) {
            ElementMatcher.Junction<MethodDescription> specMatcher = ElementMatchers
                    .isConstructor()
                    .and(ElementMatchers.isDeclaredBy(hierarchyMatcher));

            if (spec.hasParameters()) {
                List<String> parameterTypes = spec.getParameterTypeNames();
                specMatcher = specMatcher.and(ElementMatchers.takesArguments(parameterTypes.size()));
                for (int i = 0; i < parameterTypes.size(); i++) {
                    specMatcher = specMatcher.and(ElementMatchers.takesArgument(i, ElementMatchers.named(parameterTypes.get(i))));
                }
            }

            matcher = matcher.or(specMatcher);
        }
        return matcher;
    }

    /**
     * Methods listed here (by declaring class -> method name) will be excluded from the
     * matcher produced by {@link #getMethodsMatcher(TypeDescription, Map)}. This allows
     * fine-grained suppression of specific methods from otherwise instrumented classes.
     *
     * Keys are fully qualified class names; values are simple method names.
     * Initially empty; add entries as needed, e.g.:
     * Map.ofEntries(Map.entry("java.lang.Runtime", List.of("exec")))
     */
    public static final Map<String, List<String>> ignoredMethodsByClass = Map.ofEntries(
            Map.entry("java.io.ByteArrayInputStream", List.of("read")),
            Map.entry("java.io.RandomAccessFile", List.of("readFully")),
            Map.entry("java.util.zip.InflaterInputStream", List.of("read")),
            Map.entry("java.util.zip.ZipFile$ZipFileInputStream", List.of("read"))
    );

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
        // Subtract ignored methods for this type hierarchy
        if (!ignoredMethodsByClass.isEmpty()) {
            Set<String> ignored = new TreeSet<>();
            for (Map.Entry<String, List<String>> entry : ignoredMethodsByClass.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                ElementMatcher.Junction<TypeDescription> keyClassMatcher = ElementMatchers.named(key);
                ElementMatcher.Junction<TypeDescription> keySuperClassMatcher = ElementMatchers.hasSuperType(keyClassMatcher);
                ElementMatcher.Junction<TypeDescription> keyMatcher = keyClassMatcher.or(keySuperClassMatcher);
                if (keyMatcher.matches(typeDescription)) {
                    for (String name : values) {
                        if (!"<init>".equals(name)) {
                            ignored.add(name);
                        }
                    }
                }
            }
            pointcutNames.removeAll(ignored);
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
            Map.entry("java.io.BufferedInputStream", List.of("<init>", "read")),
            Map.entry("java.io.FileInputStream", List.of("<init>")),
            Map.entry("java.io.FileReader", List.of("<init>")),
            Map.entry("java.io.RandomAccessFile", List.of("<init>", "read")),
            Map.entry("java.io.BufferedReader", List.of("<init>", "read")),
            Map.entry("java.io.DataInput", List.of("read", "readBoolean", "readByte", "readChar", "readDouble", "readFloat", "readFully", "readInt", "readLine", "readLong", "readShort", "readUnsignedByte", "readUnsignedShort", "readUTF")),
            Map.entry("java.io.DataInputStream", List.of("<init>", "read", "readFully", "readUTF")),
            Map.entry("java.io.ObjectInput", List.of("read", "readObject")),
            Map.entry("java.io.ObjectInputStream", List.of("<init>", "read", "readObject")),
            Map.entry("java.io.InputStreamReader", List.of("read")),
            Map.entry("java.io.File", List.of("normalizedList", "list", "listFiles", "listRoots", "lastModified", "length", "getFreeSpace", "getTotalSpace", "getUsableSpace")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("find", "lines", "list", "newBufferedReader", "newByteChannel", "newDirectoryStream", "newInputStream", "readAllBytes", "readAllLines", "readString", "walk", "walkFileTree", "isSameFile", "size", "getLastModifiedTime", "getOwner", "getPosixFilePermissions", "getAttribute", "getFileStore", "probeContentType", "readSymbolicLink")),
            Map.entry("java.nio.channels.FileChannel", List.of("map", "open", "read", "transferFrom")),
            Map.entry("java.nio.channels.AsynchronousFileChannel", List.of("open", "read")),
            Map.entry("java.nio.channels.SeekableByteChannel", List.of("read")),
            Map.entry("java.nio.file.FileSystem", List.of("newWatchService")),
            Map.entry("java.nio.file.spi.FileSystemProvider", List.of("newAsynchronousFileChannel", "newByteChannel", "newDirectoryStream", "newFileChannel", "newInputStream", "newWatchService", "getFileStore", "isSameFile", "readSymbolicLink")),
            // java.net
            Map.entry("java.net.JarURLConnection", List.of("getInputStream")),
            // java.lang
            Map.entry("java.lang.ClassLoader", List.of("getResourceAsStream")),
            // java.awt
            Map.entry("java.awt.Toolkit", List.of("createImage", "getImage")),
            Map.entry("java.awt.image.PixelGrabber", List.of("grabPixels")),
            // javax.imageio
            Map.entry("javax.imageio.ImageIO", List.of("createImageInputStream", "getImageReaders", "read")),
            // javax.xml
            Map.entry("javax.xml.parsers.DocumentBuilder", List.of("parse")),
            Map.entry("javax.xml.parsers.SAXParser", List.of("parse")),
            Map.entry("javax.xml.bind.Unmarshaller", List.of("unmarshal")),
            // javax.sound
            Map.entry("javax.sound.sampled.AudioSystem", List.of("getAudioInputStream")),
            Map.entry("javax.sound.midi.MidiSystem", List.of("getSoundbank")),
            // java.util
            Map.entry("java.util.Scanner", List.of("<init>")),
            Map.entry("java.util.zip.ZipFile", List.of("<init>", "entries", "getInputStream")),
            Map.entry("java.util.zip.ZipInputStream", List.of("<init>", "getNextEntry", "read")),
            Map.entry("java.util.zip.GZIPInputStream", List.of("<init>", "read")),
            Map.entry("java.util.jar.JarFile", List.of("<init>", "entries", "getInputStream")),
            Map.entry("java.util.jar.JarInputStream", List.of("<init>", "getNextJarEntry")),
            Map.entry("java.util.Properties", List.of("load", "loadFromXML"))
    );
    //</editor-fold>

    //<editor-fold desc="Overwrite Path">
    /**
     * This map contains the methods which can overwrite files. The map keys represent class names,
     * and the values are lists of method names that are considered to be file overwrite operations.
     */
    public static final Map<String, List<String>> methodsWhichCanOverwriteFiles = Map.ofEntries(
            // java.io
            // Issues with System.out and System.err, as they call Writer.write internally.
            //Map.entry("java.io.Writer", List.of("append", "flush", "write")),
            Map.entry("java.io.OutputStream", List.of("write")),
            Map.entry("java.io.Writer", List.of("<init>", "append", "write")),
            Map.entry("java.io.OutputStreamWriter", List.of("<init>")),
            Map.entry("java.io.BufferedOutputStream", List.of("<init>", "write")),
            Map.entry("java.io.FileOutputStream", List.of("<init>", "write")),
            Map.entry("java.io.FileWriter", List.of("<init>", "append", "write")),
            Map.entry("java.io.BufferedWriter", List.of("<init>", "append", "write")),
            Map.entry("java.io.PrintWriter", List.of("<init>")),
            Map.entry("java.io.PrintStream", List.of("<init>")),
            Map.entry("java.io.DataOutputStream", List.of("<init>", "write", "writeBoolean", "writeByte", "writeBytes", "writeChar", "writeChars", "writeDouble", "writeFloat", "writeInt", "writeLong", "writeShort", "writeUTF")),
            Map.entry("java.io.ObjectOutputStream", List.of("<init>", "writeObject")),
            Map.entry("java.util.logging.FileHandler", List.of("<init>", "publish")),
            Map.entry("java.util.zip.GZIPOutputStream", List.of("<init>", "write")),
            Map.entry("java.util.zip.InflaterOutputStream", List.of("<init>")),
            Map.entry("java.util.zip.ZipOutputStream", List.of("<init>", "putNextEntry", "closeEntry", "write")),
            Map.entry("java.util.jar.JarOutputStream", List.of("<init>", "putNextEntry", "closeEntry")),
            Map.entry("java.util.Properties", List.of("store", "storeToXML")),
            Map.entry("java.util.Formatter", List.of("<init>")),
            Map.entry("java.io.RandomAccessFile", List.of("<init>", "write", "writeBoolean", "writeByte", "writeBytes", "writeChar", "writeChars", "writeDouble", "writeFloat", "writeInt", "writeLong", "writeShort", "writeUTF")),
            Map.entry("java.io.File", List.of("setExecutable", "setLastModified", "setReadable", "setReadOnly", "setWritable", "renameTo")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("copy", "move", "newBufferedWriter", "newByteChannel", "newOutputStream", "setAttribute", "setLastModifiedTime", "setOwner", "setPosixFilePermissions", "write", "writeString")),
            Map.entry("java.nio.file.attribute.UserDefinedFileAttributeView", List.of("write")),
            Map.entry("java.nio.channels.FileChannel", List.of("map", "open", "truncate", "write", "transferTo")),
            Map.entry("java.nio.channels.AsynchronousFileChannel", List.of("open", "truncate", "write")),
            Map.entry("java.nio.file.spi.FileSystemProvider", List.of("copy", "move", "newAsynchronousFileChannel", "newByteChannel", "newOutputStream", "setAttribute")),
            // javax.print
            Map.entry("javax.print.DocPrintJob", List.of("print")),
            // javax.imageio
            Map.entry("javax.imageio.ImageIO", List.of("write", "createImageOutputStream")),
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
            // java.lang
            Map.entry("java.lang.Runtime", List.of("exec", "load", "loadLibrary")),
            Map.entry("java.lang.System", List.of("load", "loadLibrary")),
            Map.entry("java.lang.ProcessBuilder", List.of("start", "startPipeline")),
            // java.awt
            Map.entry("java.awt.Desktop", List.of("open", "edit", "print", "browse", "browseFileDirectory"))
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
            Map.entry("java.io.File", List.of("delete", "deleteOnExit")),
            // java.nio
            Map.entry("java.nio.file.Files", List.of("delete", "deleteIfExists", "move", "copy")),
            Map.entry("java.nio.file.spi.FileSystemProvider", List.of("delete", "deleteIfExists"))
    );
    //</editor-fold>

    //<editor-fold desc="Create Path">
    /**
     * Methods that can create new files or directories (including links).
     */
    public static final Map<String, List<String>> methodsWhichCanCreateFiles = Map.ofEntries(
            Map.entry("java.io.File", List.of("createNewFile", "createTempFile", "mkdir", "mkdirs")),
            Map.entry("java.io.BufferedOutputStream", List.of("<init>")),
            Map.entry("java.io.BufferedWriter", List.of("<init>")),
            Map.entry("java.io.FileOutputStream", List.of("<init>")),
            Map.entry("java.io.FileWriter", List.of("<init>")),
            Map.entry("java.io.PrintWriter", List.of("<init>")),
            Map.entry("java.io.RandomAccessFile", List.of("<init>")),
            Map.entry("java.nio.channels.AsynchronousFileChannel", List.of("open")),
            Map.entry("java.nio.file.Files", List.of("createDirectories", "createDirectory", "createFile", "createLink", "createTempDirectory", "createTempFile", "createSymbolicLink", "newBufferedWriter", "newByteChannel", "newOutputStream", "write", "writeString")),
            Map.entry("java.nio.file.spi.FileSystemProvider", List.of("createDirectory", "createLink", "createSymbolicLink")),
            Map.entry("java.nio.channels.FileChannel", List.of("open"))
    );
    //</editor-fold>

    //<editor-fold desc="Create Thread">
    /**
     * This map contains the methods which can create threads. The map keys represent class names,
     * and the values are lists of method names that are considered to be thread create operations.
     */
    public static final Map<String, List<String>> methodsWhichCanCreateThreads = Map.ofEntries(
            //java.lang
            Map.entry("java.lang.Thread", List.of("start", "startVirtualThread")),
            Map.entry("java.lang.Thread.Builder", List.of("run", "start")),
            Map.entry("java.lang.Thread.Builder.OfPlatform", List.of("start")),
            Map.entry("java.lang.ThreadGroup", List.of("newThread")),
            // java.util
            Map.entry("java.util.Collection", List.of("parallelStream")),
            Map.entry("java.util.stream.Stream", List.of("parallel")),
            Map.entry("java.util.stream.BaseStream", List.of("parallel")),
            Map.entry("java.util.concurrent.Executor", List.of("execute")),
            Map.entry("java.util.concurrent.ExecutorService", List.of("submit", "invokeAll", "invokeAny", "execute")),
            Map.entry("java.util.concurrent.AbstractExecutorService", List.of("submit", "invokeAll", "invokeAny")),
            Map.entry("java.util.concurrent.ThreadPoolExecutor", List.of("submit", "execute")),
            Map.entry("java.util.concurrent.ScheduledExecutorService", List.of("schedule", "scheduleAtFixedRate", "scheduleWithFixedDelay")),
            Map.entry("java.util.concurrent.ScheduledThreadPoolExecutor", List.of("schedule", "scheduleAtFixedRate", "scheduleWithFixedDelay")),
            Map.entry("java.util.concurrent.CompletableFuture", List.of("runAsync", "supplyAsync", "thenApplyAsync", "thenCombineAsync", "thenCombine")),
            Map.entry("java.util.concurrent.ForkJoinPool", List.of("submit", "execute")),
            Map.entry("java.util.concurrent.ThreadFactory", List.of("newThread")),
            Map.entry("java.util.concurrent.Executors$DelegatedExecutorService", List.of("submit", "invokeAll", "invokeAny")),
            Map.entry("java.util.concurrent.Executors$DefaultThreadFactory", List.of("newThread")),
            Map.entry("java.util.concurrent.ExecutorCompletionService", List.of("submit"))
    );
    //</editor-fold>


    //<editor-fold desc="Execute command">
    /**
     * This map contains the methods which can execute commands. The map keys represent class names,
     * and the values are lists of method names that are considered to be command execution operations.
     */
    public static final Map<String, List<String>> methodsWhichCanExecuteCommands = Map.ofEntries(
            // java.lang
            Map.entry("java.lang.Runtime", List.of("exec")),
            Map.entry("java.lang.ProcessBuilder", List.of("start", "<init>"))
    );
    //</editor-fold>

}

