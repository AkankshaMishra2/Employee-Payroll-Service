# 💼 Employee Payroll System

A comprehensive **Spr## 🛠️ Technology Stackng Boot microservices** solution for employee management and automated payroll processing with modern UI and advanced features.

## ✨ Key Features

### 👥 Employee Management
- ✅ Complete CRUD operations
- ✅ Professional employee profiles
- ✅ Role-based access (ADMIN only)
- ✅ MySQL database integration

### 💰 Payroll System  
- ✅ **Automated monthly payroll** (1st of each month at 2:00 AM)
- ✅ Attendance-based salary calculations
- ✅ Manual payroll triggers for testing
- ✅ HR dashboard with real-time statistics

### 📄 PDF Export System
- ✅ Individual employee payslips
- ✅ Bulk payroll reports  
- ✅ Monthly/yearly export options
- ✅ Professional PDF formatting

### 🔐 Authentication & Security
- ✅ Role-based access control
- ✅ Remember Me (2 hours)
- ✅ Extended sessions (8 hours)
- ✅ Secure cookie management

## � Quick Start

```bash
# Clone & Navigate
git clone <repository-url>
cd Employee-Payroll-Service

# Start Employee Service (Port 8081)
cd employee-service
./mvnw spring-boot:run

# Start Payroll Service (Port 8082) 
cd payroll-service
./mvnw spring-boot:run
```

## 🎯 System Overview

| Service | Port | Purpose | Access |
|---------|------|---------|---------|
| **Employee Service** | 8081 | Employee Management | `admin/admin123` |
| **Payroll Service** | 8082 | Payroll & Automation | `hr/hr123` |

## �🛠️ Technology Stack

```
Backend:    Spring Boot 3.5.5, Spring Security, JPA/Hibernate
Frontend:   Thymeleaf, Bootstrap 5.3.2, FontAwesome
Database:   MySQL 8.0
Tools:      Maven, iText PDF, Cron Jobs
```

## 📋 Prerequisites

- **Java 17+**
- **Maven 3.6+** 
- **MySQL 8.0+**
- **Git**

## ⚙️ Database Setup

```sql
-- Create Database
CREATE DATABASE Employee_Payroll_System;

-- Configure Connection (application.properties)
spring.datasource.url=jdbc:mysql://localhost:3306/Employee_Payroll_System
spring.datasource.username=root
spring.datasource.password=YourPassword
```


## 🏗️ Project Structure

```
Employee-Payroll-Service/
├── employee-service/          # Employee Management (Port 8081)
│   ├── src/main/java/com/company/employee/
│   │   ├── controller/        # REST & Web Controllers
│   │   ├── entity/           # JPA Entities
│   │   ├── repository/       # Data Access Layer
│   │   ├── service/          # Business Logic
│   │   └── config/           # Security Config
│   └── src/main/resources/
│       ├── templates/        # Thymeleaf Templates
│       ├── static/css/       # Custom Styling
│       └── application.properties
│
├── payroll-service/          # Payroll System (Port 8082)
│   ├── src/main/java/com/company/payroll/
│   │   ├── controller/       # Payroll Controllers
│   │   ├── entity/          # Salary & Attendance Entities
│   │   ├── service/         # Payroll Logic & Scheduling
│   │   ├── scheduler/       # Cron Job Automation
│   │   └── config/          # Security & PDF Config
│   └── src/main/resources/
│       ├── templates/       # HR Dashboard Templates
│       └── application.properties
│
└── README.md                # This File
```

## 🚦 API Endpoints

### Employee Service (8081)
```
GET  /                     # Dashboard
GET  /employees           # Employee List  
GET  /employees/{id}      # Employee Details
POST /employees           # Create Employee
PUT  /employees/{id}      # Update Employee
```

### Payroll Service (8082) 
```
GET  /admin/dashboard     # HR Dashboard
POST /admin/trigger-payroll # Manual Payroll
GET  /admin/export-pdf/bulk # Bulk PDF Export
GET  /admin/export-pdf/payslip/{id} # Individual Payslip
```

## 🔍 Troubleshooting

| Issue | Solution |
|-------|----------|
| **Port 8081/8082 in use** | `taskkill /f /im java.exe` then restart |
| **Database connection error** | Check MySQL service & credentials |
| **Login keeps asking credentials** | Clear browser cookies, ensure Remember Me checked |
| **PDF export not working** | Check payroll service status, ensure iText dependency loaded |



**Built with ❤️ using Spring Boot & Modern Web Technologies**