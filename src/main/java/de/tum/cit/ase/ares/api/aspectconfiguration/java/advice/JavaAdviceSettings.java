package de.tum.cit.ase.ares.api.aspectconfiguration.java.advice;

public class JavaAdviceSettings {

    private JavaAdviceSettings() {
        throw new IllegalStateException("Utility class");
    }

    private static String restrictedPackage = "";
    private static String[] allowedListedClasses = {
            "de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaAdviceSettings",
            "de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaAdviceToolbox",
            "de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaReadPathAdvice",
            "de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaWritePathAdvice",
            "de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaExecutePathAdvice",
            "de.tum.cit.ase.ares.api.aspectconfiguration.java.JavaPointcuts",
            "de.tum.cit.ase.ares.api.aspectconfiguration.java.JavaBindings",
            "de.tum.cit.ase.ares.api.aspectconfiguration.java.JavaAgent"
    };
    private static String[] pathsAllowedToBeRead = {};
    private static String[] pathsAllowedToBeWritten = {};
    private static String[] pathsAllowedToBeExecuted = {};
}
