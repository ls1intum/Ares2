package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.IgnoreValues;

//<editor-fold desc="imports">
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;
//</editor-fold>

public abstract aspect JavaAspectJAbstractAdviceDefinitions {

    //<editor-fold desc="Constants">
    @Nonnull
    protected static final List<String> IGNORE_CALLSTACK = List.of(
            "java.lang.ClassLoader",
            "de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension",
            "de.tum.cit.ase.ares.api.jqwik.JqwikSecurityExtension",
            "de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions"
    );
    //</editor-fold>

    //<editor-fold desc="Tools methods">

    /**
     * Retrieves the value of a specified static field from the settings class.
     *
     * <p>Description: Uses reflection to access a static field in JavaAOPTestCaseSettings,
     * allowing retrieval of security-related configuration values for instrumentation and tests.
     *
     * @param fieldName the name of the field to retrieve
     * @param <T> the type of the field's value
     * @return the value of the specified field
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @SuppressWarnings("unchecked")
    @Nullable
    protected static <T> T getValueFromSettings(@Nonnull String fieldName) {
        try {
            // Take standard class loader as class loader in order to get the JavaAOPTestCaseSettings class at compile time for aspectj
            @Nonnull Class<?> adviceSettingsClass = Objects.requireNonNull(Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings"), "adviceSettingsClass must not be null");
            @Nonnull Field field = Objects.requireNonNull(adviceSettingsClass.getDeclaredField(Objects.requireNonNull(fieldName, "fieldName must not be null")), "field must not be null");
            field.setAccessible(true);
            @Nullable T value = (T) field.get(null);
            field.setAccessible(false);
            return value;

        } catch (LinkageError e) {
            throw new SecurityException(localize("security.advice.linkage.exception", fieldName), e);
        } catch (ClassNotFoundException e) {
            throw new SecurityException(localize("security.advice.class.not.found.exception", fieldName), e);
        } catch (NoSuchFieldException e) {
            throw new SecurityException(localize("security.advice.no.such.field.exception", fieldName), e);
        } catch (NullPointerException e) {
            throw new SecurityException(localize("security.advice.null.pointer.exception", fieldName), e);
        } catch (InaccessibleObjectException e) {
            throw new SecurityException(localize("security.advice.inaccessible.object.exception", fieldName), e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(localize("security.advice.illegal.access.exception", fieldName), e);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(localize("security.advice.illegal.argument.exception", fieldName), e);
        }
    }

    /**
     * Sets the value of a specified static field in the settings class.
     *
     * <p>Description: Uses reflection to modify a static field in JavaAOPTestCaseSettings,
     * allowing updates to security-related configuration values for instrumentation and tests.
     *
     * @param fieldName the name of the field to modify
     * @param newValue the new value to assign to the field
     * @param <T> the type of the field's value
     * @since 2.0.0
     * @author Markus Paulsen
     */
    protected static <T> void setValueToSettings(@Nonnull String fieldName, @Nullable T newValue) {
        try {
            // Take standard class loader as class loader in order to get the JavaAOPTestCaseSettings class at compile time for aspectj
            @Nonnull Class<?> adviceSettingsClass = Objects.requireNonNull(Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings"), "adviceSettingsClass must not be null");
            @Nonnull Field field = Objects.requireNonNull(adviceSettingsClass.getDeclaredField(Objects.requireNonNull(fieldName, "fieldName must not be null")), "field must not be null");
            field.setAccessible(true);
            field.set(null, newValue);
            field.setAccessible(false);
        } catch (LinkageError e) {
            throw new SecurityException(localize("security.advice.linkage.exception", fieldName), e);
        } catch (ClassNotFoundException e) {
            throw new SecurityException(localize("security.advice.class.not.found.exception", fieldName), e);
        } catch (NoSuchFieldException e) {
            throw new SecurityException(localize("security.advice.no.such.field.exception", fieldName), e);
        } catch (NullPointerException e) {
            throw new SecurityException(localize("security.advice.null.pointer.exception", fieldName), e);
        } catch (InaccessibleObjectException e) {
            throw new SecurityException(localize("security.advice.inaccessible.object.exception", fieldName), e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(localize("security.advice.illegal.access.exception", fieldName), e);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(localize("security.advice.illegal.argument.exception", fieldName), e);
        }
    }

    /**
     * Decrements the value at a specified index in an integer array setting.
     *
     * <p>Description: Retrieves an integer array from settings, decrements the value
     * at the given position, and updates the array back to the settings class.
     *
     * @param settingsArray the name of the array field in settings
     * @param position the index position of the value to decrement
     * @since 2.0.0
     * @author Markus Paulsen
     */
    protected static void decrementSettingsArrayValue(@Nonnull String settingsArray, int position) {
        @Nullable int[] array = getValueFromSettings(settingsArray);
        if (array != null && position >= 0 && position < array.length) {
            @Nonnull int[] clone = array.clone();
            clone[position]--;
            setValueToSettings(settingsArray, clone);
        }
    }

    /**
     * Retrieves a localized message based on a key and optional arguments.
     *
     * <p>Description: Attempts to fetch a localized string from the Messages class using reflection.
     * Falls back to the key if localization fails.
     *
     * @param key the localization key identifying the message
     * @param args optional arguments to format the localized message
     * @return the localized message string, or the key itself if localization fails
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nonnull
    public static String localize(@Nonnull String key, @Nullable Object... args) {
        try {
            @Nonnull Class<?> messagesClass = Class.forName(
                    "de.tum.cit.ase.ares.api.localization.Messages",
                    true,
                    Thread.currentThread().getContextClassLoader()
            );
            @Nonnull Method localized = messagesClass.getDeclaredMethod(
                    "localized", String.class, Object[].class
            );
            @Nullable Object result = localized.invoke(null, key, args);
            if (result instanceof String str) {
                return str;
            } else {
                throw new IllegalStateException("Method does not return a String");
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            // Fallback: Return the key if localization fails
            return key;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Callstack criteria methods">

    /**
     * Determines if a call stack element is in the allow list.
     *
     * <p>Description: Checks whether the class name of the provided stack trace element
     * starts with any of the allowed class name prefixes.
     *
     * @param allowedClasses the array of allowed class name prefixes
     * @param elementToCheck the stack trace element to check
     * @return true if the element is allowed, false otherwise
     * @since 2.0.0
     * @author Markus Paulsen
     */
    protected static boolean checkIfCallstackElementIsAllowed(@Nonnull String[] allowedClasses, @Nonnull StackTraceElement elementToCheck) {
        String className = elementToCheck.getClassName();
        for (@Nonnull String allowedClass : allowedClasses) {
            if (className.startsWith(allowedClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the current call stack for violations of restricted packages.
     *
     * <p>Description: Examines the stack trace to find the first element whose class name
     * starts with the restricted package but is not in the allowed classes list,
     * skipping any classes in the ignore list.
     *
     * @param restrictedPackage the prefix of restricted package names
     * @param allowedClasses the array of allowed class name prefixes
     * @return the fully qualified method name that violates criteria, or null if none
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nullable
    protected static String checkIfCallstackCriteriaIsViolated(String restrictedPackage, String[] allowedClasses) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (@Nonnull StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            boolean ignoreFound = false;
            for (@Nonnull String allowedClass : IGNORE_CALLSTACK) {
                if (className.startsWith(allowedClass)) {
                    ignoreFound = true;
                    break;
                }
            }
            if (ignoreFound) {
                break;
            }
            if (className.startsWith(restrictedPackage) && !checkIfCallstackElementIsAllowed(allowedClasses, element)) {
                return className + "." + element.getMethodName();
            }
        }
        return null;
    }
    //</editor-fold>

    //<editor-fold desc="Filter variables">

    /**
     * Filters variables based on the IgnoreValues criteria.
     *
     * <p>Description: Returns a new array of variables, excluding those that match the ignore criteria.
     * If all variables are ignored, returns an empty array. If all except one variable is ignored,
     * returns an array with only that variable.
     *
     * @param variables the original array of variables
     * @param ignoreVariables criteria determining which variables to skip
     * @return a filtered array of variables
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nonnull
    protected static Object[] filterVariables(@Nonnull Object[] variables, @Nonnull IgnoreValues ignoreVariables) {
        @Nonnull ArrayList<Object> newVariables = new ArrayList<>(Arrays.asList(variables.clone()));
        switch (ignoreVariables.getType()) {
            // No variable is ignored
            case NONE:
                break;
            // All variables are ignored
            case ALL:
                newVariables.clear();
                break;
            // All variables except the one at the given index are ignored
            case ALL_EXCEPT:
                @Nonnull Object toKeep = newVariables.get(ignoreVariables.getIndex());
                newVariables.clear();
                newVariables.add(toKeep);
                break;
            case NONE_EXCEPT:
                newVariables.remove(ignoreVariables.getIndex());
                break;
        }
        return newVariables.toArray();
    }
    //</editor-fold>

    /**
     * Extracts the method name without access modifiers from a full method signature.
     *
     * <p>Description: Removes access modifiers (public, private, protected, static, final, etc.)
     * from the method signature and extracts only the package.class.method part.
     *
     * @param fullMethodSignature the complete method signature from toLongString()
     * @return the method name without access modifiers (e.g., "java.lang.Runtime.exec")
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Nonnull
    protected static String extractMethodNameWithoutModifiers(@Nonnull String fullMethodSignature) {
        // Split by opening parenthesis to get method declaration part
        String methodDeclaration = fullMethodSignature.split("\\(")[0];

        // Find the last space before the method name to remove access modifiers
        int lastSpaceIndex = methodDeclaration.lastIndexOf(' ');
        if (lastSpaceIndex != -1) {
            return methodDeclaration.substring(lastSpaceIndex + 1);
        }

        // If no space found, return the whole declaration (shouldn't happen in normal cases)
        return methodDeclaration;
    }
}