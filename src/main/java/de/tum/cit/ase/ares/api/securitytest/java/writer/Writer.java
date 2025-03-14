package de.tum.cit.ase.ares.api.securitytest.java.writer;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for writing security test cases.
 */
public interface Writer {

    /**
     * Writes security test cases.
     */
    @Nonnull
    List<Path> writeSecurityTestCases(
            @Nullable Path projectDirectory,
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull String[] testClasses,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    );

}
