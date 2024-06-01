package de.tum.cit.ase.ares.integration.testuser.subject.structural.astTestFiles.recursions.overloaded.no;

public class FactorialCalculator {

    public int factorial(int n) {
        if (n == 0 || n == 1) {
            return 1;
        } else {
            return (int) (n * factorial((double) n - 1));
        }
    }

    // Overloaded method for factorial calculation with double input
    public double factorial(double n) {
        if (n == 0 || n == 1) {
            return 1;
        } else {
            return n;
        }
    }
}
