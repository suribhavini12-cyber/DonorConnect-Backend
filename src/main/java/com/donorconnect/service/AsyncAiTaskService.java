package com.donorconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncAiTaskService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncAiTaskService.class);

    @Async("donorConnectTaskExecutor")
    public CompletableFuture<String> executeNonCriticalBackgroundTask(String taskName, String details) {
        logger.info("[Async Task Started] Executing background task '{}' on thread '{}' with details: {}",
                taskName, Thread.currentThread().getName(), details);
        try {
            // Non-critical background work simulation / telemetry logging
            Thread.sleep(100);
            logger.info("[Async Task Completed] Finished background task '{}' on thread '{}'",
                    taskName, Thread.currentThread().getName());
            return CompletableFuture.completedFuture("SUCCESS: " + taskName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("[Async Task Interrupted] Task '{}' failed on thread '{}'", taskName, Thread.currentThread().getName());
            return CompletableFuture.completedFuture("FAILED: " + taskName);
        } catch (Exception ex) {
            logger.error("[Async Task Error] Error during background task '{}'", taskName, ex);
            return CompletableFuture.completedFuture("ERROR: " + ex.getMessage());
        }
    }
}
