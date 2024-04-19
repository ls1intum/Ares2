package de.tum.cit.ase.ares.integration.testuser.subject.structural.astTestFiles.classes.yes;

public class ClassWithAnyKindsOfUnsupportedClasses {

    void localClassContainingFunction() {
        class localClass {
        }
    }

    void localRecordContainingFunction() {
        record localRecord (String id){
        }
    }
}
