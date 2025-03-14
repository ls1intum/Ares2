package de.tum.cit.ase.ares.api.aop;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class JavaSecurityTestCaseSettingsTest {

    @Test
    void testConstructorThrowsException() {
        try {
            Constructor<JavaAOPTestCaseSettings> constructor = JavaAOPTestCaseSettings.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Expected SecurityException to be thrown");
        } catch (InvocationTargetException e) {
            assertInstanceOf(SecurityException.class, e.getCause());
            assertEquals("Ares Security Error (Reason: Ares-Code; Stage: Creation): JavaSecurityTestCaseSettings is a utility class and should not be instantiated.", e.getCause().getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    void testResetMethod() {
        try {
            Field aopModeField = JavaAOPTestCaseSettings.class.getDeclaredField("aopMode");
            Field allowedListedClassesField = JavaAOPTestCaseSettings.class.getDeclaredField("allowedListedClasses");
            Field portsAllowedToBeConnectedToField = JavaAOPTestCaseSettings.class.getDeclaredField("portsAllowedToBeConnectedTo");

            aopModeField.setAccessible(true);
            allowedListedClassesField.setAccessible(true);
            portsAllowedToBeConnectedToField.setAccessible(true);

            aopModeField.set(null, "test");
            allowedListedClassesField.set(null, new String[]{"testClass"});
            portsAllowedToBeConnectedToField.set(null, new int[]{8080});

            Method resetMethod = JavaAOPTestCaseSettings.class.getDeclaredMethod("reset");
            resetMethod.setAccessible(true);
            resetMethod.invoke(null);

            assertNull(aopModeField.get(null));
            assertNull(allowedListedClassesField.get(null));
            assertNull(portsAllowedToBeConnectedToField.get(null));

            Field pathsAllowedToBeReadField = JavaAOPTestCaseSettings.class.getDeclaredField("pathsAllowedToBeRead");
            pathsAllowedToBeReadField.setAccessible(true);
            assertNull(pathsAllowedToBeReadField.get(null));

            Field pathsAllowedToBeOverwrittenField = JavaAOPTestCaseSettings.class.getDeclaredField("pathsAllowedToBeOverwritten");
            pathsAllowedToBeOverwrittenField.setAccessible(true);
            assertNull(pathsAllowedToBeOverwrittenField.get(null));

        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
}