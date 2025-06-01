package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaInstrumentationAdviceFileSystemToolboxTest {


    @Test
    void testCheckFileSystemInteraction_AllowedInteraction() {
        try (MockedStatic<JavaInstrumentationAdviceFileSystemToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceFileSystemToolbox.class)) {
            Method getValueFromSettings = JavaInstrumentationAdviceFileSystemToolbox.class.getDeclaredMethod("getValueFromSettings", String.class);
            getValueFromSettings.setAccessible(true);

            mockedToolbox.when(() -> getValueFromSettings.invoke(null, "aopMode")).thenReturn("INSTRUMENTATION");
            mockedToolbox.when(() -> getValueFromSettings.invoke(null, "restrictedPackage")).thenReturn("de.tum.cit.ase");
            mockedToolbox.when(() -> getValueFromSettings.invoke(null, "allowedListedClasses")).thenReturn(new String[]{"de.tum.cit.ase.safe"});
            mockedToolbox.when(() -> getValueFromSettings.invoke(null, "pathsAllowedToBeRead")).thenReturn(new String[]{"/allowed/path"});

            assertDoesNotThrow(() -> JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(
                    "read",
                    "de.tum.cit.ase.safe.FileReader",
                    "readFile",
                    "(Ljava/lang/String;)V",
                    null,
                    new Object[]{"/allowed/path"}
            ));
        } catch (Exception e) {
            fail("Exception should not have been thrown: " + e.getMessage());
        }
    }

    @Test
    void testLocalizeFallback() {
        String key = "security.advice.test.key";
        String result = JavaInstrumentationAdviceFileSystemToolbox.localize(key, "arg1", "arg2");
        key = "!security.advice.test.key!";
        assertEquals(key, result);
    }
}