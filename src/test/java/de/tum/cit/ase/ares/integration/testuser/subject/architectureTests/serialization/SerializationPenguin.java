package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationPenguin {

    public void serializePenguin() throws IOException {
        // do nothing
        try (ObjectOutputStream oos = new ObjectOutputStream(null)) {
            // no operation
        }
    }

    public void deserializePenguin() throws IOException {
        // do nothing
        try (ObjectInputStream ois = new ObjectInputStream(null)) {
            // no operation
        }
    }
}
