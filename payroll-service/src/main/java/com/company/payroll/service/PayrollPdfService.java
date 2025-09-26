package com.company.payroll.service;

import com.company.payroll.entity.Salary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PayrollPdfService {

    /**
     * Generate PDF for individual payslip
     */
    public byte[] generatePayslipPdf(Salary salary) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add header
            addPayslipHeader(document, salary);

            // Add employee info
            addEmployeeInfo(document, salary);

            // Add salary breakdown
            addSalaryBreakdown(document, salary);

            // Add footer
            addPayslipFooter(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating payslip PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Generate PDF for bulk payroll report (HR use)
     */
    public byte[] generateBulkPayrollReport(List<Salary> salaries, String title) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add report header
            addReportHeader(document, title, salaries.size());

            // Add summary table
            addPayrollSummaryTable(document, salaries);

            // Add footer
            addReportFooter(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating bulk payroll report: " + e.getMessage(), e);
        }
    }

    private void addPayslipHeader(Document document, Salary salary) {
        // Company header
        Paragraph companyName = new Paragraph("Employee Payroll System")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);

        Paragraph payslipTitle = new Paragraph("PAYSLIP")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);

        document.add(companyName);
        document.add(payslipTitle);
    }

    private void addEmployeeInfo(Document document, Salary salary) {
        // Employee info table
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        infoTable.addCell(createInfoCell("Employee ID:", salary.getEmployeeId() != null ? salary.getEmployeeId().toString() : "N/A"));
        infoTable.addCell(createInfoCell("Employee Code:", salary.getEmployeeCode() != null ? salary.getEmployeeCode() : "N/A"));
        infoTable.addCell(createInfoCell("Employee Name:", salary.getEmployeeName() != null ? salary.getEmployeeName() : "N/A"));
        infoTable.addCell(createInfoCell("Pay Period:", formatPayPeriod(salary.getMonth(), salary.getYear())));

        document.add(infoTable);
    }

    private void addSalaryBreakdown(Document document, Salary salary) {
        // Salary breakdown table
        Table salaryTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Header
        salaryTable.addHeaderCell(createHeaderCell("Description"));
        salaryTable.addHeaderCell(createHeaderCell("Amount (₹)"));

        // Earnings
        salaryTable.addCell(createSalaryCell("Basic Salary"));
        salaryTable.addCell(createAmountCell(salary.getBasicSalary()));

        if (salary.getAllowances() != null && salary.getAllowances() > 0) {
            salaryTable.addCell(createSalaryCell("Allowances"));
            salaryTable.addCell(createAmountCell(salary.getAllowances()));
        }

        if (salary.getOvertimePay() != null && salary.getOvertimePay() > 0) {
            salaryTable.addCell(createSalaryCell("Overtime Pay"));
            salaryTable.addCell(createAmountCell(salary.getOvertimePay()));
        }

        // Gross salary
        salaryTable.addCell(createSalaryCell("Gross Salary").setBold());
        salaryTable.addCell(createAmountCell(salary.getGrossSalary()).setBold());

        // Deductions
        if (salary.getTaxDeduction() != null && salary.getTaxDeduction() > 0) {
            salaryTable.addCell(createSalaryCell("Tax Deduction"));
            salaryTable.addCell(createAmountCell(-salary.getTaxDeduction()));
        }

        if (salary.getPfDeduction() != null && salary.getPfDeduction() > 0) {
            salaryTable.addCell(createSalaryCell("PF Deduction"));
            salaryTable.addCell(createAmountCell(-salary.getPfDeduction()));
        }

        if (salary.getEsiDeduction() != null && salary.getEsiDeduction() > 0) {
            salaryTable.addCell(createSalaryCell("ESI Deduction"));
            salaryTable.addCell(createAmountCell(-salary.getEsiDeduction()));
        }

        if (salary.getLatePenalty() != null && salary.getLatePenalty() > 0) {
            salaryTable.addCell(createSalaryCell("Late Penalty"));
            salaryTable.addCell(createAmountCell(-salary.getLatePenalty()));
        }

        // Net salary
        salaryTable.addCell(createSalaryCell("NET SALARY").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));
        salaryTable.addCell(createAmountCell(salary.getNetSalary()).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY));

        document.add(salaryTable);
    }

    private void addPayrollSummaryTable(Document document, List<Salary> salaries) {
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Headers
        summaryTable.addHeaderCell(createHeaderCell("Emp ID"));
        summaryTable.addHeaderCell(createHeaderCell("Employee Name"));
        summaryTable.addHeaderCell(createHeaderCell("Basic Salary"));
        summaryTable.addHeaderCell(createHeaderCell("Gross Salary"));
        summaryTable.addHeaderCell(createHeaderCell("Deductions"));
        summaryTable.addHeaderCell(createHeaderCell("Net Salary"));

        // Data rows
        double totalBasic = 0, totalGross = 0, totalDeductions = 0, totalNet = 0;

        for (Salary salary : salaries) {
            summaryTable.addCell(createDataCell(salary.getEmployeeId() != null ? salary.getEmployeeId().toString() : "N/A"));
            summaryTable.addCell(createDataCell(salary.getEmployeeName() != null ? salary.getEmployeeName() : "N/A"));
            summaryTable.addCell(createDataCell(formatCurrency(salary.getBasicSalary())));
            summaryTable.addCell(createDataCell(formatCurrency(salary.getGrossSalary())));
            summaryTable.addCell(createDataCell(formatCurrency(salary.getTotalDeductions())));
            summaryTable.addCell(createDataCell(formatCurrency(salary.getNetSalary())));

            // Add to totals
            totalBasic += (salary.getBasicSalary() != null ? salary.getBasicSalary() : 0);
            totalGross += (salary.getGrossSalary() != null ? salary.getGrossSalary() : 0);
            totalDeductions += (salary.getTotalDeductions() != null ? salary.getTotalDeductions() : 0);
            totalNet += (salary.getNetSalary() != null ? salary.getNetSalary() : 0);
        }

        // Add totals row
        summaryTable.addCell(createDataCell("TOTAL").setBold());
        summaryTable.addCell(createDataCell("").setBold());
        summaryTable.addCell(createDataCell(formatCurrency(totalBasic)).setBold());
        summaryTable.addCell(createDataCell(formatCurrency(totalGross)).setBold());
        summaryTable.addCell(createDataCell(formatCurrency(totalDeductions)).setBold());
        summaryTable.addCell(createDataCell(formatCurrency(totalNet)).setBold());

        document.add(summaryTable);
    }

    private void addPayslipFooter(Document document) {
        Paragraph footer = new Paragraph("\nThis is a system-generated payslip.")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30);

        Paragraph generated = new Paragraph("Generated on: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);

        document.add(footer);
        document.add(generated);
    }

    private void addReportHeader(Document document, String title, int recordCount) {
        Paragraph reportTitle = new Paragraph(title)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);

        Paragraph recordInfo = new Paragraph("Total Records: " + recordCount)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);

        document.add(reportTitle);
        document.add(recordInfo);
    }

    private void addReportFooter(Document document) {
        Paragraph footer = new Paragraph("\nGenerated by Employee Payroll System")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30);

        Paragraph generated = new Paragraph("Generated on: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);

        document.add(footer);
        document.add(generated);
    }

    // Helper methods for creating table cells
    private Cell createInfoCell(String label, String value) {
        return new Cell().add(new Paragraph(label + " " + value)).setPadding(5);
    }

    private Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text))
                .setBold()
                .setBackgroundColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    private Cell createSalaryCell(String text) {
        return new Cell().add(new Paragraph(text)).setPadding(5);
    }

    private Cell createAmountCell(Double amount) {
        String formattedAmount = formatCurrency(amount);
        return new Cell().add(new Paragraph(formattedAmount))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(5);
    }

    private Cell createDataCell(String text) {
        return new Cell().add(new Paragraph(text != null ? text : "N/A"))
                .setPadding(4)
                .setFontSize(10);
    }

    private String formatCurrency(Double amount) {
        if (amount == null) {
            return "₹0.00";
        }
        return String.format("₹%.2f", amount);
    }

    private String formatPayPeriod(Integer month, Integer year) {
        if (month == null || year == null) {
            return "N/A";
        }
        String[] months = {"", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
        return months[month] + " " + year;
    }
}
