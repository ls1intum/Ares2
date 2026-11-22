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
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;

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
	 * org.example.Student class. isInfraFrame returns false for this node.
	 */
	private static CGNode applicationNode(String signature, String walaType) {
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
		return node;
	}

	private static CGNode studentNode(String signature) {
		return applicationNode(signature, "Lorg/example/Student;");
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
		return applicationNode(signature, "Lde/tum/cit/ase/ares/api/Helper;");
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

	// ----------------------------------------------------------------
	// A path through a helper class of the exercise.
	//
	// A helper class is code the student is examined with, whatever its package is
	// called, so it is judged like any other student frame. Only a path made of
	// JDK or framework frames alone is dropped silently, as the test below pins.
	// ----------------------------------------------------------------

	/**
	 * A helper class that calls the forbidden method itself is reported, and the
	 * report names the helper.
	 */
	@Test
	void aHelperThatCallsTheSinkItselfIsReported() {
		CGNode helper = applicationNode("org.example.helpers.Helper.run()V", "Lorg/example/helpers/Helper;");
		CGNode forbidden = jdkForbiddenNode("java.lang.Thread.<init>(Ljava/lang/Runnable;)V", "Thread");

		AssertionError err = runAndExpectError(List.of(helper, forbidden),
				new WalaRule("Manipulates threads", Set.of("java.lang.Thread.<init>(Ljava/lang/Runnable;)")));

		assertThat(err).as("a helper class of the exercise must not hide its own forbidden call").isNotNull();
		assertThat(err.getMessage()).contains("Method <org.example.helpers.Helper.run()>");
	}

	@Test
	void allInfrastructurePathEnteredByLoaderIsStillDropped() {
		// Nothing here is application-loaded, so supervised code could not have put
		// itself on this path and there is nothing to refuse. This must stay a silent
		// drop, or every synthetic WALA root would become a failure.
		CGNode entry = jdkForbiddenNode("java.lang.Thread.run()V", "Thread");
		CGNode forbidden = jdkForbiddenNode("java.lang.Thread.<init>(Ljava/lang/Runnable;)V", "Thread");
		WalaRule rule = new WalaRule("Manipulates threads", Set.of("java.lang.Thread.<init>(Ljava/lang/Runnable;)"));

		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> rule.check(buildMockCg(List.of(entry, forbidden))));
	}

	/**
	 * Two ways into one sink: a student call through Ares code, which is a harmless
	 * detour and reports nothing, and a helper class calling the sink itself. The
	 * harmless branch must not excuse the helper. The predecessors list the helper
	 * first, so the walk, a stack, evaluates the harmless branch before it.
	 */
	@Test
	void aHarmlessBranchDoesNotExcuseAHelperBranchIntoTheSameSink() {
		CGNode student = studentNode("org.example.Student.run()V");
		CGNode aresInfra = aresInfraNode("de.tum.cit.ase.ares.api.Helper.help()V");
		CGNode helper = applicationNode("org.example.helpers.Helper.run()V", "Lorg/example/helpers/Helper;");
		CGNode forbidden = jdkForbiddenNode("java.lang.Thread.<init>(Ljava/lang/Runnable;)V", "Thread");

		java.util.Map<CGNode, List<CGNode>> successors = new java.util.HashMap<>();
		successors.put(student, List.of(aresInfra));
		successors.put(aresInfra, List.of(forbidden));
		successors.put(helper, List.of(forbidden));
		java.util.Map<CGNode, List<CGNode>> predecessors = new java.util.HashMap<>();
		predecessors.put(aresInfra, List.of(student));
		predecessors.put(forbidden, List.of(helper, aresInfra));

		CallGraph cg = buildBranchingCg(List.of(student, aresInfra, helper, forbidden), List.of(student, helper),
				successors, predecessors);
		WalaRule rule = new WalaRule("Manipulates threads", Set.of("java.lang.Thread.<init>(Ljava/lang/Runnable;)"));

		AssertionError reported = org.junit.jupiter.api.Assertions.assertThrows(AssertionError.class,
				() -> rule.check(cg), "a harmless branch must not excuse a parallel helper branch");

		assertThat(reported.getMessage()).contains("Method <org.example.helpers.Helper.run()>");
	}

	/**
	 * A call graph with more than one way into a node, which the linear
	 * {@link #buildMockCg(List)} cannot express.
	 */
	private static CallGraph buildBranchingCg(List<CGNode> allNodes, List<CGNode> entrypoints,
			java.util.Map<CGNode, List<CGNode>> successors, java.util.Map<CGNode, List<CGNode>> predecessors) {
		CallGraph cg = mock(CallGraph.class);
		when(cg.iterator()).thenAnswer(inv -> new java.util.ArrayList<>(allNodes).iterator());
		when(cg.getEntrypointNodes()).thenReturn(entrypoints);
		when(cg.getSuccNodes(any())).thenAnswer(
				inv -> new java.util.ArrayList<>(successors.getOrDefault(inv.getArgument(0), List.of())).iterator());
		when(cg.getPredNodes(any())).thenAnswer(
				inv -> new java.util.ArrayList<>(predecessors.getOrDefault(inv.getArgument(0), List.of())).iterator());
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

	@Test
	void compilerGeneratedLambdaMetafactoryNodeIsNotReportedAsReflection() {
		CGNode student = studentNode("org.example.Student.lambda()V");
		CGNode bootstrap = jdkForbiddenNode(
				"java.lang.invoke.LambdaMetafactory.call$org$example$Student$0()Ljava/lang/Runnable;",
				"LambdaMetafactory");
		runAndExpectNoError(List.of(student, bootstrap), new WalaRule("Uses reflection", Set.of("java.lang.invoke")));
	}

	// ----------------------------------------------------------------
	// Test 2: student-rooted shallow path reports student caller
	// ----------------------------------------------------------------

	@Test
	void studentRootedShallowPathThrowsWithStudentCaller() {
		CGNode student = studentNode("org.example.Student.callForbidden()V");
		CGNode forbidden = jdkForbiddenNode("java.lang.Class.forName(Ljava/lang/String;)Ljava/lang/Class;", "Class");
		AssertionError err = runAndExpectError(List.of(student, forbidden),
				new WalaRule("Manipulates class loading", Set.of("java.lang.Class.forName(Ljava/lang/String;)")));
		assertThat(err).isNotNull();
		// WalaRule renders signatures in ArchUnit source-form, dropping the JVM
		// return-type descriptor, so the caller appears as callForbidden() not ()V.
		assertThat(err.getMessage()).contains("Method <org.example.Student.callForbidden()>");
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
		CGNode studentOuter = studentNode("org.example.Student.outerCall()V");
		CGNode studentInner = studentNode("org.example.Student.innerCall()V");
		CGNode forbidden = jdkForbiddenNode(
				"sun.nio.ch.DatagramChannelImpl.connect(Ljava/net/SocketAddress;Z)Ljava/nio/channels/DatagramChannel;",
				"DatagramChannelImpl");
		AssertionError err = runAndExpectError(List.of(studentOuter, studentInner, forbidden), new WalaRule(
				"Accesses network", Set.of("sun.nio.ch.DatagramChannelImpl.connect(Ljava/net/SocketAddress;Z)")));
		assertThat(err).isNotNull();
		// innerCall is the nearest student frame and must appear as the Method
		// <caller>,
		// rendered in ArchUnit source-form without the JVM return-type descriptor.
		assertThat(err.getMessage()).contains("Method <org.example.Student.innerCall()>");
	}

	// ----------------------------------------------------------------
	// Test 4: student → helper class of the exercise → forbidden JDK method.
	// The helper is a student frame like any other, so the violation is reported
	// and blamed on the helper, the frame closest to the forbidden call.
	// ----------------------------------------------------------------

	/**
	 * A student call that reaches the forbidden method through a helper class is
	 * reported, and the report names the helper as the caller.
	 */
	@Test
	void transitiveHelperPathToForbiddenFires() {
		CGNode studentA = studentNode("org.example.Student.runTask()V");
		CGNode toolHelper = applicationNode("org.example.helpers.TaskHelper.run()V",
				"Lorg/example/helpers/TaskHelper;");
		CGNode forbidden = jdkForbiddenNode("java.lang.Thread.<init>(Ljava/lang/Runnable;)V", "Thread");

		AssertionError err = runAndExpectError(List.of(studentA, toolHelper, forbidden),
				new WalaRule("Manipulates threads", Set.of("java.lang.Thread.<init>(Ljava/lang/Runnable;)")));
		assertThat(err).as("path through a helper class into a forbidden API must fire").isNotNull();
		assertThat(err.getMessage()).contains("Method <org.example.helpers.TaskHelper.run()>");
	}

	/**
	 * A path through a helper class is judged from the helper on, exactly as if the
	 * helper had started it: a helper that reaches the forbidden method only
	 * through JDK code is the same harmless detour as a student class doing so.
	 */
	@Test
	void aPathThroughAHelperIsJudgedLikeOneStartingAtTheHelper() {
		CGNode student = studentNode("org.example.Student.runTask()V");
		CGNode helper = applicationNode("org.example.helpers.TaskHelper.run()V", "Lorg/example/helpers/TaskHelper;");
		CGNode executors = jdkForbiddenNode(
				"java.util.concurrent.Executors.newCachedThreadPool()Ljava/util/concurrent/ExecutorService;",
				"Executors");
		CGNode thread = jdkForbiddenNode("java.lang.Thread.<init>(Ljava/lang/Runnable;)V", "Thread");
		WalaRule rule = new WalaRule("Manipulates threads", Set.of("java.lang.Thread.<init>(Ljava/lang/Runnable;)"));

		runAndExpectNoError(List.of(helper, executors, thread), rule);
		runAndExpectNoError(List.of(student, helper, executors, thread), rule);
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
		CGNode selfForbidden = studentNode("org.example.Student.selfForbidden()V");
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
				new WalaRule("Test rule", Set.of("org.example.Student.selfForbidden")));
		assertThat(err).as("size==1 student-self must fire AssertionError").isNotNull();
		// callerIdx == 0 == size-1: fallback uses path.get(0) which is the same frame.
		assertThat(err.getMessage()).contains("org.example.Student.selfForbidden");

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
		CGNode student = studentNode("org.example.Student.callForbidden()V");
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

	/**
	 * An API that student code once could call unreported now raises a violation,
	 * whose text names caller and target and opens with the Ares error template.
	 *
	 * @param apiPrefix the forbidden API
	 */
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

		CGNode student = studentNode("org.example.Student.callApi()V");
		CGNode forbidden = jdkForbiddenNode(fullSig, simpleClass);

		AssertionError err = runAndExpectError(List.of(student, forbidden),
				new WalaRule("Test rule", Set.of(apiPrefix)));
		assertThat(err).as("formerly-suppressed API '%s' from student code must now fire", apiPrefix).isNotNull();
		// WalaRule renders signatures in ArchUnit source-form (no JVM descriptor /
		// return
		// type), so assert on the descriptor-free caller and the forbidden class+method
		// name rather than the raw WALA signature held in REALISTIC_SIGNATURES.
		String forbiddenName = fullSig.substring(0, fullSig.indexOf('('));
		assertThat(err.getMessage()).contains("Method <org.example.Student.callApi()>")
				.contains("calls method <" + forbiddenName + "(");
		assertThat(err.getMessage()).matches("(?s)(Ares Security Error \\(Reason: Student-Code; Stage: Execution\\)"
				+ "|Ares Sicherheitsfehler \\(Grund: Student-Code; Phase: Ausf\u00fchrung\\)): '.*");
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
		CGNode selfForbidden = studentNode("org.example.Student.selfForbidden()V");
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
				new WalaRule("Test rule", Set.of("org.example.Student.selfForbidden")));
		assertThat(err).isNotNull();
		assertThat(err.getMessage()).contains("org.example.Student.selfForbidden");
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
	 * trailing {@code ';'} (e.g. {@code Lorg/example/Allowed}), matching what real
	 * {@code IClass.getName().toString()} returns.
	 */
	private static CGNode studentNodeInClass(String signature, String walaType) {
		CGNode node = applicationNode(signature, walaType);
		IClass cls = node.getMethod().getDeclaringClass();
		when(cls.getName()).thenReturn(TypeName.findOrCreate(walaType));
		return node;
	}

	@Test
	void allowListedCallerDoesNotMaskNonAllowedCallerOfSameSink() {
		CGNode allowedCaller = studentNodeInClass("org.example.Allowed.help()V", "Lorg/example/Allowed");
		CGNode evilCaller = studentNodeInClass("org.example.Evil.attack()V", "Lorg/example/Evil");
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
			rule.check(cg, Set.of(new ClassPermission("org.example.Allowed")));
		} catch (AssertionError ae) {
			thrown = ae;
		}
		assertThat(thrown).as("non-allowed caller of a shared sink must not be masked by an allow-listed caller")
				.isNotNull();
		assertThat(thrown.getMessage()).contains("Method <org.example.Evil.attack()>");
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

	@Test
	void emptyEntryPointsWithoutDiscoveredSinkStillFailClosed() {
		CallGraph cg = mock(CallGraph.class);
		when(cg.iterator()).thenAnswer(inv -> java.util.Collections.emptyIterator());
		when(cg.getEntrypointNodes()).thenReturn(java.util.Collections.emptyList());

		WalaRule rule = new WalaRule("Accesses file system", Set.of("java.io.RandomAccessFile.read"));
		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, () -> rule.check(cg));
	}

	@Test
	void approachSafetyLimitFailsClosed() {
		CGNode sink = jdkForbiddenNode("java.lang.Class.forName(Ljava/lang/String;)Ljava/lang/Class;", "Class");
		List<CGNode> callers = new java.util.ArrayList<>();
		Set<ClassPermission> allowedClasses = new java.util.HashSet<>();
		for (int i = 0; i < 64; i++) {
			String className = "org.example.Allowed" + i;
			callers.add(studentNodeInClass(className + ".help()V", "L" + className.replace('.', '/')));
			allowedClasses.add(new ClassPermission(className));
		}

		CallGraph cg = mock(CallGraph.class);
		List<CGNode> nodes = new java.util.ArrayList<>(callers);
		nodes.add(sink);
		when(cg.iterator()).thenAnswer(inv -> nodes.iterator());
		when(cg.getEntrypointNodes()).thenReturn(callers);
		when(cg.getSuccNodes(any())).thenAnswer(inv -> java.util.Collections.emptyIterator());
		when(cg.getPredNodes(any())).thenAnswer(inv -> java.util.Collections.emptyIterator());
		for (CGNode caller : callers) {
			when(cg.getSuccNodes(caller)).thenAnswer(inv -> List.of(sink).iterator());
		}
		when(cg.getPredNodes(sink)).thenAnswer(inv -> callers.iterator());

		WalaRule rule = new WalaRule("Manipulates class loading",
				Set.of("java.lang.Class.forName(Ljava/lang/String;)"));
		SecurityException exception = org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class,
				() -> rule.check(cg, allowedClasses));
		assertThat(exception.getMessage()).contains("safety limit");
	}

	@Test
	void inheritedTargetUsesOnlyImportedHierarchy() {
		JavaAccess<?> access = mock(JavaAccess.class);
		AccessTarget target = mock(AccessTarget.class);
		JavaClass targetOwner = mock(JavaClass.class);
		JavaClass collection = mock(JavaClass.class);
		when(target.getFullName()).thenReturn("example.CustomList.parallelStream()");
		Mockito.doReturn(target).when(access).getTarget();
		when(access.getTargetOwner()).thenReturn(targetOwner);
		when(targetOwner.getAllClassesSelfIsAssignableTo()).thenReturn(Set.of(targetOwner, collection));
		when(targetOwner.isFullyImported()).thenReturn(true);
		when(collection.isFullyImported()).thenReturn(true);
		when(targetOwner.getFullName()).thenReturn("example.CustomList");
		when(collection.getFullName()).thenReturn("java.util.Collection");

		assertThat(WalaRule.matchesInheritedTarget(access, "java.util.Collection.parallelStream()")).isTrue();
	}

	@Test
	void incompleteInheritedTargetHierarchyFailsClosed() {
		JavaAccess<?> access = mock(JavaAccess.class);
		AccessTarget target = mock(AccessTarget.class);
		JavaClass targetOwner = mock(JavaClass.class);
		when(target.getFullName()).thenReturn("unresolved.CustomList.parallelStream()");
		Mockito.doReturn(target).when(access).getTarget();
		when(access.getTargetOwner()).thenReturn(targetOwner);
		when(targetOwner.getAllClassesSelfIsAssignableTo()).thenReturn(Set.of(targetOwner));
		when(targetOwner.isFullyImported()).thenReturn(false);
		when(targetOwner.getFullName()).thenReturn("unresolved.CustomList");

		org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class,
				() -> WalaRule.matchesInheritedTarget(access, "java.util.Collection.parallelStream()"));
	}

	@Test
	void loadableUnrelatedRuntimeTypesDoNotRequireWalaHierarchy() {
		JavaAccess<?> access = mock(JavaAccess.class);
		AccessTarget target = mock(AccessTarget.class);
		JavaClass targetOwner = mock(JavaClass.class);
		when(target.getFullName()).thenReturn("java.util.Scanner.close()");
		Mockito.doReturn(target).when(access).getTarget();
		when(access.getTargetOwner()).thenReturn(targetOwner);
		when(targetOwner.getAllClassesSelfIsAssignableTo()).thenReturn(Set.of(targetOwner));
		when(targetOwner.isFullyImported()).thenReturn(false);
		when(targetOwner.getFullName()).thenReturn("java.util.Scanner");

		assertThat(WalaRule.matchesInheritedTarget(access, "java.net.DatagramSocket.close()")).isFalse();
		assertThat(WalaRule.matchesInheritedTarget(access, "sun.net.httpserver.DelegatingHttpExchange.close()"))
				.isFalse();
	}

	@Test
	void incompleteArchUnitHierarchyUsesStaticWalaHierarchy() {
		JavaAccess<?> access = mock(JavaAccess.class);
		AccessTarget target = mock(AccessTarget.class);
		JavaClass targetOwner = mock(JavaClass.class);
		IClassHierarchy hierarchy = mock(IClassHierarchy.class);
		IClass targetClass = mock(IClass.class);
		IClass forbiddenClass = mock(IClass.class);
		when(target.getFullName()).thenReturn("example.CustomList.parallelStream()");
		Mockito.doReturn(target).when(access).getTarget();
		when(access.getTargetOwner()).thenReturn(targetOwner);
		when(targetOwner.getAllClassesSelfIsAssignableTo()).thenReturn(Set.of(targetOwner));
		when(targetOwner.isFullyImported()).thenReturn(false);
		when(targetOwner.getFullName()).thenReturn("example.CustomList");
		when(hierarchy.lookupClass(any(TypeReference.class))).thenAnswer(invocation -> {
			String typeName = invocation.getArgument(0, TypeReference.class).getName().toString();
			if ("Lexample/CustomList".equals(typeName)) {
				return targetClass;
			}
			if ("Ljava/util/Collection".equals(typeName)) {
				return forbiddenClass;
			}
			return null;
		});
		when(hierarchy.isAssignableFrom(forbiddenClass, targetClass)).thenReturn(true);

		assertThat(WalaRule.matchesInheritedTarget(access, "java.util.Collection.parallelStream()", hierarchy))
				.isTrue();
	}
}
