package de.tum.cit.ase.ares.api.ast.asserting;

import com.github.javaparser.ParserConfiguration.LanguageLevel;
import de.tum.cit.ase.ares.api.AresConfiguration;
import de.tum.cit.ase.ares.api.ast.model.RecursionCheck;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.assertj.core.api.AbstractAssert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.fail;

/**
 * Checks whole Java files for unwanted simple recursion
 *
 * @author Sarp Sahinalp
 * @version 1.0.0
 * @since 1.12.0
 */
@API(status = Status.MAINTAINED)
public class UnwantedRecursionAssert extends AbstractAssert<UnwantedRecursionAssert, Path> {

    /**
     * The regular expression to match an identifier
     */
    private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    /**
     * The regular expression to match a Java identifier
     */
    private static final String JAVA_IDENTIFIER_REGEX = String.format("(%s\\.)+%s", ID_PATTERN, ID_PATTERN);

    /**
     * The regular expression to match a valid vertex
     */
    private static final String STARTING_NODE_REGEX = JAVA_IDENTIFIER_REGEX + "\\((" +
            JAVA_IDENTIFIER_REGEX + "\\[\\],|" +
            JAVA_IDENTIFIER_REGEX + "\\[\\]|" +
            JAVA_IDENTIFIER_REGEX + "|" +
            JAVA_IDENTIFIER_REGEX + "\\<" + ID_PATTERN + "\\>)*\\)";

    /**
     * The language level for the Java parser
     */
    private final LanguageLevel level;

    /**
     * The method to start the recursion check from, not optional since Optional type is not recommended for field types
     */
    private final String startingMethod;

    /**
     * The methods to exclude from the recursion check
     */
    private final Set<String> excludedMethods;

    private UnwantedRecursionAssert(Path path, LanguageLevel level, String startingMethod, Set<String> excludedMethods) {
        super(requireNonNull(path), UnwantedRecursionAssert.class);
        this.level = level;
        this.startingMethod = startingMethod;
        this.excludedMethods = excludedMethods;

        if (startingMethod != null && !startingMethod.matches(STARTING_NODE_REGEX)) {
            fail("The starting method %s does not match the regular expression %s", startingMethod, STARTING_NODE_REGEX); //$NON-NLS-1$
        }

        if (startingMethod != null && excludedMethods != null && excludedMethods.contains(startingMethod)) {
            fail("The starting method %s is also excluded from the recursion check", startingMethod); //$NON-NLS-1$
        }

        if (!Files.isDirectory(path)) {
            fail("The source directory %s does not exist", path); //$NON-NLS-1$
        }
    }

    /**
     * Creates an unwanted simple recursion assertion object for all project source files.
     * <p>
     * The project source directory gets extracted from the build configuration, and
     * a <code>pom.xml</code> or <code>build.gradle</code> in the execution path is
     * the default build configuration location. The configuration here is the same
     * as the one in the structural tests and uses {@link AresConfiguration}.
     *
     * @return An unwanted simple recursion assertion object (for chaining)
     */
    public static UnwantedRecursionAssert assertThatProjectSources() {
        var path = ProjectSourcesFinder.findProjectSourcesPath().orElseThrow(() -> //$NON-NLS-1$
                new AssertionError("Could not find project sources folder." //$NON-NLS-1$
                        + " Make sure the build file is configured correctly." //$NON-NLS-1$
                        + " If it is not located in the execution folder directly," //$NON-NLS-1$
                        + " set the location using AresConfiguration methods.")); //$NON-NLS-1$
        return new UnwantedRecursionAssert(path, null, null, Collections.emptySet());
    }

    /**
     * Creates an unwanted simple recursion node assertion object for all source files at and below
     * the given directory path.
     *
     * @param directory Path to a directory under which all files are considered
     * @return An unwanted simple recursion assertion object (for chaining)
     */
    public static UnwantedRecursionAssert assertThatSourcesIn(Path directory) {
        Objects.requireNonNull(directory, "The given source path must not be null."); //$NON-NLS-1$
        return new UnwantedRecursionAssert(directory, null, null, Collections.emptySet());
    }

    /**
     * Creates an unwanted simple recursion assertion object for all source files in the given
     * package, including all of its sub-packages.
     *
     * @param packageName Java package name in the form of, e.g.,
     *                    <code>de.tum.cit.ase.ares.api</code>, which is resolved
     *                    relative to the path of this UnwantedNodesAssert.
     * @return An unwanted simple recursion assertion object (for chaining)
     * @implNote The package is split at "." with the resulting segments being
     * interpreted as directory structure. So
     * <code>assertThatSourcesIn(Path.of("src/main/java")).withinPackage("net.example.test")</code>
     * will yield an assert for all source files located at and below the
     * relative path <code>src/main/java/net/example/test</code>
     */
    public UnwantedRecursionAssert withinPackage(String packageName) {
        Objects.requireNonNull(packageName, "The package name must not be null."); //$NON-NLS-1$
        var newPath = actual.resolve(Path.of("", packageName.split("\\."))); //$NON-NLS-1$ //$NON-NLS-2$
        return new UnwantedRecursionAssert(newPath, level, startingMethod, excludedMethods);
    }

    /**
     * Configures the language level used by the Java parser
     *
     * @param level The language level for the Java parser
     * @return An unwanted simple recursion assertion object (for chaining)
     */
    public UnwantedRecursionAssert withLanguageLevel(LanguageLevel level) {
        return new UnwantedRecursionAssert(actual, level, startingMethod, excludedMethods);
    }

    /**
     * Configures the method to start the recursion check from
     *
     * @param node The method to start the recursion check from
     * @return An unwanted simple recursion assertion object (for chaining)
     */
    public UnwantedRecursionAssert startingWithMethod(String node) {
        return new UnwantedRecursionAssert(actual, level, node, excludedMethods);
    }

    public UnwantedRecursionAssert excludeMethods(String... methods) {
        // Check if passed in exclude methods are correct quantifiers
        for (String s : methods) {
            if (!s.matches(STARTING_NODE_REGEX)) {
                fail("The method %s does not match the regular expression %s", s, STARTING_NODE_REGEX); //$NON-NLS-1$
            }
        }

        return new UnwantedRecursionAssert(actual, level, startingMethod, Set.of(methods));
    }

    /**
     * Verifies that the selected Java files (do not) contain any recursion.
     */
    private void checkRecursion(boolean shouldHaveRecursion) {
        if (level == null) {
            failWithMessage("The 'level' is not set. Please use UnwantedNodesAssert.withLanguageLevel(LanguageLevel)."); //$NON-NLS-1$
        }

        Optional<String> errorMessage;
        if (shouldHaveRecursion) {
            errorMessage = RecursionCheck.hasNoCycle(actual, level, startingMethod, excludedMethods).isEmpty() ? Optional.of(localized("ast.recursion.no.recursion.found")) : Optional.empty();
        } else {
            errorMessage = RecursionCheck.hasNoCycle(actual, level, startingMethod, excludedMethods);
        }

        String messageKey = shouldHaveRecursion ? "ast.recursion.has.recursion" : "ast.recursion.has.no_recursion";
        errorMessage.ifPresent(unwantedSimpleRecursionMessageForAllJavaFiles -> failWithMessage(
                localized(messageKey) + System.lineSeparator() + unwantedSimpleRecursionMessageForAllJavaFiles)); //$NON-NLS-1$

    }

    /**
     * Verifies that the selected Java files do contain any recursion.
     */
    public void hasRecursion() {
        checkRecursion(true);
    }

    /**
     * Verifies that the selected Java files do not contain any recursion.
     */
    public void hasNoRecursion() {
        checkRecursion(false);
    }
}


