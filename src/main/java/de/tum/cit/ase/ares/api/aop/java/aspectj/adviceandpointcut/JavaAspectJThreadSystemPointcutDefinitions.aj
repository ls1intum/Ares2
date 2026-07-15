package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

@SuppressWarnings("AopLanguageInspection") public aspect JavaAspectJThreadSystemPointcutDefinitions {

    pointcut threadCreateMethodsWithParameters(): (
            call(* java.lang.ThreadGroup+.newThread(..)) ||
            call(* java.util.concurrent.Executor+.execute(..)) ||
            call(* java.util.concurrent.ExecutorService+.submit(..)) ||
            call(* java.util.concurrent.ExecutorService+.invokeAll(..)) ||
            call(* java.util.concurrent.ExecutorService+.invokeAny(..)) ||
            call(* java.util.concurrent.ExecutorService+.execute(..)) ||
            call(* java.util.concurrent.AbstractExecutorService+.submit(..)) ||
            call(* java.util.concurrent.AbstractExecutorService+.invokeAll(..)) ||
            call(* java.util.concurrent.AbstractExecutorService+.invokeAny(..)) ||
            call(* java.util.concurrent.AbstractExecutorService+.execute(..)) ||
            call(* java.util.concurrent.ThreadPoolExecutor+.execute(..)) ||
            call(* java.util.concurrent.ThreadPoolExecutor+.submit(..)) ||
            call(* java.util.concurrent.ThreadPoolExecutor+.invokeAll(..)) ||
            call(* java.util.concurrent.ThreadPoolExecutor+.invokeAny(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService+.schedule(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService+.scheduleAtFixedRate(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService+.scheduleWithFixedDelay(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService+.submit(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService+.invokeAll(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService+.invokeAny(..)) ||
            call(* java.util.concurrent.ScheduledExecutorService+.execute(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor+.schedule(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor+.scheduleAtFixedRate(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor+.scheduleWithFixedDelay(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor+.submit(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor+.execute(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor+.invokeAll(..)) ||
            call(* java.util.concurrent.ScheduledThreadPoolExecutor+.invokeAny(..)) ||
            call(* java.util.concurrent.ForkJoinPool+.execute(..)) ||
            call(* java.util.concurrent.ForkJoinPool+.submit(..)) ||
            call(* java.util.concurrent.ForkJoinPool+.invokeAll(..)) ||
            call(* java.util.concurrent.ForkJoinPool+.invokeAny(..)) ||
            call(* java.util.concurrent.CompletableFuture+.runAsync(..)) ||
            call(* java.util.concurrent.CompletableFuture+.supplyAsync(..)) ||
            call(* java.util.concurrent.CompletableFuture+.thenApplyAsync(..)) ||
            call(* java.util.concurrent.CompletableFuture+.thenCombineAsync(..)) ||
            call(* java.util.concurrent.CompletableFuture+.thenCombine(..)) ||
            call(* java.util.concurrent.ThreadFactory+.newThread(..)) ||
            call(* java.util.concurrent.Executors$DelegatedExecutorService+.submit(..)) ||
            call(* java.util.concurrent.Executors$DelegatedExecutorService+.invokeAll(..)) ||
            call(* java.util.concurrent.Executors$DelegatedExecutorService+.invokeAny(..)) ||
            call(* java.util.concurrent.Executors$DefaultThreadFactory+.newThread(..)) ||
            call(* java.util.concurrent.ExecutorCompletionService+.submit(..)) ||
            call(* java.lang.Thread+.startVirtualThread(..)) ||
            call(* java.lang.Thread$Builder+.start(..)) ||
            call(* java.lang.Thread$Builder$OfPlatform+.start(..)) ||
            call(* java.util.concurrent.SubmissionPublisher+.submit(..)) ||
            call(* java.util.concurrent.SubmissionPublisher+.offer(..))
            );

    pointcut threadCreateMethodsWithoutParameters(): (
            call(* java.lang.Thread+.start()) ||
            call(* java.util.Collection+.parallelStream()) ||
            call(* java.util.stream.Stream+.parallel()) ||
            call(* java.util.stream.BaseStream+.parallel())
            );

    // Thread monitor manipulation (notify/notifyAll/wait). These are final methods declared by
    // java.lang.Object, so call(* java.lang.Thread+.notify()) does not weave (AspectJ resolves the
    // declaring type to java.lang.Object). call() on Object+ combined with a target(Thread+) runtime
    // guard confines interception to Thread receivers. The Instrumentation engine cannot mirror
    // this: target-class rewriting has no Thread.notify() to weave. See
    // docs/aop/AspectJVsInstrumentationWeaknesses.md.
    pointcut threadManipulateMethods(): (
            (call(* java.lang.Object+.notify())        && target(java.lang.Thread+)) ||
            (call(* java.lang.Object+.notifyAll())     && target(java.lang.Thread+)) ||
            (call(* java.lang.Object+.wait())          && target(java.lang.Thread+)) ||
            (call(* java.lang.Object+.wait(long))      && target(java.lang.Thread+)) ||
            (call(* java.lang.Object+.wait(long, int)) && target(java.lang.Thread+))
            );
}
