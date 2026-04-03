# Discovery Server (Eureka) - Port 8761
# START THIS FIRST - required for service discovery

param(
    [switch]$Clean,
    [switch]$Help
)

if ($Help) {
    Write-Host "Discovery Server (Eureka) - Port 8761"
    Write-Host "========================================"
    Write-Host "This is the SERVICE DISCOVERY server - MUST START FIRST"
    Write-Host ""
    Write-Host "Usage: .\run-eureka.ps1 [OPTIONS]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Clean    : Clean build before running"
    Write-Host "  -Help     : Show this help message"
    Write-Host ""
    Write-Host "Access: http://localhost:8761"
    exit 0
}

$ServiceName = "Discovery Server (Eureka)"
$ServicePort = 8761
$LogFile = "$PSScriptRoot\logs\eureka.log"
$ErrorActionPreference = "Stop"

# Create logs directory
if (-not (Test-Path "$PSScriptRoot\logs")) {
    New-Item -ItemType Directory -Path "$PSScriptRoot\logs" | Out-Null
}

# Configure Java
$JavaHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
if (-not (Test-Path $JavaHome)) {
    Write-Host "[ERROR] Java 17 not found at: $JavaHome" -ForegroundColor Red
    Write-Host "[INFO] Please install Eclipse Adoptium JDK 17" -ForegroundColor Yellow
    exit 1
}

$env:JAVA_HOME = $JavaHome
$env:Path = "$JavaHome\bin;$env:Path"

# Set window title
try {
    $host.UI.RawUI.WindowTitle = "[$ServicePort] $ServiceName"
} catch {}

Write-Host ""
Write-Host "====================================================================="
Write-Host "Discovery Server (Eureka) - Starting on port $ServicePort"
Write-Host "====================================================================="
Write-Host "[INFO] Service: $ServiceName"
Write-Host "[INFO] Port: $ServicePort"
Write-Host "[INFO] Java Home: $JavaHome"
Write-Host ""

try {
    Set-Location $PSScriptRoot

    # Clean build if requested
    if ($Clean) {
        Write-Host "[INFO] Cleaning project..." -ForegroundColor Yellow
        mvn clean -pl microservices/discovery-server -q
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[ERROR] Clean failed!" -ForegroundColor Red
            exit 1
        }
    }

    Write-Host "[INFO] Starting Discovery Server..." -ForegroundColor Yellow
    Write-Host "[INFO] Logs will be saved to: $LogFile" -ForegroundColor Gray
    Write-Host "[INFO] Access dashboard at: http://localhost:$ServicePort" -ForegroundColor Cyan
    Write-Host ""

    # Run service
    mvn -DskipTests -pl microservices/discovery-server spring-boot:run 2>&1 | Tee-Object -FilePath $LogFile

    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Service failed to start!" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "[ERROR] Exception: $_" -ForegroundColor Red
    exit 1
}
