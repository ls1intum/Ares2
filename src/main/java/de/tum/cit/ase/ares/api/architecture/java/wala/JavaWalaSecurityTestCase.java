package de.tum.cit.ase.ares.api.architecture.java.wala;

import de.tum.cit.ase.ares.api.architecture.ArchitectureSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

/**
 * A utility class to check security rules in a call graph.
 */
public class JavaWalaSecurityTestCase implements ArchitectureSecurityTestCase {

    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    // TODO Sarp: Generalize this
    private final JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;

    public JavaWalaSecurityTestCase(JavaArchitecturalTestCaseSupported javaSecurityTestCaseSupported) {
        this.javaArchitectureTestCaseSupported = javaSecurityTestCaseSupported;
    }

    @Override
    public String writeArchitectureTestCase() {
        // TODO: For further releases
        return "";
    }

    @Override
    public void executeArchitectureTestCase(Object path) {
        if (!(path instanceof String claasPath)) {
            throw new IllegalArgumentException(localized("security.archunit.invalid.argument"));
        }
        try {
            // TODO Sarp: implement this more efficiently only pass the predicate that we need!!!!! also fpr ArchUnit
            switch (this.javaArchitectureTestCaseSupported) {
                default -> throw new UnsupportedOperationException("Not implemented yet");
            }
        } catch (AssertionError e) {
            String[] split = null;
            if (e.getMessage() == null || e.getMessage().split("\n").length < 2) {
                throw new SecurityException(localized("security.archunit.illegal.execution", e.getMessage()));
            }
            split = e.getMessage().split("\n");
            throw new SecurityException(localized("security.archunit.violation.error", split[0].replaceAll(".*?'(.*?)'.*", "$1"), split[1]));
        }
    }
}
