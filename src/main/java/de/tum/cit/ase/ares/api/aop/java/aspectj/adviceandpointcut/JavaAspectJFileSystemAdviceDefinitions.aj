package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import org.aspectj.lang.JoinPoint;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public aspect JavaAspectJFileSystemAdviceDefinitions {

    //<editor-fold desc="Tool methods">
    private static Object getValueFromSettings(String fieldName) {
        try {
            // Take standard class loader as class loader in order to get the JavaSecurityTestCaseSettings class at compile time for aspectj
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings");
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
    /**
     * This method checks if the callstack criteria is violated.
     * The callstack criteria is violated if the restricted package is found in the callstack and the calling class is not in the allowed classes.
     *
     * @param restrictedPackage The package that is restricted.
     * @param allowedClasses    The classes that are allowed to access the restricted package.
     * @param readingMethod     The method that is trying to access the restricted package.
     * @return The method that is trying to access the restricted package if the callstack criteria is violated, otherwise null.
     */
    private static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses, String readingMethod) {
        if(readingMethod.startsWith(restrictedPackage)) {
            for (String allowedClass : allowedClasses) {
                if (readingMethod.startsWith(allowedClass)) {
                    return null;
                }
            }
            return readingMethod;
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
    private void checkFileSystemInteraction(
            String action,
            JoinPoint thisJoinPoint
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
        Object[] parameters = thisJoinPoint.getArgs();
        // TODO delete this statement, this is a workaround since the YAML reader doesn't work properly
        if (restrictedPackage == null) {
            restrictedPackage = "de.tum.cit.ase";
        }
        final String fullMethodSignature = thisJoinPoint.getSignature().toLongString();
        String illegallyReadingMethod = allowedPaths == null ? null : checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, thisJoinPoint.getSourceLocation().getWithinType().getName());
        if (illegallyReadingMethod != null) {
            String illegallyReadPath = (parameters == null || parameters.length == 0) ? null : checkIfVariableCriteriaIsViolated(parameters, allowedPaths);
            if (illegallyReadPath != null) {
                throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution):" + illegallyReadingMethod + " tried to illegally " + action + " from " + illegallyReadPath + " via " + fullMethodSignature + "but was blocked by Ares.");
            }
        }
    }
    //</editor-fold>
    //</editor-fold>

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileInputStreamInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.midiSystemMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        checkFileSystemInteraction("read", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        checkFileSystemInteraction("write", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.objectStreamClassMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        checkFileSystemInteraction("execute", thisJoinPoint);
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        checkFileSystemInteraction("delete", thisJoinPoint);
    }

}