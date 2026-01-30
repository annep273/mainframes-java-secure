package com.mainframes.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Rate Limiting Configuration
 * Protects API from abuse and DDoS attacks
 */
@Configuration
public class RateLimitConfig {

    /**
     * Configure rate limiting bucket
     * Allows 100 requests per minute per IP
     */
    @Bean
    public Bucket rateLimitBucket() {
        // Refill 100 tokens every minute
        Bandwidth limit = Bandwidth.classic(
            100, 
            Refill.intervally(100, Duration.ofMinutes(1))
        );
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}
