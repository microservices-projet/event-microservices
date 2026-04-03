# ============================================================================
# API Gateway (Spring Cloud Gateway) - Port 8080
# START AFTER EUREKA + CONFIG SERVER
# ============================================================================

param(
    [switch]$Clean,
    [switch]$Help
)

if ($Help) {
    Write-Host "
API Gateway - Port 8080
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Central entry point for all client requests - routes to microservices

Usage: .\run-gateway.ps1 [OPTIONS]

Prerequisites:
  ✓ Eureka (Discovery Server) must be running on port 8761
  ✓ Config Server must be running on port 9888

Options:
  -Clean    : Clean build before running (removes target folder)
  -Help     : Show this help message

Access:
  🚪 Gateway: http://localhost:8080
  📋 Available routes: Check gateway routes in properties files
  
" -ForegroundColor Cyan
    exit 0
}

# Setup
$ServiceName = "API Gateway"
$ServicePort = 8080
$LogFile = "$PSScriptRoot\logs\gateway.log"
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
║       API Gateway - Startup                                  ║
╚══════════════════════════════════════════════════════════════╝
" -ForegroundColor Green

Write-Host "📌 Service: $ServiceName" -ForegroundColor Cyan
Write-Host "🔌 Port: $ServicePort" -ForegroundColor Cyan
Write-Host "📂 Location: $PSScriptRoot" -ForegroundColor Cyan
Write-Host "☕ Java Home: $JavaHome" -ForegroundColor Cyan
Write-Host "ℹ️  Routes requests to microservices" -ForegroundColor Cyan
Write-Host ""

try {
    Set-Location $PSScriptRoot

    # Clean build if requested
    if ($Clean) {
        Write-Host "🧹 Cleaning project..." -ForegroundColor Yellow
        mvn clean -pl microservices/api-gateway -q
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed!" -ForegroundColor Red
            exit 1
        }
    }

    Write-Host "🚀 Starting $ServiceName..." -ForegroundColor Yellow
    Write-Host "📝 Logs: $LogFile" -ForegroundColor Gray
    Write-Host ""

    # Run service
    mvn -DskipTests -pl microservices/api-gateway spring-boot:run 2>&1 | Tee-Object -FilePath $LogFile

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Service failed to start!" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
}
