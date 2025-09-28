@echo off
echo.
echo ============================================
echo    Employee-Payroll System Startup
echo ============================================
echo.

echo [1/3] Starting Corporate Portal (Port 8080)...
start "Corporate-Portal" cmd /k "cd /d landing-portal && mvnw.cmd spring-boot:run"
timeout /t 3 /nobreak > nul

echo [2/3] Starting Employee Service (Port 8081)...
start "Employee-Service" cmd /k "cd /d employee-service && mvnw.cmd spring-boot:run"
timeout /t 3 /nobreak > nul

echo [3/3] Starting Payroll Service (Port 8082)...
start "Payroll-Service" cmd /k "cd /d payroll-service && mvnw.cmd spring-boot:run"

echo.
echo ============================================
echo             Services Starting...
echo ============================================
echo.
echo Corporate Portal: http://localhost:8080
echo Employee Portal:  http://localhost:8081
echo Payroll Portal:   http://localhost:8082
echo.
echo Press any key to open Corporate Portal...
pause > nul
start http://localhost:8080