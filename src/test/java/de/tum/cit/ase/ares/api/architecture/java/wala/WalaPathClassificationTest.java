package de.tum.cit.ase.ares.api.architecture.java.wala;

import static org.assertj.core.api.Assertions.assertThat;
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
        return mockNode(ClassLoaderReference.Primordial,
                ref(ClassLoaderReference.Primordial, descriptor));
    }

    private static CGNode applicationNode(String descriptor) {
        return mockNode(ClassLoaderReference.Application,
                ref(ClassLoaderReference.Application, descriptor));
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
        assertThat(WalaPathClassification.isInfraFrame(
                applicationNode("Lanonymous/classloadingsystem/ClassLoadingAccess;"))).isFalse();
    }

    @Test
    void applicationToolclassIsInfra() {
        assertThat(WalaPathClassification.isInfraFrame(
                applicationNode("Lanonymous/toolclasses/Helper;"))).isTrue();
    }

    @Test
    void applicationAresClassIsInfra() {
        assertThat(WalaPathClassification.isInfraFrame(
                applicationNode("Lde/tum/cit/ase/ares/api/SomeClass;"))).isTrue();
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
        assertThat(WalaPathClassification.nearestStudentFrame(List.of(student)))
                .isEqualTo(OptionalInt.of(0));
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
        assertThat(WalaPathClassification.nearestStudentFrame(List.of(student, sink)))
                .isEqualTo(OptionalInt.of(0));
    }

    @Test
    void studentThenMultipleInfraSinksReturnsZero() {
        CGNode student = applicationNode("Lanonymous/Student;");
        CGNode infra1 = primordialNode("Lsun/nio/ch/DatagramChannelImpl;");
        CGNode infra2 = primordialNode("Lsun/nio/ch/DatagramChannelImpl;");
        CGNode sink = primordialNode("Ljava/net/InetAddress;");
        assertThat(WalaPathClassification.nearestStudentFrame(
                List.of(student, infra1, infra2, sink))).isEqualTo(OptionalInt.of(0));
    }

    @Test
    void nearestStudentIsReturnedWhenMultipleStudentFrames() {
        CGNode studentA = applicationNode("Lanonymous/A;");
        CGNode infra = primordialNode("Lsun/nio/ch/SomeImpl;");
        CGNode studentB = applicationNode("Lanonymous/B;");
        CGNode sink = primordialNode("Ljava/lang/Thread;");
        assertThat(WalaPathClassification.nearestStudentFrame(
                List.of(studentA, infra, studentB, sink))).isEqualTo(OptionalInt.of(2));
    }

    @Test
    void toolclassHelperTreatedAsInfraNotStudent() {
        CGNode studentA = applicationNode("Lanonymous/A;");
        CGNode toolclass = applicationNode("Lanonymous/toolclasses/Helper;");
        CGNode studentB = applicationNode("Lanonymous/B;");
        CGNode sink = primordialNode("Ljava/lang/Thread;");
        assertThat(WalaPathClassification.nearestStudentFrame(
                List.of(studentA, toolclass, studentB, sink))).isEqualTo(OptionalInt.of(2));
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
        when(cls.getReference()).thenReturn(
                ref(ClassLoaderReference.Primordial, "[Ljava/lang/String;"));
        assertThat(WalaPathClassification.packageNameOf(cls)).isEqualTo("java.lang.");
    }

    @Test
    void packageNameOfTwoDimensionalStudentArray() {
        IClass cls = mock(IClass.class);
        when(cls.getReference()).thenReturn(
                ref(ClassLoaderReference.Application, "[[Lanonymous/Student;"));
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
    void declaringClassThrowsIsInfra() {
        CGNode node = mock(CGNode.class);
        IMethod method = mock(IMethod.class);
        when(node.getMethod()).thenReturn(method);
        when(method.getDeclaringClass()).thenThrow(new RuntimeException("simulated WALA error"));
        assertThat(WalaPathClassification.isInfraFrame(node)).isTrue();
    }

    // ----------------------------------------------------------------
    // Generic all-infra invariant — case 19
    // ----------------------------------------------------------------

    @Test
    void allInfraPathWithMixedLoadersReturnsEmpty() {
        // Primordial (JDK) + Application (Ares infra) — both infra, different classloaders.
        // Asserts the no-student-frame contract independent of forbidden-API family.
        CGNode primordial = primordialNode("Ljava/lang/String;");
        CGNode ares = applicationNode("Lde/tum/cit/ase/ares/api/SomeHelper;");
        assertThat(WalaPathClassification.nearestStudentFrame(
                List.of(primordial, ares)).isEmpty()).isTrue();
    }
}
