# ============================================================================
# Payment Service - Port 8091
# ============================================================================

param(
    [switch]$Clean,
    [switch]$Help
)

if ($Help) {
    Write-Host "Payment Service - Port 8091"
    Write-Host "========================================"
    Write-Host "Usage: .\run-payment-service.ps1 [OPTIONS]"
    Write-Host "Options:"
    Write-Host "  -Clean    : Clean build before running"
    Write-Host "  -Help     : Show this help message"
    exit 0
}

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if ($Clean) {
    mvn clean -pl microservices/payment-service -q
    if ($LASTEXITCODE -ne 0) { exit 1 }
}

mvn -DskipTests -pl microservices/payment-service spring-boot:run
