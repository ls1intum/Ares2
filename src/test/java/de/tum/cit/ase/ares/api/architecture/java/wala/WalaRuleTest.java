package de.tum.cit.ase.ares.api.architecture.java.wala;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.util.strings.Atom;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;

class WalaRuleTest {

	private static Locale originalDefault;
	private static Locale originalDisplay;

	@BeforeAll
	static void pinEnglishLocale() {
		originalDefault = Locale.getDefault();
		originalDisplay = Locale.getDefault(Locale.Category.DISPLAY);
		Locale.setDefault(Locale.ENGLISH);
		Locale.setDefault(Locale.Category.DISPLAY, Locale.ENGLISH);
	}

	@AfterAll
	static void restoreLocale() {
		Locale.setDefault(originalDefault);
		Locale.setDefault(Locale.Category.DISPLAY, originalDisplay);
	}

	// ----------------------------------------------------------------
	// Mock helpers
	// ----------------------------------------------------------------

	/**
	 * Creates a student-code CGNode classified as Application-loaded in the
	 * anonymous.Student class. isInfraFrame returns false for this node.
	 */
	private static CGNode studentNode(String signature) {
		CGNode node = mock(CGNode.class);
		IMethod method = mock(IMethod.class);
		IClass cls = mock(IClass.class);
		IClassLoader loader = mock(IClassLoader.class);
		when(node.getMethod()).thenReturn(method);
		when(method.getSignature()).thenReturn(signature);
		when(method.getDeclaringClass()).thenReturn(cls);
		when(cls.getClassLoader()).thenReturn(loader);
		when(loader.getReference()).thenReturn(ClassLoaderReference.Application);
		when(cls.getReference()).thenReturn(TypeReference.findOrCreate(ClassLoaderReference.Application,
				TypeName.findOrCreate("Lanonymous/Student;")));
		return node;
	}

	/**
	 * Creates a JDK (Primordial-loaded) CGNode suitable as the forbidden sink.
	 * Provides enough mocking for both isInfraFrame and WalaRule.check's
	 * declaringClass/lineNumber extraction.
	 */
	private static CGNode jdkForbiddenNode(String signature, String simpleClassName) {
		CGNode node = mock(CGNode.class);
		IMethod method = mock(IMethod.class);
		IClass cls = mock(IClass.class);
		IClassLoader loader = mock(IClassLoader.class);
		com.ibm.wala.types.TypeName typeName = mock(com.ibm.wala.types.TypeName.class);
		Atom classAtom = Atom.findOrCreateAsciiAtom(simpleClassName);
		when(node.getMethod()).thenReturn(method);
		when(method.getSignature()).thenReturn(signature);
		when(method.getDeclaringClass()).thenReturn(cls);
		try {
			Mockito.doReturn(null).when(method).getSourcePosition(0);
		} catch (com.ibm.wala.shrike.shrikeCT.InvalidClassFileException ignored) {
			// never thrown by a mock; satisfies compiler
		}
		when(cls.getClassLoader()).thenReturn(loader);
		when(loader.getReference()).thenReturn(ClassLoaderReference.Primordial);
		when(cls.getName()).thenReturn(typeName);
		when(typeName.getClassName()).thenReturn(classAtom);
		return node;
	}

	/**
	 * Creates an Ares-infra CGNode (Application-loaded but in de.tum.cit.ase.ares
	 * package). isInfraFrame returns true via INFRA_PREFIXES check.
	 */
	private static CGNode aresInfraNode(String signature) {
		CGNode node = mock(CGNode.class);
		IMethod method = mock(IMethod.class);
		IClass cls = mock(IClass.class);
		IClassLoader loader = mock(IClassLoader.class);
		when(node.getMethod()).thenReturn(method);
		when(method.getSignature()).thenReturn(signature);
		when(method.getDeclaringClass()).thenReturn(cls);
		when(cls.getClassLoader()).thenReturn(loader);
		when(loader.getReference()).thenReturn(ClassLoaderReference.Application);
		when(cls.getReference()).thenReturn(TypeReference.findOrCreate(ClassLoaderReference.Application,
				TypeName.findOrCreate("Lde/tum/cit/ase/ares/api/Helper;")));
		return node;
	}

	private static CallGraph buildMockCg(List<CGNode> path) {
		CallGraph cg = mock(CallGraph.class);
		final List<CGNode> nodes = new java.util.ArrayList<>(path);
		// WalaRule now iterates the call graph to find forbidden sinks and walks
		// predecessor edges for reverse reachability, so the mock must expose both the
		// node set and the reverse (predecessor) edges in addition to the successors.
		when(cg.iterator()).thenAnswer(inv -> nodes.iterator());
		when(cg.getSuccNodes(any())).thenAnswer(inv -> java.util.Collections.emptyIterator());
		when(cg.getPredNodes(any())).thenAnswer(inv -> java.util.Collections.emptyIterator());
		if (path.isEmpty()) {
			when(cg.getEntrypointNodes()).thenReturn(java.util.Collections.emptyList());
			return cg;
		}
		when(cg.getEntrypointNodes()).thenReturn(java.util.Collections.singletonList(path.get(0)));
		for (int i = 0; i < path.size() - 1; i++) {
			final CGNode child = path.get(i + 1);
			final CGNode parent = path.get(i);
			when(cg.getSuccNodes(parent)).thenAnswer(inv -> java.util.Collections.singletonList(child).iterator());
			when(cg.getPredNodes(child)).thenAnswer(inv -> java.util.Collections.singletonList(parent).iterator());
		}
		return cg;
	}

	private static AssertionError runAndExpectError(List<CGNode> path, WalaRule rule) {
		AssertionError thrown = null;
		try {
			rule.check(buildMockCg(path));
		} catch (AssertionError ae) {
			thrown = ae;
		}
		return thrown;
	}

	private static void runAndExpectNoError(List<CGNode> path, WalaRule rule) {
		rule.check(buildMockCg(path));
	}

	/**
	 * Realistic full WALA method signatures for every formerly-suppressed API. Each
	 * value begins with the corresponding ALLOWED_HELPER_APIS prefix literal.
	 */
	private static final Map<String, String[]> REALISTIC_SIGNATURES = Map.ofEntries(
			Map.entry("java.lang.Thread.<init>",
					new String[] { "java.lang.Thread.<init>(Ljava/lang/Runnable;)V", "Thread" }),
			Map.entry("java.lang.Thread.interrupt", new String[] { "java.lang.Thread.interrupt()V", "Thread" }),
			Map.entry("java.lang.ClassLoader.getSystemClassLoader",
					new String[] { "java.lang.ClassLoader.getSystemClassLoader()Ljava/lang/ClassLoader;",
							"ClassLoader" }),
			Map.entry("java.lang.ClassLoader.loadLibrary",
					new String[] { "java.lang.ClassLoader.loadLibrary(Ljava/lang/String;)V", "ClassLoader" }),
			Map.entry("java.lang.Runtime.load",
					new String[] { "java.lang.Runtime.load(Ljava/lang/String;)V", "Runtime" }),
			Map.entry("java.lang.Runtime.loadLibrary",
					new String[] { "java.lang.Runtime.loadLibrary(Ljava/lang/String;)V", "Runtime" }),
			Map.entry("java.io.File.getName", new String[] { "java.io.File.getName()Ljava/lang/String;", "File" }),
			Map.entry("java.lang.Class.forName",
					new String[] { "java.lang.Class.forName(Ljava/lang/String;)Ljava/lang/Class;", "Class" }),
			Map.entry("java.net.InetAddress.getAllByName",
					new String[] { "java.net.InetAddress.getAllByName(Ljava/lang/String;)[Ljava/net/InetAddress;",
							"InetAddress" }),
			Map.entry("java.lang.Thread.contextClassLoader",
					new String[] { "java.lang.Thread.contextClassLoader()Ljava/lang/ClassLoader;", "Thread" }),
			Map.entry("java.lang.Class.getDeclaredField",
					new String[] { "java.lang.Class.getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;",
							"Class" }),
			Map.entry("java.lang.reflect.Method.invoke",
					new String[] {
							"java.lang.reflect.Method.invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
							"Method" }),
			Map.entry("java.lang.Class.checkMemberAccess",
					new String[] { "java.lang.Class.checkMemberAccess(Ljava/lang/Class;I)V", "Class" }),
			Map.entry("java.lang.Thread.getContextClassLoader",
					new String[] { "java.lang.Thread.getContextClassLoader()Ljava/lang/ClassLoader;", "Thread" }),
			Map.entry("java.lang.Thread.getStackTrace",
					new String[] { "java.lang.Thread.getStackTrace()[Ljava/lang/StackTraceElement;", "Thread" }),
			Map.entry("java.io.File.<init>", new String[] { "java.io.File.<init>(Ljava/lang/String;)V", "File" }),
			Map.entry("java.lang.Class.getClassLoader",
					new String[] { "java.lang.Class.getClassLoader()Ljava/lang/ClassLoader;", "Class" }));

	// ----------------------------------------------------------------
	// Test 1: all-infra path does not fire
	// ----------------------------------------------------------------

	@Test
	void allInfraPathDoesNotThrow() {
		CGNode jdk1 = jdkForbiddenNode("java.io.RandomAccessFile.read([BII)I", "RandomAccessFile");
		CGNode jdk2 = jdkForbiddenNode("java.io.FileInputStream.read([BII)I", "FileInputStream");
		runAndExpectNoError(List.of(jdk1, jdk2),
				new WalaRule("Accesses file system", Set.of("java.io.RandomAccessFile.read")));
	}

	// ----------------------------------------------------------------
	// Test 2: student-rooted shallow path reports student caller
	// ----------------------------------------------------------------

	@Test
	void studentRootedShallowPathThrowsWithStudentCaller() {
		CGNode student = studentNode("anonymous.Student.callForbidden()V");
		CGNode forbidden = jdkForbiddenNode("java.lang.Class.forName(Ljava/lang/String;)Ljava/lang/Class;", "Class");
		AssertionError err = runAndExpectError(List.of(student, forbidden),
				new WalaRule("Manipulates class loading", Set.of("java.lang.Class.forName(Ljava/lang/String;)")));
		assertThat(err).isNotNull();
		// WalaRule renders signatures in ArchUnit source-form, dropping the JVM
		// return-type descriptor, so the caller appears as callForbidden() not ()V.
		assertThat(err.getMessage()).contains("Method <anonymous.Student.callForbidden()>");
	}

	// ----------------------------------------------------------------
	// Test 3: when two student frames are in the path, the one nearest to the
	// forbidden sink is reported (not the outer caller).
	// A student → JDK-intermediate → forbidden path is suppressed by
	// isFalsePositiveTransitivePath; to exercise nearestStudentFrame we use two
	// student nodes so the inner one is adjacent to the sink.
	// ----------------------------------------------------------------

	@Test
	void deepPathReportsNearestStudentFrameToForbiddenSink() {
		CGNode studentOuter = studentNode("anonymous.Student.outerCall()V");
		CGNode studentInner = studentNode("anonymous.Student.innerCall()V");
		CGNode forbidden = jdkForbiddenNode(
				"sun.nio.ch.DatagramChannelImpl.connect(Ljava/net/SocketAddress;Z)Ljava/nio/channels/DatagramChannel;",
				"DatagramChannelImpl");
		AssertionError err = runAndExpectError(List.of(studentOuter, studentInner, forbidden), new WalaRule(
				"Accesses network", Set.of("sun.nio.ch.DatagramChannelImpl.connect(Ljava/net/SocketAddress;Z)")));
		assertThat(err).isNotNull();
		// innerCall is the nearest student frame and must appear as the Method
		// <caller>,
		// rendered in ArchUnit source-form without the JVM return-type descriptor.
		assertThat(err.getMessage()).contains("Method <anonymous.Student.innerCall()>");
	}

	// ----------------------------------------------------------------
	// Test 4: student → project-test-helper (anonymous.toolclasses.) → forbidden
	// JDK.
	// The toolclasses helper is in INFRA_PREFIXES (so it is not reported as the
	// student frame) but is deliberately NOT in TRANSITIVE_FALSE_POSITIVE_PREFIXES.
	// A student routing into a forbidden API through such a helper is therefore a
	// REAL violation: the rule fires and attributes it to the nearest student
	// frame.
	// ----------------------------------------------------------------

	@Test
	void transitiveHelperPathToForbiddenFires() {
		CGNode studentA = studentNode("anonymous.Student.runTask()V");
		CGNode toolHelper = mock(CGNode.class);
		IMethod toolMethod = mock(IMethod.class);
		IClass toolCls = mock(IClass.class);
		IClassLoader toolLoader = mock(IClassLoader.class);
		when(toolHelper.getMethod()).thenReturn(toolMethod);
		when(toolMethod.getSignature()).thenReturn("anonymous.toolclasses.TaskHelper.run()V");
		when(toolMethod.getDeclaringClass()).thenReturn(toolCls);
		when(toolCls.getClassLoader()).thenReturn(toolLoader);
		when(toolLoader.getReference()).thenReturn(ClassLoaderReference.Application);
		when(toolCls.getReference()).thenReturn(TypeReference.findOrCreate(ClassLoaderReference.Application,
				TypeName.findOrCreate("Lanonymous/toolclasses/TaskHelper;")));
		CGNode forbidden = jdkForbiddenNode("java.lang.Thread.<init>(Ljava/lang/Runnable;)V", "Thread");

		AssertionError err = runAndExpectError(List.of(studentA, toolHelper, forbidden),
				new WalaRule("Manipulates threads", Set.of("java.lang.Thread.<init>(Ljava/lang/Runnable;)")));
		assertThat(err).as("path through a project test helper into a forbidden API must fire").isNotNull();
		assertThat(err.getMessage()).contains("Method <anonymous.Student.runTask()>");
	}

	// ----------------------------------------------------------------
	// Test 5: size==1 student-self path (callerIdx == size-1 fallback)
	// This pins the defensive branch: when the student frame IS the forbidden node,
	// we fall back to path[0] (entry == the same frame here), and the rule still
	// fires.
	// ----------------------------------------------------------------

	@Test
	void sizeOneSelfForbiddenStudentPathThrows() {
		// A student method is itself listed as forbidden. This is a theoretical edge
		// case
		// (production forbidden methods are always JDK), but we must pin the behaviour.
		CGNode selfForbidden = studentNode("anonymous.Student.selfForbidden()V");
		// Override the mock to also supply getName() for WalaRule.check's
		// declaringClass line
		IMethod selfMethod = selfForbidden.getMethod();
		IClass cls = selfMethod.getDeclaringClass();
		com.ibm.wala.types.TypeName typeName = mock(com.ibm.wala.types.TypeName.class);
		try {
			Mockito.doReturn(null).when(selfMethod).getSourcePosition(0);
		} catch (com.ibm.wala.shrike.shrikeCT.InvalidClassFileException ignored) {
			// never thrown by a mock; satisfies compiler
		}
		when(cls.getName()).thenReturn(typeName);
		when(typeName.getClassName()).thenReturn(Atom.findOrCreateAsciiAtom("Student"));

		AssertionError err = runAndExpectError(List.of(selfForbidden),
				new WalaRule("Test rule", Set.of("anonymous.Student.selfForbidden")));
		assertThat(err).as("size==1 student-self must fire AssertionError").isNotNull();
		// callerIdx == 0 == size-1: fallback uses path.get(0) which is the same frame.
		assertThat(err.getMessage()).contains("anonymous.Student.selfForbidden");

		SecurityException parsed = null;
		try {
			JavaArchitectureTestCase.parseErrorMessage(err);
		} catch (SecurityException se) {
			parsed = se;
		}
		assertThat(parsed).as("parseErrorMessage must produce a SecurityException").isNotNull();
	}

	// ----------------------------------------------------------------
	// Test 6: size==1 JDK-only path does not fire
	// ----------------------------------------------------------------

	@Test
	void sizeOneJdkOnlyPathDoesNotThrow() {
		CGNode jdkNode = jdkForbiddenNode("java.lang.Thread.<init>(Ljava/lang/Runnable;)V", "Thread");
		runAndExpectNoError(List.of(jdkNode),
				new WalaRule("Manipulates threads", Set.of("java.lang.Thread.<init>(Ljava/lang/Runnable;)")));
	}

	// ----------------------------------------------------------------
	// Test 7: student → JDK intermediate → Thread.<init>
	// isFalsePositiveTransitivePath suppresses this: Executors is Primordial
	// (infra), so every frame between student and Thread.<init> is infra.
	// In production, Executors itself would also be in the forbidden set, making
	// the 2-node [student, Executors] path fire directly without suppression.
	// This test pins the suppression behaviour for the 3-node path.
	// ----------------------------------------------------------------

	@Test
	void transitiveJdkExecutorToThreadIsSuppressed() {
		CGNode student = studentNode("anonymous.Student.callForbidden()V");
		CGNode executors = jdkForbiddenNode(
				"java.util.concurrent.Executors.newCachedThreadPool()Ljava/util/concurrent/ExecutorService;",
				"Executors");
		CGNode thread = jdkForbiddenNode("java.lang.Thread.<init>(Ljava/lang/Runnable;)V", "Thread");
		runAndExpectNoError(List.of(student, executors, thread),
				new WalaRule("Manipulates threads", Set.of("java.lang.Thread.<init>(Ljava/lang/Runnable;)")));
	}

	// ----------------------------------------------------------------
	// Test 8: parameterised over all 17 formerly-suppressed APIs
	// Each variant uses a realistic full WALA signature beginning with the literal.
	// ----------------------------------------------------------------

	@ParameterizedTest
	@ValueSource(strings = { "java.lang.Thread.<init>", "java.lang.Thread.interrupt",
			"java.lang.ClassLoader.getSystemClassLoader", "java.lang.ClassLoader.loadLibrary", "java.lang.Runtime.load",
			"java.lang.Runtime.loadLibrary", "java.io.File.getName", "java.lang.Class.forName",
			"java.net.InetAddress.getAllByName", "java.lang.Thread.contextClassLoader",
			"java.lang.Class.getDeclaredField", "java.lang.reflect.Method.invoke", "java.lang.Class.checkMemberAccess",
			"java.lang.Thread.getContextClassLoader", "java.lang.Thread.getStackTrace", "java.io.File.<init>",
			"java.lang.Class.getClassLoader" })
	void formerlyAllowedApiFromStudentCodeThrows(String apiPrefix) {
		String[] sigAndClass = REALISTIC_SIGNATURES.get(apiPrefix);
		String fullSig = sigAndClass[0];
		String simpleClass = sigAndClass[1];

		CGNode student = studentNode("anonymous.Student.callApi()V");
		CGNode forbidden = jdkForbiddenNode(fullSig, simpleClass);

		AssertionError err = runAndExpectError(List.of(student, forbidden),
				new WalaRule("Test rule", Set.of(apiPrefix)));
		assertThat(err).as("formerly-suppressed API '%s' from student code must now fire", apiPrefix).isNotNull();
		// WalaRule renders signatures in ArchUnit source-form (no JVM descriptor /
		// return
		// type), so assert on the descriptor-free caller and the forbidden class+method
		// name rather than the raw WALA signature held in REALISTIC_SIGNATURES.
		String forbiddenName = fullSig.substring(0, fullSig.indexOf('('));
		assertThat(err.getMessage()).contains("Method <anonymous.Student.callApi()>")
				.contains("calls method <" + forbiddenName + "(");
	}

	// ----------------------------------------------------------------
	// Test 9: infra-only path to a formerly-suppressed API must NOT fire
	// Codifies that the new infra-frame filter replaces wrapIgnoringJdkHelpers.
	// ----------------------------------------------------------------

	@Test
	void infraOnlyPathToFormerlyAllowedApiDoesNotThrow() {
		CGNode aresHelper = aresInfraNode("de.tum.cit.ase.ares.api.SomeHelper.checkClass(Ljava/lang/Class;)V");
		CGNode forbidden = jdkForbiddenNode("java.lang.Class.forName(Ljava/lang/String;)Ljava/lang/Class;", "Class");
		runAndExpectNoError(List.of(aresHelper, forbidden),
				new WalaRule("Manipulates class loading", Set.of("java.lang.Class.forName")));
	}

	// ----------------------------------------------------------------
	// Test 10: callerIdx == size-1 fallback — student IS the forbidden node
	// ----------------------------------------------------------------

	@Test
	void callerIdxEqualsSizeMinus1FallbackTest() {
		// When nearestStudentFrame returns size-1 (student is the forbidden node),
		// WalaRule falls back to path[0] as callerSignature. For size==1 this is the
		// same frame, so callerSig == forbiddenSig == studentSig.
		CGNode selfForbidden = studentNode("anonymous.Student.selfForbidden()V");
		IMethod selfMethod2 = selfForbidden.getMethod();
		IClass cls2 = selfMethod2.getDeclaringClass();
		com.ibm.wala.types.TypeName typeName2 = mock(com.ibm.wala.types.TypeName.class);
		try {
			Mockito.doReturn(null).when(selfMethod2).getSourcePosition(0);
		} catch (com.ibm.wala.shrike.shrikeCT.InvalidClassFileException ignored) {
			// never thrown by a mock; satisfies compiler
		}
		when(cls2.getName()).thenReturn(typeName2);
		when(typeName2.getClassName()).thenReturn(Atom.findOrCreateAsciiAtom("Student"));

		AssertionError err = runAndExpectError(List.of(selfForbidden),
				new WalaRule("Test rule", Set.of("anonymous.Student.selfForbidden")));
		assertThat(err).isNotNull();
		assertThat(err.getMessage()).contains("anonymous.Student.selfForbidden");
	}

	// ----------------------------------------------------------------
	// Test 11: de-masking — two student callers of ONE forbidden sink, one
	// allow-listed and one not. The previous global-visited DFS reported each sink
	// on a single path, so if the allow-listed caller was discovered first the real
	// violation by the non-allowed caller was silently masked. Reverse reachability
	// must examine both approaches and still report the non-allowed caller.
	// ----------------------------------------------------------------

	/**
	 * Student CGNode in an arbitrary declaring class (for distinct allow-list
	 * identities). {@code walaType} is the WALA class TypeName form WITHOUT a
	 * trailing {@code ';'} (e.g. {@code Lanonymous/Allowed}), matching what real
	 * {@code IClass.getName().toString()} returns.
	 */
	private static CGNode studentNodeInClass(String signature, String walaType) {
		CGNode node = mock(CGNode.class);
		IMethod method = mock(IMethod.class);
		IClass cls = mock(IClass.class);
		IClassLoader loader = mock(IClassLoader.class);
		when(node.getMethod()).thenReturn(method);
		when(method.getSignature()).thenReturn(signature);
		when(method.getDeclaringClass()).thenReturn(cls);
		when(cls.getClassLoader()).thenReturn(loader);
		when(loader.getReference()).thenReturn(ClassLoaderReference.Application);
		when(cls.getReference()).thenReturn(
				TypeReference.findOrCreate(ClassLoaderReference.Application, TypeName.findOrCreate(walaType)));
		when(cls.getName()).thenReturn(TypeName.findOrCreate(walaType));
		return node;
	}

	@Test
	void allowListedCallerDoesNotMaskNonAllowedCallerOfSameSink() {
		CGNode allowedCaller = studentNodeInClass("anonymous.Allowed.help()V", "Lanonymous/Allowed");
		CGNode evilCaller = studentNodeInClass("anonymous.Evil.attack()V", "Lanonymous/Evil");
		CGNode forbidden = jdkForbiddenNode("java.lang.Class.forName(Ljava/lang/String;)Ljava/lang/Class;", "Class");

		CallGraph cg = mock(CallGraph.class);
		List<CGNode> nodes = List.of(allowedCaller, evilCaller, forbidden);
		when(cg.iterator()).thenAnswer(inv -> nodes.iterator());
		when(cg.getEntrypointNodes()).thenReturn(List.of(allowedCaller, evilCaller));
		when(cg.getSuccNodes(any())).thenAnswer(inv -> java.util.Collections.emptyIterator());
		when(cg.getPredNodes(any())).thenAnswer(inv -> java.util.Collections.emptyIterator());
		when(cg.getSuccNodes(allowedCaller)).thenAnswer(inv -> List.of(forbidden).iterator());
		when(cg.getSuccNodes(evilCaller)).thenAnswer(inv -> List.of(forbidden).iterator());
		when(cg.getPredNodes(forbidden)).thenAnswer(inv -> List.of(allowedCaller, evilCaller).iterator());

		WalaRule rule = new WalaRule("Manipulates class loading",
				Set.of("java.lang.Class.forName(Ljava/lang/String;)"));

		AssertionError thrown = null;
		try {
			rule.check(cg, Set.of(new ClassPermission("anonymous.Allowed")));
		} catch (AssertionError ae) {
			thrown = ae;
		}
		assertThat(thrown).as("non-allowed caller of a shared sink must not be masked by an allow-listed caller")
				.isNotNull();
		assertThat(thrown.getMessage()).contains("Method <anonymous.Evil.attack()>");
	}

	// ----------------------------------------------------------------
	// Entry-set fail-closed: a forbidden sink with no entry points must not pass
	// ----------------------------------------------------------------

	@Test
	void emptyEntryPointsWithForbiddenSinkFailsClosed() {
		CGNode sink = jdkForbiddenNode("java.io.RandomAccessFile.read([BII)I", "RandomAccessFile");
		CallGraph cg = mock(CallGraph.class);
		final List<CGNode> nodes = List.of(sink);
		when(cg.iterator()).thenAnswer(inv -> nodes.iterator());
		when(cg.getEntrypointNodes()).thenReturn(java.util.Collections.emptyList());
		when(cg.getSuccNodes(any())).thenAnswer(inv -> java.util.Collections.emptyIterator());
		when(cg.getPredNodes(any())).thenAnswer(inv -> java.util.Collections.emptyIterator());

		WalaRule rule = new WalaRule("Accesses file system", Set.of("java.io.RandomAccessFile.read"));
		boolean failedClosed = false;
		try {
			rule.check(cg);
		} catch (SecurityException expected) {
			failedClosed = true;
		}
		assertThat(failedClosed).as("a forbidden sink with no entry points must fail closed, not silently pass")
				.isTrue();
	}
}
