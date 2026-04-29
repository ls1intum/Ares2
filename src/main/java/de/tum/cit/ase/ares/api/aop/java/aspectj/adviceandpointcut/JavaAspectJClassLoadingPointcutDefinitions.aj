package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

@SuppressWarnings("AopLanguageInspection")
public aspect JavaAspectJClassLoadingPointcutDefinitions {

    pointcut studentScope(): !within(de.tum.cit.ase.ares..*)
            && !within(net.bytebuddy..*) && !within(org.aspectj..*)
            && !within(com.ibm.wala..*) && !within(java..*) && !within(javax..*)
            && !within(sun..*) && !within(jdk..*)
            && !within(anonymous.toolclasses..*) && !within(metatest..*);

    pointcut classLoadingMethods(): studentScope() &&
            (call(* java.lang.ClassLoader+.loadClass(..)) ||
                    call(* java.lang.ClassLoader+.findClass(..)) ||
                    call(* java.lang.ClassLoader+.defineClass(..)) ||
                    call(* java.lang.ClassLoader+.getResource(..)) ||
                    call(* java.lang.ClassLoader+.getResources(..)) ||
                    call(* java.lang.ClassLoader+.getResourceAsStream(..)) ||
                    call(java.net.URLClassLoader+.new(..)) ||
                    call(* java.lang.Class+.getClassLoader()) ||
                    call(* java.lang.Thread+.getContextClassLoader()) ||
                    call(* java.lang.Thread+.setContextClassLoader(..)));
}
