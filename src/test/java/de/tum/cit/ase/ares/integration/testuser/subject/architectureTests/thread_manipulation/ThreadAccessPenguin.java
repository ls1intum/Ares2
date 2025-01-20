package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thread_manipulation;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ThreadAccessPenguin {

    void createThread() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.execute(() -> System.out.println("Hello from the thread!"));
    }
}
