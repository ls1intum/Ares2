package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

public aspect JavaAspectJCommandSystemPointcutDefinitions {

    pointcut commandExecuteMethods(): (
            call(* java.lang.Runtime.exec(..)) ||
            call(java.lang.ProcessBuilder.new(..)) ||
            call(* java.lang.ProcessBuilder.start(..))
            );
}