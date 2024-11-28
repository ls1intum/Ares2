package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.commandexecution;

import java.io.IOException;

public class CommandExecutingPenguin {

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
}
