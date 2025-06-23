package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

public aspect JavaAspectJCommandSystemPointcutDefinitions {

    pointcut commandExecuteMethods(): (
            call(* java.lang.Process.exec(..)) ||
            call(* java.lang.ProcessBuilder.start(..))
            );
}