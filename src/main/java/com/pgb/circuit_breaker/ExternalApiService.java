package com.pgb.circuit_breaker;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExternalApiService {
    
    private static final Logger log = LoggerFactory.getLogger(ExternalApiService.class);
    private final Random random = new Random();
    private final AtomicInteger callCount = new AtomicInteger(0);
    
    @CircuitBreaker(name = "myServiceCB", fallbackMethod = "fallback")
    public String callApi() {
        int count = callCount.incrementAndGet();
        log.info("Attempting API call #{}", count);
        
        try {
            Thread.sleep(100);
            
            if (random.nextDouble() < 0.6) {
                log.error("API call #{} FAILED", count);
                throw new RuntimeException("External API unavailable!");
            }
            
            log.info("API call #{} SUCCESS", count);
            return "Success from API - Call #" + count;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", e);
        }
    }
    
    public String fallback(Throwable t) {
        log.warn("CIRCUIT BREAKER ACTIVE - Fallback triggered: {}", t.getMessage());
        return "Service temporarily unavailable (Circuit Breaker OPEN) - " + t.getMessage();
    }
    
    public int getCallCount() {
        return callCount.get();
    }
}
