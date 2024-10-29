package de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile;

//<editor-fold desc="Imports">

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.thirdparty.com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.thirdparty.com.google.common.base.Preconditions.checkNotNull;
import static com.tngtech.archunit.thirdparty.com.google.common.collect.Iterables.getLast;
import static de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile.CustomClassResolver.getImmediateSubclasses;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
//</editor-fold>

/**
 * Checks that a class transitively accesses methods that match a given predicate.
 */
public class TransitivelyAccessesMethodsCondition extends ArchCondition<JavaClass> {
    private static final Logger log = LoggerFactory.getLogger(TransitivelyAccessesMethodsCondition.class);
    //<editor-fold desc="Attributes">
    /**
     * Predicate to match the accessed methods
     */
    private final DescribedPredicate<? super JavaAccess<?>> conditionPredicate;

    /**
     * Transitive access path to find the path to the accessed method
     */
    private final TransitiveAccessPath transitiveAccessPath = new TransitiveAccessPath();
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * @param conditionPredicate Predicate to match the accessed methods
     */
    public TransitivelyAccessesMethodsCondition(DescribedPredicate<? super JavaAccess<?>> conditionPredicate) {
        super("transitively depend on classes that " + conditionPredicate.getDescription());
        this.conditionPredicate = checkNotNull(conditionPredicate);
    }
    //</editor-fold>

    /**
     * Checks that a class transitively accesses methods that match a given predicate.
     */
    @Override
    public void check(JavaClass item, ConditionEvents events) {
        long start = System.currentTimeMillis();
        for (JavaAccess<?> target : item.getAccessesFromSelf()) {
            List<JavaAccess<?>> dependencyPath = transitiveAccessPath.findPathTo(target);
            if (!dependencyPath.isEmpty()) {
                events.add(newTransitiveAccessPathFoundEvent(target, dependencyPath));
            }
        }
        long end = System.currentTimeMillis();
        log.info("Transitive access check took {} ms for class {}", end - start, item.getFullName());
    }

    /**
     * @return a satisfied event if a transitive dependency path was found, a violated event otherwise
     */
    private static ConditionEvent newTransitiveAccessPathFoundEvent(JavaAccess<?> javaClass, List<JavaAccess<?>> transitiveDependencyPath) {
        String message = (transitiveDependencyPath.size() > 1 ? "transitively " : "") + "accesses <" + getLast(transitiveDependencyPath).getTarget().getFullName() + ">";

        if (transitiveDependencyPath.size() > 1) {
            message += " by [" +
                    transitiveDependencyPath
                            .stream()
                            .map(access -> access.getOrigin().getFullName())
                            .collect(joining("->")) +
                    "]";
        }

        return SimpleConditionEvent.satisfied(javaClass, createMessage(javaClass, message));
    }

    private class TransitiveAccessPath {
        /**
         * @return some outgoing transitive dependency path to the supplied class or empty if there is none
         */
        List<JavaAccess<?>> findPathTo(JavaAccess<?> method) {
            ImmutableList.Builder<JavaAccess<?>> transitivePath = ImmutableList.builder();
            addAccessesToPathFrom(method, transitivePath, new HashSet<>());
            return transitivePath.build().reverse();
        }

        /**
         * Adds all accesses to the same target as the supplied method to the supplied path
         */
        private boolean addAccessesToPathFrom(
                JavaAccess<?> method,
                ImmutableList.Builder<JavaAccess<?>> transitivePath,
                Set<String> analyzedMethods
        ) {
            if (conditionPredicate.test(method)) {
                transitivePath.add(method);
                return true;
            }

            analyzedMethods.add(method.getTarget().getFullName());

            for (JavaAccess<?> access : getDirectAccessTargetsOutsideOfAnalyzedClasses(method)) {
                if (!analyzedMethods.contains(access.getTarget().getFullName()) &&
                        addAccessesToPathFrom(access, transitivePath, analyzedMethods)
                ) {
                    transitivePath.add(method);
                    return true;
                }
            }

            return false;
        }

        /**
         * We are currently using CHA, which resolves all the subclasses of a given class
         *
         * @return all accesses to the same target as the supplied item that are not in the analyzed classes
         */
        private Set<JavaAccess<?>> getDirectAccessTargetsOutsideOfAnalyzedClasses(JavaAccess<?> item) {
            // Get all subclasses of the target owner including the target owner
            JavaClass resolvedTarget = resolveTargetOwner(item.getTargetOwner());

            /**
             * We are currently using CHA, which resolves all the subclasses of a given class
             * @return all accesses to the same target as the supplied item that are not in the analyzed classes
             * Resolves immediate subclasses of the target class to find potential method implementations
             * @param item The access to analyze + * @return all accesses to the same target as the supplied item from immediate subclasses
             */
            Set<JavaClass> directSubclasses = new HashSet<>(getImmediateSubclasses(item.getTargetOwner().getFullName()));
            directSubclasses.add(resolvedTarget);

            return directSubclasses.stream()
                    .map(javaClass ->
                            getAccessesFromClass(javaClass, item.getTarget().getFullName().substring(item.getTargetOwner().getFullName().length())))
                    .flatMap(Set::stream)
                    .collect(toSet());
        }

        private Set<JavaAccess<?>> getAccessesFromClass(JavaClass javaClass, String methodName) {
            return javaClass.getAccessesFromSelf()
                    .stream()
                    .filter(a -> a
                            .getOrigin()
                            .getFullName()
                            .substring(javaClass.getFullName().length())
                            .equals(methodName))
                    .collect(toSet());
        }

        private JavaClass resolveTargetOwner(JavaClass targetOwner) {
            Optional<JavaClass> resolvedTarget = CustomClassResolver.tryResolve(targetOwner.getFullName());
            return resolvedTarget.orElse(targetOwner);
        }
    }
}
