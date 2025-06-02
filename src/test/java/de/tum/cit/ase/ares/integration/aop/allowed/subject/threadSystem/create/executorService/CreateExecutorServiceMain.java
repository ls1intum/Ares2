package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.create.executorService;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.LegalThread;

import java.util.concurrent.ThreadPoolExecutor;

public class CreateExecutorServiceMain {

    public static void createExecutorService() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                1,
                1,
                0L,
                java.util.concurrent.TimeUnit.MILLISECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>()
        );
        threadPoolExecutor.submit(new LegalThread());
    }
}
