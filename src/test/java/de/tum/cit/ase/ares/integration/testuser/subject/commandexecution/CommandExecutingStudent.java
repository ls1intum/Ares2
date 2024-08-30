package de.tum.cit.ase.ares.integration.testuser.subject.commandexecution;

import java.io.IOException;

public class CommandExecutingStudent {

    public void method() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.start();
    }

    public void method2() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("ls");
        processBuilder.start();
    }

    public void method3() throws IOException {
        Runtime.getRuntime().exec("ls");
    }

    public void method4() throws IOException {
        Runtime.getRuntime().exec(new String[] {"ls"});
    }
}
