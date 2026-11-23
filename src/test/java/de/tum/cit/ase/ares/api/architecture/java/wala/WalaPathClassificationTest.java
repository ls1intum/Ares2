package de.tum.cit.ase.ares.api.architecture.java.wala;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

import de.tum.cit.ase.ares.api.localization.Messages;

class WalaPathClassificationTest {

	// ----------------------------------------------------------------
	// Mock helpers
	// ----------------------------------------------------------------

	private static CGNode mockNode(ClassLoaderReference loaderRef, TypeReference typeRef) {
		CGNode node = mock(CGNode.class);
		IMethod method = mock(IMethod.class);
		IClass cls = mock(IClass.class);
		IClassLoader loader = mock(IClassLoader.class);
		when(node.getMethod()).thenReturn(method);
		when(method.getDeclaringClass()).thenReturn(cls);
		when(cls.getClassLoader()).thenReturn(loader);
		when(loader.getReference()).thenReturn(loaderRef);
		when(cls.getReference()).thenReturn(typeRef);
		return node;
	}

	private static TypeReference ref(ClassLoaderReference clr, String descriptor) {
		return TypeReference.findOrCreate(clr, TypeName.findOrCreate(descriptor));
	}

	private static CGNode primordialNode(String descriptor) {
		return mockNode(ClassLoaderReference.Primordial, ref(ClassLoaderReference.Primordial, descriptor));
	}

	private static CGNode applicationNode(String descriptor) {
		return mockNode(ClassLoaderReference.Application, ref(ClassLoaderReference.Application, descriptor));
	}

	// ----------------------------------------------------------------
	// isInfraFrame — cases 1-5
	// ----------------------------------------------------------------

	@Test
	void primordialJdkClassIsInfra() {
		assertThat(WalaPathClassification.isInfraFrame(primordialNode("Ljava/lang/String;"))).isTrue();
	}

	@Test
	void applicationStudentClassIsNotInfra() {
		assertThat(WalaPathClassification
				.isInfraFrame(applicationNode("Lanonymous/classloadingsystem/ClassLoadingAccess;"))).isFalse();
	}

	@Test
	void applicationToolclassIsInfra() {
		assertThat(WalaPathClassification.isInfraFrame(applicationNode("Lanonymous/toolclasses/Helper;"))).isTrue();
	}

	@Test
	void applicationAresClassIsInfra() {
		assertThat(WalaPathClassification.isInfraFrame(applicationNode("Lde/tum/cit/ase/ares/api/SomeClass;")))
				.isTrue();
	}

	@Test
	void applicationLoadedJavaxLibraryIsNotMistakenForPlatformCode() {
		assertThat(WalaPathClassification.isInfraFrame(applicationNode("Ljavax/activation/FileDataSource;"))).isFalse();
	}

	@Test
	void applicationArrayOfStudentClassIsNotInfra() {
		CGNode node = mockNode(ClassLoaderReference.Application,
				ref(ClassLoaderReference.Application, "[Lanonymous/Student;"));
		assertThat(WalaPathClassification.isInfraFrame(node)).isFalse();
	}

	// ----------------------------------------------------------------
	// nearestStudentFrame — cases 6-12
	// ----------------------------------------------------------------

	@Test
	void emptyPathReturnsEmpty() {
		assertThat(WalaPathClassification.nearestStudentFrame(List.of()).isEmpty()).isTrue();
	}

	@Test
	void singleStudentNodeReturnsZero() {
		CGNode student = applicationNode("Lanonymous/Student;");
		assertThat(WalaPathClassification.nearestStudentFrame(List.of(student))).isEqualTo(OptionalInt.of(0));
	}

	@Test
	void singleInfraNodeReturnsEmpty() {
		CGNode infra = primordialNode("Ljava/lang/String;");
		assertThat(WalaPathClassification.nearestStudentFrame(List.of(infra)).isEmpty()).isTrue();
	}

	@Test
	void studentThenInfraSinkReturnsZero() {
		CGNode student = applicationNode("Lanonymous/Student;");
		CGNode sink = primordialNode("Ljava/lang/Class;");
		assertThat(WalaPathClassification.nearestStudentFrame(List.of(student, sink))).isEqualTo(OptionalInt.of(0));
	}

	@Test
	void studentThenMultipleInfraSinksReturnsZero() {
		CGNode student = applicationNode("Lanonymous/Student;");
		CGNode infra1 = primordialNode("Lsun/nio/ch/DatagramChannelImpl;");
		CGNode infra2 = primordialNode("Lsun/nio/ch/DatagramChannelImpl;");
		CGNode sink = primordialNode("Ljava/net/InetAddress;");
		assertThat(WalaPathClassification.nearestStudentFrame(List.of(student, infra1, infra2, sink)))
				.isEqualTo(OptionalInt.of(0));
	}

	@Test
	void nearestStudentIsReturnedWhenMultipleStudentFrames() {
		CGNode studentA = applicationNode("Lanonymous/A;");
		CGNode infra = primordialNode("Lsun/nio/ch/SomeImpl;");
		CGNode studentB = applicationNode("Lanonymous/B;");
		CGNode sink = primordialNode("Ljava/lang/Thread;");
		assertThat(WalaPathClassification.nearestStudentFrame(List.of(studentA, infra, studentB, sink)))
				.isEqualTo(OptionalInt.of(2));
	}

	@Test
	void toolclassHelperTreatedAsInfraNotStudent() {
		CGNode studentA = applicationNode("Lanonymous/A;");
		CGNode toolclass = applicationNode("Lanonymous/toolclasses/Helper;");
		CGNode studentB = applicationNode("Lanonymous/B;");
		CGNode sink = primordialNode("Ljava/lang/Thread;");
		assertThat(WalaPathClassification.nearestStudentFrame(List.of(studentA, toolclass, studentB, sink)))
				.isEqualTo(OptionalInt.of(2));
	}

	// ----------------------------------------------------------------
	// packageNameOf — cases 13-16
	// ----------------------------------------------------------------

	@Test
	void packageNameOfJavaLangString() {
		IClass cls = mock(IClass.class);
		when(cls.getReference()).thenReturn(TypeReference.JavaLangString);
		assertThat(WalaPathClassification.packageNameOf(cls)).isEqualTo("java.lang.");
	}

	@Test
	void packageNameOfStringArray() {
		IClass cls = mock(IClass.class);
		when(cls.getReference()).thenReturn(ref(ClassLoaderReference.Primordial, "[Ljava/lang/String;"));
		assertThat(WalaPathClassification.packageNameOf(cls)).isEqualTo("java.lang.");
	}

	@Test
	void packageNameOfTwoDimensionalStudentArray() {
		IClass cls = mock(IClass.class);
		when(cls.getReference()).thenReturn(ref(ClassLoaderReference.Application, "[[Lanonymous/Student;"));
		assertThat(WalaPathClassification.packageNameOf(cls)).isEqualTo("anonymous.");
	}

	@Test
	void packageNameOfPrimitiveIntArray() {
		IClass cls = mock(IClass.class);
		when(cls.getReference()).thenReturn(TypeReference.IntArray);
		assertThat(WalaPathClassification.packageNameOf(cls)).isEqualTo("");
	}

	// ----------------------------------------------------------------
	// isInfraFrame edge cases — cases 17-18
	// ----------------------------------------------------------------

	@Test
	void extensionClassLoaderIsInfra() {
		// ClassLoaderReference.Synthetic does not exist in WALA 1.6.x.
		// Extension loader (platform/endorsed) is the correct secondary infra signal.
		CGNode node = mockNode(ClassLoaderReference.Extension,
				ref(ClassLoaderReference.Primordial, "Ljava/lang/String;"));
		assertThat(WalaPathClassification.isInfraFrame(node)).isTrue();
	}

	@Test
	void declaringClassFailureFailsClosed() {
		CGNode node = mock(CGNode.class);
		IMethod method = mock(IMethod.class);
		when(node.getMethod()).thenReturn(method);
		when(method.getDeclaringClass()).thenThrow(new RuntimeException("simulated WALA error"));
		assertThatThrownBy(() -> WalaPathClassification.isInfraFrame(node)).isInstanceOf(SecurityException.class)
				.hasMessageContaining("WALA").hasCauseInstanceOf(RuntimeException.class);
	}

	@Test
	void nullNodeFailsClosed() {
		assertThatThrownBy(() -> WalaPathClassification.isInfraFrame(null)).isInstanceOf(SecurityException.class)
				.hasMessageContaining("missing call-graph node or method");
	}

	@Test
	void missingMethodFailsClosed() {
		CGNode node = mock(CGNode.class);
		assertThatThrownBy(() -> WalaPathClassification.nearestStudentFrame(List.of(node)))
				.isInstanceOf(SecurityException.class).hasMessageContaining("missing call-graph node or method");
	}

	@Test
	void missingDeclaringClassFailsClosedForTransitiveClassification() {
		CGNode node = mock(CGNode.class);
		IMethod method = mock(IMethod.class);
		when(node.getMethod()).thenReturn(method);
		assertThatThrownBy(() -> WalaPathClassification.isTransitiveFalsePositiveFrame(node))
				.isInstanceOf(SecurityException.class).hasMessageContaining("missing declaring class");
	}

	// ----------------------------------------------------------------
	// Generic all-infra invariant — case 19
	// ----------------------------------------------------------------

	@Test
	void allInfraPathWithMixedLoadersReturnsEmpty() {
		// Primordial (JDK) + Application (Ares infra) — both infra, different
		// classloaders.
		// Asserts the no-student-frame contract independent of forbidden-API family.
		CGNode primordial = primordialNode("Ljava/lang/String;");
		CGNode ares = applicationNode("Lde/tum/cit/ase/ares/api/SomeHelper;");
		assertThat(WalaPathClassification.nearestStudentFrame(List.of(primordial, ares)).isEmpty()).isTrue();
	}

	/**
	 * A JDK frame between the student and the sink makes the path a false positive.
	 */
	@Test
	void checksEveryFrameStrictlyBetweenStudentAndSink() {
		CGNode student = applicationNode("Lanonymous/Student;");
		CGNode jdk = primordialNode("Lsun/nio/ch/SomeImpl;");
		CGNode sink = applicationNode("Lanonymous/Sink;");
		assertThat(WalaPathClassification.isTransitiveFalsePositiveFrame(sink)).isFalse();
		assertThat(WalaPathClassification.isFalsePositiveTransitivePath(List.of(student, jdk, sink), 0)).isTrue();
	}

	/**
	 * A frame that cannot be classified is never called unreserved infrastructure.
	 */
	@Test
	void answersNoForAnUnclassifiableFrame() {
		CGNode noMethod = mock(CGNode.class);
		CGNode throwing = mock(CGNode.class);
		when(throwing.getMethod()).thenThrow(new IllegalStateException("simulated WALA error"));
		CGNode noLoader = mockNode(ClassLoaderReference.Application,
				ref(ClassLoaderReference.Application, "Lanonymous/toolclasses/Helper;"));
		when(noLoader.getMethod().getDeclaringClass().getClassLoader()).thenReturn(null);
		CGNode noReference = mockNode(null, ref(ClassLoaderReference.Application, "Lanonymous/toolclasses/Helper;"));
		assertThat(WalaPathClassification.isUnreservedInfrastructureFrame(null)).isFalse();
		assertThat(WalaPathClassification.isUnreservedInfrastructureFrame(noMethod)).isFalse();
		assertThat(WalaPathClassification.isUnreservedInfrastructureFrame(throwing)).isFalse();
		assertThat(WalaPathClassification.isUnreservedInfrastructureFrame(noLoader)).isFalse();
		assertThat(WalaPathClassification.isUnreservedInfrastructureFrame(noReference)).isFalse();
		assertThat(WalaPathClassification
				.isUnreservedInfrastructureFrame(applicationNode("Lanonymous/toolclasses/Helper;"))).isTrue();
	}

	/** A node without a class loader identity is refused. */
	@Test
	void refusesANodeItCannotClassify() {
		CGNode noReference = mockNode(null, ref(ClassLoaderReference.Application, "Lanonymous/Student;"));
		assertThatThrownBy(() -> WalaPathClassification.isInfraFrame(noReference)).isInstanceOf(SecurityException.class)
				.hasMessage(Messages.localized("security.architecture.wala.unclassifiable.node",
						"missing class-loader identity"));
		CGNode noLoader = mockNode(ClassLoaderReference.Application,
				ref(ClassLoaderReference.Application, "Lanonymous/Student;"));
		when(noLoader.getMethod().getDeclaringClass().getClassLoader()).thenReturn(null);
		assertThatThrownBy(() -> WalaPathClassification.isInfraFrame(noLoader)).isInstanceOf(SecurityException.class)
				.hasMessage(Messages.localized("security.architecture.wala.unclassifiable.node",
						"missing class-loader identity"));
	}

	/** Primitive arrays and the default package have no package name. */
	@Test
	void namesNoPackageForPrimitiveArraysAndTheDefaultPackage() {
		assertThat(WalaPathClassification.packageNameOf(TypeReference.IntArray)).isEqualTo("");
		assertThat(WalaPathClassification
				.packageNameOf(TypeReference.findOrCreate(ClassLoaderReference.Application, "LFoo"))).isEqualTo("");
	}
}
