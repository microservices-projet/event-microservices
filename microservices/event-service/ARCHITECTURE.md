# Event Service - Optimized Microservice Architecture

## Overview
This document describes the optimized communication architecture between User Service and Event Service, utilizing **Feign HTTP clients** and **Kafka event streaming** with full CRUD operations.

---

## Architecture Components

### 1. **Feign Clients (HTTP/REST Communication)**

#### UserServiceClient
**Location:** `org.example.eventmodule.client.UserServiceClient`

**Purpose:** Synchronous REST communication with User Service

**CRUD Operations:**
```
CREATE - N/A (User Service responsibility)
READ:
  - getUserById(Long id)
  - getCurrentUserProfile()
  - getUserByEmail(String email)
  - getUsersByIds(List<Long> userIds)
  - userExists(Long id)
UPDATE - N/A (User Service responsibility)
DELETE - N/A (User Service responsibility)
```

**Features:**
- ✅ Circuit breaker pattern
- ✅ Automatic retry logic
- ✅ Error handling with custom exceptions
- ✅ Request/response logging
- ✅ Service unavailability fallback

**Configuration:** `UserServiceClientConfig`
- Retry on service unavailable (503)
- Circuit breaker timeout: 5000ms
- Retry backoff: exponential
- Max retry attempts: 3

**Usage:**
```java
@Autowired
UserServiceClient userServiceClient;

// Get user details
UserDTO user = userServiceClient.getUserById(userId);

// Validate multiple users
List<UserDTO> users = userServiceClient.getUsersByIds(userIds);

// Check user exists (before creating event)
Boolean exists = userServiceClient.userExists(organizerId);
```

---

### 2. **Kafka Event Streaming**

#### A. Event Publisher: `OptimizedEventKafkaConsumer`
**Location:** `org.example.eventmodule.kafka.OptimizedEventKafkaConsumer`

**Topic:** `optimized-event-stream`

**CRUD Event Messages:**
```
CREATE   → Event.CREATED
READ     → Event.FETCHED (for analytics)
UPDATE   → Event.UPDATED
DELETE   → Event.DELETED
ACTION   → Event.LIKED, Event.SEARCHED
```

**Example Kafka Messages:**

```json
{
  "eventType": "CREATED",
  "aggregateType": "Event",
  "aggregateId": "123",
  "payload": {
    "id": 123,
    "title": "Tech Conference 2026",
    "organizerId": 1,
    "date": "2026-04-15",
    "place": "New York",
    "nbPlaces": 100,
    "price": 99.99
  },
  "eventId": "uuid-1234",
  "correlationId": "uuid-5678",
  "timestamp": 1711000000000
}
```

**Published Events:**

| Operation | Event Type | Listeners | Use Case |
|-----------|-----------|-----------|----------|
| Create Event | CREATED | Reservation, Feedback, Ticket Services | Notify about new event |
| Fetch Event | FETCHED | Analytics Service | Track event views |
| Update Event | UPDATED | All Services | Sync event changes |
| Delete Event | DELETED | All Services | Cleanup related data |
| Like Event | LIKED | Analytics Service | Track popularity |
| Search Event | SEARCHED | Analytics Service | Track search metrics |

**API:**
```java
// Publish events
kafkaConsumer.sendEventCreated(event);
kafkaConsumer.sendEventUpdated(event);
kafkaConsumer.sendEventDeleted(eventId, eventTitle);
kafkaConsumer.sendEventLiked(eventId, userId, totalLikes);
kafkaConsumer.sendEventSearched(keyword, resultCount);
kafkaConsumer.sendEventFetched(event);
```

---

#### B. Event Consumer: `EventServiceKafkaListener`
**Location:** `org.example.eventmodule.kafka.EventServiceKafkaListener`

**Consumed Topics:**

| Topic | Source Service | Events | Purpose |
|-------|----------------|--------|---------|
| user-events | User Service | CREATED, UPDATED, DELETED | Update event cache, validate users |
| feedback-events | Feedback Service | CREATED, MODERATED, DELETED | Update event ratings/statistics |
| reservation-events | Reservation Service | CREATED, CANCELLED, CONFIRMED | Update available places |

**Consumed Event Handlers:**

```java
// User Service Events
handleUserCreated(event)      // Cache user, create preferences
handleUserUpdated(event)      // Update cached user data
handleUserDeleted(event)      // Archive user's events

// Feedback Service Events
handleFeedbackCreated(event)  // Update event rating
handleFeedbackModerated(event) // Update moderation status
handleFeedbackDeleted(event)  // Recalculate event statistics

// Reservation Service Events
handleReservationCreated(event)   // Decrement available places
handleReservationCancelled(event) // Increment available places
handleReservationConfirmed(event) // Update payment status
```

**Retry Policy:**
- Attempts: 3
- Backoff strategy: Exponential (1000ms base, 2.0 multiplier)
- Retry topics: Automatic topic suffixing
- Error handling: Dead letter queue support

---

## Service Layer: `OptimizedEventService`

**Location:** `org.example.eventmodule.service.OptimizedEventService`

### CRUD Operations

#### CREATE
```java
public Event createEvent(EventRequest request)
```
- Validates organizer via Feign client
- Creates event with DRAFT status
- Publishes CREATED event to Kafka
- Clears event cache

#### READ
```java
public List<Event> getAllEvents()          // Cached
public Event getEventById(Long id)         // Cached
public List<Event> getEventsByOrganizer(Long organizerId)
public List<Event> searchEvents(String keyword)
```
- Results cached for performance
- FETCHED events published for analytics
- SEARCHED events published for tracking

#### UPDATE
```java
public Event updateEvent(Long id, EventRequest request)
public Event updateEventStatus(Long id, EventStatus status)
public Event publishEvent(Long id)
```
- Validates data integrity
- Publishes UPDATED event to Kafka
- Invalidates cache

#### DELETE
```java
public void deleteEvent(Long id)
```
- Archives event (soft delete)
- Publishes DELETED event to Kafka
- Clears cache
- Cleanup handled by listening services

#### ACTIONS
```java
public Event likeEvent(Long id, Long userId)
```
- Increments like counter
- Publishes LIKED event for analytics
- Updates cache

---

## REST API Endpoints

### Base URL: `/api/v1/events`

#### CREATE
```
POST /api/v1/events
Content-Type: application/json

{
  "title": "Tech Conference 2026",
  "description": "Annual tech conference",
  "date": "2026-04-15T09:00:00",
  "place": "New York Convention Center",
  "price": 99.99,
  "organizerId": 1,
  "imageUrl": "https://example.com/image.jpg",
  "nbPlaces": 500,
  "domaines": ["Technology", "AI", "Cloud"]
}

Response: 201 Created
{
  "id": 123,
  "title": "Tech Conference 2026",
  "status": "DRAFT",
  ...
}
```

#### READ
```
GET /api/v1/events                      # Get all (cached)
GET /api/v1/events/{id}                 # Get by ID (cached)
GET /api/v1/events/organizer/{orgId}    # Get by organizer
GET /api/v1/events/search?keyword=tech  # Search events

Response: 200 OK
[
  {
    "id": 123,
    "title": "Tech Conference 2026",
    "nbLikes": 42,
    "nbPlaces": 500,
    ...
  }
]
```

#### UPDATE
```
PUT /api/v1/events/{id}
Content-Type: application/json

{
  "title": "Updated Title",
  "price": 89.99,
  ...
}

Response: 200 OK

PATCH /api/v1/events/{id}/status?status=PUBLISHED
Response: 200 OK

PATCH /api/v1/events/{id}/publish
Response: 200 OK
```

#### DELETE
```
DELETE /api/v1/events/{id}
Response: 204 No Content
```

#### ACTIONS
```
POST /api/v1/events/{id}/like?userId={userId}
Response: 200 OK
{
  "id": 123,
  "nbLikes": 43,
  ...
}
```

---

## Communication Flow Diagram

### Scenario 1: Create Event (User Service Validation)
```
Frontend
   |
   v
OptimizedEventController.createEvent()
   |
   v
OptimizedEventService.createEvent()
   |-- Validate organizer via Feign
   |   |
   |   v
   |   UserServiceClient.userExists(organizerId)
   |   |
   |   v
   |   User Service (/api/users/exists/{id})
   |
   |-- Save event to database
   |
   v
Kafka: Event.CREATED published
   |
   v
All consuming services (Reservation, Feedback, etc.)
```

### Scenario 2: Update Event (Cache Invalidation)
```
Frontend
   |
   v
OptimizedEventController.updateEvent()
   |
   v
OptimizedEventService.updateEvent()
   |-- Update database
   |-- @CacheEvict clears cache
   |
   v
Kafka: Event.UPDATED published
   |
   v
Related services update their views
```

### Scenario 3: Process Reservation Event
```
Reservation Service
   |
   v
Publishes "reservation-events" to Kafka
   |
   v
Event Service receives via KafkaListener
   |
   v
EventServiceKafkaListener.handleReservationEvent()
   |-- Parse event
   |-- Route to specific handler
   |-- Update event availability
   |
   v
Cache updated
```

---

## Configuration Properties

### `application.properties` / `event-service.properties`

```properties
# Feign Configuration
spring.cloud.openfeign.client.config.user-service.connect-timeout=5000
spring.cloud.openfeign.client.config.user-service.read-timeout=10000
feign.client.config.user-service.logger-level=full

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=event-service-group
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.properties.linger.ms=10

# Cache Configuration
spring.cache.type=simple
cache.events.ttl=3600

# Service URLs
app.services.user-service.url=http://localhost:8081
app.services.event-service.url=http://localhost:8083
```

---

## Error Handling

### Feign Client Errors
```
UserNotFoundException      → User doesn't exist (404)
UserServiceUnavailableException → User service down (503)
UserServiceException       → Generic service error
```

### Kafka Consumer Errors
- Automatic retry with exponential backoff
- Dead letter queue for failed messages
- Comprehensive logging for debugging

### Service Layer Errors
- Validation errors: 400 Bad Request
- Not found errors: 404 Not Found
- Server errors: 500 Internal Server Error

---

## Performance Optimizations

### Caching Strategy
- **Cache Names:** events, event-search, organizer-events
- **TTL:** 3600 seconds (1 hour)
- **Invalidation:** @CacheEvict on CREATE, UPDATE, DELETE
- **Engine:** ConcurrentMapCacheManager (can upgrade to Redis)

### Kafka Optimization
- **Batching:** linger.ms=10 (batch messages)
- **Compression:** gzip enabled
- **Partitioning:** By aggregate ID for ordering
- **Consumer:** Parallel processing with retries

### Feign Optimization
- **Connection pooling:** Apache HttpClient
- **Request compression:** Enabled
- **Caching:** Spring Cache integration
- **Timeout:** Read=10s, Connect=5s

---

## Monitoring & Logging

### Log Levels
```
INFO   - All CRUD operations with timestamps
DEBUG  - Feign client interactions
WARN   - Service unavailability, retries
ERROR  - Critical failures, exceptions
```

### Metrics
- Event creation rate
- Cache hit/miss ratio
- Kafka publishing latency
- Feign client response time
- Error rate per operation

### Example Logs
```
2026-03-21 17:30:45.123 INFO  → POST /api/v1/events - Create event: Tech Conference
2026-03-21 17:30:45.234 DEBUG → UserServiceClient.userExists(1)
2026-03-21 17:30:45.567 INFO  ✓ Event created successfully: id=123, title=Tech Conference
2026-03-21 17:30:45.678 INFO  ✓ Event CREATED published: eventId=123
```

---

## Testing

### Unit Tests
```java
@Test
public void testCreateEventWithValidOrganizer() {
    // Mock UserServiceClient
    when(userServiceClient.userExists(1L)).thenReturn(true);
    
    // Create event
    Event event = eventService.createEvent(request);
    
    // Assert
    assertNotNull(event);
    verify(kafkaConsumer).sendEventCreated(event);
}

@Test
public void testCreateEventWithInvalidOrganizer() {
    // Mock UserServiceClient
    when(userServiceClient.userExists(999L))
        .thenThrow(new UserNotFoundException("User not found"));
    
    // Assert exception
    assertThrows(UserNotFoundException.class, 
        () -> eventService.createEvent(invalidRequest));
}
```

### Integration Tests
- Test Feign client with WireMock
- Test Kafka publishing/consuming
- Test cache invalidation
- Test error handling and retries

---

## Dependencies Required

Add to `pom.xml`:
```xml
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

<!-- Circuit Breaker (optional, for resilience4j) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-circuitbreaker-resilience4j</artifactId>
</dependency>
```

---

## Migration Guide

### Step 1: Update Event Service POM
Add Feign, Kafka, and Cache dependencies

### Step 2: Enable Feign
```java
@EnableFeignClients(basePackages = "org.example.eventmodule.client")
```

### Step 3: Inject new service
```java
private final OptimizedEventService eventService;
```

### Step 4: Update endpoints
Replace old `/api/events` with new `/api/v1/events`

### Step 5: Test thoroughly
- Unit tests for service layer
- Integration tests with Feign/Kafka
- Load tests for caching effectiveness

---

## Conclusion

This optimized architecture provides:
- ✅ **Reliability:** Circuit breakers, retries, fallbacks
- ✅ **Performance:** Caching, batching, async messaging
- ✅ **Scalability:** Kafka-based event streaming
- ✅ **Observability:** Comprehensive logging & metrics
- ✅ **Maintainability:** Clean separation of concerns, CRUD operations

