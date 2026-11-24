package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.util.List;
import java.util.OptionalInt;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.core.util.strings.Atom;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.UnimplementedError;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Utility for classifying WALA call-graph nodes as student-authored or
 * infrastructure (JDK / Ares / test helpers).
 * <p>
 * Used by {@link WalaRule} to attribute security violations to the nearest
 * student frame rather than an intermediate JDK method.
 * </p>
 */
public final class WalaPathClassification {

	// <editor-fold desc="Constructor">
	private WalaPathClassification() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "WalaPathClassification"));
	}
	// </editor-fold>

	// <editor-fold desc="Constants">

	/**
	 * Package prefixes of application-loaded code that belongs to Ares or to a
	 * library Ares supervises with, so a frame there is never blamed as student
	 * code. JDK classes are absent: they are recognised by the class loader WALA
	 * gives them. The Ares prefix stops at {@code .api.} because Ares' own
	 * integration-test subjects, under {@code de.tum.cit.ase.ares.integration},
	 * must stay supervised. Every prefix here must also be reserved, so no student
	 * class can be named into it.
	 */
	public static final List<String> INFRA_PREFIXES = List.of("de.tum.cit.ase.ares.api.", "net.bytebuddy.",
			"org.aspectj.", "com.ibm.wala.", "com.tngtech.archunit.");

	/**
	 * Package prefixes supervised code may not declare, so a student cannot land a
	 * class in a namespace the grader trusts or relies on: the platform, the
	 * supervision libraries (Byte Buddy, AspectJ, WALA, ArchUnit, Ares), the test
	 * harness (JUnit, opentest4j, AssertJ, Mockito) and Ares' other non-shaded
	 * dependencies. Shadowing any of them subverts the examination. Broader than
	 * {@link #INFRA_PREFIXES}, where a real library in such a namespace is
	 * analysed, not trusted.
	 */
	public static final List<String> RESERVED_PACKAGE_PREFIXES = List.of("java.", "javax.", "sun.", "jdk.", "com.sun.",
			"de.tum.cit.ase.ares.api.", "net.bytebuddy.", "org.aspectj.", "com.ibm.wala.", "com.tngtech.archunit.",
			"junit.", "org.junit.", "org.opentest4j.", "org.assertj.", "org.mockito.", "ch.qos.logback.", "org.slf4j.",
			"com.github.javaparser.", "com.fasterxml.jackson.", "org.yaml.", "org.json.", "com.opencsv.",
			"org.jgrapht.", "org.apiguardian.", "org.apache.", "org.eclipse.", "org.gradle.", "org.jacoco.",
			"com.intellij.", "worker.org.gradle.", "de.tum.in.test.api.");

	/**
	 * Version of the prefix data above. Version 3 restores the test harness and
	 * Ares' other non-shaded dependencies, matching the Ares 1 breadth.
	 * <p>
	 * A version 1 or 2 snippet now <em>under</em>-reserves and is bypassable, not
	 * merely over-strict: a copied snippet must be refreshed, since upgrading the
	 * Ares dependency alone does not update it. One version must not denote two
	 * different lists.
	 */
	public static final String RESERVED_PACKAGE_PREFIX_VERSION = "3";

	/**
	 * Version of the build check that enforces {@link #RESERVED_PACKAGE_PREFIXES},
	 * kept apart from the prefix data. Version 3 matches packages ignoring case and
	 * rejects every file under META-INF/services or mockito-extensions plus root
	 * files a framework reads before Ares is active: archunit.properties,
	 * archunit_ignore_patterns.txt, junit-platform.properties,
	 * jacoco-agent.properties and the Logback files. Version 2 gates every Gradle
	 * Test task. An older snippet is bypassable.
	 */
	public static final String RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION = "3";

	/**
	 * Package prefixes of application-loaded framework code (Ares, AspectJ, Byte
	 * Buddy, WALA, ArchUnit) that mark a harmless detour: the student called a
	 * permitted framework or JDK API, and that code went on to reach the forbidden
	 * one. JDK frames are recognised by their class loader instead. Helper classes
	 * of the exercise itself never belong here, since a call routed through them is
	 * the student's own.
	 */
	static final List<String> TRANSITIVE_FALSE_POSITIVE_PREFIXES = List.of("de.tum.cit.ase.ares.", "net.bytebuddy.",
			"org.aspectj.", "com.ibm.wala.", "com.tngtech.archunit.");
	// </editor-fold>

	// <editor-fold desc="Public API">

	/**
	 * Returns the index (in {@code path}) of the frame closest to the forbidden
	 * sink that belongs to student code (i.e., not in {@link #INFRA_PREFIXES} and
	 * not loaded by a JDK classloader).
	 * <p>
	 * The scan runs from {@code path.size()-1} (the sink) down to {@code 0} (the
	 * entry point). The first non-infra frame found is returned.
	 * </p>
	 *
	 * @param path ordered call-graph path, {@code path.get(path.size()-1)} is the
	 *             forbidden node
	 * @return the index of the nearest student frame, or empty if the entire path
	 *         is infrastructure
	 */
	static OptionalInt nearestStudentFrame(List<CGNode> path) {
		for (int i = path.size() - 1; i >= 0; i--) {
			if (!isInfraFrame(path.get(i))) {
				return OptionalInt.of(i);
			}
		}
		return OptionalInt.empty();
	}

	/**
	 * Returns {@code true} when the path represents a transitive-JDK false
	 * positive: the student frame at {@code studentIdx} exists but every frame
	 * strictly between it and the forbidden sink is an infrastructure frame.
	 * <p>
	 * This pattern occurs when student code uses a permitted JDK API (e.g.
	 * {@code BufferedReader}, {@code AsynchronousSocketChannel}) whose internal
	 * implementation transitively reaches a forbidden method such as
	 * {@code Class.forName()} or {@code Thread.start()}. The student did not
	 * intentionally call the forbidden API; it was an internal JDK side-effect.
	 * </p>
	 * <p>
	 * Direct violations — where the student frame is immediately adjacent to the
	 * forbidden sink (no intermediate frames) — are never suppressed.
	 * </p>
	 *
	 * @param path       the call-graph path ({@code path.get(path.size()-1)} is the
	 *                   forbidden sink)
	 * @param studentIdx the index returned by {@link #nearestStudentFrame}
	 * @return {@code true} if the violation is a transitive-JDK false positive
	 */
	static boolean isFalsePositiveTransitivePath(List<CGNode> path, int studentIdx) {
		int sinkIdx = path.size() - 1;
		if (studentIdx >= sinkIdx - 1) {
			return false;
		}
		for (int j = studentIdx + 1; j < sinkIdx; j++) {
			if (!isTransitiveFalsePositiveFrame(path.get(j))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns {@code true} when the given CGNode belongs to infrastructure: JDK
	 * boot/extension/synthetic classes or any package in {@link #INFRA_PREFIXES}.
	 */
	static boolean isInfraFrame(CGNode node) {
		return hasInfrastructureOrigin(node, INFRA_PREFIXES);
	}

	/**
	 * Returns {@code true} when the given CGNode is JDK code or framework code
	 * under {@link #TRANSITIVE_FALSE_POSITIVE_PREFIXES}, the only frames
	 * {@link #isFalsePositiveTransitivePath} accepts between the student frame and
	 * the forbidden sink.
	 */
	static boolean isTransitiveFalsePositiveFrame(CGNode node) {
		return hasInfrastructureOrigin(node, TRANSITIVE_FALSE_POSITIVE_PREFIXES);
	}

	private static boolean hasInfrastructureOrigin(CGNode node, List<String> applicationPrefixes) {
		try {
			if (node == null || node.getMethod() == null) {
				throw unclassifiableNode("missing call-graph node or method", null);
			}
			IClass cls = node.getMethod().getDeclaringClass();
			if (cls == null) {
				throw unclassifiableNode("missing declaring class", null);
			}
			if (cls.getClassLoader() == null || cls.getClassLoader().getReference() == null) {
				throw unclassifiableNode("missing class-loader identity", null);
			}
			ClassLoaderReference loader = cls.getClassLoader().getReference();
			if (loader.equals(ClassLoaderReference.Primordial) || loader.equals(ClassLoaderReference.Extension)) {
				// Primordial covers JDK boot classes and WALA's synthetic FakeRoot
				// nodes, which use Primordial loader in WALA 1.6.x.
				return true;
			}
			String pkg = packageNameOf(cls);
			return applicationPrefixes.stream().anyMatch(pkg::startsWith);
		} catch (SecurityException failClosed) {
			throw failClosed;
		} catch (RuntimeException | UnimplementedError unclassifiable) {
			throw unclassifiableNode("WALA could not classify the declaring class", unclassifiable);
		}
	}

	private static SecurityException unclassifiableNode(String reason, Throwable cause) {
		String message = Messages.localized("security.architecture.wala.unclassifiable.node", reason);
		return cause == null ? new SecurityException(message) : new SecurityException(message, cause);
	}
	// </editor-fold>

	// <editor-fold desc="Package helpers">

	/**
	 * Returns the dotted package name of an {@link IClass}, with a trailing dot
	 * (e.g. {@code "java.lang."}). Returns an empty string for primitive types or
	 * classes in the default package.
	 * <p>
	 * Array types are unwrapped to their element type recursively.
	 * </p>
	 */
	/**
	 * Whether this frame is skipped as infrastructure although supervised code may
	 * declare its package: application-loaded, inside {@link #INFRA_PREFIXES} and
	 * outside {@link #RESERVED_PACKAGE_PREFIXES}. Its violations would be blamed on
	 * nobody, so such a path is refused. Every infrastructure prefix is reserved
	 * today, so this answers {@code false}.
	 *
	 * @param node the call-graph node to classify
	 * @return whether the frame is skipped but nameable by supervised code
	 */
	static boolean isUnreservedInfrastructureFrame(CGNode node) {
		try {
			if (node == null || node.getMethod() == null) {
				return false;
			}
			IClass cls = node.getMethod().getDeclaringClass();
			if (cls == null || cls.getClassLoader() == null || cls.getClassLoader().getReference() == null) {
				return false;
			}
			ClassLoaderReference loader = cls.getClassLoader().getReference();
			if (loader.equals(ClassLoaderReference.Primordial) || loader.equals(ClassLoaderReference.Extension)) {
				// Skipped because of where it came from rather than what it is called,
				// and a loader is not something supervised code gets to choose.
				return false;
			}
			String pkg = packageNameOf(cls);
			return INFRA_PREFIXES.stream().anyMatch(pkg::startsWith)
					&& RESERVED_PACKAGE_PREFIXES.stream().noneMatch(pkg::startsWith);
		} catch (RuntimeException unclassifiable) {
			// Only ever asked about a sink that was about to be dropped, so answering
			// "no" leaves it exactly as it was before this question existed.
			return false;
		}
	}

	static String packageNameOf(IClass cls) {
		return packageNameOf(cls.getReference());
	}

	/**
	 * Returns the dotted package name of a {@link TypeReference}.
	 * <p>
	 * For array types, recurses on the element type. For reference types, reads
	 * {@link com.ibm.wala.types.TypeName#getPackage()} and converts the
	 * slash-separated result to dot notation with a trailing dot. For primitive
	 * types or unknown packages, returns {@code ""}.
	 * </p>
	 */
	static String packageNameOf(TypeReference ref) {
		if (ref.isArrayType()) {
			TypeReference element = ref.getArrayElementType();
			if (element != null && element.isReferenceType()) {
				return packageNameOf(element);
			}
			return ""; // array of primitives: no package
		}
		Atom pkg = ref.getName().getPackage();
		if (pkg == null) {
			return "";
		}
		return pkg.toString().replace('/', '.') + ".";
	}
	// </editor-fold>
}
