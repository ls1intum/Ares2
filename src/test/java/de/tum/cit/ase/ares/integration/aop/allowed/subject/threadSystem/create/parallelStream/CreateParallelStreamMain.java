package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.parallelStream;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class CreateParallelStreamMain {

    private CreateParallelStreamMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests Collection.parallelStream() method
     */
    public static void collectionParallelStream() {
        Collection<String> collection = Arrays.asList("a", "b", "c");
        collection.parallelStream().forEach(System.out::println);
    }

    /**
     * Tests Stream.parallel() method
     */
    public static void streamParallel() {
        Stream<String> stream = Arrays.asList("a", "b", "c").stream();
        stream.parallel().forEach(System.out::println);
    }
}
