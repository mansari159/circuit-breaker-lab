**Project Overview**

This project demonstrates the Circuit Breaker pattern using Spring Boot and Resilience4j. The circuit breaker prevents cascade failures in microservices by monitoring service calls and automatically stopping requests to failing services.
What is a Circuit Breaker?
A circuit breaker is a design pattern used in microservices to prevent cascading failures. It works like an electrical circuit breaker:

CLOSED: Normal operation, all requests pass through
OPEN: Too many failures detected, requests are blocked immediately
HALF_OPEN: Testing if service has recovered

Project Structure
circuit-breaker-lab/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/pgb/circuit_breaker/
    │   │   ├── CircuitBreakerApplication.java    # Main Spring Boot application
    │   │   ├── ExternalApiService.java           # Service with circuit breaker
    │   │   ├── HelloController.java              # REST endpoints
    │   │   └── LoadTestClient.java               # Automated test client
    │   └── resources/
    │       └── application.yml                    # Circuit breaker configuration
    └── test/
        └── java/com/pgb/circuit_breaker/
Technologies Used

Spring Boot 3.3.1 - Application framework
Spring Cloud Circuit Breaker - Circuit breaker abstraction
Resilience4j 2.0 - Circuit breaker implementation
Java 21 - Programming language
Maven - Build tool

Circuit Breaker Configuration
yamlresilience4j:
  circuitbreaker:
    instances:
      myServiceCB:
        slidingWindowSize: 10              # Monitor last 10 calls
        minimumNumberOfCalls: 5            # Need 5 calls before evaluating
        failureRateThreshold: 50           # Open circuit at 50% failure rate
        waitDurationInOpenState: 5s        # Stay open for 5 seconds
        permittedNumberOfCallsInHalfOpenState: 3  # Test with 3 calls
Prerequisites

Java 21 or higher
Maven 3.6+
Ubuntu VM (or any Linux/Mac/Windows)

Installation & Setup
On Ubuntu VM:
bash# Install Java and Maven
sudo apt update
sudo apt install -y openjdk-21-jdk maven

# Verify installations
java -version
mvn -version

# Clone or create project
cd ~
# (follow manual setup steps from guide)
How to Run
Option 1: Automated Load Test (Recommended)
bashcd ~/circuit-breaker-lab
mvn spring-boot:run -Dspring-boot.run.profiles=loadtest
This will:

Start the Spring Boot server
Launch 5 concurrent clients
Make 50 total API calls (10 per client)
Show circuit breaker state transitions
Display final statistics

Option 2: Manual Testing
bash# Terminal 1 - Start server
mvn spring-boot:run

# Terminal 2 - Make test calls
for i in {1..30}; do 
    curl http://localhost:8080/test
    echo ""
    sleep 0.5
done
Option 3: Browser Testing
bash# Start server
mvn spring-boot:run

# Open browser and visit:
# http://localhost:8080/
# http://localhost:8080/test (refresh multiple times)
# http://localhost:8080/actuator/health
# http://localhost:8080/actuator/circuitbreakers
Available Endpoints
EndpointDescription/Home page with links/testTest circuit breaker (60% failure rate)/api/helloSimple hello endpoint/statsView total call count/actuator/healthHealth check with circuit breaker status/actuator/circuitbreakersDetailed circuit breaker metrics
Expected Output
Load Test Output:
==================================================
   CIRCUIT BREAKER LOAD TEST
==================================================
Starting 5 concurrent clients...

Client 1 - Call  1: SUCCESS
Client 2 - Call  1: FAILED
Client 3 - Call  1: SUCCESS
Client 4 - Call  1: FAILED
Client 5 - Call  1: FAILED
Client 1 - Call  2: BLOCKED (Circuit Open)
Client 2 - Call  2: BLOCKED (Circuit Open)
...

==================================================
   RESULTS
==================================================
Successful Calls:          18 (36.0%)
Failed Calls:              12 (24.0%)
Blocked by Circuit Breaker: 20 (40.0%)
--------------------------------------------------
   Total Attempts:          50

==================================================
   ANALYSIS
==================================================
Circuit breaker blocked 20 calls when service was failing
Prevented cascade failures and system overload
Fast-fail behavior: blocked calls return immediately
Automatic recovery after wait period
How It Works
1. Service Simulation (ExternalApiService)
java@CircuitBreaker(name = "myServiceCB", fallbackMethod = "fallback")
public String callApi() {
    // 60% failure rate to simulate unreliable service
    if (random.nextDouble() < 0.6) {
        throw new RuntimeException("External API unavailable!");
    }
    return "Success from API";
}
2. Circuit Breaker States

CLOSED (Initial state)

All requests pass through normally
Monitors failure rate in sliding window


OPEN (After 50% failures)

Requests are blocked immediately
Fallback method is called
Waits 5 seconds before testing recovery


HALF_OPEN (Testing recovery)

Allows 3 test calls through
If successful: transition to CLOSED
If failed: back to OPEN



3. Benefits Demonstrated
Helps:

Prevents cascade failures (20+ calls blocked)
Fast-fail response (< 1ms vs 1-30s timeout)
Automatic recovery without manual intervention
System stays responsive during failures
Reduces load on failing service

Trade-offs:

Some valid requests blocked during OPEN state
Requires proper configuration tuning
Adds complexity to architecture

Key Metrics
From a typical test run:

Success Rate: ~36% (expected with 60% failure rate)
Blocked Rate: ~40% (circuit breaker preventing calls)
Failed Rate: ~24% (actual failures that got through)

Circuit Breaker Pattern Analysis
Without Circuit Breaker:

All 50 calls hit the failing service
Each waits for timeout (1-30 seconds)
Total wasted time: 50-1500 seconds
System becomes unresponsive
Cascade failures possible

With Circuit Breaker:

20 calls blocked immediately
Fast-fail responses (< 1ms)
System remains responsive
Automatic recovery testing
Protected from cascade failures

Troubleshooting
Port 8080 already in use
bash# Find and kill process
sudo lsof -ti:8080 | xargs kill -9
Maven dependencies won't download
bash# Clear cache and retry
rm -rf ~/.m2/repository
mvn clean compile
Java version mismatch
bash# Check Java version
java -version

# Should be Java 21
# If not, install: sudo apt install openjdk-21-jdk
