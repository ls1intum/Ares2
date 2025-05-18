package %s.api.architecture.java.archunit;

//<editor-fold desc="Imports">

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.thirdparty.com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.thirdparty.com.google.common.base.Preconditions.checkNotNull;
import static com.tngtech.archunit.thirdparty.com.google.common.collect.Iterables.getLast;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
//</editor-fold>

/**
 * An ArchUnit condition that checks if classes transitively access methods matching a predicate.
 *
 * <p>Description: This class extends ArchCondition to analyze class dependencies and detect
 * methods that match specific criteria through transitive dependencies. It tracks access paths
 * to identify how classes are connected through method calls.
 *
 * <p>Design Rationale: Using transitive dependency analysis allows detecting security-relevant
 * method calls even if they occur indirectly through multiple layers of dependencies, which is
 * crucial for security analysis in the Ares framework.
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
public class TransitivelyAccessesMethodsCondition extends ArchCondition<JavaClass> {
    //<editor-fold desc="Attributes">
    /**
     * Predicate used to identify method accesses, which is violating.
     *
     * <p>Description: This predicate defines the criteria for determining which method
     * accesses is violating (calling a blocked method) in the transitive dependency analysis.
     */
    private final DescribedPredicate<? super JavaAccess<?>> checkIfAccessIsViolating;

    /**
     * Helper class that finds paths through method call dependencies.
     *
     * <p>Description: Maintains state and logic for traversing the access graph to find
     * connections between methods and build dependency paths.
     */
    private final TransitiveAccessPath transitiveAccessPath = new TransitiveAccessPath();
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Creates a condition to check for transitive method access matching a predicate.
     *
     * <p>Description: Initializes the condition with a predicate that defines what method
     * accesses should be detected in the transitive dependency analysis.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param checkIfAccessIsViolating Predicate to match the accessed methods
     */
    public TransitivelyAccessesMethodsCondition(DescribedPredicate<? super JavaAccess<?>> checkIfAccessIsViolating) {
        // Provides this condition with a description of the predicate
        super(checkIfAccessIsViolating.getDescription());
        this.checkIfAccessIsViolating = checkNotNull(checkIfAccessIsViolating, "checkIfAccessIsViolating cannot be null");
    }
    //</editor-fold>

    //<editor-fold desc="Helper Methods">

    /**
     * Creates an event indicating a found transitive access path.
     *
     * <p>Description: Constructs a descriptive event with details about the detected path,
     * including the originating class, target method, and intermediate steps.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param access The class access that initiated the path
     * @param transitiveAccessDependencyPath The complete path of accesses leading to the target
     * @return A condition event describing the discovered dependency path
     */
    private static ConditionEvent newTransitiveAccessPathFoundEvent(
            JavaAccess<?> access,
            List<JavaAccess<?>> transitiveAccessDependencyPath
    ) {
        // Indicates if the path is transitive
        boolean hasTransitiveDependencyPath = transitiveAccessDependencyPath.size() > 1;
        StringBuilder messageBuilder = new StringBuilder();
        if (hasTransitiveDependencyPath) {
            messageBuilder.append("transitively ");
        }
        messageBuilder.append("accesses <");
        messageBuilder.append(getLast(transitiveAccessDependencyPath).getTarget().getFullName());
        messageBuilder.append(">");
        // In case the path is transitive, it adds the chain of method calls
        if (hasTransitiveDependencyPath) {
            messageBuilder.append(" by [");
            String joinedChain = transitiveAccessDependencyPath
                    .stream()
                    .map(transitiveAccess -> transitiveAccess.getOrigin().getFullName())
                    .collect(joining("->"));
            messageBuilder.append(joinedChain);
            messageBuilder.append("]");
        }
        // Creates a message indicating the methods that are transitively accessed
        String simpleConditionEventMessage = createMessage(access, messageBuilder.toString());
        // Creates a new event with the message
        return SimpleConditionEvent.satisfied(access, simpleConditionEventMessage);
    }
    //</editor-fold>

    //<editor-fold desc="ArchCondition Method">

    /**
     * Performs the class analysis to find transitive method access paths.
     *
     * <p>Description: Examines all outgoing method calls from the given class and identifies
     * if any of them lead to methods matching the condition predicate through direct or
     * indirect dependencies.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param clazz The class to analyze
     * @param events Collection for recording detected violations or satisfactions
     */
    @Override
    public void check(JavaClass clazz, ConditionEvents events) {
        // Finds all method accesses from the class to check
        for (JavaAccess<?> accessInsideClass : clazz.getAccessesFromSelf()) {
            // Finds all method accesses from the class that are calling a violating method
            List<JavaAccess<?>> transitiveMethodPathToViolatingMethod = transitiveAccessPath.findPathFromViolatingMethodTo(accessInsideClass);
            // If there are any transitive method paths to violating methods, it creates an event
            if (!transitiveMethodPathToViolatingMethod.isEmpty()) {
                events.add(newTransitiveAccessPathFoundEvent(accessInsideClass, transitiveMethodPathToViolatingMethod));
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="TransitiveAccessPath">

    /**
     * Traverses the call graph to find paths to methods matching the predicate.
     *
     * <p>Description: This inner class implements the algorithm for finding transitive
     * access paths through method calls, tracking visited methods to avoid cycles.
     *
     * <p>Design Rationale: Separating the graph traversal logic allows for cleaner code
     * organization and potentially reusing this functionality in other contexts.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @version 2.0.0
     */
    private class TransitiveAccessPath {
        /**
         * Filters accesses from a class to find those matching a specific method name.
         *
         * <p>Description: Used to identify method calls from a class that target a
         * specific method identified by its name.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param javaClass The class whose outgoing accesses are being examined
         * @param specificMethodName The name of the target method to filter for
         * @return Set of matching method accesses from the class
         */
        private Set<JavaAccess<?>> getAccessesFromClassCalledBySpecificMethod(JavaClass javaClass, String specificMethodName) {
            Set<JavaAccess<?>> accessesInsideJavaClass = javaClass.getAccessesFromSelf();
            // Filters the accesses to find those that match the specific method name
            return accessesInsideJavaClass
                    .stream()
                    .filter(accessInsideJavaClass -> {
                        JavaCodeUnit callingMethod = accessInsideJavaClass.getOrigin();
                        return callingMethod.getFullName().equals(specificMethodName);
                    })
                    .collect(toSet());
        }

        /**
         * Finds all outgoing accesses from the target of the given accessInsideJavaClass.
         *
         * <p>Description: Retrieves all method accesses from the class that owns
         * the target method of the supplied accessInsideJavaClass.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param accessInsideJavaClass The method accessInsideJavaClass whose target class is being analyzed
         * @return Set of method accesses originating from the target owner class
         */
        private Set<JavaAccess<?>> getTransitiveAccessesFrom(JavaAccess<?> accessInsideJavaClass) {
            JavaClass relatedClassWhichIsCalledByAccess = accessInsideJavaClass.getTargetOwner();
            AccessTarget relatedMethodWhichIsCalledByAccess = accessInsideJavaClass.getTarget();
            String nameOfRelatedClassWhichIsCalledByAccess = relatedClassWhichIsCalledByAccess.getFullName();
            String nameOfRelatedMethodWhichIsCalledByAccess = relatedMethodWhichIsCalledByAccess.getFullName();
            int lengthOfNameOfRelatedClassWhichIsCalledByAccess = nameOfRelatedClassWhichIsCalledByAccess.length();
            // Removes the class name from the method name
            String specificMethodName = nameOfRelatedMethodWhichIsCalledByAccess.substring(lengthOfNameOfRelatedClassWhichIsCalledByAccess);
            // Finds all method accesses from the class that owns the target method
            return getAccessesFromClassCalledBySpecificMethod(relatedClassWhichIsCalledByAccess, specificMethodName);
        }

        /**
         * Recursively builds a path to a method matching the predicate.
         *
         * <p>Description: Depth-first search algorithm that explores the call graph,
         * tracking visited methods to prevent cycles and building a path when a
         * matching method is violating.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param access Current access being examined
         * @param transitiveAccessPathBuilder Builder for collecting the access path
         * @param namesOfAnalyzedMethods Set of already visited methods to avoid cycles
         * @return True if a path to a violating method was found, false otherwise
         */
        private boolean recursivelyFindPathFromViolatingMethodTo(
                JavaAccess<?> access,
                ImmutableList.Builder<JavaAccess<?>> transitiveAccessPathBuilder,
                Set<String> namesOfAnalyzedMethods
        ) {
            // Checks if the access is violating (matches the predicate).
            // If it is, adds it to the path and returns true
            if (checkIfAccessIsViolating.test(access)) {
                transitiveAccessPathBuilder.add(access);
                return true;
            }
            // If the access is not violating, it checks if the method has already been analyzed to avoid cycles
            else {
                AccessTarget calledMethod = access.getTarget();
                String nameOfCalledMethod = calledMethod.getFullName();
                namesOfAnalyzedMethods.add(nameOfCalledMethod);
                // If the method has not been analyzed, it retrieves all direct accesses to other methods
                for (JavaAccess<?> directAccess : getTransitiveAccessesFrom(access)) {
                    AccessTarget directAccessedMethod = directAccess.getTarget();
                    String nameOfDirectAccessedMethod = directAccessedMethod.getFullName();
                    // Checks if the direct access is not already analyzed and recursively searches for a path
                    if (
                        // Avoid cycles by checking if the method has already been analyzed
                            !namesOfAnalyzedMethods.contains(nameOfDirectAccessedMethod)
                                    // Goes deeper in the call graph
                                    && recursivelyFindPathFromViolatingMethodTo(directAccess, transitiveAccessPathBuilder, namesOfAnalyzedMethods)
                    ) {
                        // If a path to a matching method is found, adds the direct access to the path and returns true
                        transitiveAccessPathBuilder.add(directAccess);
                        return true;
                    }
                }
                // If no path to a matching method is found, return false
                return false;
            }
        }

        /**
         * Discovers a path from the given access to a violating method.
         *
         * <p>Description: Starting from the specified method access, builds a path through
         * the call graph to a violating method, if one exists. The path is built in reverse order.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param access The starting access to analyze
         * @return A list of accesses forming a path to a matching method, or empty if none found
         */
        List<JavaAccess<?>> findPathFromViolatingMethodTo(JavaAccess<?> access) {
            // Creates a new ImmutableList to track visited methods
            ImmutableList.Builder<JavaAccess<?>> transitiveAccessPathBuilder = ImmutableList.builder();
            // Transitively adds accesses to the path, starting from the given access
            recursivelyFindPathFromViolatingMethodTo(access, transitiveAccessPathBuilder, new HashSet<>());
            // Builds the path and reverses the order of the path to show the correct traversal from start to end
            return transitiveAccessPathBuilder.build().reverse();
        }


    }
    //</editor-fold>
}
