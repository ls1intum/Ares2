package %s.aop.java.aspectj.adviceandpointcut;

import org.aspectj.lang.JoinPoint;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public aspect JavaAspectJFileSystemAdviceDefinitions {

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
            if (allowedPath.equals(pathToCheck.toString())) {
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
            Class<?> adviceSettingsClass = Class.forName("%s.aop.java.JavaSecurityTestCaseSettings", true, null);
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
                || parameters == null
                || parameters.length == 0
        ) {
            return;
        }
        String illegallyReadPath = checkIfVariableCriteriaIsViolated(parameters, allowedPaths);
        if (illegallyReadPath != null) {
            throw new SecurityException(thisJoinPoint.getSourceLocation().getWithinType().getName() + " tried to illegally " + action + " from " + illegallyReadPath + " via " +  thisJoinPoint.getSignature().toLongString());

        }
    }

    before():
            %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReadMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesReadMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileInputStreamInitMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelReadMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.midiSystemMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsReadMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        checkFileSystemInteraction(thisJoinPoint, "read");
    }

    before():
            %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        checkFileSystemInteraction(thisJoinPoint, "write");
    }

    before():
            %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileExecuteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesExecuteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathExecuteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemExecuteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelExecuteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.objectStreamClassMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        checkFileSystemInteraction(thisJoinPoint, "execute");
    }

    before():
            %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathDeleteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesDeleteMethods() ||
                    %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        checkFileSystemInteraction(thisJoinPoint, "delete");
    }

    /*before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileInputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileOutputStreamInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.objectStreamClassMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.randomAccessFileInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileTypeDetectorProbeContentTypeMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileImageSourceInitMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.filesExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }


    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.pathDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileChannelWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileWriterMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileHandlerMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.midiSystemMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemsExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.defaultFileSystemExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderReadMethods() {
        if (handleAroundAdvice(thisJoinPoint, "read")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderWriteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "write")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.fileSystemProviderDeleteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "delete")) {
            throwSecurityException(thisJoinPoint);
        }
    }

    before(): %s.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions.desktopExecuteMethods() {
        if (handleAroundAdvice(thisJoinPoint, "execute")) {
            throwSecurityException(thisJoinPoint);
        }
    }*/

}