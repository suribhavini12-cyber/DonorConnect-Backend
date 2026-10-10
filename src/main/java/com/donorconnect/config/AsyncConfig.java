package com.donorconnect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Developer Documentation - Spring Async Infrastructure:
 * 
 * 1. What work is asynchronous?
 *    - Non-critical background telemetry, audit logging, and AI background preparation tasks (via AsyncAiTaskService).
 * 
 * 2. Why critical business operations remain synchronous?
 *    - User registration, login, JWT validation, blood request creation, status transitions, donor response confirmation, and admin moderation MUST remain synchronous to guarantee immediate transaction consistency, database integrity, and correct HTTP status codes.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "donorConnectTaskExecutor")
    public Executor donorConnectTaskExecutor() {
        logger.info("Initializing ThreadPoolTaskExecutor 'donorConnectTaskExecutor'");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("DonorConnectAsync-");
        executor.initialize();
        return executor;
    }
}
