package de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class JavaAOPAdviceSettingTripleTest {

    @Test
    public void testValidConstructionAndGetters() {
        JavaAOPAdviceSettingTriple triple =
                new JavaAOPAdviceSettingTriple("String", "settingName", 123);
        Assertions.assertEquals("String", triple.dataTyp());
        Assertions.assertEquals("settingName", triple.adviceSetting());
        Assertions.assertEquals(123, triple.value());
    }

    @Test
    public void testConstructorThrowsOnNullDataTyp() {
        Assertions.assertThrows(NullPointerException.class, () ->
                new JavaAOPAdviceSettingTriple(null, "setting", "value"),
                "Expected NullPointerException for null dataTyp");
    }

    @Test
    public void testConstructorThrowsOnNullAdviceSetting() {
        Assertions.assertThrows(NullPointerException.class, () ->
                new JavaAOPAdviceSettingTriple("String", null, "value"),
                "Expected NullPointerException for null adviceSetting");
    }

    @Test
    public void testValueCanBeNull() {
        JavaAOPAdviceSettingTriple triple =
                new JavaAOPAdviceSettingTriple("String", "setting", null);
        Assertions.assertNull(triple.value());
    }

    @Test
    public void testEqualsAndHashCode() {
        JavaAOPAdviceSettingTriple t1 =
                new JavaAOPAdviceSettingTriple("T", "S", 1);
        JavaAOPAdviceSettingTriple t2 =
                new JavaAOPAdviceSettingTriple("T", "S", 1);
        JavaAOPAdviceSettingTriple t3 =
                new JavaAOPAdviceSettingTriple("T", "S", 2);
        Assertions.assertEquals(t1, t2);
        Assertions.assertEquals(t1.hashCode(), t2.hashCode());
        Assertions.assertNotEquals(t1, t3);
    }

    @Test
    public void testToStringFormat() {
        JavaAOPAdviceSettingTriple triple =
                new JavaAOPAdviceSettingTriple("Type", "Name", "Val");
        String expected = "JavaAOPAdviceSettingTriple[dataTyp=Type, adviceSetting=Name, value=Val]";
        Assertions.assertEquals(expected, triple.toString());
    }
}
