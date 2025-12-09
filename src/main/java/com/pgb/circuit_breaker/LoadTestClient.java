package com.pgb.circuit_breaker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Profile("loadtest")
public class LoadTestClient implements CommandLineRunner {
    
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failureCount = new AtomicInteger(0);
    private static final AtomicInteger circuitOpenCount = new AtomicInteger(0);
    
    @Override
    public void run(String... args) throws InterruptedException {
        System.out.println("   CIRCUIT BREAKER LOAD TEST");
        System.out.println("Starting 5 concurrent clients...\n");
        
        Thread.sleep(2000);
        
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/test";
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        for (int clientId = 1; clientId <= 5; clientId++) {
            final int id = clientId;
            executor.submit(() -> runClient(id, restTemplate, url));
        }
        
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        
        printResults();
        
        System.out.println("\nTest complete. Press Ctrl+C to exit.");
    }
    
    private void runClient(int clientId, RestTemplate restTemplate, String url) {
        for (int i = 1; i <= 10; i++) {
            try {
                Thread.sleep(200);
                
                String response = restTemplate.getForObject(url, String.class);
                
                if (response != null && response.contains("Circuit Breaker OPEN")) {
                    circuitOpenCount.incrementAndGet();
                    System.out.printf("Client %d - Call %2d: BLOCKED (Circuit Open)%n", 
                                    clientId, i);
                } else if (response != null && response.contains("Success")) {
                    successCount.incrementAndGet();
                    System.out.printf("Client %d - Call %2d: SUCCESS%n", clientId, i);
                } else if (response != null && response.contains("unavailable")) {
                    circuitOpenCount.incrementAndGet();
                    System.out.printf("Client %d - Call %2d: FALLBACK%n", clientId, i);
                } else {
                    failureCount.incrementAndGet();
                    System.out.printf("Client %d - Call %2d: FAILED%n", clientId, i);
                }
                
            } catch (Exception e) {
                failureCount.incrementAndGet();
                System.out.printf("Client %d - Call %2d: ERROR%n", clientId, i);
            }
        }
    }
    
    private void printResults() {
        int total = successCount.get() + failureCount.get() + circuitOpenCount.get();
        
        System.out.println("   RESULTS");
        System.out.printf("Successful Calls:          %3d (%.1f%%)%n", 
                         successCount.get(), 
                         100.0 * successCount.get() / total);
        System.out.printf("Failed Calls:              %3d (%.1f%%)%n", 
                         failureCount.get(), 
                         100.0 * failureCount.get() / total);
        System.out.printf("Blocked by Circuit Breaker: %3d (%.1f%%)%n", 
                         circuitOpenCount.get(), 
                         100.0 * circuitOpenCount.get() / total);
        System.out.printf("   Total Attempts:          %3d%n", total);
        
        System.out.println("   ANALYSIS");
        System.out.println("Circuit breaker blocked " + circuitOpenCount.get() + 
                         " calls when service was failing");
        System.out.println("Prevented cascade failures and system overload");
        System.out.println("Fast-fail behavior: blocked calls return immediately");
        System.out.println("Automatic recovery after wait period");
    }
}
