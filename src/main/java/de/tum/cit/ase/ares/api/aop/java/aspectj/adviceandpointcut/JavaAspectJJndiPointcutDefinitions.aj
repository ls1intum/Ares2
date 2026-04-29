package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

@SuppressWarnings("AopLanguageInspection")
public aspect JavaAspectJJndiPointcutDefinitions {

    pointcut studentScope(): !within(de.tum.cit.ase.ares..*)
            && !within(net.bytebuddy..*) && !within(org.aspectj..*)
            && !within(com.ibm.wala..*) && !within(java..*) && !within(javax..*)
            && !within(sun..*) && !within(jdk..*)
            && !within(anonymous.toolclasses..*) && !within(metatest..*);

    pointcut jndiMethods(): studentScope() &&
            (call(javax.naming.InitialContext+.new(..)) ||
                    call(javax.naming.directory.InitialDirContext+.new(..)) ||
                    call(javax.naming.ldap.InitialLdapContext+.new(..)) ||
                    call(* javax.naming.Context+.lookup(..)) ||
                    call(* javax.naming.Context+.lookupLink(..)) ||
                    call(* javax.naming.directory.DirContext+.search(..)) ||
                    call(* javax.naming.directory.DirContext+.getAttributes(..)));
}
