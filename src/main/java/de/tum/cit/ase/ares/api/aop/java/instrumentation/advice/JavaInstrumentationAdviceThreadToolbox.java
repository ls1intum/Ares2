package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox.*;

public class JavaInstrumentationAdviceThreadToolbox extends JavaInstrumentationAdviceToolbox {
    private static List<String> threadSystemIgnoreCallstack = List.of();
    private static List<String> threadSystemIgnoreAttributes = List.of();
    private static List<String> threadSystemIgnoreParameter = List.of();

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
                Class<?> resultingClass = variableToClass(variable);
                if (!checkIfClassIsAllowed(threadClassAllowedToBeCreated, threadNumberAllowedToBeCreated, resultingClass)) {
                    return resultingClass.toString();
                }
            } catch (InvalidPathException ignored) {
            }
        }
        return null;




        int positionToDecrement = -1;
        boolean starOperatorExists = false;
        int starOperatorPosition = -1;
        for (Object variable : variables) {
            final String className = variable == null ? "" : variable.getClass().getCanonicalName();
            for (int i = 0; i < threadClassAllowedToBeCreated.length; i++) {
                if (Objects.equals(className, "*")) {
                    starOperatorExists = true;
                    starOperatorPosition = i;
                } else if (Objects.equals(className, threadClassAllowedToBeCreated[i])) {
                    positionToDecrement = i;
                }
            }
            if(positionToDecrement == -1) {
                if(starOperatorExists) {
                    positionToDecrement = starOperatorPosition;
                } else {
                    throw new SecurityException();
                }
            }
            if (threadNumberAllowedToBeCreated[positionToDecrement] == 0) {
                throw new SecurityException();
            } else {
                decrementSettingsArrayValue("threadNumberAllowedToBeCreated", positionToDecrement);
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
        String aopMode = (String) getValueFromSettings("aopMode");
        if (aopMode == null || !aopMode.equals("INSTRUMENTATION")) {
            return;
        }
        String restrictedPackage = (String) getValueFromSettings("restrictedPackage");
        String[] allowedClasses = (String[]) getValueFromSettings("allowedListedClasses");

        String[] threadClassAllowedToBeCreated = (String[]) getValueFromSettings("threadClassAllowedToBeCreated");
        int threadClassAllowedToBeCreatedSize = threadClassAllowedToBeCreated == null ? 0 : threadClassAllowedToBeCreated.length;
        int[] threadNumberAllowedToBeCreated = (int[]) getValueFromSettings("threadNumberAllowedToBeCreated");
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
