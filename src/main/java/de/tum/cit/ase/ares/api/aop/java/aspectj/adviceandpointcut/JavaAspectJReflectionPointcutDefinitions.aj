package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

@SuppressWarnings("AopLanguageInspection")
public aspect JavaAspectJReflectionPointcutDefinitions {

    // Restrict weaving to student-code call sites only. Excluding Ares internals, the
    // AspectJ/Byte Buddy/WALA libraries, the JDK, and the test infrastructure prevents
    // the JVM from crashing during agent premain (which itself uses Class.forName,
    // setAccessible, etc.) and avoids unbounded recursion via reflective settings lookup.
    pointcut studentScope(): !within(de.tum.cit.ase.ares..*)
            && !within(net.bytebuddy..*) && !within(org.aspectj..*)
            && !within(com.ibm.wala..*) && !within(java..*) && !within(javax..*)
            && !within(sun..*) && !within(jdk..*)
            && !within(anonymous.toolclasses..*) && !within(metatest..*);

    pointcut reflectionMethods(): studentScope() &&
            (call(* java.lang.Class+.forName(..)) ||
                    call(* java.lang.Class+.getMethod(..)) ||
                    call(* java.lang.Class+.getDeclaredMethod(..)) ||
                    call(* java.lang.Class+.getMethods()) ||
                    call(* java.lang.Class+.getDeclaredMethods()) ||
                    call(* java.lang.Class+.getField(..)) ||
                    call(* java.lang.Class+.getDeclaredField(..)) ||
                    call(* java.lang.Class+.getFields()) ||
                    call(* java.lang.Class+.getDeclaredFields()) ||
                    call(* java.lang.Class+.getConstructor(..)) ||
                    call(* java.lang.Class+.getDeclaredConstructor(..)) ||
                    call(* java.lang.reflect.Method+.invoke(..)) ||
                    call(* java.lang.reflect.Constructor+.newInstance(..)) ||
                    call(* java.lang.reflect.Field+.get*(..)) ||
                    call(* java.lang.reflect.Field+.set*(..)) ||
                    call(* java.lang.reflect.AccessibleObject+.setAccessible(..)) ||
                    call(* java.lang.invoke.MethodHandles$Lookup+.findVirtual(..)) ||
                    call(* java.lang.invoke.MethodHandles$Lookup+.findStatic(..)) ||
                    call(* java.lang.invoke.MethodHandles$Lookup+.findSpecial(..)) ||
                    call(* java.lang.invoke.MethodHandles$Lookup+.findConstructor(..)) ||
                    call(* java.lang.invoke.MethodHandles$Lookup+.findGetter(..)) ||
                    call(* java.lang.invoke.MethodHandles$Lookup+.findSetter(..)) ||
                    call(* java.lang.invoke.MethodHandle+.invoke(..)) ||
                    call(* java.lang.invoke.MethodHandle+.invokeExact(..)) ||
                    call(* java.lang.invoke.MethodHandle+.invokeWithArguments(..)));
}
