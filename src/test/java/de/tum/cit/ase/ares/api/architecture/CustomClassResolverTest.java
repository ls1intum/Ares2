package de.tum.cit.ase.ares.api.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile.CustomClassResolver;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CustomClassResolverTest {

    @Test
    void testTryResolveExistingClass() {
        Optional<JavaClass> resolvedClass = CustomClassResolver.tryResolve("de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile.CustomClassResolver");
        assertThat(resolvedClass).isPresent();
        assertThat(resolvedClass.get().getFullName()).isEqualTo("de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile.CustomClassResolver");
    }

    @Test
    void testTryResolveNonExistingClass() {
        Optional<JavaClass> resolvedClass = CustomClassResolver.tryResolve("non.existing.ClassName");
        assertThat(resolvedClass).isNotPresent();
    }

    @Test
    void testTryResolveAdviceDefinitionClass() {
        Optional<JavaClass> resolvedClass = CustomClassResolver.tryResolve("de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions");
        assertThat(resolvedClass).isNotPresent();
    }

    @Test
    void testTryImportJrtClass() {
        Optional<JavaClass> resolvedClass = CustomClassResolver.tryResolve("java.lang.System");
        assertThat(resolvedClass).isNotPresent();
    }
}
