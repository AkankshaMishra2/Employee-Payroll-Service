# 🏢 Employee Payroll Management System

## 📋 Overview & Prerequisites

A comprehensive **Spring Boot microservices** enterprise solution for employee management and automated payroll processing. This system features a corporate landing portal, JWT-based authentication, role-based access control, and advanced PDF export capabilities. Built with modern web technologies and designed for scalable enterprise environments.

### Prerequisites
- **Java 17+** (JDK 17 or higher)
- **Maven 3.6+** for dependency management
- **MySQL 8.0+** database server
- **Git** for version control
- **Modern Web Browser** (Chrome, Firefox, Safari, Edge)

---

## ✨ Key Features

### 🌐 Corporate Landing Portal
- ✅ Professional department-based navigation
- ✅ Role-specific service routing (HR vs Employee Management)
- ✅ Real-time system status monitoring
- ✅ Responsive corporate design with modern UI/UX

### 👥 Employee Management Service
- ✅ Complete CRUD operations for employee records
- ✅ Professional employee profiles with detailed information
- ✅ Department management and organizational structure
- ✅ Admin dashboard with comprehensive analytics

### 💰 Payroll Management Service
- ✅ **Automated monthly payroll processing** (1st of each month at 2:00 AM)
- ✅ Attendance-based salary calculations with overtime support
- ✅ Manual payroll triggers for testing and corrections
- ✅ HR dashboard with real-time payroll statistics

### 🔐 JWT Authentication & Security
- ✅ **Stateless JWT-based authentication** (no server sessions)
- ✅ Role-based access control (ADMIN, HR roles)
- ✅ **Remember Me functionality** (7 days extended sessions)
- ✅ HttpOnly secure cookie implementation
- ✅ Cross-service authentication with shared JWT validation

### 📄 Advanced PDF Export System
- ✅ Individual employee payslip generation
- ✅ Bulk payroll reports for entire organization
- ✅ Monthly/yearly export options with filtering
- ✅ Professional PDF formatting with company branding

### 🔗 OAuth Integration (Ready)
- ✅ OAuth 2.0 framework integration prepared
- ✅ Support for Google/GitHub/Microsoft login (configurable)
- ✅ JWT token exchange for OAuth providers

---

## 🛠️ Technology Stack & Project Structure

### Backend Technologies
```
Framework:     Spring Boot 3.5.5
Security:      Spring Security 6.2 + JWT Authentication
Data Access:   Spring Data JPA + Hibernate ORM
Database:      MySQL 8.0 with HikariCP connection pooling
Scheduling:    Spring Cron Jobs for automated payroll
PDF Export:    iText PDF Library for report generation
```

### Frontend Technologies
```
Template Engine:  Thymeleaf 3.1
CSS Framework:    Bootstrap 5.3.2
Icons:            FontAwesome 6.0
UI Components:    Custom responsive components
JavaScript:       Vanilla JS with modern ES6+
```

### Development Tools
```
Build Tool:       Maven 3.9
Version Control:  Git
IDE Support:      VS Code, IntelliJ IDEA, Eclipse
DevOps:           Spring Boot DevTools for hot reload
```

### 🏗️ Project Structure
```
Employee-Payroll-Service/
├── 🌐 landing-portal/           # Corporate Gateway (Port 8080)
│   ├── src/main/java/com/company/landingportal/
│   │   └── controller/          # Navigation Controllers
│   └── src/main/resources/
│       ├── templates/           # Corporate Landing Page
│       └── static/             # Assets & Styling
│
├── 👥 employee-service/         # Employee Management (Port 8081)
│   ├── src/main/java/com/company/employee/
│   │   ├── controller/         # Employee CRUD Controllers
│   │   ├── entity/            # Employee JPA Entities
│   │   ├── repository/        # Data Access Layer
│   │   ├── service/           # Business Logic
│   │   ├── security/          # JWT Utils & Filters
│   │   └── config/            # Security Configuration
│   └── src/main/resources/
│       ├── templates/         # Employee Management UI
│       ├── static/css/        # Custom Styling
│       └── application.properties
│
├── 💰 payroll-service/          # Payroll System (Port 8082)
│   ├── src/main/java/com/company/payroll/
│   │   ├── controller/        # Payroll & Export Controllers
│   │   ├── entity/           # Salary & Attendance Entities
│   │   ├── service/          # Payroll Logic & PDF Export
│   │   ├── scheduler/        # Automated Cron Jobs
│   │   ├── security/         # JWT Authentication
│   │   └── config/           # Security & Database Config
│   └── src/main/resources/
│       ├── templates/        # HR Dashboard & Reports
│       └── application.properties
│
├── 🚀 start-all.ps1            # Windows PowerShell Startup
├── 🚀 start-all.sh             # Mac/Linux Shell Startup
└── 📚 README.md                # Documentation
```

---

## 🔑 System Credentials & Service Configuration

### 🌐 Service Ports & Access Points
| Service | Port | URL | Purpose |
|---------|------|-----|---------|
| **Corporate Portal** | 8080 | `http://localhost:8080` | Landing page & navigation |
| **Employee Service** | 8081 | `http://localhost:8081` | Employee management |
| **Payroll Service** | 8082 | `http://localhost:8082` | Payroll processing |

### 👤 System Credentials
| Service | Username | Password | Role | Access Level |
|---------|----------|----------|------|--------------|
| **Employee Service** | `admin` | `admin123` | ADMIN | Full employee management |
| **Payroll Service** | `hr` | `hr123` | HR | Payroll & reporting |

### 🔗 API Endpoints

#### Corporate Portal (8080)
```
GET  /                        # Corporate landing page
```

#### Employee Service (8081)
```
GET  /                        # Employee dashboard
GET  /login                   # Authentication endpoint
GET  /employees               # Employee directory
GET  /employees/add           # Add new employee
GET  /employees/{id}          # Employee details
POST /employees               # Create employee
PUT  /employees/{id}          # Update employee
DELETE /employees/{id}        # Remove employee
```

#### Payroll Service (8082)
```
GET  /                        # HR dashboard
GET  /login                   # HR authentication
GET  /admin/dashboard         # Payroll analytics
POST /admin/trigger-payroll   # Manual payroll processing
GET  /admin/export-pdf/bulk   # Bulk PDF export
GET  /admin/export-pdf/payslip/{id} # Individual payslip
GET  /api/payrolls           # Payroll data API (JWT required)
```

---

## 🚀 How to Run the Project

### Option 1: Quick Start (Recommended)

#### For Windows (PowerShell):
```powershell
# Navigate to project directory
cd Employee-Payroll-Service

# Run the automated startup script
.\start-all.ps1
```

#### For Mac/Linux (Terminal):
```bash
# Navigate to project directory
cd Employee-Payroll-Service

# Make script executable and run
chmod +x start-all.sh
./start-all.sh
```

### Option 2: Manual Service Startup

#### Database Setup:
```sql
CREATE DATABASE Employee_Payroll_System;
```

#### Start Services Individually:
```bash
# Terminal 1: Corporate Portal
cd landing-portal
./mvnw spring-boot:run

# Terminal 2: Employee Service  
cd employee-service
./mvnw spring-boot:run

# Terminal 3: Payroll Service
cd payroll-service
./mvnw spring-boot:run
```

### Option 3: IDE Development
```
1. Import as Maven projects in your IDE
2. Configure MySQL connection in application.properties
3. Run each *Application.java file separately
4. Access via http://localhost:8080 for landing portal
```

### 🎯 Access Flow
1. **Start**: http://localhost:8080 (Corporate Portal)
2. **Choose**: Employee Management or Payroll Department
3. **Login**: Use respective credentials based on your role
4. **Navigate**: Access features based on your permissions

---

## 🔍 Troubleshooting

| Issue | Solution |
|-------|----------|
| **Port already in use (8080/8081/8082)** | `taskkill /f /im java.exe` (Windows) or `pkill java` (Mac/Linux) |
| **Database connection refused** | Start MySQL service & verify credentials in `application.properties` |
| **Login redirects to error page** | Clear browser cache, ensure Remember Me is checked for extended session |
| **JWT token expired quickly** | Check Remember Me checkbox for 7-day sessions vs 15-minute default |
| **PDF export fails** | Verify payroll service status, check iText dependency in logs |
| **Services won't start via script** | Ensure Maven wrapper permissions: `chmod +x mvnw` (Mac/Linux) |
| **Cross-service authentication fails** | Verify JWT secret matches in both services' `application.properties` |
| **Landing portal shows 404** | Ensure landing-portal service started successfully on port 8080 |
| **Tab completion not working in PowerShell** | Run `Import-Module PSReadLine -Force` |

### 🆘 Emergency Reset
```bash
# Stop all Java processes
taskkill /f /im java.exe        # Windows
# OR
pkill java                      # Mac/Linux

# Clear application caches
mvn clean install -DskipTests  # Rebuild all services

# Restart from corporate portal
./start-all.ps1                # Windows  
./start-all.sh                 # Mac/Linux
```

---


**. . .**

**Built with ❤️ using Spring Boot ecosystem,**

**Developed by future engineers!**

**. . .**