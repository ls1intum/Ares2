package de.tum.cit.ase.ares.integration.testuser.subject.structural.astTestFiles.recursions.complex.no;

public class Fifth {

    public void callSecond() {
        First first = new First();
        first.noRecursion();
    }

    public void callThird() {
        Third third = new Third();
    }

    public void callFourth() {
        Fourth fourth = new Fourth();
    }
}
