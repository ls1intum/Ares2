package de.tum.cit.ase.ares.api.aop.java.aspectj;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;

/**
 * Checks that an AspectJ run refuses the constructs the woven aspects cannot
 * see, method references, calls through super and super constructors, and still
 * accepts the direct calls and lambdas AspectJ does check.
 */
class JavaAspectJUnobservableAccessCheckTest {

	/** The checked file methods. */
	private static final List<Path> FILES = List.of(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS);

	/** The checked network methods. */
	private static final List<Path> NETWORK = List.of(FileHandlerConstants.ARCHUNIT_NETWORK_METHODS);

	/** The checked thread methods. */
	private static final List<Path> THREADS = List.of(FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS);

	/** A folder for the classes this test compiles. */
	@TempDir
	Path tempDir;

	/**
	 * Compiles one class with javac and reads every class in the folder.
	 *
	 * @param simpleName the class name in package {@code example}
	 * @param body       the class declaration without package line
	 * @return the classes compiled so far
	 */
	private JavaClasses compile(String simpleName, String body) throws IOException {
		Path source = tempDir.resolve("src/example/" + simpleName + ".java");
		Files.createDirectories(source.getParent());
		Files.writeString(source, "package example;\n" + body, StandardCharsets.UTF_8);
		Path classes = tempDir.resolve("classes");
		Files.createDirectories(classes);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		ByteArrayOutputStream messages = new ByteArrayOutputStream();
		int exitCode = compiler.run(null, messages, messages, "-d", classes.toString(), source.toString());
		assertEquals(0, exitCode, messages.toString(StandardCharsets.UTF_8));
		return new ClassFileImporter().importPath(classes);
	}

	/**
	 * Runs the check with the given lists for references and super calls, and the
	 * same lists for super constructors.
	 *
	 * @param classes the classes
	 * @param lists   the method lists
	 * @return the refusal
	 */
	private static SecurityException refused(JavaClasses classes, List<Path> lists) {
		return assertThrows(SecurityException.class,
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), lists, lists));
	}

	/** A method reference to a checked file method is refused and named. */
	@Test
	void aMethodReferenceToAFileMethodIsRefused() throws IOException {
		JavaClasses classes = compile("Ref", """
				public class Ref {
					interface Io { String read(java.nio.file.Path p) throws java.io.IOException; }
					static String go() throws Exception {
						Io f = java.nio.file.Files::readString;
						return f.read(java.nio.file.Path.of("x"));
					}
				}
				""");
		SecurityException refusal = refused(classes, FILES);
		assertTrue(refusal.getMessage().contains("example.Ref") && refusal.getMessage().contains("readString"),
				refusal.getMessage());
	}

	/** A constructor reference to a checked file constructor is refused. */
	@Test
	void aConstructorReferenceToAFileStreamIsRefused() throws IOException {
		JavaClasses classes = compile("CtorRef", """
				public class CtorRef {
					interface Open { java.io.FileInputStream open(String s) throws java.io.IOException; }
					static Object go() throws Exception {
						Open o = java.io.FileInputStream::new;
						return o.open("x");
					}
				}
				""");
		assertTrue(refused(classes, FILES).getMessage().contains("java.io.FileInputStream"));
	}

	/**
	 * The same read in a lambda or as a direct call, which AspectJ checks, passes.
	 */
	@Test
	void aLambdaAndADirectCallPass() throws IOException {
		JavaClasses classes = compile("Lambda",
				"""
						public class Lambda {
							interface Io { String read(java.nio.file.Path p) throws java.io.IOException; }
							static String go() throws Exception {
								Io f = p -> java.nio.file.Files.readString(p);
								return f.read(java.nio.file.Path.of("x")) + java.nio.file.Files.readString(java.nio.file.Path.of("y"));
							}
						}
						""");
		assertDoesNotThrow(
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), FILES, FILES));
	}

	/** A call through super to a checked method of File is refused. */
	@Test
	void aSuperCallToAFileMethodIsRefused() throws IOException {
		JavaClasses classes = compile("Lister", """
				public class Lister extends java.io.File {
					public Lister(String p) { super(p); }
					public String[] names() { return super.list(); }
				}
				""");
		SecurityException refusal = assertThrows(SecurityException.class,
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), FILES, List.of()));
		assertTrue(refusal.getMessage().contains("java.io.File.list"), refusal.getMessage());
	}

	/**
	 * An override handing on to the method it overrides passes, since AspectJ sees
	 * every call of the override, and so does a thread's start override.
	 */
	@Test
	void anOverrideHandingOnToSuperPasses() throws IOException {
		JavaClasses classes = compile("Delegating", """
				public class Delegating extends java.io.File {
					public Delegating(String p) { super(p); }
					@Override public String[] list() { return super.list(); }
				}
				""");
		assertDoesNotThrow(
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), FILES, List.of()));
		JavaClasses worker = compile("Starting", """
				public class Starting extends Thread {
					@Override public synchronized void start() { super.start(); }
				}
				""");
		assertDoesNotThrow(
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(worker, List.of(), THREADS, List.of()));
	}

	/** The same method called on an object, which AspectJ checks, passes. */
	@Test
	void aCallOnAnObjectInASubclassPasses() throws IOException {
		JavaClasses classes = compile("Other", """
				public class Other extends java.io.File {
					public Other(String p) { super(p); }
					public String[] names() { return new java.io.File(".").list(); }
					public String[] own() { return list(); }
				}
				""");
		assertDoesNotThrow(
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), FILES, List.of()));
	}

	/**
	 * A subclass reaching a file stream constructor through super(...) is refused.
	 */
	@Test
	void aSuperConstructorOfAFileStreamIsRefused() throws IOException {
		JavaClasses classes = compile("Evil", """
				public class Evil extends java.io.FileInputStream {
					public Evil() throws java.io.FileNotFoundException { super("x"); }
				}
				""");
		assertTrue(refused(classes, FILES).getMessage().contains("java.io.FileInputStream.<init>"));
	}

	/**
	 * A subclass of a JDK class whose constructor without arguments is listed, as a
	 * plain Reader's is, passes: that constructor opens nothing.
	 */
	@Test
	void aSubclassCallingAConstructorWithoutArgumentsPasses() throws IOException {
		JavaClasses classes = compile("OwnReader", """
				public class OwnReader extends java.io.Reader {
					@Override public int read(char[] b, int o, int l) { return -1; }
					@Override public void close() { }
				}
				""");
		assertDoesNotThrow(
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), FILES, FILES));
	}

	/**
	 * A thread subclass is accepted with the thread list, since its constructor
	 * call is seen as {@code new Worker()}, but a start through super is refused,
	 * and so is a reference to Thread.start.
	 */
	@Test
	void aThreadSubclassPassesButSuperStartAndAStartReferenceAreRefused() throws IOException {
		JavaClasses worker = compile("Worker", """
				public class Worker extends Thread {
					@Override public void run() { Math.sqrt(2); }
				}
				""");
		assertDoesNotThrow(
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(worker, List.of(), THREADS, List.of()));
		JavaClasses sneaky = compile("Sneaky", """
				public class Sneaky extends Thread {
					public void go() { super.start(); }
				}
				""");
		assertThrows(SecurityException.class,
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(sneaky, List.of(), THREADS, List.of()));
		JavaClasses starter = compile("Starter", """
				public class Starter {
					static void go(java.util.List<Thread> ts) { ts.forEach(Thread::start); }
				}
				""");
		assertThrows(SecurityException.class,
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(starter, List.of(), THREADS, List.of()));
	}

	/**
	 * A lambda in a thread subclass passes: its body is a method of that class,
	 * woven like the rest of it, although ArchUnit lists it as a reference.
	 */
	@Test
	void aLambdaInAThreadSubclassPasses() throws IOException {
		JavaClasses classes = compile("Lambdas", """
				public class Lambdas extends Thread {
					@Override public void run() { Runnable r = () -> Math.sqrt(2); r.run(); }
				}
				""");
		assertDoesNotThrow(
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), THREADS, List.of()));
	}

	/**
	 * A call through super to a method the student's own superclass declares
	 * passes.
	 */
	@Test
	void aSuperCallToAMethodOfAStudentClassPasses() throws IOException {
		JavaClasses classes = compile("Family", """
				public class Family {
					static class Parent extends Thread { void helper() { Math.sqrt(2); } }
					static class Child extends Parent { void go() { super.helper(); } }
				}
				""");
		assertDoesNotThrow(
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), THREADS, List.of()));
	}

	/**
	 * A student overload of the same name does not hide a reference to the
	 * inherited checked method.
	 */
	@Test
	void anOverloadDoesNotHideAReferenceToTheInheritedMethod() throws IOException {
		JavaClasses classes = compile("Overloading", """
				public class Overloading extends java.io.File {
					public Overloading(String p) { super(p); }
					public String[] list(int ignored) { return new String[0]; }
					public Object names() { java.util.function.Supplier<String[]> s = this::list; return s.get(); }
				}
				""");
		assertThrows(SecurityException.class,
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), FILES, List.of()));
	}

	/** A constructor reference to a socket is refused with the network list. */
	@Test
	void aSocketConstructorReferenceIsRefused() throws IOException {
		JavaClasses classes = compile("Net", """
				public class Net {
					interface Open { java.net.Socket open(String h, int p) throws java.io.IOException; }
					static Object go() throws Exception {
						Open o = java.net.Socket::new;
						return o.open("127.0.0.1", 80);
					}
				}
				""");
		assertTrue(refused(classes, NETWORK).getMessage().contains("java.net.Socket.<init>"));
	}

	/**
	 * Without a list, which means no area is decided at runtime, nothing is
	 * refused.
	 */
	@Test
	void withoutAListNothingIsRefused() throws IOException {
		JavaClasses classes = compile("Evil", """
				public class Evil extends java.io.FileInputStream {
					public Evil() throws java.io.FileNotFoundException { super("x"); }
				}
				""");
		assertDoesNotThrow(
				() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of(), List.of(), List.of()));
	}

	/** An exempted class, such as a test class, is not checked. */
	@Test
	void anExemptedClassIsNotChecked() throws IOException {
		JavaClasses classes = compile("Evil", """
				public class Evil extends java.io.FileInputStream {
					public Evil() throws java.io.FileNotFoundException { super("x"); }
				}
				""");
		assertDoesNotThrow(() -> JavaAspectJUnobservableAccessCheck.requireObservable(classes, List.of("example.Evil"),
				FILES, FILES));
	}
}
