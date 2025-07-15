package de.tum.cit.ase.ares.integration.aop.forbidden.subject;

public class IllegalThread extends Thread {
    @Override
    public void run() {
        System.out.println("I am a Thread");
    }
}
