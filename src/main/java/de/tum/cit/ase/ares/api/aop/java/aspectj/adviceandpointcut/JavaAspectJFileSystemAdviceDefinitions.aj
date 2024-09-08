package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import org.aspectj.lang.JoinPoint;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public aspect JavaAspectJFileSystemAdviceDefinitions {

    //<editor-fold desc="Callstack criteria methods">
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
            case Path path -> path.normalize().toAbsolutePath();
            case String string -> Path.of(string).normalize().toAbsolutePath();
            case File file -> Path.of(file.toURI()).normalize().toAbsolutePath();
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

    private static Object getValueFromSettings(String fieldName) {
        try {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings");
            Field field = adviceSettingsClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(null);
            field.setAccessible(false);
            return value;
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new SecurityException("Cannot read field " + fieldName + " in AdviceSettings", e);
        }
    }

    // This method handles the security check for file system interactions by validating if the requested operation type is allowed for the file in context.
    private void checkFileSystemInteraction(JoinPoint thisJoinPoint, String action) {
        String restrictedPackage = (String) getValueFromSettings("restrictedPackage");
        String[] allowedClasses = (String[]) getValueFromSettings("allowedListedClasses");
        String[] allowedPaths = (String[]) getValueFromSettings(
                switch (action) {
                    case "read" -> "pathsAllowedToBeRead";
                    case "write" -> "pathsAllowedToBeOverwritten";
                    case "execute" -> "pathsAllowedToBeExecuted";
                    default -> throw new IllegalArgumentException("Unknown action: " + action);
                }
        );
        Object[] parameters = thisJoinPoint.getArgs();

        if (restrictedPackage == null
                || allowedPaths == null
                || (parameters == null || parameters.length == 0)
        ) {
            return;
        }

        final String fullMethodSignature = thisJoinPoint.getSignature().toLongString();
        String illegallyReadingMethod = checkIfCallstackCriteriaIsViolated(restrictedPackage, allowedClasses, thisJoinPoint.getSourceLocation().getWithinType().getName());
        if (illegallyReadingMethod != null) {
            String illegallyReadPath = checkIfVariableCriteriaIsViolated(parameters, allowedPaths);
            if (illegallyReadPath != null) {
                throw new SecurityException(illegallyReadingMethod + " tried to illegally " + action + " from " + illegallyReadPath + " via " + fullMethodSignature);
            }
        }
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileInputStreamInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.midiSystemMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsReadMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        checkFileSystemInteraction(thisJoinPoint, "read");
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        checkFileSystemInteraction(thisJoinPoint, "write");
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
        checkFileSystemInteraction(thisJoinPoint, "execute");
    }

    before():
            de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesDeleteMethods() ||
                    de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        checkFileSystemInteraction(thisJoinPoint, "delete");
    }

}