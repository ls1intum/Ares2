package de.tum.cit.ase.ares.integration.testuser.subject.structural.astTestFiles.recursions.complex.no;

public class Third {

    public void callFifth() {
        Fifth fifth = new Fifth();
    }

    public void callFourth() {
        Fourth fourth = new Fourth();
        fourth.callFifth();
    }

    public void callSecond() {
        Second second = new Second();
    }
}
