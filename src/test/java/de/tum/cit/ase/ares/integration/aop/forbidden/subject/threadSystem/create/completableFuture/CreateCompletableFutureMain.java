package de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.completableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiFunction;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.IllegalThread;

public class CreateCompletableFutureMain {

    private CreateCompletableFutureMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }

    /**
     * Tests CompletableFuture.runAsync(Runnable) method
     */
    public static void runAsync() {
        CompletableFuture.runAsync(new IllegalThread());
    }

    /**
     * Tests CompletableFuture.runAsync(Runnable, Executor) method
     */
    public static void runAsyncWithExecutor() {
        CompletableFuture.runAsync(new IllegalThread(), Runnable::run);
    }

    /**
     * Tests CompletableFuture.supplyAsync(Supplier) method
     */
    public static void supplyAsync() {
        Supplier<String> supplier = () -> "test";
        CompletableFuture.supplyAsync(supplier);
    }

    /**
     * Tests CompletableFuture.supplyAsync(Supplier, Executor) method
     */
    public static void supplyAsyncWithExecutor() {
        Supplier<String> supplier = () -> "test";
        CompletableFuture.supplyAsync(supplier, Runnable::run);
    }

    /**
     * Tests CompletableFuture.thenApplyAsync(Function) method
     */
    public static void thenApplyAsync() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("test");
        Function<String, String> function = String::toUpperCase;
        future.thenApplyAsync(function);
    }

    /**
     * Tests CompletableFuture.thenApplyAsync(Function, Executor) method
     */
    public static void thenApplyAsyncWithExecutor() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("test");
        Function<String, String> function = String::toUpperCase;
        future.thenApplyAsync(function, Runnable::run);
    }

    /**
     * Tests CompletableFuture.thenCombine(CompletionStage, BiFunction) method
     */
    public static void thenCombine() {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("test1");
        CompletionStage<String> future2 = CompletableFuture.completedFuture("test2");
        BiFunction<String, String, String> combiner = (s1, s2) -> s1 + s2;
        future1.thenCombine(future2, combiner);
    }

    /**
     * Tests CompletableFuture.thenCombineAsync(CompletionStage, BiFunction) method
     */
    public static void thenCombineAsync() {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("test1");
        CompletionStage<String> future2 = CompletableFuture.completedFuture("test2");
        BiFunction<String, String, String> combiner = (s1, s2) -> s1 + s2;
        future1.thenCombineAsync(future2, combiner);
    }

    /**
     * Tests CompletableFuture.thenCombineAsync(CompletionStage, BiFunction, Executor) method
     */
    public static void thenCombineAsyncWithExecutor() {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("test1");
        CompletionStage<String> future2 = CompletableFuture.completedFuture("test2");
        BiFunction<String, String, String> combiner = (s1, s2) -> s1 + s2;
        future1.thenCombineAsync(future2, combiner, Runnable::run);
    }
}
