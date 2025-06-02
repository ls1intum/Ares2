package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

public class CustomCallgraphBuilderTest {

    @Test
    void testConvertTypeNameToClassName_Valid() throws Exception {
        String input = "com.example.MyClass";
        String expected = "/com/example/MyClass.class";
        
        Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToClassName", String.class);
        method.setAccessible(true);
        String actual = (String) method.invoke(null, input);
        
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testConvertTypeNameToClassName_Invalid_Null() throws Exception {
        Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToClassName", String.class);
        method.setAccessible(true);
        
        Assertions.assertThrows(SecurityException.class, () -> {
            try {
                method.invoke(null, (String) null);
            } catch (Exception e) {
                if (e.getCause() instanceof SecurityException) {
                    throw (SecurityException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testConvertTypeNameToClassName_Invalid_Empty() throws Exception {
        Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToClassName", String.class);
        method.setAccessible(true);
        
        Assertions.assertThrows(SecurityException.class, () -> {
            try {
                method.invoke(null, "");
            } catch (Exception e) {
                if (e.getCause() instanceof SecurityException) {
                    throw (SecurityException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testConvertTypeNameToWalaName_Valid() throws Exception {
        String input = "com.example.MyClass";
        String expected = "Lcom/example/MyClass";
        
        Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToWalaName", String.class);
        method.setAccessible(true);
        String actual = (String) method.invoke(null, input);
        
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testConvertTypeNameToWalaName_Invalid_Null() throws Exception {
        Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToWalaName", String.class);
        method.setAccessible(true);
        
        Assertions.assertThrows(SecurityException.class, () -> {
            try {
                method.invoke(null, (String) null);
            } catch (Exception e) {
                if (e.getCause() instanceof SecurityException) {
                    throw (SecurityException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testConvertTypeNameToWalaName_Invalid_Empty() throws Exception {
        Method method = CustomCallgraphBuilder.class.getDeclaredMethod("convertTypeNameToWalaName", String.class);
        method.setAccessible(true);
        
        Assertions.assertThrows(SecurityException.class, () -> {
            try {
                method.invoke(null, "");
            } catch (Exception e) {
                if (e.getCause() instanceof SecurityException) {
                    throw (SecurityException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testTryResolve_NonExistent() throws Exception {
        CustomCallgraphBuilder builder = new CustomCallgraphBuilder();
        
        Method method = CustomCallgraphBuilder.class.getDeclaredMethod("tryResolve", String.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Optional<JavaClass> result = (Optional<JavaClass>) method.invoke(builder, "non.existent.ClassName");
        
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testGetImmediateSubclasses_NonExistent() {
        CustomCallgraphBuilder builder = new CustomCallgraphBuilder();
        Set<JavaClass> subclasses = builder.getImmediateSubclasses("non.existent.ClassName");
        Assertions.assertTrue(subclasses.isEmpty());
    }

    @Test
    void testBuildCallGraph_InvalidPath() {
        CustomCallgraphBuilder builder = new CustomCallgraphBuilder();
        Assertions.assertThrows(SecurityException.class, () -> {
            builder.buildCallGraph("invalid/path/to/classes");
        });
    }
}
