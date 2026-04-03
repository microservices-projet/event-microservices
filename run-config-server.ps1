# ============================================================================
# Config Server - Port 8888
# START THIS AFTER EUREKA
# ============================================================================

param(
    [switch]$Clean,
    [switch]$Help
)

if ($Help) {
    Write-Host "
Config Server - Port 8888
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Centralized configuration server for all microservices

Usage: .\run-config-server.ps1 [OPTIONS]

Prerequisites:
  ✓ Eureka (Discovery Server) must be running on port 8761

Usage: .\run-config-server.ps1 [OPTIONS]

Options:
  -Clean    : Clean build before running (removes target folder)
  -Help     : Show this help message

Access:
  ⚙️  Config Server: http://localhost:8888
  
" -ForegroundColor Cyan
    exit 0
}

# Setup
$ServiceName = "Config Server"
$ServicePort = 8888
$LogFile = "$PSScriptRoot\logs\config-server.log"
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
$env:CONFIG_REPO_LABEL = "main"
$env:CONFIG_REPO_LOCATION = "file:$PSScriptRoot/microservices/config-repo"

# Check if config repo exists
if (-not (Test-Path (Convert-Path $env:CONFIG_REPO_LOCATION))) {
    Write-Host "❌ Error: Config repo not found at: $env:CONFIG_REPO_LOCATION" -ForegroundColor Red
    exit 1
}

# Set window title
$host.UI.RawUI.WindowTitle = "[$ServicePort] $ServiceName"

Write-Host "
╔══════════════════════════════════════════════════════════════╗
║       Config Server - Startup                                ║
╚══════════════════════════════════════════════════════════════╝
" -ForegroundColor Green

Write-Host "📌 Service: $ServiceName" -ForegroundColor Cyan
Write-Host "🔌 Port: $ServicePort" -ForegroundColor Cyan
Write-Host "📂 Location: $PSScriptRoot" -ForegroundColor Cyan
Write-Host "☕ Java Home: $JavaHome" -ForegroundColor Cyan
Write-Host "📁 Config Repo: $env:CONFIG_REPO_LOCATION" -ForegroundColor Cyan
Write-Host ""

try {
    Set-Location $PSScriptRoot

    # Clean build if requested
    if ($Clean) {
        Write-Host "🧹 Cleaning project..." -ForegroundColor Yellow
        mvn clean -pl microservices/config-server -q
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed!" -ForegroundColor Red
            exit 1
        }
    }

    Write-Host "🚀 Starting $ServiceName..." -ForegroundColor Yellow
    Write-Host "📝 Logs: $LogFile" -ForegroundColor Gray
    Write-Host ""

    # Run service
    mvn -DskipTests -pl microservices/config-server spring-boot:run 2>&1 | Tee-Object -FilePath $LogFile

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Service failed to start!" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
}
