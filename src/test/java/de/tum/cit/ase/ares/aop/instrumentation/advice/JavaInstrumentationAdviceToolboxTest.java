package de.tum.cit.ase.ares.aop.instrumentation.advice;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JavaInstrumentationAdviceToolboxTest {

    @Test
    void testCheckFileSystemInteraction_AllowedInteraction() {
        try (MockedStatic<JavaInstrumentationAdviceToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceToolbox.class)) {
            Method getValueFromSettings = JavaInstrumentationAdviceToolbox.class.getDeclaredMethod("getValueFromSettings", String.class);
            getValueFromSettings.setAccessible(true);

            mockedToolbox.when(() -> getValueFromSettings.invoke(null, "aopMode")).thenReturn("INSTRUMENTATION");
            mockedToolbox.when(() -> getValueFromSettings.invoke(null, "restrictedPackage")).thenReturn("de.tum.cit.ase");
            mockedToolbox.when(() -> getValueFromSettings.invoke(null, "allowedListedClasses")).thenReturn(new String[]{"de.tum.cit.ase.safe"});
            mockedToolbox.when(() -> getValueFromSettings.invoke(null, "pathsAllowedToBeRead")).thenReturn(new String[]{"/allowed/path"});

            assertDoesNotThrow(() -> JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
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
        String result = JavaInstrumentationAdviceToolbox.localize(key, "arg1", "arg2");
        key = "!security.advice.test.key!";
        assertEquals(key, result);
    }
}