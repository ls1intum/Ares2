package de.tum.cit.ase.ares.integration.testuser.subject.structural.astTestFiles.recursions.excludeMethods;

public class RandomParameterThatShouldBeResolved {

    private final int value;

    public RandomParameterThatShouldBeResolved(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
