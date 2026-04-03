# ============================================================================
# Notification Service - Port 8087
# Message Broker: RabbitMQ (requires Docker RabbitMQ running)
# ============================================================================

param(
    [switch]$Clean,
    [switch]$Help
)

if ($Help) {
    Write-Host "
Notification Service - Port 8087
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Handles email, SMS, and push notifications

Usage: .\run-notification-service.ps1 [OPTIONS]

Prerequisites:
  ✓ Docker RabbitMQ container running (port 5672)
  ✓ Eureka (Discovery Server) on port 8761
  ✓ Config Server on port 9888

Message Broker: RabbitMQ
  🗄️  Host: localhost:5672
  Management: http://localhost:15672 (guest/guest)

Options:
  -Clean    : Clean build before running (removes target folder)
  -Help     : Show this help message

Access:
  📍 API: http://localhost:8087
  
" -ForegroundColor Cyan
    exit 0
}

# Setup
$ServiceName = "Notification Service"
$ServicePort = 8087
$LogFile = "$PSScriptRoot\logs\notification-service.log"
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
║       Notification Service - Startup                         ║
╚══════════════════════════════════════════════════════════════╝
" -ForegroundColor Green

Write-Host "📌 Service: $ServiceName" -ForegroundColor Cyan
Write-Host "🔌 Port: $ServicePort" -ForegroundColor Cyan
Write-Host "📂 Location: $PSScriptRoot" -ForegroundColor Cyan
Write-Host "☕ Java Home: $JavaHome" -ForegroundColor Cyan
Write-Host "📨 Message Broker: RabbitMQ (localhost:5672)" -ForegroundColor Cyan
Write-Host ""

try {
    Set-Location $PSScriptRoot

    # Check RabbitMQ availability
    Write-Host "⏳ Checking RabbitMQ availability..." -ForegroundColor Yellow
    $RabbitCheck = $null
    try {
        $RabbitCheck = Test-NetConnection -ComputerName localhost -Port 5672 -WarningAction SilentlyContinue -InformationLevel Quiet
    }
    catch { }

    if (-not $RabbitCheck) {
        Write-Host "⚠️  RabbitMQ not accessible - ensure Docker container is running:" -ForegroundColor Yellow
        Write-Host "   docker-compose up -d" -ForegroundColor Gray
    }

    # Clean build if requested
    if ($Clean) {
        Write-Host "🧹 Cleaning project..." -ForegroundColor Yellow
        mvn clean -pl microservices/notification-service -q
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed!" -ForegroundColor Red
            exit 1
        }
    }

    Write-Host "🚀 Starting $ServiceName..." -ForegroundColor Yellow
    Write-Host "📝 Logs: $LogFile" -ForegroundColor Gray
    Write-Host ""

    # Run service
    mvn -DskipTests -pl microservices/notification-service spring-boot:run 2>&1 | Tee-Object -FilePath $LogFile

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Service failed to start!" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
}
