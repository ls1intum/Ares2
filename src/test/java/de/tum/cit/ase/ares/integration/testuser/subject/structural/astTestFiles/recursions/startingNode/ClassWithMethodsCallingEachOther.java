package de.tum.cit.ase.ares.integration.testuser.subject.structural.astTestFiles.recursions.startingNode;

public class ClassWithMethodsCallingEachOther {

        public void method1() {
            method2();
        }

        public void method2() {
            method1();
        }

        public static void main(String[] args) {
            new ClassWithMethodsCallingEachOther().method1();
        }

        public void method3() {
            method4();
        }

        public void method4() {
            System.out.println();
        }
}
