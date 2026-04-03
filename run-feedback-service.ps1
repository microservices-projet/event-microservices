# ============================================================================
# Feedback Service - Port 8085
# Database: H2 (in-memory, no external DB needed)
# ============================================================================

param(
    [switch]$Clean,
    [switch]$Help
)

if ($Help) {
    Write-Host "
Feedback Service - Port 8085
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Manages user feedback and reviews

Usage: .\run-feedback-service.ps1 [OPTIONS]

Prerequisites:
  ✓ Eureka (Discovery Server) on port 8761
  ✓ Config Server on port 9888

Database: H2 (in-memory)
  🗄️  H2 Console: http://localhost:8085/h2-console
  Connection URL: jdbc:h2:mem:feedbackdb
  Username: sa
  Password: (leave empty)

Options:
  -Clean    : Clean build before running (removes target folder)
  -Help     : Show this help message

Access:
  📍 API: http://localhost:8085/api/feedback
  
" -ForegroundColor Cyan
    exit 0
}

# Setup
$ServiceName = "Feedback Service"
$ServicePort = 8085
$LogFile = "$PSScriptRoot\logs\feedback-service.log"
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
║       Feedback Service - Startup                             ║
╚══════════════════════════════════════════════════════════════╝
" -ForegroundColor Green

Write-Host "📌 Service: $ServiceName" -ForegroundColor Cyan
Write-Host "🔌 Port: $ServicePort" -ForegroundColor Cyan
Write-Host "📂 Location: $PSScriptRoot" -ForegroundColor Cyan
Write-Host "☕ Java Home: $JavaHome" -ForegroundColor Cyan
Write-Host "🗄️  Database: H2 (in-memory)" -ForegroundColor Cyan
Write-Host ""

try {
    Set-Location $PSScriptRoot

    # Clean build if requested
    if ($Clean) {
        Write-Host "🧹 Cleaning project..." -ForegroundColor Yellow
        mvn clean -pl microservices/feedback-service -q > $null 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed!" -ForegroundColor Red
            exit 1
        }
    }

    Write-Host "🚀 Starting $ServiceName..." -ForegroundColor Yellow
    Write-Host "📝 Logs: $LogFile" -ForegroundColor Gray
    Write-Host ""

    # Run service
    mvn -DskipTests -pl microservices/feedback-service spring-boot:run 2>&1 | Tee-Object -FilePath $LogFile

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Service failed to start!" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
    exit 1
}
