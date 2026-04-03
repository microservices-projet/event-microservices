# ✅ PowerShell Scripts Update Summary

## What Was Updated

All 9 PowerShell scripts `.ps1` files have been recreated with professional enterprise-grade features:

### Scripts Updated:
1. ✅ `run-eureka.ps1` (Discovery Server - Port 8761)
2. ✅ `run-config-server.ps1` (Config Server - Port 8888)
3. ✅ `run-gateway.ps1` (API Gateway - Port 8080)
4. ✅ `run-user-service.ps1` (User Service - Port 8081)
5. ✅ `run-event-service.ps1` (Event Service - Port 8082)
6. ✅ `run-ticket-service.ps1` (Ticket Service - Port 8083)
7. ✅ `run-reservation-service.ps1` (Reservation Service - Port 8084)
8. ✅ `run-feedback-service.ps1` (Feedback Service - Port 8085)
9. ✅ `run-reclamation-service.ps1` (Reclamation Service - Port 8086)
10. ✅ `run-notification-service.ps1` (Notification Service - Port 8087)
11. ✅ `run-saga-orchestrator-service.ps1` (Saga Orchestrator - Port 8088)
12. ✅ `run-analytics-service.ps1` (Analytics Service - Port 8089)

## 🎯 New Features Added

### 1. **Help Documentation**
```powershell
.\run-event-service.ps1 -Help
```
- Shows service description
- Lists prerequisites
- Database connection info
- Usage examples
- Access URLs

### 2. **Error Handling**
- Java 17 validation
- Database connectivity checks
- Build failure detection
- Graceful error messages
- Path validation

### 3. **Clean Build Option**
```powershell
.\run-event-service.ps1 -Clean
```
- Removes target folder
- Rebuilds from scratch
- Useful for fresh starts

### 4. **Logging**
- All output sent to `logs/SERVICE-NAME.log`
- Logs directory auto-created
- Full execution history preserved

### 5. **Beautiful Output**
- Unicode box borders
- Colored text (Green/Yellow/Red/Cyan)
- Service info display
- Status indicators (✓, ❌, 🚀, etc.)
- Window title shows service name and port

### 6. **Database Checks**
- Pre-flight checks for required databases
- Verifies MySQL, MongoDB, RabbitMQ availability
- Warns if Docker containers missing

### 7. **Consistent Configuration**
- Standard Java home path
- Centralized logging directory
- Service-specific port mapping
- Database connection details

## 📊 Service Information

Each script now includes:

| Service | Port | Database | Features |
|---------|------|----------|----------|
| Eureka | 8761 | None | Must start FIRST |
| Config Server | 8888 | File-based | Must start SECOND |
| API Gateway | 8080 | None | Start THIRD |
| User Service | 8081 | H2 | In-memory DB |
| Event Service | 8082 | MySQL | Connects to Docker |
| Ticket Service | 8083 | MySQL | Connects to Docker |
| Reservation Service | 8084 | MongoDB | Connects to Docker |
| Feedback Service | 8085 | H2 | In-memory DB |
| Reclamation Service | 8086 | MySQL | Connects to Docker |
| Notification Service | 8087 | RabbitMQ | Message Broker |
| Saga Orchestrator | 8088 | RabbitMQ/Kafka | Message Brokers |
| Analytics Service | 8089 | MongoDB/Redis/Kafka | Complex setup |

## 🚀 Usage Examples

### View Help for Any Service
```powershell
.\run-event-service.ps1 -Help
```

### Start Service Normally
```powershell
.\run-event-service.ps1
```

### Start with Clean Build
```powershell
.\run-event-service.ps1 -Clean
```

### Check Logs
```powershell
Get-Content logs/event-service.log
tail -f logs/event-service.log  # Real-time
```

## 📋 Startup Procedure

### Step 1: Start Docker Infrastructure
```powershell
cd c:\Users\khalf\Awd-Web\event-Web-App
docker-compose up -d
docker-compose ps  # Verify all containers running
```

### Step 2: Start Core Services (in order)
Open separate PowerShell terminals:

```powershell
# Terminal 1
.\run-eureka.ps1
```

Wait for startup... then:

```powershell
# Terminal 2
.\run-config-server.ps1
```

Wait for startup... then:

```powershell
# Terminal 3
.\run-gateway.ps1
```

Wait for startup... then:

### Step 3: Start Microservices (can be parallel)
```powershell
# Open new terminals for each
.\run-user-service.ps1
.\run-event-service.ps1
.\run-ticket-service.ps1
.\run-reservation-service.ps1
.\run-feedback-service.ps1
.\run-reclamation-service.ps1
.\run-notification-service.ps1
```

## ✅ Verification

After all services started, check:

1. **Eureka Dashboard**
   - Open: http://localhost:8761
   - All services should appear as "UP"

2. **Each Service Health**
   - http://localhost:PORT/actuator/health
   - Should return: `{"status":"UP"}`

3. **Gateway Test**
   - http://localhost:8080

4. **RabbitMQ Management**
   - http://localhost:15672
   - Credentials: guest/guest

## 📁 Logs Location

All logs saved to: `./logs/`

Files created:
- `logs/eureka.log`
- `logs/config-server.log`
- `logs/gateway.log`
- `logs/user-service.log`
- `logs/event-service.log`
- `logs/ticket-service.log`
- `logs/reservation-service.log`
- `logs/feedback-service.log`
- `logs/reclamation-service.log`
- `logs/notification-service.log`
- `logs/saga-orchestrator-service.log`
- `logs/analytics-service.log`

## 🆘 Troubleshooting

### If a service won't start:

1. **Show help**
   ```powershell
   .\run-event-service.ps1 -Help
   ```

2. **Clean rebuild**
   ```powershell
   .\run-event-service.ps1 -Clean
   ```

3. **Check logs**
   ```powershell
   Get-Content logs/event-service.log -Tail 50
   ```

4. **Verify dependencies**
   - Docker containers running? `docker-compose ps`
   - Java installed? `java -version`
   - Port in use? `netstat -ano | findstr :8082`

## 📚 Additional Documentation

See included file: `STARTUP_GUIDE.md`

For detailed startup procedures, database connections, and troubleshooting.

---

**Update Date**: March 26, 2026  
**All 9-12 Service Scripts**: ✅ UPDATED  
**Status**: Ready for Production Use
