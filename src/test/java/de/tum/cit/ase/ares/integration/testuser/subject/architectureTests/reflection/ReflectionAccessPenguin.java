package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.reflection;

import de.tum.cit.ase.ares.api.util.ReflectionTestUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static org.junit.platform.commons.util.ReflectionUtils.invokeMethod;

public class ReflectionAccessPenguin {

    static void accessPenguin() throws InvocationTargetException, IllegalAccessException {
        try {
            // Step 1: Get the Class object
            Class<?> clazz = ReflectionAccessPenguin.class;

            // Step 2: Get the Method object for the static method
            Method method = clazz.getDeclaredMethod("accessPenguin");

            // Step 3: Invoke the static method
            // Since it's a static method, the 'target' parameter is null
            method.invoke(new ReflectionAccessPenguin());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void accessTypes() {
        ReflectionAccessPenguin.class.getTypeParameters();
    }
}
