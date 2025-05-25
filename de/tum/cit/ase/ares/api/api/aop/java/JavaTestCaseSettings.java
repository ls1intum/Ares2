package de.tum.cit.ase.ares.api.aop.java;

/**
 * Configuration settings for Java instrumentation aspect configurations.
 * <p>
 * This class holds the static configuration settings used by Java instrumentation aspects,
 * such as allowed file paths, network hosts and ports, command executions, and thread creations.
 * These settings are used to control and manage various security-related behaviors in the application.
 * </p>
 * <p>
 * As a utility class, it is not intended to be instantiated.
 * </p>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class JavaTestCaseSettings {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * The constructor throws an {@link UnsupportedOperationException} to enforce the utility class pattern.
     * </p>
     */
    private JavaTestCaseSettings() {
        throw new UnsupportedOperationException("JavaTestCaseSettings is a utility class and should not be instantiated");
    }
private static String  aopMode = "ASPECTJ";
private static String  restrictedPackage = "de.tum.cit.ase.ares";
private static String[]  allowedListedClasses = new String[] {"de.tum.cit.ase.ares.api.architecture.java.archunit", "de.tum.cit.ase.ares.api.architecture.java.wala", "de.tum.cit.ase.ares.api.aop.java.aspectj", "de.tum.cit.ase.ares.api.aop.java.instrumentation", "de.tum.cit.ase.ares.api.internal", "de.tum.cit.ase.ares.api.aop.java.JavaTestCaseSettings", "de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector", "de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader", "de.tum.cit.ase.ares.api.policy.director.java.SecurityPolicyJavaDirector", "de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner", "de.tum.cit.ase.ares.api.securitytest.java.JavaSecurityTestCaseFactoryAndBuilder", "de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml.EssentialDataYAMLReader", "de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder", "de.tum.cit.ase.ares.api.securitytest.java.JavaTestCaseFactoryAndBuilder", "de.tum.cit.ase.ares.api.util.ProjectSourcesFinder", "de.tum.cit.ase.ares.api.util.FileTools", "de.tum.cit.ase.ares.api.architecture.ArchitectureMode", "de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator", "de.tum.cit.ase.ares.api.jupiter"};
private static String[]  pathsAllowedToBeRead = new String[] {};
private static String[]  pathsAllowedToBeOverwritten = new String[] {};
private static String[]  pathsAllowedToBeExecuted = new String[] {};
private static String[]  pathsAllowedToBeDeleted = new String[] {};
private static String[]  hostsAllowedToBeConnectedTo = new String[] {};
private static int[]  portsAllowedToBeConnectedTo = new int[] {};
private static String[]  hostsAllowedToBeSentTo = new String[] {};
private static int[]  portsAllowedToBeSentTo = new int[] {};
private static String[]  hostsAllowedToBeReceivedFrom = new String[] {};
private static int[]  portsAllowedToBeReceivedFrom = new int[] {};
private static String[]  commandsAllowedToBeExecuted = new String[] {};
private static String[][]  argumentsAllowedToBePassed = new String[][] {};
private static int[]  threadNumberAllowedToBeCreated = new int[] {};
private static String[]  threadClassAllowedToBeCreated = new String[] {};

}