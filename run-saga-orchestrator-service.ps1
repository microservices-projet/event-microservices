# ============================================================================
# Saga Orchestrator Service - Port 8088
# Message Broker: RabbitMQ/Kafka (requires Docker running)
# ============================================================================

param(
    [switch]$Clean,
    [switch]$Help
)

if ($Help) {
    Write-Host "
Saga Orchestrator Service - Port 8088
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Orchestrates distributed transactions using SAGA pattern

Usage: .\run-saga-orchestrator-service.ps1 [OPTIONS]

Prerequisites:
  ✓ Docker infrastructure running (RabbitMQ/Kafka)
  ✓ Eureka (Discovery Server) on port 8761
  ✓ Config Server on port 8888 (optional; local application.properties used if unavailable)

Requirements:
  Message Broker: RabbitMQ (localhost:5672) AND/OR Kafka (localhost:9092)

Options:
  -Clean    : Clean build before running (removes target folder)
  -Help     : Show this help message

Access:
  📍 API: http://localhost:8088
  
" -ForegroundColor Cyan
    exit 0
}

# Setup
$ServiceName = "Saga Orchestrator Service"
$ServicePort = 8088
$LogFile = "$PSScriptRoot\logs\saga-orchestrator-service.log"
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
║       Saga Orchestrator Service - Startup                    ║
╚══════════════════════════════════════════════════════════════╝
" -ForegroundColor Green

Write-Host "📌 Service: $ServiceName" -ForegroundColor Cyan
Write-Host "🔌 Port: $ServicePort" -ForegroundColor Cyan
Write-Host "📂 Location: $PSScriptRoot" -ForegroundColor Cyan
Write-Host "☕ Java Home: $JavaHome" -ForegroundColor Cyan
Write-Host "📨 Message Brokers: RabbitMQ (5672), Kafka (9092)" -ForegroundColor Cyan
Write-Host ""

try {
    Set-Location $PSScriptRoot

    # Check message brokers availability
    Write-Host "⏳ Checking message brokers..." -ForegroundColor Yellow
    $RabbitCheck = $null
    $KafkaCheck = $null
    try {
        $RabbitCheck = Test-NetConnection -ComputerName localhost -Port 5672 -WarningAction SilentlyContinue -InformationLevel Quiet
        $KafkaCheck = Test-NetConnection -ComputerName localhost -Port 9092 -WarningAction SilentlyContinue -InformationLevel Quiet
    }
    catch { }

    if (-not $RabbitCheck -or -not $KafkaCheck) {
        Write-Host "⚠️  Some message brokers not accessible - ensure Docker containers are running:" -ForegroundColor Yellow
        Write-Host "   docker-compose up -d" -ForegroundColor Gray
    }

    # Clean build if requested
    if ($Clean) {
        Write-Host "🧹 Cleaning project..." -ForegroundColor Yellow
        mvn clean -pl microservices/saga-orchestrator-service -q
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed!" -ForegroundColor Red
            exit 1
        }
    }

    Write-Host "🚀 Starting $ServiceName..." -ForegroundColor Yellow
    Write-Host "📝 Logs: $LogFile" -ForegroundColor Gray
    Write-Host ""

    # Run service
    mvn -DskipTests -pl microservices/saga-orchestrator-service spring-boot:run 2>&1 | Tee-Object -FilePath $LogFile

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Service failed to start!" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
}
