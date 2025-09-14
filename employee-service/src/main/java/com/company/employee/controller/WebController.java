package com.company.employee.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import com.company.employee.entity.Employee;
import com.company.employee.service.EmployeeService;

@Controller
public class WebController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${payroll.service.url}")
    private String payrollServiceUrl;

    // Login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Dashboard - Home page
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        model.addAttribute("totalEmployees", employees.size());

        // Count by department
        long itCount = employees.stream().filter(e -> "IT".equals(e.getDepartment())).count();
        long hrCount = employees.stream().filter(e -> "HR".equals(e.getDepartment())).count();
        long financeCount = employees.stream().filter(e -> "Finance".equals(e.getDepartment())).count();

        model.addAttribute("itCount", itCount);
        model.addAttribute("hrCount", hrCount);
        model.addAttribute("financeCount", financeCount);

        return "dashboard";
    }

    // Show all employees
    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEmployees(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        return "employees";
    }

    // Show employee form for adding new employee
    @GetMapping("/employees/new")
    public String showEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("isEdit", false);
        return "employee_form";
    }

    // Show employee form for editing
    @GetMapping("/employees/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if (employee.isPresent()) {
            model.addAttribute("employee", employee.get());
            model.addAttribute("isEdit", true);
            return "employee_form";
        }
        return "redirect:/employees";
    }

    // Save employee (create or update)
    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute Employee employee) {
        if (employee.getJoinDate() == null) {
            employee.setJoinDate(LocalDate.now());
        }
        employeeService.saveEmployee(employee);
        return "redirect:/employees";
    }

    // Delete employee
    @GetMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }

    // Show employees by department
    @GetMapping("/employees/department/{department}")
    public String showEmployeesByDepartment(@PathVariable String department, Model model) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(department);
        model.addAttribute("employees", employees);
        model.addAttribute("department", department);
        model.addAttribute("filterTitle", department + " Department Employees");
        return "employees";
    }

    // Show payroll page
    @GetMapping("/payroll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public String showPayroll(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);

        // Try to get payroll data from payroll service
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth("hr", "hr123");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    payrollServiceUrl + "/api/payroll",
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            model.addAttribute("payrollRecords", response.getBody());
        } catch (Exception e) {
            model.addAttribute("payrollRecords", List.of());
            model.addAttribute("payrollError", "Payroll service unavailable");
        }

        return "payroll";
    }

    // Show employee details with payroll
    @GetMapping("/employees/{id}/details")
    public String showEmployeeDetails(@PathVariable Long id, Model model) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeById(id);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            model.addAttribute("employee", employee);

            // Try to get payroll data for this employee
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth("hr", "hr123");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<List> response = restTemplate.exchange(
                        payrollServiceUrl + "/api/payroll/employee/" + id,
                        HttpMethod.GET,
                        entity,
                        List.class
                );

                model.addAttribute("salaryRecords", response.getBody());
            } catch (Exception e) {
                System.out.println("Error fetching payroll data: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("salaryRecords", List.of());
                model.addAttribute("payrollError", "Unable to fetch salary records: " + e.getMessage());
            }

            return "employee_details";
        }
        return "redirect:/employees";
    }

    // Generate payroll for an employee
    @GetMapping("/employees/{id}/generate-payroll")
    @PreAuthorize("hasRole('ADMIN')")
    public String generatePayroll(@PathVariable Long id) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeById(id);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();

            try {
                // Create salary object with current month
                String salaryData = String.format(
                        "{\"employeeId\":%d,\"employeeCode\":\"%s\",\"basicSalary\":%.2f,\"payPeriod\":\"%s\",\"status\":\"PENDING\"}",
                        employee.getId(),
                        employee.getEmployeeCode(),
                        employee.getBasicSalary(),
                        java.time.LocalDate.now().toString()
                );

                System.out.println("Generating payroll for: " + employee.getFirstName() + " " + employee.getLastName());
                System.out.println("Payroll data: " + salaryData);

                // Call payroll service to generate salary
                HttpHeaders headers = new HttpHeaders();
                headers.setBasicAuth("hr", "hr123");
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(salaryData, headers);

                restTemplate.exchange(
                        payrollServiceUrl + "/api/payroll",
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                System.out.println("Payroll generated successfully!");

            } catch (Exception e) {
                System.out.println("Error generating payroll: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return "redirect:/employees";
    }
}
