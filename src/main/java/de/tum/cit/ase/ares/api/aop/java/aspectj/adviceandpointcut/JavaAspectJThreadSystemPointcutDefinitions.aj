package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

public aspect JavaAspectJThreadSystemPointcutDefinitions {

    pointcut threadCreateMethodsWithParameters(): (
            call(* java.lang.Thread.startVirtualThread(..)) ||
            call(* java.lang.Thread.Builder.start(..)) ||
            call(* java.util.concurrent.Executor.execute(..)) ||
            call(* java.util.concurrent.ExecutorService.submit(..)) ||
            call(* java.util.concurrent.ExecutorService.invokeAll(..)) ||
            call(* java.util.concurrent.ExecutorService.invokeAny(..)) ||
            call(* java.util.concurrent.ExecutorService.execute(..)) ||
            call(* java.util.concurrent.AbstractExecutorService.submit(..)) ||
            call(* java.util.concurrent.AbstractExecutorService.invokeAll(..)) ||
            call(* java.util.concurrent.AbstractExecutorService.invokeAny(..)) ||
            call(* java.util.concurrent.ThreadPoolExecutor.execute(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService.schedule(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService.scheduleAtFixedRate(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService.scheduleWithFixedDelay(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor.schedule(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor.scheduleAtFixedRate(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor.scheduleWithFixedDelay(..)) ||
            call(* java.util.concurrent.ForkJoinPool.execute(..)) ||
            call(* java.util.concurrent.ForkJoinPool.submit(..)) ||
            call(* java.util.concurrent.CompletableFuture.runAsync(..)) ||
            call(* java.util.concurrent.CompletableFuture.supplyAsync(..)) ||
            call(* java.util.concurrent.CompletableFuture.thenApplyAsync(..)) ||
            call(* java.util.concurrent.CompletableFuture.thenCombineAsync(..)) ||
            call(* java.util.concurrent.CompletableFuture.thenCombine(..)) ||
            call(* java.util.concurrent.ThreadFactory.newThread(..)) ||
            call(* java.util.concurrent.Executors$DelegatedExecutorService.submit(..)) ||
            call(* java.util.concurrent.Executors$DelegatedExecutorService.invokeAll(..)) ||
            call(* java.util.concurrent.Executors$DelegatedExecutorService.invokeAny(..)) ||
            call(* java.util.concurrent.Executors$DefaultThreadFactory.newThread(..)) ||
            call(* java.util.concurrent.ExecutorCompletionService.submit(..))
            );

    pointcut threadCreateMethodsWithoutParameters(): (
            call(* java.lang.Thread.start()) ||
                    call(* java.util.Collection.parallelStream()) ||
                    call(* java.util.stream.BaseStream.parallel())
            );
}