package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem;

public class LegalThread extends Thread {
    @Override
    public void run() {
        System.out.println("I am a Thread");
    }
}
