package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.thirdparty.com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.thirdparty.com.google.common.base.Preconditions.checkNotNull;
import static com.tngtech.archunit.thirdparty.com.google.common.collect.Iterables.getLast;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * Checks that a class transitively accesses methods that match a given predicate.
 */
public class TransitivelyAccessesMethodsCondition extends ArchCondition<JavaClass> {

    private final DescribedPredicate<? super JavaAccess<?>> conditionPredicate;
    private final TransitiveAccessPath transitiveAccessPath = new TransitiveAccessPath();

    /**
     * @param conditionPredicate Predicate to match the accessed methods
     */
    public TransitivelyAccessesMethodsCondition(DescribedPredicate<? super JavaAccess<?>> conditionPredicate) {
        super("transitively depend on classes that " + conditionPredicate.getDescription());

        this.conditionPredicate = checkNotNull(conditionPredicate);
    }

    /**
     * Checks that a class transitively accesses methods that match a given predicate.
     */
    @Override
    public void check(JavaClass item, ConditionEvents events) {
        boolean hastTransitiveAccess = false;
        for (JavaAccess<?> target : item.getAccessesFromSelf()) {
            List<JavaAccess<?>> dependencyPath = transitiveAccessPath.findPathTo(target);
            if (!dependencyPath.isEmpty()) {
                events.add(newTransitiveAccessPathFoundEvent(target, dependencyPath));
                hastTransitiveAccess = true;
            }
        }
        if (!hastTransitiveAccess) {
            events.add(newNoTransitiveDependencyPathFoundEvent(item));
        }
    }

    /**
     * @return a satisfied event if a transitive dependency path was found, a violated event otherwise
     */
    private static ConditionEvent newTransitiveAccessPathFoundEvent(JavaAccess<?> javaClass, List<JavaAccess<?>> transitiveDependencyPath) {
        String message = String.format("%saccesses <%s>",
                transitiveDependencyPath.size() > 1 ? "transitively " : "",
                getLast(transitiveDependencyPath).getTarget().getFullName());

        if (transitiveDependencyPath.size() > 1) {
            message += " by [" + transitiveDependencyPath.stream().map(access -> access.getOrigin().getFullName()).collect(joining("->")) + "]";
        }

        return SimpleConditionEvent.satisfied(javaClass, createMessage(javaClass, message));
    }

    /**
     * @return a violated event if no transitive dependency path was found
     */
    private static ConditionEvent newNoTransitiveDependencyPathFoundEvent(JavaClass javaClass) {
        return SimpleConditionEvent.violated(javaClass, createMessage(javaClass, "does not transitively depend on any matching class"));
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
                if (!analyzedMethods.contains(access.getTarget().getFullName()) && addAccessesToPathFrom(access, transitivePath, analyzedMethods)) {
                    transitivePath.add(method);
                    return true;
                }
            }

            return false;
        }

        /**
         * @return all accesses to the same target as the supplied item that are not in the analyzed classes
         */
        private Set<JavaAccess<?>> getDirectAccessTargetsOutsideOfAnalyzedClasses(JavaAccess<?> item) {
            return item.getTargetOwner().getAccessesFromSelf()
                    .stream()
                    .filter(a -> a.getOrigin().getFullName().equals(item.getTarget().getFullName())).collect(toSet());
        }
    }
}
