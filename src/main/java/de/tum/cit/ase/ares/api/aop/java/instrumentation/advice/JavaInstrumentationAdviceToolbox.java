package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class JavaInstrumentationAdviceToolbox {
    //<editor-fold desc="Constructor">
    private JavaInstrumentationAdviceToolbox() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): JavaInstrumentationAdviceToolbox is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">
    private static Object getValueFromSettings(String fieldName) {
        try {
            // Take bootloader as class loader in order to get the JavaSecurityTestCaseSettings class at bootloading time for instrumentation
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, null);
            Field field = adviceSettingsClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(null);
            field.setAccessible(false);
            return value;
        } catch (LinkageError e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Linkage error while accessing field '" + fieldName + "' in AdviceSettings", e);
        } catch (ClassNotFoundException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Could not find 'JavaSecurityTestCaseSettings' class to access field '" + fieldName + "'", e);
        } catch (NoSuchFieldException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Field '" + fieldName + "' not found in AdviceSettings", e);
        } catch (NullPointerException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Null pointer exception while accessing field '" + fieldName + "' in AdviceSettings", e);
        } catch (IllegalAccessException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Field '" + fieldName + "' is not accessible in AdviceSettings", e);
        } catch (InaccessibleObjectException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Field '" + fieldName + "' is inaccessible in AdviceSettings", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="File system methods">

    //<editor-fold desc="Callstack criteria methods">
    private static boolean checkIfCallstackElementIsAllowed(String[] allowedClasses, StackTraceElement elementToCheck) {
        for (String allowedClass : allowedClasses) {
            if (elementToCheck.getClassName().equals(allowedClass)) {
                return true;
            }
        }
        return false;
    }

    private static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.toString().contains(restrictedPackage)) {
                if (!checkIfCallstackElementIsAllowed(allowedClasses, element)) {
                    return element.toString();
                }
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Variable criteria methods">
    private static Path variableToPath(Object variableValue) {
        return switch (variableValue) {
            case null -> throw new InvalidPathException("null", "Cannot transform to path");
            case Path path -> {
                try {
                    yield path.normalize().toAbsolutePath();
                } catch (InvalidPathException e) {
                    throw new InvalidPathException(path.toString(), "Cannot transform to path");
                }
            }
            case String string -> {
                try {
                    yield Path.of(string).normalize().toAbsolutePath();
                } catch (InvalidPathException e) {
                    throw new InvalidPathException(string, "Cannot transform to path");
                }
            }
            case File file -> {
                try {
                    yield Path.of(file.toURI()).normalize().toAbsolutePath();
                } catch (InvalidPathException e) {
                    throw new InvalidPathException(file.toString(), "Cannot transform to path");
                }
            }
            default -> throw new InvalidPathException(variableValue.toString(), "Cannot transform to path");
        };
    }

    private static boolean checkIfPathIsAllowed(String[] allowedPaths, Path pathToCheck) {
        for (String allowedPath : allowedPaths) {
            if (variableToPath(allowedPath).toString().equals(pathToCheck.toString())) {
                return true;
            }
        }
        return false;
    }

    private static String checkIfVariableCriteriaIsViolated(Object[] variables, String[] allowedPaths) {
        for (Object variable : variables) {
            try {
                Path resultingPath = variableToPath(variable);
                if (!checkIfPathIsAllowed(allowedPaths, resultingPath)) {
                    return resultingPath.toString();
                }
            } catch (InvalidPathException ignored) {
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Check methods">
    public static void checkFileSystemInteraction(
            String action,
            String declaringTypeName,
            String methodName,
            String methodSignature,
            Object[] attributes,
            Object[] parameters
    ) {
        String restrictedPackage = (String) getValueFromSettings("restrictedPackage");
        String[] allowedClasses = (String[]) getValueFromSettings("allowedListedClasses");
        String[] allowedPaths = (String[]) getValueFromSettings(
                switch (action) {
                    case "read" -> "pathsAllowedToBeRead";
                    case "write" -> "pathsAllowedToBeOverwritten";
                    case "execute" -> "pathsAllowedToBeExecuted";
                    case "delete" -> "pathsAllowedToBeDeleted";
                    default -> throw new IllegalArgumentException("Unknown action: " + action);
                }
        );
        // TODO delete this statement, this is a workaround since the YAML reader doesn't work properly
        if (restrictedPackage == null) {
            restrictedPackage = "de.tum.cit.ase";
        }
        final String fullMethodSignature = declaringTypeName + "." + methodName + methodSignature;
        String illegallyReadingMethod = allowedPaths == null ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (illegallyReadingMethod != null) {
            String illegallyReadPath = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths);
            if (illegallyReadPath == null) {
                illegallyReadPath = (attributes == null || attributes.length == 0) ? null : checkIfVariableCriteriaIsViolated(attributes, allowedPaths);
            }
            if (illegallyReadPath != null) {
                throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution):" + illegallyReadingMethod + " tried to illegally " + action + " from " + illegallyReadPath + " via " + fullMethodSignature + "but was blocked by Ares.");
            }
        }
    }
    //</editor-fold>
    //</editor-fold>
}