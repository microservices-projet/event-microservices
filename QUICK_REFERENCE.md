# 🚀 QUICK REFERENCE - Microservices Startup

## ⚡ 30-Second Quick Start

```powershell
# 1. Start Docker containers
docker-compose up -d

# 2. Terminal 1: Eureka
.\run-eureka.ps1

# 3. Terminal 2: Config (wait ~5 seconds)
.\run-config-server.ps1

# 4. Terminal 3: Gateway (wait ~5 seconds)
.\run-gateway.ps1

# 5. Terminals 4+: Launch all microservices in parallel
.\run-user-service.ps1
.\run-event-service.ps1
.\run-ticket-service.ps1
# ... others
```

---

## 📍 Service Port Map

| Service | Port | URL | Database |
|---------|------|-----|----------|
| **Eureka** | 8761 | http://localhost:8761 | None |
| **Config** | 8888 | http://localhost:8888 | File |
| **Gateway** | 8080 | http://localhost:8080 | None |
| **User** | 8081 | http://localhost:8081/api/users | H2 |
| **Event** | 8082 | http://localhost:8082/api/events | MySQL |
| **Ticket** | 8083 | http://localhost:8083/api/tickets | MySQL |
| **Reservation** | 8084 | http://localhost:8084/api/reservations | MongoDB |
| **Feedback** | 8085 | http://localhost:8085/api/feedback | H2 |
| **Reclamation** | 8086 | http://localhost:8086/api/reclamations | MySQL |
| **Notification** | 8087 | http://localhost:8087 | RabbitMQ |
| **Saga** | 8088 | http://localhost:8088 | RabbitMQ/Kafka |
| **Analytics** | 8089 | http://localhost:8089 | MongoDB/Redis/Kafka |

---

## ✨ Script Usage

```powershell
# Get help for any service
.\run-event-service.ps1 -Help

# Run normally
.\run-event-service.ps1

# Clean rebuild
.\run-event-service.ps1 -Clean
```

---

## 🔗 Infrastructure Ports

| Service | Port | Access |
|---------|------|--------|
| MySQL | 3306 | localhost:3306 |
| MongoDB | 27017 | localhost:27017 |
| PostgreSQL (Keycloak DB) | 5433 | localhost:5433 |
| RabbitMQ | 5672 | localhost:5672 |
| RabbitMQ Management | 15672 | http://localhost:15672 |
| Kafka | 9092 | localhost:9092 |
| Zookeeper | 2181 | localhost:2181 |
| Redis | 6379 | localhost:6379 |
| Keycloak | 8180 | http://localhost:8180 |

---

## ✅ Startup Verification

After all services running:

```powershell
# 1. Check Docker
docker-compose ps

# 2. Eureka Dashboard (all services UP?)
http://localhost:8761

# 3. Individual service health
http://localhost:8081/actuator/health  # User
http://localhost:8082/actuator/health  # Event
http://localhost:8083/actuator/health  # Ticket
# etc...

# 4. RabbitMQ status
http://localhost:15672
# Login: guest / guest
```

---

## 📝 Logs

```powershell
# View logs for any service
Get-Content logs/event-service.log

# Real-time log tail (PowerShell 7+)
Get-Content logs/event-service.log -Tail 50 -Wait

# Or use external tool
tail -f logs/event-service.log
```

---

## ⚠️ Common Issues

| Problem | Solution |
|---------|----------|
| Port already in use | `netstat -ano \| findstr :8082` then kill process |
| MySQL not responding | `docker-compose ps` - ensure container running |
| Build fails | Try `.\run-event-service.ps1 -Clean` |
| Java not found | Verify Java 17 installed at `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot` |
| Services won't register | Ensure Eureka started FIRST |
| Config not loading | Ensure Config Server started BEFORE other services |

---

## 🔄 Service Dependencies

```
Must Start First          Then Start              Then All Can Run
Phase 1: Infrastructure
├─ Eureka (8761) ────────→ Config (8888) ────────→ Gateway (8080)
                                                        ↓
                                            All Microservices (parallel)
```

---

## 🎯 Useful Commands

```powershell
# Check all Docker containers
docker-compose ps

# Stop Docker containers
docker-compose down

# Restart Docker containers
docker-compose down && docker-compose up -d

# Kill a port (e.g., port 8082)
Get-Process -Id (Get-NetTCPConnection -LocalPort 8082).OwningProcess | Stop-Process

# Clear logs
Remove-Item logs\* -Force

# Check if service is running on port
Test-NetConnection localhost -Port 8082
```

---

## 📚 Documentation Files

- **`STARTUP_GUIDE.md`** - Detailed startup procedures
- **`PS1_SCRIPTS_UPDATE.md`** - Full update summary
- **`BACKEND-FRONTEND-ANALYSIS.md`** - Architecture issues & solutions

---

## 🌐 Frontend Setup

After all backend services running:

```powershell
cd frontend
npm install
npm start
# Access at http://localhost:4200
```

---

## 💡 Pro Tips

1. **Use multiple terminals** - Open PowerShell for each service to see logs in real-time
2. **Check logs first** - If service fails, check `logs/` for error details
3. **Docker first** - Always ensure `docker-compose ps` shows all containers UP
4. **Startup order matters** - Always start Eureka → Config → Gateway → Others
5. **Wait between starts** - Give each service 3-5 seconds to fully initialize

---

**Status**: ✅ All 12 Services Ready  
**Last Updated**: March 26, 2026
