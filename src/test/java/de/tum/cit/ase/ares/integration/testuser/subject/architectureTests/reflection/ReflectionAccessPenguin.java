package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
