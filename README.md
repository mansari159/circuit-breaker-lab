# Circuit Breaker Lab

## Description

This project demonstrates the Circuit Breaker pattern using Spring Boot and Resilience4j. The circuit breaker monitors an unreliable service (60% failure rate) and automatically blocks requests when failures exceed 50%, preventing cascade failures in the system.

## Requirements

- Java 21
- Maven 3.6+

## Installation
```bash
# Install Java and Maven
sudo apt update
sudo apt install -y openjdk-21-jdk maven

# Verify
java -version
mvn -version
```

## Running the Application

### Option 1: Automated Load Test (5 clients, 50 calls)
```bash
cd ~/circuit-breaker-lab
mvn spring-boot:run -Dspring-boot.run.profiles=loadtest
```

### Option 2: Start Server and Test Manually
```bash
# Terminal 1 - Start server
mvn spring-boot:run

# Terminal 2 - Test with curl
for i in {1..30}; do 
    curl http://localhost:8080/test
    echo ""
    sleep 0.5
done
```

### Option 3: Browser
```bash
mvn spring-boot:run
```

Then visit:
- http://localhost:8080/test (refresh multiple times)
- http://localhost:8080/actuator/health
- http://localhost:8080/actuator/circuitbreakers

## Expected Results

The circuit breaker will:
1. Allow requests initially (CLOSED state)
2. Open after 50% failure rate detected
3. Block subsequent requests while OPEN
4. Test recovery after 5 seconds (HALF_OPEN)
5. Close again if service recovers

Typical output: ~36% success, ~24% failures, ~40% blocked by circuit breaker

