package de.tum.cit.ase.ares.integration.testuser.subject.structural.astTestFiles.recursions.dynamicDispatch;

class Animal {
    void speak() {
        // Some logic
        if (this instanceof Dog) {
            Dog.eat();
        }
    }

    static void recursiveCall() {
        // Some logic
        Dog.eat();
    }
}

class Dog extends Animal {

    @Override
    void speak() {
        // Some logic
        super.speak(); // Recursive call
    }

    static void eat() {
        // Some logic
        Animal.recursiveCall();
    }
}

class Cat extends Animal {
    @Override
    void speak() {
        // Some logic
        super.speak(); // Recursive call
    }

    static void eat() {
        // Some logic
    }
}
