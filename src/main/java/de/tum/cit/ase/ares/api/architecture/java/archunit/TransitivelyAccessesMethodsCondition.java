package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.thirdparty.com.google.common.base.Preconditions;
import com.tngtech.archunit.thirdparty.com.google.common.collect.Iterables;
//</editor-fold>

/**
 * An Archunit condition that checks if classes transitively access methods
 * matching a predicate.
 * <p>
 * Description: This class extends ArchCondition to analyse class dependencies
 * and detect methods that match specific criteria through transitive
 * dependencies. It tracks access paths to identify how classes are connected
 * through method calls.
 * <p>
 * Design Rationale: Using transitive dependency analysis allows detecting
 * security-relevant method calls even if they occur indirectly through multiple
 * layers of dependencies, which is crucial for security analysis in the Ares
 * framework.
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
public class TransitivelyAccessesMethodsCondition extends ArchCondition<JavaClass> {
	// <editor-fold desc="Attributes">
	/**
	 * Predicate used to identify method accesses, which is violating.
	 * <p>
	 * Description: This predicate defines the criteria for determining which method
	 * accesses is violating (calling a blocked method) in the transitive dependency
	 * analysis.
	 */
	private final DescribedPredicate<? super JavaAccess<?>> checkIfAccessIsViolating;

	private final Map<String, List<JavaAccess<?>>> outgoingAccessesByMethod = new HashMap<>();

	private final Map<String, List<JavaAccess<?>>> violationPathsByMethod = new HashMap<>();

	private final Set<String> methodsWithoutViolation = new HashSet<>();

	private final Map<JavaAccess<?>, Boolean> violationByAccess = new IdentityHashMap<>();

	private final Set<JavaClass> indexedClasses = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * Helper class that finds paths through method call dependencies.
	 * <p>
	 * Description: Maintains state and logic for traversing the access graph to
	 * find connections between methods and build dependency paths.
	 */
	private final TransitiveAccessPath transitiveAccessPath = new TransitiveAccessPath();
	// </editor-fold>

	// <editor-fold desc="Constructor">

	/**
	 * Creates a condition to check for transitive method access matching a
	 * predicate.
	 * <p>
	 * Description: Initializes the condition with a predicate that defines what
	 * method accesses should be detected in the transitive dependency analysis.
	 *
	 * @since 2.0.0
	 * @author Sarp Sahinalp
	 * @param checkIfAccessIsViolating Predicate to match the accessed methods
	 */
	public TransitivelyAccessesMethodsCondition(DescribedPredicate<? super JavaAccess<?>> checkIfAccessIsViolating) {
		// Provides this condition with a description of the predicate
		super(checkIfAccessIsViolating.getDescription());
		this.checkIfAccessIsViolating = Preconditions.checkNotNull(checkIfAccessIsViolating,
				"checkIfAccessIsViolating must not be null");
	}
	// </editor-fold>

	// <editor-fold desc="Helper Methods">

	/**
	 * Creates an event indicating a found transitive access path.
	 * <p>
	 * Description: Constructs a descriptive event with details about the detected
	 * path, including the originating class, target method, and intermediate steps.
	 *
	 * @since 2.0.0
	 * @author Sarp Sahinalp
	 * @param access                         The class access that initiated the
	 *                                       path
	 * @param transitiveAccessDependencyPath The complete path of accesses leading
	 *                                       to the target
	 * @return A condition event describing the discovered dependency path
	 */
	private static ConditionEvent newTransitiveAccessPathFoundEvent(JavaAccess<?> access,
			List<JavaAccess<?>> transitiveAccessDependencyPath) {
		// Indicates if the path is transitive
		boolean hasTransitiveDependencyPath = transitiveAccessDependencyPath.size() > 1;
		StringBuilder messageBuilder = new StringBuilder();
		if (hasTransitiveDependencyPath) {
			messageBuilder.append("transitively ");
		}
		messageBuilder.append("accesses <");
		messageBuilder.append(Iterables.getLast(transitiveAccessDependencyPath).getTarget().getFullName());
		messageBuilder.append(">");
		// In case the path is transitive, it adds the chain of method calls
		if (hasTransitiveDependencyPath) {
			messageBuilder.append(" by [");
			String joinedChain = transitiveAccessDependencyPath.stream()
					.map(transitiveAccess -> transitiveAccess.getOrigin().getFullName())
					.collect(Collectors.joining("->"));
			messageBuilder.append(joinedChain);
			messageBuilder.append("]");
		}
		// Creates a message indicating the methods that are transitively accessed
		String simpleConditionEventMessage = ConditionEvent.createMessage(access, messageBuilder.toString());
		// Creates a new event with the message
		return SimpleConditionEvent.satisfied(access, simpleConditionEventMessage);
	}
	// </editor-fold>

	// <editor-fold desc="ArchCondition Method">

	/**
	 * Performs the class analysis to find transitive method access paths.
	 * <p>
	 * Description: Examines all outgoing method calls from the given class and
	 * identifies if any of them lead to methods matching the condition predicate
	 * through direct or indirect dependencies.
	 *
	 * @since 2.0.0
	 * @author Sarp Sahinalp
	 * @param clazz  The class to analyse
	 * @param events Collection for recording detected violations or satisfactions
	 */
	@Override
	public void check(JavaClass clazz, ConditionEvents events) {
		// Finds all method accesses from the class to check
		for (JavaAccess<?> accessInsideClass : clazz.getAccessesFromSelf()) {
			// Finds all method accesses from the class that are calling a violating method
			List<JavaAccess<?>> transitiveMethodPathToViolatingMethod = transitiveAccessPath
					.findPathFromViolatingMethodTo(accessInsideClass);
			// If there are any transitive method paths to violating methods, it creates an
			// event
			if (!transitiveMethodPathToViolatingMethod.isEmpty()) {
				events.add(newTransitiveAccessPathFoundEvent(accessInsideClass, transitiveMethodPathToViolatingMethod));
			}
		}
	}

	@Override
	public void init(Collection<JavaClass> allObjectsToTest) {
		outgoingAccessesByMethod.clear();
		violationPathsByMethod.clear();
		methodsWithoutViolation.clear();
		violationByAccess.clear();
		indexedClasses.clear();
	}
	// </editor-fold>

	// <editor-fold desc="TransitiveAccessPath">

	/**
	 * Traverses the call graph to find paths to methods matching the predicate.
	 * <p>
	 * Description: This inner class implements the algorithm for finding transitive
	 * access paths through method calls, tracking visited methods to avoid cycles.
	 * <p>
	 * Design Rationale: Separating the graph traversal logic allows for cleaner
	 * code organization and potentially reusing this functionality in other
	 * contexts.
	 *
	 * @since 2.0.0
	 * @author Sarp Sahinalp
	 * @version 2.0.0
	 */
	private class TransitiveAccessPath {
		/**
		 * Filters accesses from a class to find those matching a specific method name.
		 * <p>
		 * Description: Used to identify method calls from a class that target a
		 * specific method identified by its name.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param javaClass          The class whose outgoing accesses are being
		 *                           examined
		 * @param specificMethodName The name of the target method to filter for
		 * @return matching method accesses from the class
		 */
		private List<JavaAccess<?>> getAccessesFromClassCalledBySpecificMethod(JavaClass javaClass,
				String specificMethodName) {
			indexClass(javaClass);
			return outgoingAccessesByMethod.getOrDefault(specificMethodName, List.of());
		}

		private void indexClass(JavaClass javaClass) {
			if (!indexedClasses.add(javaClass)) {
				return;
			}
			Map<String, List<JavaAccess<?>>> accessesByOrigin = new LinkedHashMap<>();
			for (JavaAccess<?> access : javaClass.getAccessesFromSelf()) {
				JavaCodeUnit origin = access.getOrigin();
				accessesByOrigin.computeIfAbsent(origin.getFullName(), unusedKey -> new ArrayList<>()).add(access);
			}
			accessesByOrigin.forEach((method, accesses) -> outgoingAccessesByMethod.put(method, List.copyOf(accesses)));
		}

		/**
		 * Classifies an imported access once and reuses the result when another path
		 * reaches the same access.
		 *
		 * @param access access to classify
		 * @return whether the access is forbidden
		 */
		private boolean isViolating(JavaAccess<?> access) {
			return violationByAccess.computeIfAbsent(access, checkIfAccessIsViolating::test);
		}

		private Optional<List<JavaAccess<?>>> findPathFromMethod(String startingMethod, JavaClass startingClass) {
			if (methodsWithoutViolation.contains(startingMethod)) {
				return Optional.empty();
			}
			List<JavaAccess<?>> cachedPath = violationPathsByMethod.get(startingMethod);
			if (cachedPath != null) {
				return Optional.of(cachedPath);
			}

			Queue<MethodNode> methodsToVisit = new ArrayDeque<>();
			Map<String, JavaAccess<?>> parentAccessByMethod = new HashMap<>();
			Map<String, MethodNode> visitedMethods = new LinkedHashMap<>();
			MethodNode startingNode = new MethodNode(startingMethod, startingClass);
			methodsToVisit.add(startingNode);
			visitedMethods.put(startingMethod, startingNode);

			while (!methodsToVisit.isEmpty()) {
				MethodNode currentMethod = methodsToVisit.remove();
				List<JavaAccess<?>> currentCachedPath = violationPathsByMethod.get(currentMethod.name());
				if (currentCachedPath != null) {
					List<JavaAccess<?>> path = pathTo(currentMethod.name(), startingMethod, parentAccessByMethod);
					path.addAll(currentCachedPath);
					cacheViolationPath(startingMethod, path);
					return Optional.of(List.copyOf(path));
				}
				if (methodsWithoutViolation.contains(currentMethod.name())) {
					continue;
				}

				for (JavaAccess<?> outgoingAccess : getAccessesFromClassCalledBySpecificMethod(currentMethod.owner(),
						currentMethod.name())) {
					if (isViolating(outgoingAccess)) {
						List<JavaAccess<?>> path = pathTo(currentMethod.name(), startingMethod, parentAccessByMethod);
						path.add(outgoingAccess);
						cacheViolationPath(startingMethod, path);
						return Optional.of(List.copyOf(path));
					}
					String targetMethod = outgoingAccess.getTarget().getFullName();
					JavaClass targetClass = outgoingAccess.getTargetOwner();
					if (targetClass != null && !visitedMethods.containsKey(targetMethod)
							&& !methodsWithoutViolation.contains(targetMethod)) {
						MethodNode targetNode = new MethodNode(targetMethod, targetClass);
						visitedMethods.put(targetMethod, targetNode);
						parentAccessByMethod.put(targetMethod, outgoingAccess);
						methodsToVisit.add(targetNode);
					}
				}
			}

			methodsWithoutViolation.addAll(visitedMethods.keySet());
			return Optional.empty();
		}

		private List<JavaAccess<?>> pathTo(String targetMethod, String startingMethod,
				Map<String, JavaAccess<?>> parentAccessByMethod) {
			List<JavaAccess<?>> reversedPath = new ArrayList<>();
			String currentMethod = targetMethod;
			while (!currentMethod.equals(startingMethod)) {
				JavaAccess<?> parentAccess = parentAccessByMethod.get(currentMethod);
				if (parentAccess == null) {
					break;
				}
				reversedPath.add(parentAccess);
				currentMethod = parentAccess.getOrigin().getFullName();
			}
			Collections.reverse(reversedPath);
			return reversedPath;
		}

		private void cacheViolationPath(String startingMethod, List<JavaAccess<?>> path) {
			violationPathsByMethod.put(startingMethod, List.copyOf(path));
			for (int index = 0; index < path.size(); index++) {
				JavaAccess<?> access = path.get(index);
				violationPathsByMethod.putIfAbsent(access.getOrigin().getFullName(),
						List.copyOf(path.subList(index, path.size())));
			}
		}

		/**
		 * Discovers a path from the given access to a violating method.
		 * <p>
		 * Description: Starting from the specified method access, builds a path through
		 * the call graph to a violating method, if one exists. The path is built in
		 * traversal order.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param access The starting access to analyse
		 * @return A list of accesses forming a path to a matching method, or empty if
		 *         none found
		 */
		List<JavaAccess<?>> findPathFromViolatingMethodTo(JavaAccess<?> access) {
			if (isViolating(access)) {
				return List.of(access);
			}
			AccessTarget target = access.getTarget();
			JavaClass targetClass = access.getTargetOwner();
			if (targetClass == null) {
				return List.of();
			}
			Optional<List<JavaAccess<?>>> suffix = findPathFromMethod(target.getFullName(), targetClass);
			if (suffix.isEmpty()) {
				return List.of();
			}
			List<JavaAccess<?>> completePath = new ArrayList<>(suffix.get().size() + 1);
			completePath.add(access);
			completePath.addAll(suffix.get());
			return List.copyOf(completePath);
		}
	}

	private record MethodNode(String name, JavaClass owner) {
	}
	// </editor-fold>
}
