package de.tum.cit.ase.ares.integration.testuser.subject.commandexecution;

import com.tngtech.archunit.junit.internal.ArchUnitTestEngine;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import java.io.IOException;

public class CommandExecutingStudent {

    public void processBuilder() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.start();
    }

    public void processBuilderWithCommand() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("ls");
        processBuilder.start();
    }

    public void runtimeExec() throws IOException {
        Runtime.getRuntime().exec("ls");
    }

    public void runtimeExecNewerVersion() throws IOException {
        Runtime.getRuntime().exec(new String[] {"ls"});
    }

    public void something() {
        ArchRuleDefinition.noClasses().should().accessClassesThat().resideInAnyPackage("java.io");
    }
}
