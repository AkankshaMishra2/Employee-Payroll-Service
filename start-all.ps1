Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "    Employee-Payroll System Startup" -ForegroundColor Cyan  
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/3] Starting Corporate Portal (Port 8080)..." -ForegroundColor Yellow
Start-Process -FilePath 'c:\Users\dell\Desktop\Employee-Payroll-Service\Employee-Payroll-Service\landing-portal\mvnw.cmd' -ArgumentList 'spring-boot:run' -WorkingDirectory 'c:\Users\dell\Desktop\Employee-Payroll-Service\Employee-Payroll-Service\landing-portal'
Start-Sleep -Seconds 3

Write-Host "[2/3] Starting Employee Service (Port 8081)..." -ForegroundColor Yellow
Start-Process -FilePath 'c:\Users\dell\Desktop\Employee-Payroll-Service\Employee-Payroll-Service\employee-service\mvnw.cmd' -ArgumentList 'spring-boot:run' -WorkingDirectory 'c:\Users\dell\Desktop\Employee-Payroll-Service\Employee-Payroll-Service\employee-service'
Start-Sleep -Seconds 3

Write-Host "[3/3] Starting Payroll Service (Port 8082)..." -ForegroundColor Yellow
Start-Process -FilePath 'c:\Users\dell\Desktop\Employee-Payroll-Service\Employee-Payroll-Service\payroll-service\mvnw.cmd' -ArgumentList 'spring-boot:run' -WorkingDirectory 'c:\Users\dell\Desktop\Employee-Payroll-Service\Employee-Payroll-Service\payroll-service'

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "             Services Starting..." -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Corporate Portal: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Employee Portal:  http://localhost:8081" -ForegroundColor Blue  
Write-Host "Payroll Portal:   http://localhost:8082" -ForegroundColor Magenta
Write-Host ""
Write-Host "Press any key to open Corporate Portal..." -ForegroundColor White
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
Start-Process "http://localhost:8080"