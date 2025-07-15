
package de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.Arrays;
import java.util.List;

public class JavaAOPTestCaseToolboxTest {

    @Test
    public void testGetStringAssignmentWithValidValue() {
        String result = JavaAOPTestCaseToolbox.getStringAssignment("myString", "hello");
        Assertions.assertEquals("private static String myString = \"hello\";\n", result);
    }

    @Test
    public void testGetStringAssignmentWithInvalidValue() {
        Object invalidValue = 123;
        Assertions.assertThrows(SecurityException.class, () ->
                JavaAOPTestCaseToolbox.getStringAssignment("myString", invalidValue));
    }

    @Test
    public void testGetIntegerAssignmentWithValidValue() {
        String result = JavaAOPTestCaseToolbox.getIntegerAssignment("myInt", 42);
        Assertions.assertEquals("private static int myInt = 42;\n", result);
    }

    @Test
    public void testGetIntegerAssignmentWithInvalidValue() {
        Object invalidValue = "notAnInt";
        Assertions.assertThrows(SecurityException.class, () ->
                JavaAOPTestCaseToolbox.getIntegerAssignment("myInt", invalidValue));
    }

    @Test
    public void testGetStringOneDArrayAssignmentWithValidValue() {
        List<String> values = Arrays.asList("foo", "bar");
        String result = JavaAOPTestCaseToolbox.getStringOneDArrayAssignment("myArray", values);
        Assertions.assertEquals("private static String[] myArray = new String[] {\"foo\", \"bar\"};\n", result);
    }

    @Test
    public void testGetStringOneDArrayAssignmentWithInvalidValue() {
        Object invalidValue = "notAList";
        Assertions.assertThrows(SecurityException.class, () ->
                JavaAOPTestCaseToolbox.getStringOneDArrayAssignment("myArray", invalidValue));
    }

    @Test
    public void testGetIntegerOneDArrayAssignmentWithValidValue() {
        List<Integer> values = Arrays.asList(1, 2, 3);
        String result = JavaAOPTestCaseToolbox.getIntegerOneDArrayAssignment("intArray", values);
        Assertions.assertEquals("private static int[] intArray = new int[] {1, 2, 3};\n", result);
    }

    @Test
    public void testGetIntegerOneDArrayAssignmentWithInvalidValue() {
        Object invalidValue = "notAList";
        Assertions.assertThrows(SecurityException.class, () ->
                JavaAOPTestCaseToolbox.getIntegerOneDArrayAssignment("intArray", invalidValue));
    }

    @Test
    public void testGetStringTwoDArrayAssignmentWithValidValue() {
        List<List<String>> values = Arrays.asList(
                Arrays.asList("a", "b"),
                Arrays.asList("c", "d")
        );
        String result = JavaAOPTestCaseToolbox.getStringTwoDArrayAssignment("twoDArray", values);
        Assertions.assertEquals(
                "private static String[][] twoDArray = new String[][] {new String[]{\"a\", \"b\"}, new String[]{\"c\", \"d\"}};\n",
                result);
    }

    @Test
    public void testGetStringTwoDArrayAssignmentWithInvalidValue() {
        Object invalidValue = "notAList";
        Assertions.assertThrows(SecurityException.class, () ->
                JavaAOPTestCaseToolbox.getStringTwoDArrayAssignment("twoDArray", invalidValue));
    }

    @Test
    public void testGetIntegerTwoDArrayAssignmentWithValidValue() {
        List<List<Integer>> values = Arrays.asList(
                Arrays.asList(1, 2),
                Arrays.asList(3, 4)
        );
        String result = JavaAOPTestCaseToolbox.getIntegerTwoDArrayAssignment("twoIntArray", values);
        Assertions.assertEquals(
                "private static int[][] twoIntArray = new int[][] {new int[]{1, 2}, new int[]{3, 4}};\n",
                result);
    }

    @Test
    public void testGetIntegerTwoDArrayAssignmentWithInvalidValue() {
        Object invalidValue = "notAList";
        Assertions.assertThrows(SecurityException.class, () ->
                JavaAOPTestCaseToolbox.getIntegerTwoDArrayAssignment("twoIntArray", invalidValue));
    }
}
