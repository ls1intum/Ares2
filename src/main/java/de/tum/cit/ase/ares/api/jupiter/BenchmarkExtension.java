package de.tum.cit.ase.ares.api.jupiter;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Used for benchmarking test execution time.
 * Author: Sarp Sahinalp
 * Date: 2025-01-05
 */
public class BenchmarkExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final String START_TIME = "start_time";
    private static final Logger log = LoggerFactory.getLogger(BenchmarkExtension.class);

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(START_TIME, System.nanoTime());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws IOException {
        long startTime = context.getStore(ExtensionContext.Namespace.GLOBAL).get(START_TIME, long.class);
        long duration = (System.nanoTime() - startTime) / 1_000_000;
//        Files.write(Path.of("benchmark.csv"), String.format("%s,%d,%d\n", context.getDisplayName(), duration, 6).getBytes(), java.nio.file.StandardOpenOption.APPEND);
        BenchmarkExtension.log.info("Test '{}' executed in: {} ms", context.getDisplayName(), duration);
    }
}
