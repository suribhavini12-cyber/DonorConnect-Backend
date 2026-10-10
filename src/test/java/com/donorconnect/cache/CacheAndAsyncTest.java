package com.donorconnect.cache;

import com.donorconnect.config.AsyncConfig;
import com.donorconnect.config.CacheConfig;
import com.donorconnect.service.AsyncAiTaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class CacheAndAsyncTest {

    @Test
    @DisplayName("CacheManager initializes named caches 'openRequests' and 'donorMatches'")
    void cacheManager_Configuration() {
        CacheConfig cacheConfig = new CacheConfig();
        CacheManager cacheManager = cacheConfig.cacheManager();

        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrder("openRequests", "donorMatches");
    }

    @Test
    @DisplayName("Async executor initializes with expected thread pool settings")
    void asyncExecutor_Configuration() {
        AsyncConfig asyncConfig = new AsyncConfig();
        Executor executor = asyncConfig.donorConnectTaskExecutor();

        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        ThreadPoolTaskExecutor poolExecutor = (ThreadPoolTaskExecutor) executor;
        assertThat(poolExecutor.getThreadNamePrefix()).isEqualTo("DonorConnectAsync-");
    }

    @Test
    @DisplayName("AsyncAiTaskService executes background task successfully")
    void asyncTaskExecution() throws Exception {
        AsyncAiTaskService asyncService = new AsyncAiTaskService();
        CompletableFuture<String> future = asyncService.executeNonCriticalBackgroundTask("TestTask", "Details");

        assertThat(future).isNotNull();
        String result = future.get();
        assertThat(result).contains("SUCCESS");
    }
}
