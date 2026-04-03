# ============================================================================
# Analytics Streams Service - Port 8086
# Databases: MongoDB (analytics-db), Redis (cache)
# Message Broker: Kafka
# ============================================================================

param(
    [switch]$Clean,
    [switch]$Help
)

if ($Help) {
    Write-Host "
Analytics Streams Service - Port 8086
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Real-time analytics and stream processing

Usage: .\run-analytics-service.ps1 [OPTIONS]

Prerequisites:
  ✓ Docker MongoDB container running (port 27017)
  ✓ Docker Redis container running (port 6379)
  ✓ Docker Kafka container running (port 9092) — for Streams; REST still starts if broker is down
  ✓ Eureka (Discovery Server) on port 8761
  ✓ Config Server on port 8888 (optional import — service starts with local fallbacks)

Databases:
  🗄️  MongoDB: localhost:27017/analytics-db
  💾 Redis: localhost:6379

Message Broker:
  📨 Kafka: localhost:9092

Options:
  -Clean    : Clean build before running (removes target folder)
  -Help     : Show this help message

Access:
  📍 API: http://localhost:8086
  
" -ForegroundColor Cyan
    exit 0
}

# Setup
$ServiceName = "Analytics Streams Service"
$ServicePort = 8086
$LogFile = "$PSScriptRoot\logs\analytics-service.log"
$ErrorActionPreference = "Stop"

# Create logs directory
if (-not (Test-Path "$PSScriptRoot\logs")) {
    mkdir "$PSScriptRoot\logs" | Out-Null
}

# Configure Java
$JavaHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
if (-not (Test-Path $JavaHome)) {
    Write-Host "❌ Error: Java 17 not found at: $JavaHome" -ForegroundColor Red
    Write-Host "📋 Please install Eclipse Adoptium JDK 17 or update the path in this script" -ForegroundColor Yellow
    exit 1
}

$env:JAVA_HOME = $JavaHome
$env:Path = "$JavaHome\bin;$env:Path"

# Set window title
$host.UI.RawUI.WindowTitle = "[$ServicePort] $ServiceName"

Write-Host "
╔══════════════════════════════════════════════════════════════╗
║       Analytics Streams Service - Startup                    ║
╚══════════════════════════════════════════════════════════════╝
" -ForegroundColor Green

Write-Host "📌 Service: $ServiceName" -ForegroundColor Cyan
Write-Host "🔌 Port: $ServicePort" -ForegroundColor Cyan
Write-Host "📂 Location: $PSScriptRoot" -ForegroundColor Cyan
Write-Host "☕ Java Home: $JavaHome" -ForegroundColor Cyan
Write-Host "🗄️  MongoDB: localhost:27017/analytics-db" -ForegroundColor Cyan
Write-Host "💾 Redis: localhost:6379" -ForegroundColor Cyan
Write-Host "📨 Kafka: localhost:9092" -ForegroundColor Cyan
Write-Host ""

try {
    Set-Location $PSScriptRoot

    # Check infrastructure availability
    Write-Host "⏳ Checking infrastructure..." -ForegroundColor Yellow
    $MongoCheck = $null
    $RedisCheck = $null
    $KafkaCheck = $null
    try {
        $MongoCheck = Test-NetConnection -ComputerName localhost -Port 27017 -WarningAction SilentlyContinue -InformationLevel Quiet
        $RedisCheck = Test-NetConnection -ComputerName localhost -Port 6379 -WarningAction SilentlyContinue -InformationLevel Quiet
        $KafkaCheck = Test-NetConnection -ComputerName localhost -Port 9092 -WarningAction SilentlyContinue -InformationLevel Quiet
    }
    catch { }

    if (-not $MongoCheck -or -not $RedisCheck -or -not $KafkaCheck) {
        Write-Host "⚠️  Some services not accessible - ensure Docker containers are running:" -ForegroundColor Yellow
        Write-Host "   docker-compose up -d" -ForegroundColor Gray
    }

    # Clean build if requested
    if ($Clean) {
        Write-Host "🧹 Cleaning project..." -ForegroundColor Yellow
        mvn clean -pl microservices/analytics-streams-service -q
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed!" -ForegroundColor Red
            exit 1
        }
    }

    Write-Host "🚀 Starting $ServiceName..." -ForegroundColor Yellow
    Write-Host "📝 Logs: $LogFile" -ForegroundColor Gray
    Write-Host ""

    # Run service
    mvn -DskipTests -pl microservices/analytics-streams-service spring-boot:run 2>&1 | Tee-Object -FilePath $LogFile

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Service failed to start!" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
}
