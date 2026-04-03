# ============================================================================
# Docker Compose - infrastructure (MySQL, Kafka, Redis, RabbitMQ, MongoDB, Keycloak, AKHQ)
# Run from repo root. Default does NOT start Spring services (use -App for those).
# Requires: Docker Desktop (Windows), compose file: docker-compose.yml
# ============================================================================

param(
    [switch]$App,
    [switch]$Build,
    [switch]$Down,
    [switch]$Help
)

if ($Help) {
    Write-Host @"
Docker Compose - event-web-app

Usage: .\run-docker-containers.ps1 [OPTIONS]

Default:
  Starts infrastructure only (no config-server, Eureka, gateway, user/event services).
  Equivalent: docker compose up -d

Options:
  -App     Also start Spring microservices (profile: app). Use with local dev only if you want full stack in Docker.
  -Build   With -App: build images before up (docker compose up -d --build).
  -Down    Stop and remove containers for this project (docker compose down).
  -Help    Show this message.

Examples:
  .\run-docker-containers.ps1
  .\run-docker-containers.ps1 -App -Build
  .\run-docker-containers.ps1 -Down

Ports (host):
  MySQL 3306 | Kafka host localhost:9092 (IntelliJ); in Docker network kafka:29092 | Redis 6379 | RabbitMQ 5672 / UI 15672
  MongoDB 27017 | Keycloak http://localhost:8180 | AKHQ http://localhost:8095 | Analytics API http://localhost:8086
"@ -ForegroundColor Cyan
    exit 0
}

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$composeFile = Join-Path $PSScriptRoot "docker-compose.yml"
if (-not (Test-Path $composeFile)) {
    Write-Error "docker-compose.yml not found: $composeFile"
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "Docker CLI not found. Start Docker Desktop and ensure docker is on PATH."
}

Write-Host "Using: $composeFile" -ForegroundColor DarkGray

if ($Down) {
    if ($App) {
        docker compose -f $composeFile --profile app down
    }
    else {
        docker compose -f $composeFile down
    }
    exit $LASTEXITCODE
}

if ($App) {
    if ($Build) {
        docker compose -f $composeFile --profile app up -d --build
    }
    else {
        docker compose -f $composeFile --profile app up -d
    }
}
else {
    if ($Build) {
        Write-Warning "-Build applies to Spring images; use -App -Build. Running infrastructure up without build."
    }
    docker compose -f $composeFile up -d
}

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "`nStatus:" -ForegroundColor Green
docker compose -f $composeFile ps
