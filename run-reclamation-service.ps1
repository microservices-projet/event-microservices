# ============================================================================
# Reclamation Service - Port 8086
# Database: MySQL (requires Docker MySQL running)
# ============================================================================

param(
    [switch]$Clean,
    [switch]$Help
)

if ($Help) {
    Write-Host "
Reclamation Service - Port 8086
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Manages customer complaints and reclamations

Usage: .\run-reclamation-service.ps1 [OPTIONS]

Prerequisites:
  ✓ Docker MySQL container running (port 3306)
  ✓ Eureka (Discovery Server) on port 8761
  ✓ Config Server on port 8888 (local / IntelliJ)

Database: MySQL (reclamationdb)
  🗄️  Host: localhost:3306
  Database: reclamationdb
  Username: root
  Password: (empty)

Options:
  -Clean    : Clean build before running (removes target folder)
  -Help     : Show this help message

Access:
  📍 API: http://localhost:8086/api/reclamations
  
" -ForegroundColor Cyan
    exit 0
}

# Setup
$ServiceName = "Reclamation Service"
$ServicePort = 8086
$LogFile = "$PSScriptRoot\logs\reclamation-service.log"
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
║       Reclamation Service - Startup                          ║
╚══════════════════════════════════════════════════════════════╝
" -ForegroundColor Green

Write-Host "📌 Service: $ServiceName" -ForegroundColor Cyan
Write-Host "🔌 Port: $ServicePort" -ForegroundColor Cyan
Write-Host "📂 Location: $PSScriptRoot" -ForegroundColor Cyan
Write-Host "☕ Java Home: $JavaHome" -ForegroundColor Cyan
Write-Host "🗄️  Database: MySQL (localhost:3306/reclamationdb)" -ForegroundColor Cyan
Write-Host ""

try {
    Set-Location $PSScriptRoot

    # Check MySQL availability
    Write-Host "⏳ Checking MySQL availability..." -ForegroundColor Yellow
    $MySQLCheck = $null
    try {
        $MySQLCheck = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue -InformationLevel Quiet
    }
    catch { }

    if (-not $MySQLCheck) {
        Write-Host "⚠️  MySQL not accessible - ensure Docker container is running:" -ForegroundColor Yellow
        Write-Host "   docker-compose up -d" -ForegroundColor Gray
    }

    # Clean build if requested
    if ($Clean) {
        Write-Host "🧹 Cleaning project..." -ForegroundColor Yellow
        mvn clean -pl microservices/reclamation-service -q
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed!" -ForegroundColor Red
            exit 1
        }
    }

    Write-Host "🚀 Starting $ServiceName..." -ForegroundColor Yellow
    Write-Host "📝 Logs: $LogFile" -ForegroundColor Gray
    Write-Host ""

    # Run service
    mvn -DskipTests -pl microservices/reclamation-service spring-boot:run 2>&1 | Tee-Object -FilePath $LogFile

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Service failed to start!" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
}
