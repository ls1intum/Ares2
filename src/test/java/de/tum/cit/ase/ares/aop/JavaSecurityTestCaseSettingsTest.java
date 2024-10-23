package de.tum.cit.ase.ares.aop;

import de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings;
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
            Constructor<JavaSecurityTestCaseSettings> constructor = JavaSecurityTestCaseSettings.class.getDeclaredConstructor();
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
            Field aopModeField = JavaSecurityTestCaseSettings.class.getDeclaredField("aopMode");
            Field allowedListedClassesField = JavaSecurityTestCaseSettings.class.getDeclaredField("allowedListedClasses");
            Field portsAllowedToBeConnectedToField = JavaSecurityTestCaseSettings.class.getDeclaredField("portsAllowedToBeConnectedTo");

            aopModeField.setAccessible(true);
            allowedListedClassesField.setAccessible(true);
            portsAllowedToBeConnectedToField.setAccessible(true);

            aopModeField.set(null, "test");
            allowedListedClassesField.set(null, new String[]{"testClass"});
            portsAllowedToBeConnectedToField.set(null, new int[]{8080});

            Method resetMethod = JavaSecurityTestCaseSettings.class.getDeclaredMethod("reset");
            resetMethod.setAccessible(true);
            resetMethod.invoke(null);

            assertNull(aopModeField.get(null));
            assertNull(allowedListedClassesField.get(null));
            assertNull(portsAllowedToBeConnectedToField.get(null));

            Field pathsAllowedToBeReadField = JavaSecurityTestCaseSettings.class.getDeclaredField("pathsAllowedToBeRead");
            pathsAllowedToBeReadField.setAccessible(true);
            assertNull(pathsAllowedToBeReadField.get(null));

            Field pathsAllowedToBeOverwrittenField = JavaSecurityTestCaseSettings.class.getDeclaredField("pathsAllowedToBeOverwritten");
            pathsAllowedToBeOverwrittenField.setAccessible(true);
            assertNull(pathsAllowedToBeOverwrittenField.get(null));

        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
}