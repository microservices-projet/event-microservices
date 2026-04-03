# 🔗 Integration Guide: User ↔ Event Service

## Overview
This guide shows how to integrate the optimized User and Event services using Feign and Kafka.

---

## Step 1: Add Dependencies to Event Service `pom.xml`

```xml
<!-- In the <dependencies> section, add: -->

<!-- Feign Client -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Kafka -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>

<!-- Caching -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Jackson (for Kafka message serialization) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

---

## Step 2: Create Event Service Properties File

**File:** `microservices/config-repo/event-service.properties`

Add these properties:

```properties
# ==================== Feign Configuration ====================
spring.cloud.openfeign.client.config.user-service.connect-timeout=5000
spring.cloud.openfeign.client.config.user-service.read-timeout=10000
feign.client.config.user-service.logger-level=full

# ==================== Kafka Configuration ====================
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=event-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.properties.linger.ms=10
spring.kafka.producer.compression-type=gzip

# ==================== Cache Configuration ====================
spring.cache.type=simple
spring.cache.cache-names=events,event-search,organizer-events

# ==================== Service URLs ====================
app.services.user-service.url=http://localhost:8081
app.services.event-service.url=http://localhost:8083
```

---

## Step 3: Copy Generated Files to Event Service

Copy these files to your event-service project:

```
src/main/java/org/example/eventmodule/
├── client/
│   ├── UserServiceClient.java
│   └── UserServiceClientConfig.java
├── service/
│   └── OptimizedEventService.java
├── controller/
│   └── OptimizedEventController.java
├── kafka/
│   ├── OptimizedEventKafkaConsumer.java
│   └── EventServiceKafkaListener.java
└── config/
    └── EventServiceConfiguration.java
```

---

## Step 4: Update Main Application Class

**File:** `EventModuleApplication.java`

```java
package org.example.eventmodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "org.example.eventmodule.client")  // ← ADD THIS
@EnableCaching                                                          // ← ADD THIS
@EnableKafka                                                            // ← ADD THIS
public class EventModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventModuleApplication.class, args);
    }
}
```

---

## Step 5: Update or Create Bootstrap Configuration

**File:** `src/main/resources/bootstrap.properties` or `bootstrap.yml`

```properties
spring.application.name=event-service
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.fail-fast=true
```

---

## Step 6: Test the Integration

### Start Services in Order

```bash
# Terminal 1: Config Server
.\run-config-server.ps1

# Terminal 2: Zookeeper
docker-compose up -d zookeeper

# Terminal 3: Kafka
docker-compose up -d kafka

# Terminal 4: User Service
.\run-user-service.ps1

# Terminal 5: Event Service
.\run-event-service.ps1
```

### Test Endpoints

```bash
# 1. Create Event (validates user via Feign)
curl -X POST http://localhost:8083/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tech Conference 2026",
    "description": "Annual tech conference",
    "date": "2026-04-15T09:00:00",
    "place": "New York",
    "price": 99.99,
    "organizerId": 1,
    "imageUrl": "https://example.com/image.jpg",
    "nbPlaces": 500,
    "domaines": ["Technology", "AI"]
  }'

# 2. Get all events (cached)
curl http://localhost:8083/api/v1/events

# 3. Get event by ID (cached)
curl http://localhost:8083/api/v1/events/1

# 4. Search events
curl "http://localhost:8083/api/v1/events/search?keyword=tech"

# 5. Like event
curl -X POST http://localhost:8083/api/v1/events/1/like?userId=1

# 6. Update event
curl -X PUT http://localhost:8083/api/v1/events/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "price": 89.99
  }'

# 7. Delete event
curl -X DELETE http://localhost:8083/api/v1/events/1
```

---

## Step 7: Verify Kafka Communication

### Check Kafka Topics

```bash
# List all topics
docker exec event-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Should see:
# - optimized-event-stream
# - user-events
# - feedback-events
# - reservation-events
```

### Consume Events (in separate terminal)

```bash
# Monitor optimized-event-stream topic
docker exec event-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic optimized-event-stream \
  --from-beginning
```

### Expected Output

When you create an event, you should see:
```json
{
  "eventType": "CREATED",
  "aggregateType": "Event",
  "aggregateId": "1",
  "payload": {...},
  "eventId": "uuid-123",
  "correlationId": "uuid-456",
  "timestamp": 1711000000000
}
```

---

## Step 8: Monitor Logs

Watch the console output to verify:

### Log Signs of Success

```
2026-03-21 17:30:45.123 INFO  → POST /api/v1/events - Create event
2026-03-21 17:30:45.234 DEBUG → Calling UserServiceClient.userExists(1)
2026-03-21 17:30:45.567 INFO  ✓ Event created successfully: id=1
2026-03-21 17:30:45.678 INFO  ✓ Event CREATED published: eventId=1
```

### Warning Signs

```
2026-03-21 17:30:45.234 WARN  ⚠ User service unavailable, proceeding with event creation
2026-03-21 17:30:45.567 WARN  ⚠ Failed to publish event to Kafka
```

---

## Step 9: Enable Monitoring (Optional)

### Add Actuator Endpoints

**In `event-service.properties`:**
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
```

### Access Metrics

```bash
# Health check
curl http://localhost:8083/actuator/health

# Metrics
curl http://localhost:8083/actuator/metrics

# Prometheus metrics
curl http://localhost:8083/actuator/prometheus
```

---

## Step 10: Create User Service Feign Client

### For other services wanting to call Event Service

Create `EventServiceClient.java`:

```java
package org.example.eventmodule.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "event-service",
    url = "${app.services.event-service.url:http://localhost:8083}",
    configuration = EventServiceClientConfig.class
)
public interface EventServiceClient {

    @GetMapping("/api/v1/events")
    List<Event> getAllEvents();

    @GetMapping("/api/v1/events/{id}")
    Event getEventById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/events/organizer/{organizerId}")
    List<Event> getEventsByOrganizer(@PathVariable("organizerId") Long organizerId);

    @GetMapping("/api/v1/events/search")
    List<Event> searchEvents(@RequestParam("keyword") String keyword);
}
```

---

## Step 11: Test Complete Flow

### Scenario: Create Event → Trigger Kafka → Consume in Other Service

```
1. POST /api/v1/events
   ↓
2. Event Service calls UserServiceClient.userExists()
   ↓
3. Event saved to database
   ↓
4. OptimizedEventKafkaConsumer.sendEventCreated() published to Kafka
   ↓
5. EventServiceKafkaListener in other services receives CREATED event
   ↓
6. Update their local cache/state based on event
```

---

## Troubleshooting

### Issue: Feign client throws UserNotFoundException

**Solution:** Ensure User Service is running and organizer ID exists
```bash
curl http://localhost:8081/api/users/1
```

### Issue: Kafka messages not being published

**Solution:** Check Kafka is running and bootstrap servers configured correctly
```bash
docker ps | grep kafka
```

### Issue: Cache not working

**Solution:** Verify caching is enabled and cache names are correct
```properties
spring.cache.type=simple
spring.cache.cache-names=events,event-search,organizer-events
```

### Issue: Connection timeout to User Service

**Solution:** Adjust Feign client timeouts in configuration
```properties
spring.cloud.openfeign.client.config.user-service.connect-timeout=10000
spring.cloud.openfeign.client.config.user-service.read-timeout=20000
```

---

## Performance Tuning

### Kafka Performance
```properties
# Batch messages for better throughput
spring.kafka.producer.properties.linger.ms=10
spring.kafka.producer.batch-size=32768

# Enable compression
spring.kafka.producer.compression-type=gzip

# Multiple consumer threads
spring.kafka.listener.concurrency=4
```

### Cache Performance
```properties
# Increase cache size if needed (upgrade to Redis for production)
spring.cache.type=simple

# Custom TTL per cache (requires code change)
@Cacheable(value = "events", cacheManager = "cacheManager")
```

### Feign Performance
```properties
# Connection pooling
spring.cloud.openfeign.client.config.default.logger-level=basic

# Timeout tuning
spring.cloud.openfeign.client.config.user-service.read-timeout=10000
spring.cloud.openfeign.client.config.user-service.connect-timeout=5000
```

---

## Production Deployment Checklist

- [ ] All services running and healthy
- [ ] Kafka cluster configured with replication
- [ ] Feign client timeouts tuned
- [ ] Cache backend upgraded to Redis
- [ ] Monitoring and alerting configured
- [ ] Load testing completed
- [ ] Backup/disaster recovery plan
- [ ] Security: SSL/TLS enabled
- [ ] API rate limiting configured
- [ ] Circuit breaker policies documented

---

## Support & Documentation

For more information, see:
- **ARCHITECTURE.md** - Full technical documentation
- **QUICK_REFERENCE.md** - Quick lookup guide
- **Code comments** - Javadoc on all classes
- **Config examples** - Property file templates

---

## Next: Create Similar Clients for Other Services

Repeat this process for other services:
- Reservation Service → Create UserServiceClient & EventServiceClient
- Feedback Service → Create UserServiceClient & EventServiceClient
- Ticket Service → Create UserServiceClient & EventServiceClient

Each service should:
1. ✓ Add Feign/Kafka dependencies
2. ✓ Create Feign clients for dependent services
3. ✓ Publish domain events to Kafka
4. ✓ Listen to relevant Kafka topics
5. ✓ Implement caching where appropriate

This creates a fully event-driven microservices architecture! 🚀

