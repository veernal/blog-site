# Build script for all microservices

Write-Host "Building Blog Site Microservices..." -ForegroundColor Green

# Build parent and common-lib first
Write-Host "`nBuilding common-lib..." -ForegroundColor Cyan
Set-Location common-lib
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build common-lib" -ForegroundColor Red
    exit 1
}
Set-Location ..

# Build all microservices
$services = @("eureka-server", "api-gateway", "user-service", "blog-command-service", "blog-query-service", "event-store-service")

foreach ($service in $services) {
    Write-Host "`nBuilding $service..." -ForegroundColor Cyan
    Set-Location $service
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to build $service" -ForegroundColor Red
        Set-Location ..
        exit 1
    }
    Set-Location ..
}

Write-Host "`nAll services built successfully!" -ForegroundColor Green
