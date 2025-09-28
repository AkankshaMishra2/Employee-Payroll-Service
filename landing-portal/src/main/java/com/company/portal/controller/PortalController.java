package com.company.portal.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PortalController {

    @Value("${app.employee-service.url}")
    private String employeeServiceUrl;

    @Value("${app.payroll-service.url}")
    private String payrollServiceUrl;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("employeeServiceUrl", employeeServiceUrl);
        model.addAttribute("payrollServiceUrl", payrollServiceUrl);
        return "index";
    }

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "Corporate Portal is running on port 8080";
    }
}