Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "    Employee-Payroll System Startup" -ForegroundColor Cyan  
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Get current directory for relative paths
$currentDir = Get-Location

Write-Host "[1/3] Starting Corporate Portal (Port 8080)..." -ForegroundColor Yellow
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$currentDir\landing-portal`" && mvnw.cmd spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 8

Write-Host "[2/3] Starting Employee Service (Port 8081)..." -ForegroundColor Yellow
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$currentDir\employee-service`" && mvnw.cmd spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 8

Write-Host "[3/3] Starting Payroll Service (Port 8082)..." -ForegroundColor Yellow
Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$currentDir\payroll-service`" && mvnw.cmd spring-boot:run" -WindowStyle Normal

Write-Host ""
Write-Host "⏳ Waiting for Corporate Portal to initialize..." -ForegroundColor Yellow
Write-Host "This usually takes 20-30 seconds..." -ForegroundColor Gray

# Wait and check if Corporate Portal is ready
$timeout = 60
$elapsed = 0
do {
    Start-Sleep -Seconds 3
    $elapsed += 3
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/health" -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) { 
            Write-Host ""
            Write-Host "✅ Corporate Portal is ready!" -ForegroundColor Green
            break 
        }
    } catch { }
    Write-Host "." -NoNewline -ForegroundColor Yellow
    
    if ($elapsed -ge $timeout) {
        Write-Host ""
        Write-Host "⚠️  Timeout waiting for Corporate Portal. Continuing anyway..." -ForegroundColor Yellow
        break
    }
} while ($true)

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "             Services Starting!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Corporate Portal: http://localhost:8080" -ForegroundColor Cyan
Write-Host "👥 Employee Portal:  http://localhost:8081" -ForegroundColor Blue  
Write-Host "💰 Payroll Portal:   http://localhost:8082" -ForegroundColor Magenta
Write-Host ""
Write-Host "Opening Corporate Portal..." -ForegroundColor White
Start-Process "http://localhost:8080"