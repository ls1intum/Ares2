package de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class JavaAOPTestCaseToolbox {
    private JavaAOPTestCaseToolbox() {}

    //<editor-fold desc="Basic Types">
    public static String getStringAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
        if (!(value instanceof String)) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.string", value!=null ? value.getClass() : null));
        }
        return String.format("private static String %s = \"%s\";%n", adviceSetting, value);
    }

    public static String getIntegerAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
        if (!(value instanceof Integer)) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.int", value!=null ? value.getClass() : null));
        }
        return String.format("private static int %s = %d;%n", adviceSetting, value);
    }
    //</editor-fold>

    //<editor-fold desc="1-Dimensional Array Types">
    public static String getStringOneDArrayAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
        if (!(value instanceof List<?>)) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.string[]", value!=null ? value.getClass() : null));
        }
        String stringArrayValue = ((List<?>) value).stream()
                .map(Object::toString)
                .map(s -> String.format("\"%s\"", s))
                .collect(Collectors.joining(", "));
        return String.format("private static String[] %s = new String[] {%s};%n", adviceSetting, stringArrayValue);
    }

    public static String getIntegerOneDArrayAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
        if (!(value instanceof List<?>)) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.int[]", value!=null ? value.getClass() : null));
        }
        String intArrayValue = ((List<?>) value).stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        return String.format("private static int[] %s = new int[] {%s};%n", adviceSetting, intArrayValue);
    }
    //</editor-fold>

    //<editor-fold desc="2-Dimensional Array Types">
    public static String getStringTwoDArrayAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
        if (!(value instanceof List<?>)) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.string[][]", value!=null ? value.getClass() : null));
        }
        String stringArrayArrayValue = ((List<?>) value).stream()
                .filter(e -> e instanceof List<?>)
                .map(e -> (List<?>) e)
                .map(innerList -> innerList.stream()
                        .map(Object::toString)
                        .map(s -> String.format("\"%s\"", s))
                        .collect(Collectors.joining(", ")))
                .map(innerArray -> "new String[]{" + innerArray + "}")
                .collect(Collectors.joining(", "));
        return String.format("private static String[][] %s = new String[][] {%s};%n", adviceSetting, stringArrayArrayValue);
    }

    public static String getIntegerTwoDArrayAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
        if (!(value instanceof List<?>)) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.int[][]", value!=null ? value.getClass() : null));
        }
        String intArrayArrayValue = ((List<?>) value).stream()
                .filter(e -> e instanceof List<?>)
                .map(e -> (List<?>) e)
                .map(innerList -> innerList.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", ")))
                .map(innerArray -> "new int[]{" + innerArray + "}")
                .collect(Collectors.joining(", "));
        return String.format("private static int[][] %s = new int[][] {%s};%n", adviceSetting, intArrayArrayValue);
    }
    //</editor-fold>
}
