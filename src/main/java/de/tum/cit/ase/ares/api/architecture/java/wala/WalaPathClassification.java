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
	 * Application-loaded package prefixes (with trailing dot) that belong to Ares
	 * or its test infrastructure and must not be attributed as student code.
	 * Platform classes are deliberately absent: they are classified by their WALA
	 * classloader origin, because a third-party JAR may legally use a
	 * {@code javax.*} or {@code com.sun.*} namespace.
	 * <p>
	 * The Ares prefix is the narrow {@code de.tum.cit.ase.ares.api.}, matching the
	 * ArchUnit importer exclusion ({@code /de/tum/cit/ase/ares/api/}) and the
	 * runtime AOP ignored-frame prefix. The broader {@code de.tum.cit.ase.ares.}
	 * would also classify the reproducibility test subjects (which live under
	 * {@code de.tum.cit.ase.ares.integration.*}) as infrastructure, which would
	 * wrongly suppress their violations; all Ares production code is under
	 * {@code .api.}, so narrowing loses no genuine infrastructure frame.
	 * <p>
	 * The two reproducibility-repo helper packages ({@code anonymous.toolclasses.}
	 * and {@code metatest.}) are included here intentionally: they are
	 * test-framework helpers, not student-authored code under test. If a future
	 * Ares consumer uses different helper packages, this list will need to be made
	 * configurable.
	 * <p>
	 * </p>
	 */
	public static final List<String> INFRA_PREFIXES = List.of("de.tum.cit.ase.ares.api.", "net.bytebuddy.",
			"org.aspectj.", "com.ibm.wala.", "com.tngtech.archunit.", "anonymous.toolclasses.", "metatest.");

	/**
	 * Package prefixes that supervised code may not declare. This is broader than
	 * {@link #INFRA_PREFIXES}: platform namespaces remain reserved against student
	 * impersonation even though application-loaded third-party libraries in those
	 * namespaces are analysed rather than trusted.
	 */
	public static final List<String> RESERVED_PACKAGE_PREFIXES = List.of("java.", "javax.", "sun.", "jdk.", "com.sun.",
			"de.tum.cit.ase.ares.api.", "net.bytebuddy.", "org.aspectj.", "com.ibm.wala.", "com.tngtech.archunit.",
			"anonymous.toolclasses.", "metatest.");
	public static final String RESERVED_PACKAGE_PREFIX_VERSION = "1";

	/**
	 * Application-loaded subset of {@link #INFRA_PREFIXES} that genuinely indicates
	 * a transitive false-positive path: the student called a permitted JDK or
	 * framework API and the JDK or framework internally invoked a forbidden one.
	 * JDK frames are classified separately by their primordial/extension loader;
	 * this list covers Ares, AspectJ, ByteBuddy, WALA and ArchUnit packages.
	 * <p>
	 * Project-specific test helpers ({@code anonymous.toolclasses.} and
	 * {@code metatest.}) are deliberately omitted: when student code routes through
	 * such a helper into a forbidden JDK API, the helper exists for the sole
	 * purpose of exercising that API. Treating those paths as false positives
	 * silently disabled BLOCKED_ALL detection for every Network / FileSystem /
	 * Command call placed inside a project test helper.
	 * </p>
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
			return INFRA_PREFIXES.stream().anyMatch(pkg::startsWith);
		} catch (SecurityException failClosed) {
			throw failClosed;
		} catch (RuntimeException | UnimplementedError unclassifiable) {
			throw unclassifiableNode("WALA could not classify the declaring class", unclassifiable);
		}
	}

	/**
	 * Returns {@code true} when the given CGNode counts as transitive
	 * infrastructure for the purpose of {@link #isFalsePositiveTransitivePath}
	 * only. Project test helpers ({@code anonymous.toolclasses.} /
	 * {@code metatest.}) explicitly do NOT count here, so a student call that
	 * routes through such a helper into a forbidden JDK API is reported as a real
	 * violation.
	 */
	static boolean isTransitiveFalsePositiveFrame(CGNode node) {
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
				return true;
			}
			String pkg = packageNameOf(cls);
			return TRANSITIVE_FALSE_POSITIVE_PREFIXES.stream().anyMatch(pkg::startsWith);
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
