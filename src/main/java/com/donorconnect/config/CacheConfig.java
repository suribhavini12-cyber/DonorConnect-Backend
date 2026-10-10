package com.donorconnect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Developer Documentation - Spring Cache Infrastructure:
 * 
 * 1. What is cached?
 *    - "openRequests": Safe read-only list of available blood requests for donors.
 *    - "donorMatches": Safe DTO list of potential donor matches for a specific blood request ID.
 * 
 * 2. Why is it cached?
 *    - Reduces repeated database queries for high-frequency read operations (listing open requests & matching donors).
 * 
 * 3. When is the cache invalidated?
 *    - "openRequests": Evicted whenever a request is created, updated, cancelled, closed, or fulfilled.
 *    - "donorMatches": Evicted when request attributes (blood group, location, status) or donor profile attributes (blood group, preferred locations, participation status, consent) change.
 * 
 * 4. What is intentionally NOT cached?
 *    - User passwords, JWT tokens, authentication state, donor private contact details, admin moderation reports, private user responses.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public CacheManager cacheManager() {
        logger.info("Initializing ConcurrentMapCacheManager with caches: 'openRequests', 'donorMatches'");
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList("openRequests", "donorMatches"));
        return cacheManager;
    }
}
