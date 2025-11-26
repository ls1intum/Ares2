package de.tum.cit.ase.ares.api;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        SecurityPolicyReaderAndDirector securityPolicyReaderAndDirector = new SecurityPolicyReaderAndDirector(
                Path.of("/Users", "markuspaulsen", "Documents", "Ares2", "src", "main", "resources", "ExampleConfiguration.yaml"),
                Path.of("/Users", "markuspaulsen", "Documents", "Ares2UI")
//                Path.of("G:\\Programming\\ares2-test-classes\\ExampleConfiguration.yaml"),
//                Path.of("G:\\Programming\\ares2-test-classes\\SCOREReproducibilityPackage")
        ).createTestCases();
        securityPolicyReaderAndDirector.writeTestCases(
                Path.of("/Users", "markuspaulsen", "Documents", "Ares2UI", "src", "test", "java")
//                Path.of("G:\\Programming\\ares2-test-classes\\SCOREReproducibilityPackage\\src\\test\\java")
        );
    }
}