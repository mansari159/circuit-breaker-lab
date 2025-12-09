package com.pgb.circuit_breaker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class HelloController {
    
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);
    private final ExternalApiService service;
    
    public HelloController(ExternalApiService service) {
        this.service = service;
    }
    
    @GetMapping("/")
    public String home() {
        return "Circuit Breaker Demo - Visit /test to test the circuit breaker";
    }
    
    @GetMapping("/test")
    public String testCircuitBreaker() {
        log.info("Received request to /test");
        return service.callApi();
    }
    
    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from Spring Boot circuit breaker example!";
    }
    
    @GetMapping("/stats")
    public String stats() {
        return "Total API calls attempted: " + service.getCallCount();
    }
}
