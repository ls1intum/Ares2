package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.aspectj.lang.JoinPoint;

/**
 * Block class-loading manipulation from student code in BLOCKED_ALL mode.
 *
 * <p>Description: ArchUnit catches all classloader references statically; WALA does not.
 * In {@code WALA_AND_ASPECTJ}, classloader operations from the restricted student package
 * fall through to here and are rejected with {@link SecurityException}.
 */
@SuppressWarnings("AopLanguageInspection")
public aspect JavaAspectJClassLoadingAdviceDefinitions extends JavaAspectJAbstractAdviceDefinitions {

    before(): JavaAspectJClassLoadingPointcutDefinitions.classLoadingMethods() {
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
                        + systemMethodToCheck + " tried to illegally manipulate class loading via "
                        + fullMethodSignature
                        + (studentCalledMethod == null ? "" : " (called by " + studentCalledMethod + ")")
                        + " but was blocked by Ares.");
    }
}
