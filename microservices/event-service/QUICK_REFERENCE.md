# Quick Reference: User ↔ Event Service Communication

## 📡 Communication Channels

### 1. Feign HTTP (Synchronous)
**Used for:** Real-time, request-response operations
**Client:** `UserServiceClient`
**Typical calls:** User validation, getting user details

### 2. Kafka Events (Asynchronous)
**Used for:** Event streaming, notifications, eventual consistency
**Publisher:** `OptimizedEventKafkaConsumer`
**Consumer:** `EventServiceKafkaListener`
**Topics:** user-events, feedback-events, reservation-events, optimized-event-stream

---

## 🔄 CRUD Operation Matrix

### Event Service CRUD with User Service

| Operation | Endpoint | Method | Feign | Kafka |
|-----------|----------|--------|-------|-------|
| **CREATE** | `POST /api/v1/events` | Event Creation | ✓ (Validate Organizer) | ✓ (Publish CREATED) |
| **READ** | `GET /api/v1/events/{id}` | Fetch Event | ✗ | ✓ (Publish FETCHED) |
| **UPDATE** | `PUT /api/v1/events/{id}` | Update Event | ✗ | ✓ (Publish UPDATED) |
| **DELETE** | `DELETE /api/v1/events/{id}` | Delete Event | ✗ | ✓ (Publish DELETED) |
| **LIKE** | `POST /api/v1/events/{id}/like` | Like Event | ✗ | ✓ (Publish LIKED) |
| **SEARCH** | `GET /api/v1/events/search` | Search Events | ✗ | ✓ (Publish SEARCHED) |

---

## 📋 Feign Client Operations

### UserServiceClient Interface

```java
// Check if user exists
Boolean userExists(Long userId)

// Get user details
UserDTO getUserById(Long userId)
UserDTO getUserByEmail(String email)

// Get current user
UserDTO getCurrentUserProfile()

// Batch operations
List<UserDTO> getUsersByIds(List<Long> userIds)
```

---

## 🎯 Kafka Events Published by Event Service

### Topic: `optimized-event-stream`

| Event | Trigger | Payload | Consumers |
|-------|---------|---------|-----------|
| `CREATED` | New event created | Full event object | All services |
| `UPDATED` | Event details changed | Updated event object | All services |
| `DELETED` | Event archived | Event ID + Title | All services |
| `FETCHED` | Event viewed | Event object | Analytics |
| `LIKED` | User likes event | Event ID, User ID, Total Likes | Analytics |
| `SEARCHED` | Event search performed | Keyword, Result count | Analytics |

---

## 🎧 Kafka Events Consumed by Event Service

### From: `user-events`
```
CREATED  → User registered
UPDATED  → User profile changed
DELETED  → User deleted (archive events)
```

### From: `feedback-events`
```
CREATED   → Feedback submitted (update ratings)
MODERATED → Feedback reviewed
DELETED   → Feedback removed (recalculate stats)
```

### From: `reservation-events`
```
CREATED   → Reservation made (decrement places)
CANCELLED → Reservation cancelled (increment places)
CONFIRMED → Reservation confirmed (update status)
```

---

## 🛠️ Code Examples

### Create Event with Validation

```java
@PostMapping
public ResponseEntity<Event> createEvent(@RequestBody EventRequest request) {
    // This internally:
    // 1. Calls userServiceClient.userExists(organizerId) via Feign
    // 2. Creates event
    // 3. Publishes CREATED event to Kafka
    Event event = eventService.createEvent(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(event);
}
```

### Like Event

```java
@PostMapping("/{id}/like")
public ResponseEntity<Event> likeEvent(
        @PathVariable Long id,
        @RequestParam Long userId) {
    // This internally:
    // 1. Increments like counter
    // 2. Publishes LIKED event to Kafka
    Event event = eventService.likeEvent(id, userId);
    return ResponseEntity.ok(event);
}
```

### Listen to User Events

```java
@KafkaListener(topics = "user-events", groupId = "event-service-user-group")
public void handleUserEvent(String eventPayload) {
    // Automatically invoked when user-events topic receives messages
    // Handles CREATED, UPDATED, DELETED events
    // Can update event cache or perform cleanup
}
```

---

## ⚡ Performance Features

### Caching
- Event cache: 1-hour TTL
- Automatic invalidation on CREATE, UPDATE, DELETE
- Cache engine: ConcurrentMapCacheManager (upgradeable to Redis)

### Kafka Features
- Message batching: 10ms linger
- Compression: gzip enabled
- Retry: 3 attempts with exponential backoff
- Partitioning: By aggregate ID for message ordering

### Feign Client Features
- Circuit breaker: Fallback when User Service unavailable
- Retry: Automatic with exponential backoff
- Connection pooling: Apache HttpClient
- Timeout: 5s connect, 10s read

---

## 🚨 Error Handling

### Feign Errors
```
404 Not Found      → UserNotFoundException
503 Service Error  → UserServiceUnavailableException
Other              → UserServiceException
```

### Kafka Consumer Errors
- Automatic retry: 3 attempts
- Backoff: 1000ms base, 2.0x multiplier
- Dead letter queue: Failed messages

### Validation Errors
- Invalid event data: 400 Bad Request
- Event not found: 404 Not Found
- Server errors: 500 Internal Server Error

---

## 📊 Logging

```
INFO   ✓ Operation successful with details
DEBUG  🔍 Feign client interactions
WARN   ⚠ Service unavailable, retrying
ERROR  ✗ Critical failure, need attention
```

### Example
```
2026-03-21 17:30:45.123 INFO  → POST /api/v1/events - Create event
2026-03-21 17:30:45.234 DEBUG → Calling UserServiceClient.userExists(1)
2026-03-21 17:30:45.567 INFO  ✓ Event created: id=123
2026-03-21 17:30:45.678 INFO  ✓ Event CREATED published to Kafka
```

---

## 🔐 Security

### Feign Client Configuration
- SSL/TLS support: Enabled
- Request signing: Spring Security integrated
- Authentication: Bearer token support

### Kafka Security
- SASL/SCRAM: Optional authentication
- SSL: Optional encryption
- ACLs: Configure topic access

---

## 📈 Monitoring Checklist

- [ ] Feign client: Response time, success rate
- [ ] Kafka: Publishing latency, consumer lag
- [ ] Cache: Hit/miss ratio
- [ ] Error rate: By operation type
- [ ] Database: Query performance

---

## 🧪 Testing Checklist

- [ ] Unit: Service layer CRUD operations
- [ ] Integration: Feign client with WireMock
- [ ] Integration: Kafka producer/consumer
- [ ] E2E: Full event lifecycle
- [ ] Load: Cache effectiveness under traffic
- [ ] Chaos: Feign fallback, Kafka unavailability

---

## 📚 File Locations

```
microservices/event-service/src/main/java/org/example/eventmodule/
├── client/
│   ├── UserServiceClient.java           # Feign client
│   └── UserServiceClientConfig.java     # Configuration
├── service/
│   └── OptimizedEventService.java       # CRUD + Feign + Kafka
├── controller/
│   └── OptimizedEventController.java    # REST endpoints
├── kafka/
│   ├── OptimizedEventKafkaConsumer.java  # Event publisher
│   └── EventServiceKafkaListener.java    # Event consumer
├── config/
│   └── EventServiceConfiguration.java   # Caching, Feign setup
└── ARCHITECTURE.md                      # Full documentation
```

---

## 🚀 Deployment Checklist

- [ ] All Feign clients configured with proper timeouts
- [ ] Kafka broker URLs configured correctly
- [ ] Cache TTL appropriate for use case
- [ ] Retry policies set (3 attempts, exponential backoff)
- [ ] Error handlers configured
- [ ] Monitoring & alerts setup
- [ ] Logging level appropriate
- [ ] Documentation updated
- [ ] Tests passing (unit + integration)
- [ ] Performance tested under load

---

## 📞 Support

For issues or questions, refer to:
1. ARCHITECTURE.md for detailed documentation
2. Code comments and Javadoc in classes
3. Application logs with detailed error messages
4. Integration tests for usage examples

