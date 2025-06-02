package de.tum.cit.ase.ares.integration.architecture.forbidden.subject.threadSystem.create.executorService;

import de.tum.cit.ase.ares.integration.architecture.forbidden.subject.threadSystem.IllegalThread;

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
        threadPoolExecutor.submit(new IllegalThread());
    }
}
