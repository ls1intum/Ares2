package de.tum.cit.ase.ares.api.aspect;

public aspect PointcutDefinitions {

        // Pointcut for Files.write method
        pointcut filesWriteMethod() : call(* java.nio.file.Files.write(..));

        // Pointcut for Files.readAllBytes and Files.readAllLines methods
        pointcut filesReadMethod() : call(* java.nio.file.Files.readAllBytes(..)) || call(* java.nio.file.Files.readAllLines(..));

        // Pointcut for Files.delete method
        pointcut filesDeleteMethod() : call(* java.nio.file.Files.delete(..));
}