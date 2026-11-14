package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

/**
 * Puts {@link JavaAspectJWoven} on every class the AspectJ compiler weaves while
 * the Ares JAR is on its aspect path. The reserved namespaces of
 * ReservedPackagePrefixes.txt are left out: no student class may live there, and
 * naming them spares a compiler warning for every library type woven code uses.
 */
@SuppressWarnings("AopLanguageInspection")
public aspect JavaAspectJWovenMarkerDefinitions {

    declare @type: !(java..* || javax..* || sun..* || jdk..* || com.sun..* || de.tum.cit.ase.ares.api..*
            || net.bytebuddy..* || org.aspectj..* || com.ibm.wala..* || com.tngtech.archunit..* || junit..*
            || org.junit..* || org.opentest4j..* || org.assertj..* || org.mockito..* || ch.qos.logback..*
            || org.slf4j..* || com.github.javaparser..* || com.fasterxml.jackson..* || org.yaml..* || org.json..*
            || com.opencsv..* || org.jgrapht..* || org.apiguardian..* || org.apache..* || org.eclipse..*
            || org.gradle..* || org.jacoco..* || com.intellij..* || worker.org.gradle..* || de.tum.in.test.api..*)
            : @JavaAspectJWoven;
}
