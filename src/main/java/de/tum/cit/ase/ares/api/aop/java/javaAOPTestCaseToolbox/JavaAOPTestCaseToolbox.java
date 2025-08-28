package de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.stream.Stream;

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
        if (!(value instanceof List<?> || value instanceof Object[])) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.string[][]", value!=null ? value.getClass() : null));
        }
        Stream<?> outerStream = (value instanceof List<?> l)
                ? l.stream()
                : Arrays.stream((Object[]) value);
        // Validate and format inner elements; allow List, String[], Object[], or single values
        String stringArrayArrayValue = outerStream
                .map(inner -> {
                    if (inner instanceof List<?> || inner instanceof String[] || inner instanceof Object[]) {
                        return formatInnerStringArray(inner);
                    } else if (inner == null) {
                        // Represent null inner as empty array
                        return "new String[] {}";
                    } else if (inner instanceof CharSequence) {
                        String s = inner.toString().trim();
                        // If already a formatted array expression, pass through
                        if (s.startsWith("new String[]")) {
                            return s;
                        }
                        // Treat text as single value otherwise
                        return "new String[] {\"" + s + "\"}";
                    } else {
                        // Treat single value as a one-element string array
                        return "new String[] {\"" + inner + "\"}";
                    }
                })
                .collect(Collectors.joining(", "));
        // No trailing semicolon/newline to match expected output
        return String.format("private static String[][] %s = new String[][] {%s};%n", adviceSetting, stringArrayArrayValue);
    }

    private static String formatInnerStringArray(Object inner) {
        if (inner instanceof List<?>) {
            String innerVals = ((List<?>) inner).stream()
                    .map(obj -> obj == null ? "null" : String.format("\"%s\"", obj))
                    .collect(Collectors.joining(", "));
            return "new String[] {" + innerVals + "}";
        } else if (inner instanceof String[]) {
            String innerVals = Arrays.stream((String[]) inner)
                    .map(s -> s == null ? "null" : String.format("\"%s\"", s))
                    .collect(Collectors.joining(", "));
            return "new String[] {" + innerVals + "}";
        } else if (inner instanceof Object[]) {
            String innerVals = Arrays.stream((Object[]) inner)
                    .map(obj -> obj == null ? "null" : String.format("\"%s\"", obj))
                    .collect(Collectors.joining(", "));
            return "new String[] {" + innerVals + "}";
        }
        throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.string[][]", inner!=null ? inner.getClass() : null));
    }

    public static String getIntegerTwoDArrayAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
        if (!(value instanceof List<?>)) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.int[][]", value!=null ? value.getClass() : null));
        }
        List<?> outer = (List<?>) value;
        // Ensure all inner elements are lists; otherwise, fail fast like other methods
        boolean allInnerAreLists = outer.stream().allMatch(e -> e instanceof List<?>);
        if (!allInnerAreLists) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.mismatch.int[][]", value!=null ? value.getClass() : null));
        }
        String intArrayArrayValue = outer.stream()
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
