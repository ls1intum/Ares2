package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.completableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.Function;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.LegalThread;

public class CreateCompletableFutureMain {

    private CreateCompletableFutureMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests CompletableFuture.runAsync(Runnable) method
     */
    public static void runAsync() {
        CompletableFuture.runAsync(new LegalThread());
    }

    /**
     * Tests CompletableFuture.supplyAsync(Supplier) method
     */
    public static void supplyAsync() {
        Supplier<String> supplier = () -> "test";
        CompletableFuture.supplyAsync(supplier);
    }

    /**
     * Tests CompletableFuture.thenApplyAsync(Function) method
     */
    public static void thenApplyAsync() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("test");
        Function<String, String> function = s -> s.toUpperCase();
        future.thenApplyAsync(function);
    }
}
