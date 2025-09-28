#!/bin/bash

# Colors for Mac/Linux terminal
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
WHITE='\033[1;37m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

echo ""
echo -e "${CYAN}============================================${NC}"
echo -e "${CYAN}    Employee-Payroll System Startup${NC}"
echo -e "${CYAN}============================================${NC}"
echo ""

# Create logs directory if it doesn't exist
mkdir -p logs

echo -e "${YELLOW}[1/3] Starting Corporate Portal (Port 8080)...${NC}"
cd landing-portal
./mvnw spring-boot:run > ../logs/landing-portal.log 2>&1 &
cd ..
sleep 8

echo -e "${YELLOW}[2/3] Starting Employee Service (Port 8081)...${NC}"
cd employee-service
./mvnw spring-boot:run > ../logs/employee-service.log 2>&1 &
cd ..
sleep 8

echo -e "${YELLOW}[3/3] Starting Payroll Service (Port 8082)...${NC}"
cd payroll-service
./mvnw spring-boot:run > ../logs/payroll-service.log 2>&1 &
cd ..

echo ""
echo -e "${YELLOW}⏳ Waiting for Corporate Portal to initialize...${NC}"
echo -e "${GRAY}This usually takes 20-30 seconds...${NC}"

# Wait and check if Corporate Portal is ready
timeout=60
elapsed=0

while [ $elapsed -lt $timeout ]; do
    sleep 3
    elapsed=$((elapsed + 3))
    
    # Check if health endpoint is responding
    if curl -s http://localhost:8080/health > /dev/null 2>&1; then
        echo ""
        echo -e "${GREEN}✅ Corporate Portal is ready!${NC}"
        break
    fi
    
    echo -n "."
    
    if [ $elapsed -ge $timeout ]; then
        echo ""
        echo -e "${YELLOW}⚠️  Timeout waiting for Corporate Portal. Continuing anyway...${NC}"
        break
    fi
done

echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}             Services Starting!${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo -e "${CYAN}🌐 Corporate Portal: http://localhost:8080${NC}"
echo -e "${BLUE}👥 Employee Portal:  http://localhost:8081${NC}"
echo -e "${MAGENTA}💰 Payroll Portal:   http://localhost:8082${NC}"
echo ""
echo -e "${WHITE}Opening Corporate Portal...${NC}"
sleep 1
open http://localhost:8080 2>/dev/null || xdg-open http://localhost:8080 2>/dev/null || echo "Please open http://localhost:8080 manually"