package de.tum.cit.ase.ares.api.aspectconfiguration.java.advice;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class JavaAdviceToolbox {
    //<editor-fold desc="Constructor">
    private JavaAdviceToolbox() {
        throw new IllegalStateException("Utility class");
    }
    //</editor-fold>

    //<editor-fold desc="File System">
    private static Path variableToPath(Object variableValue) {
        return switch (variableValue) {
            case null -> throw new InvalidPathException("null", "Cannot transform to path");
            case Path path -> path.normalize().toAbsolutePath();
            case String string -> Path.of(string).normalize().toAbsolutePath();
            case File file -> Path.of(file.toURI()).normalize().toAbsolutePath();
            default -> throw new InvalidPathException(variableValue.toString(), "Cannot transform to path");
        };
    }

    private static boolean checkIfPathIsAllowed(String[] allowedPaths, Path pathToCheck) {
        for (String allowedPath : allowedPaths) {
            if (allowedPath.equals(pathToCheck.toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkIfCallstackElementIsAllowed(String[] allowedClasses, StackTraceElement elementToCheck) {
        for (String allowedClass : allowedClasses) {
            if (elementToCheck.getClassName().equals(allowedClass)) {
                return true;
            }
        }
        return false;
    }

    private static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses) {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.toString().contains(restrictedPackage)) {
                if (!checkIfCallstackElementIsAllowed(allowedClasses, element)) {
                    return element.toString();
                }
            }
        }
        return null;
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

    public static void checkFileSystemInteraction(
            String action,
            String declaringTypeName, String methodName, String methodSignature, Object[] attributes, Object[] parameters,
            String restrictedPackage, String[] allowedClasses, String[] allowedPaths
    ) {
        final String fullMethodSignature = String.format("%s.%s%s", declaringTypeName, methodName, methodSignature);
        String illegallyReadingMethod = checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses);
        if (illegallyReadingMethod != null) {
            String illegallyReadPath = checkIfVariableCriteriaIsViolated(attributes, allowedPaths);
            if (illegallyReadPath == null) {
                illegallyReadPath = checkIfVariableCriteriaIsViolated(parameters, allowedPaths);
            }
            if (illegallyReadPath != null) {
                throw new SecurityException(String.format("%s tried to illegally %s from %s via %s", illegallyReadingMethod, action, illegallyReadPath, fullMethodSignature));
            }
        }
    }
    //</editor-fold>
}