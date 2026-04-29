package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.aspectj.lang.JoinPoint;

/**
 * Block reflection use from student code in BLOCKED_ALL mode.
 *
 * <p>Description: ArchUnit's syntactic rule {@code NO_CLASS_MUST_USE_REFLECTION} catches all
 * reflection references at architecture-check time, so {@code ARCHUNIT_AND_*} configs never
 * reach the runtime advice for this category. WALA's reachability analysis is too narrow to
 * catch dynamic reflection calls; runs under {@code WALA_AND_ASPECTJ} fall through to here,
 * which throws {@link SecurityException} when the call originates from the restricted
 * student package.
 *
 * <p>Design Rationale: PERMITTED and BLOCKED_PARTIAL skip the {@code ReflectiveAccessTest}
 * test class via {@code skip-tests.json}, so this aspect only ever fires for BLOCKED_ALL.
 * No allowed-list lookup is needed.
 */
@SuppressWarnings("AopLanguageInspection")
public aspect JavaAspectJReflectionAdviceDefinitions extends JavaAspectJAbstractAdviceDefinitions {

    before(): JavaAspectJReflectionPointcutDefinitions.reflectionMethods() {
        @Nullable final String aopMode = getValueFromSettings("aopMode");
        if (aopMode == null || !aopMode.equals("ASPECTJ")) {
            return;
        }
        @Nullable final String restrictedPackage = getValueFromSettings("restrictedPackage");
        @Nullable final String[] allowedClasses = getValueFromSettings("allowedListedClasses");
        @Nonnull final String declaringTypeName = thisJoinPoint.getSignature().getDeclaringTypeName();
        @Nonnull final String methodName = thisJoinPoint.getSignature().getName();
        @Nullable String systemMethodToCheck = (restrictedPackage == null) ? null
                : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, declaringTypeName, methodName);
        if (systemMethodToCheck == null) {
            return;
        }
        @Nullable String studentCalledMethod = findFirstMethodOutsideOfRestrictedPackage(restrictedPackage);
        @Nonnull String fullMethodSignature = formatSignature(thisJoinPoint.getSignature());
        throw new SecurityException(
                "Ares Security Error (Reason: Student-Code; Stage: Execution): "
                        + systemMethodToCheck + " tried to illegally use reflection via "
                        + fullMethodSignature
                        + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
                        + " but was blocked by Ares.");
    }
}
