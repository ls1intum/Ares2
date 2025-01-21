package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.util.List;
import java.nio.file.InvalidPathException;

public class JavaInstrumentationAdviceThreadToolbox extends JavaInstrumentationAdviceToolbox {
    private final static List<String> threadSystemIgnoreCallstack = List.of();
    private final static List<String> threadSystemIgnoreAttributes = List.of();
    private final static List<String> threadSystemIgnoreParameter = List.of();

    //<editor-fold desc="Constructor">

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private JavaInstrumentationAdviceThreadToolbox() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): JavaInstrumentationAdviceToolbox is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Thread system methods">

    //<editor-fold desc="Variable criteria methods">

    /**
     * Returns the canonical name of the given object's class, unless it is identified as a
     * synthetic lambda class (i.e., the class name contains <code>$$Lambda$</code>), in which case
     * the string <code>"Lambda-Expression"</code> is returned. If the provided object is
     * <code>null</code>, an {@link InvalidPathException} is thrown to indicate an invalid (null) path.
     *
     * @param variableValue The object whose class name is to be determined. Must not be <code>null</code>.
     * @return The canonical name of the object's class, or <code>"Lambda-Expression"</code> if the
     *         class is a synthetic lambda.
     * @throws InvalidPathException If <code>variableValue</code> is <code>null</code>.
     */
    private static String variableToClassname(Object variableValue) {
        if (variableValue == null) {
            throw new InvalidPathException("null", localize("security.advice.transform.path.exception"));
        }
        Class<?> variableClass = variableValue.getClass();
        if (variableClass.isSynthetic() && variableClass.getName().contains("$$Lambda$")) {
            return "Lambda-Expression";
        } else {
            return variableClass.getCanonicalName();
        }
    }

    /**
     * Checks whether the thread limit for the class at the specified index is still available.
     * If the count at that index is greater than zero, this method decrements it and returns
     * <code>false</code> (indicating it was allowed). If the count is zero or below, it returns
     * <code>true</code> (indicating the class is disallowed because the quota is exhausted).
     *
     * @param threadNumberAllowedToBeCreated An array of permissible thread counts, parallel to
     *                                       the class array that determines which classes can create threads.
     * @param index                          The index corresponding to the class being checked.
     * @return <code>true</code> if no more threads can be created (disallowed), or
     *         <code>false</code> if the class is allowed to create another thread and the count was decremented.
     */
    private static boolean handleFoundClassIsAllowed(int[] threadNumberAllowedToBeCreated, int index) {
        boolean threadDisallowed = threadNumberAllowedToBeCreated[index] <= 0;
        if(!threadDisallowed) {
            decrementSettingsArrayValue("threadNumberAllowedToBeCreated", index);
        }
        return threadDisallowed;
    }

    /**
     * Determines if the specified <code>resultingClassname</code> is permitted to create a thread.
     * This method searches for an exact match of <code>resultingClassname</code> in the
     * <code>threadClassAllowedToBeCreated</code> array. If found, it checks whether there is
     * a remaining quota at that index via {@link #handleFoundClassIsAllowed(int[], int)}.
     * <p>
     * If no exact match is found, but a wildcard (<code>"*"</code>) entry is present in
     * <code>threadClassAllowedToBeCreated</code>, the method treats it as a fallback match
     * and checks its corresponding quota.
     * <p>
     * If a quota is available, it is decremented, and this method returns <code>true</code>.
     * Otherwise, it returns <code>false</code>.
     *
     * @param threadClassAllowedToBeCreated  An array of allowed class names or a wildcard string (<code>"*"</code>).
     * @param threadNumberAllowedToBeCreated An array specifying how many threads each corresponding class may create.
     * @param resultingClassname             The class name of the thread being requested.
     * @return <code>true</code> if the specified class name (or the wildcard) still has quota left and was decremented,
     *         or <code>false</code> if the class is not allowed or its quota is exhausted.
     */
    private static boolean checkIfClassIsAllowed(String[] threadClassAllowedToBeCreated, int[] threadNumberAllowedToBeCreated,  String resultingClassname) {
        int starIndex = -1;
        for (int i = 0; i < threadClassAllowedToBeCreated.length; i++) {
            String allowedClass = threadClassAllowedToBeCreated[i];
            if ("*".equals(allowedClass)) {
                starIndex = i;
            } else if (resultingClassname.equals(allowedClass)) {
                return handleFoundClassIsAllowed(threadNumberAllowedToBeCreated, i);
            }
        }
        if (starIndex != -1) {
            return handleFoundClassIsAllowed(threadNumberAllowedToBeCreated, starIndex);
        }
        return false;
    }

    /**
     * Check if the variable criteria is violated.
     *
     * @param variables    The variables to check.
     * @param threadClassAllowedToBeCreated The thread classes that are allowed to be created.
     * @param threadNumberAllowedToBeCreated The number of threads allowed to be created.
     * @return The path that violates the criteria, null if no violation occurred.
     */
    private static String checkIfVariableCriteriaIsViolated(Object[] variables, String[] threadClassAllowedToBeCreated, int[] threadNumberAllowedToBeCreated) {
        for (Object variable : variables) {
            try {
                String resultingClassname = variableToClassname(variable);
                if (!checkIfClassIsAllowed(threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated, resultingClassname)) {
                    return resultingClassname;
                }
            } catch (InvalidPathException ignored) {
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Check methods">

    /**
     * Check if the thread system interaction is allowed according to security policies.
     * This method verifies that the specified thread system action (create) complies
     * with the allowed name, number and call stack criteria. If any violation is detected, a SecurityException is thrown.
     * It checks if the action is restricted based on the method call, attributes, and parameters. If a method
     * violates the file system security rules, the action is blocked.
     *
     * @param action            The file system action being performed (e.g., read, write, execute, delete).
     * @param declaringTypeName The name of the class declaring the method.
     * @param methodName        The name of the method being invoked.
     * @param methodSignature   The signature of the method.
     * @param attributes        The attributes of the method (if any).
     * @param parameters        The parameters of the method (if any).
     * @throws SecurityException If the file system interaction is found to be unauthorized.
     */
    public static void checkThreadSystemInteraction(
            String action,
            String declaringTypeName,
            String methodName,
            String methodSignature,
            Object[] attributes,
            Object[] parameters
    ) {
        String aopMode = getValueFromSettings("aopMode");
        if (aopMode == null || !aopMode.equals("INSTRUMENTATION")) {
            return;
        }
        String restrictedPackage = getValueFromSettings("restrictedPackage");
        String[] allowedClasses = getValueFromSettings("allowedListedClasses");

        String[] threadClassAllowedToBeCreated = getValueFromSettings("threadClassAllowedToBeCreated");
        int threadClassAllowedToBeCreatedSize = threadClassAllowedToBeCreated == null ? 0 : threadClassAllowedToBeCreated.length;
        int[] threadNumberAllowedToBeCreated = getValueFromSettings("threadNumberAllowedToBeCreated");
        int threadNumberAllowedToBeCreatedSize = threadNumberAllowedToBeCreated == null ? 0 : threadNumberAllowedToBeCreated.length;

        if (threadNumberAllowedToBeCreatedSize != threadClassAllowedToBeCreatedSize) {
            throw new SecurityException(localize("security.advice.thread.allowed.size", threadNumberAllowedToBeCreatedSize, threadClassAllowedToBeCreatedSize));
        }

        final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
        String illegallyCreatingMethod = threadClassAllowedToBeCreated == null ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (illegallyCreatingMethod != null) {
            String illegallyCreatedThread = null;
            if (!threadSystemIgnoreParameter.contains(fullMethodSignature + "." + methodName)) {
                illegallyCreatedThread = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated);
            }
            if (illegallyCreatedThread == null && !threadSystemIgnoreAttributes.contains(fullMethodSignature + "." + methodName)) {
                illegallyCreatedThread = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(new String[]{declaringTypeName}, threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated);
            }
            if (illegallyCreatedThread != null) {
                throw new SecurityException(localize("security.advice.illegal.method.execution", illegallyCreatingMethod, action, illegallyCreatedThread, fullMethodSignature));
            }
        }
    }
    //</editor-fold>
    //</editor-fold>
}
