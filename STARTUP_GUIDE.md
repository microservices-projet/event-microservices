# 🚀 MICROSERVICES STARTUP GUIDE

## Quick Start

All PowerShell scripts have been updated with professional features:
- ✅ Error handling & validation
- ✅ Database connectivity checks  
- ✅ Logging to files
- ✅ Help documentation (`-Help` parameter)
- ✅ Clean build option (`-Clean` parameter)
- ✅ Beautiful formatted output with emojis
- ✅ Window title showing service name and port

All logs are saved to: `./logs/` directory

---

## 📋 STARTUP ORDER

**⚠️ IMPORTANT: Services must start in this order:**

### Phase 1: Core Infrastructure (Required for everything)
Must start these FIRST and wait for them to be fully ready:

1. **Eureka (Discovery Server)** - Port 8761
   ```powershell
   .\run-eureka.ps1
   ```
   - Access: http://localhost:8761
   - Wait for startup message
   - ⚠️ No dependencies! Starts independently

2. **Config Server** - Port 8888
   ```powershell
   .\run-config-server.ps1
   ```
   - Access: http://localhost:8888
   - Wait for startup message
   - Depends on: Eureka (for discovery)

3. **API Gateway** - Port 8080
   ```powershell
   .\run-gateway.ps1
   ```
   - Access: http://localhost:8080
   - Central entry point for all requests
   - Depends on: Eureka + Config Server

### Phase 2: Microservices (Can start in parallel after Phase 1)

#### Core Services (Start these after Gateway is running):

4. **User Service** - Port 8081 (H2 Database, no Docker needed)
   ```powershell
   .\run-user-service.ps1
   ```
   - API: http://localhost:8081/api/users
   - H2 Console: http://localhost:8081/h2-console

5. **Event Service** - Port 8082 (MySQL)
   ```powershell
   .\run-event-service.ps1
   ```
   - API: http://localhost:8082/api/events
   - Requires: MySQL container running

6. **Ticket Service** - Port 8083 (MySQL)
   ```powershell
   .\run-ticket-service.ps1
   ```
   - API: http://localhost:8083/api/tickets
   - Requires: MySQL container running

#### Supporting Services:

7. **Reservation Service** - Port 8084 (MongoDB)
   ```powershell
   .\run-reservation-service.ps1
   ```
   - API: http://localhost:8084/api/reservations
   - Requires: MongoDB container running

8. **Feedback Service** - Port 8085 (H2 Database)
   ```powershell
   .\run-feedback-service.ps1
   ```
   - API: http://localhost:8085/api/feedback
   - H2 Console: http://localhost:8085/h2-console

9. **Reclamation Service** - Port 8086 (MySQL)
   ```powershell
   .\run-reclamation-service.ps1
   ```
   - API: http://localhost:8086/api/reclamations
   - Requires: MySQL container running

10. **Notification Service** - Port 8087 (RabbitMQ)
    ```powershell
    .\run-notification-service.ps1
    ```
    - Requires: RabbitMQ container running

11. **Saga Orchestrator Service** - Port 8088
    ```powershell
    .\run-saga-orchestrator-service.ps1
    ```
    - Requires: RabbitMQ & Kafka containers running

12. **Analytics Streams Service** - Port 8089 (MongoDB, Redis, Kafka)
    ```powershell
    .\run-analytics-service.ps1
    ```
    - Requires: MongoDB, Redis, Kafka containers running

---

## 🐳 Docker Infrastructure Prerequisites

Before starting microservices, ensure Docker containers are running:

```powershell
cd c:\Users\khalf\Awd-Web\event-Web-App
docker-compose up -d
```

This starts:
- ✓ MySQL (port 3306)
- ✓ MongoDB (port 27017)
- ✓ PostgreSQL for Keycloak (port 5433)
- ✓ RabbitMQ (ports 5672, 15672)
- ✓ Kafka (port 9092)
- ✓ Zookeeper (port 2181)
- ✓ Redis (port 6379)
- ✓ Keycloak (port 8180)
- ✓ Config Server (Docker image, port 9888)

Check status:
```powershell
docker-compose ps
```

---

## 📊 SERVICE DEPENDENCIES MAP

```
Docker Infrastructure (MySQL, MongoDB, RabbitMQ, Kafka, Redis)
    ↓
Eureka (8761) ← Must Start FIRST
    ↓
Config Server (8888) ← Must Start SECOND
    ↓
API Gateway (8080) ← Must Start THIRD
    ↓
[All microservices can start in parallel]
├─ User Service (8081) → H2
├─ Event Service (8082) → MySQL
├─ Ticket Service (8083) → MySQL
├─ Reservation Service (8084) → MongoDB
├─ Feedback Service (8085) → H2
├─ Reclamation Service (8086) → MySQL
├─ Notification Service (8087) → RabbitMQ
├─ Saga Orchestrator (8088) → RabbitMQ/Kafka
└─ Analytics Service (8089) → MongoDB/Redis/Kafka
```

---

## 💻 Script Usage Examples

### Basic Usage
```powershell
# Start a service
.\run-event-service.ps1

# Show help
.\run-event-service.ps1 -Help

# Clean build before running
.\run-event-service.ps1 -Clean

# Clean then run
.\run-event-service.ps1 -Clean
```

### Database Connections

**MySQL Services:**
- Host: `localhost:3306`
- Username: `root`
- Password: (empty)
- Databases: `event-db`, `ticketdb`, `reclamationdb`

**MongoDB Services:**
- URI: `mongodb://localhost:27017`
- Databases: `reservationdb`, `analytics-db`

**H2 Services:**
- Access H2 Console at: `http://localhost:PORT/h2-console`
- Connection URL: `jdbc:h2:mem:DBNAME`
- Username: `sa`
- Password: (leave empty)

**RabbitMQ:**
- Host: `localhost:5672`
- Management UI: http://localhost:15672
- Username/Password: `guest`/`guest`

**Kafka:**
- Bootstrap Server: `localhost:9092`

---

## 📝 Viewing Logs

Each service logs to: `./logs/SERVICE-NAME.log`

Examples:
- `logs/eureka.log`
- `logs/event-service.log`
- `logs/ticket-service.log`
- etc.

View logs in real-time in the terminal output.

---

## ✅ Verification Checklist

After starting all services, verify:

- [ ] Eureka Dashboard: http://localhost:8761
  - You should see all services registered
  
- [ ] Config Server: http://localhost:8888/actuator/health
  - Should return status UP

- [ ] API Gateway: http://localhost:8080
  - Should respond

- [ ] Individual Service Health:
  - http://localhost:8081/actuator/health (User)
  - http://localhost:8082/actuator/health (Event)
  - http://localhost:8083/actuator/health (Ticket)
  - etc.

- [ ] RabbitMQ Management: http://localhost:15672
  - Connection status: Connected

- [ ] Keycloak Admin: http://localhost:8180
  - Login with admin/admin

---

## 🔍 Troubleshooting

### Service Won't Start
```powershell
# Check if port is already in use
netstat -ano | findstr :8082

# Run with clean build
.\run-event-service.ps1 -Clean

# Check Java installation
java -version
```

### Database Connection Error
```powershell
# Ensure Docker containers are running
docker-compose ps

# Restart Docker infrastructure
docker-compose down
docker-compose up -d

# Wait 30 seconds before starting services
```

### Service Discovery Issues
```powershell
# Ensure Eureka is started first
# Check Eureka dashboard for service registration
# http://localhost:8761
```

### Build Failures
```powershell
# Try clean build option
.\run-event-service.ps1 -Clean

# Check Maven is in PATH
mvn --version

# Check Java version (should be 17)
java -version
```

---

## 🎯 Full Startup Script (Recommended)

Create a file `startup-all.ps1` to start all services in separate terminals:

```powershell
# Phase 1: Core Infrastructure
Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-eureka.ps1"
Start-Sleep -Seconds 10

Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-config-server.ps1"
Start-Sleep -Seconds 10

Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-gateway.ps1"
Start-Sleep -Seconds 5

# Phase 2: Microservices (parallel)
Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-user-service.ps1"
Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-event-service.ps1"
Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-ticket-service.ps1"
Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-reservation-service.ps1"
Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-feedback-service.ps1"
Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-reclamation-service.ps1"
Start-Process powershell -ArgumentList "-NoExit -Command cd $PSScriptRoot; .\run-notification-service.ps1"

Write-Host "All services started in separate terminals" -ForegroundColor Green
```

Note: This opens each service in a new terminal window.

---

## 📱 Frontend Setup

After all backend services are running:

```powershell
cd frontend
npm install
npm start
```

Frontend will run at: http://localhost:4200

---

## 📚 Additional Resources

- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management**: http://localhost:15672
- **Keycloak Admin**: http://localhost:8180
- **Config Server**: http://localhost:8888
- **API Gateway**: http://localhost:8080

---

**Last Updated**: March 26, 2026
**Created by**: GitHub Copilot
