package com.example.hrms.service;

import com.example.hrms.dto.*;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.Payroll;
import com.example.hrms.entity.Payroll.PayrollStatus;
import com.example.hrms.repository.EmployeeRepository;
import com.example.hrms.repository.PayrollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepo;
    private final EmployeeRepository employeeRepo;

    public PayrollService(PayrollRepository payrollRepo, EmployeeRepository employeeRepo) {
        this.payrollRepo = payrollRepo;
        this.employeeRepo = employeeRepo;
    }

    // Helper: rounding to 2 decimals
    private BigDecimal round2(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private PayrollResponseDto toDto(Payroll p) {
        Employee e = p.getEmployee();
        String name = e == null ? null : e.getFirstName() + (e.getLastName() == null ? "" : " " + e.getLastName());
        return PayrollResponseDto.builder()
                .payrollId(p.getPayrollId())
                .employeeId(e == null ? null : e.getEmpId())
                .employeeName(name)
                .month(p.getMonth())
                .year(p.getYear())
                .basicSalary(p.getBasicSalary())
                .allowances(p.getAllowances())
                .deductions(p.getDeductions())
                .taxAmount(p.getTaxAmount())
                .netSalary(p.getNetSalary())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private List<String> validateForWarnings(Employee e, Payroll p) {
        List<String> warnings = new ArrayList<>();
        if (p.getBasicSalary() == null || p.getBasicSalary().compareTo(BigDecimal.ZERO) <= 0) {
            warnings.add("Basic salary is zero or missing.");
        }
        if (e != null && (e.getEmail() == null || e.getEmail().isBlank())) {
            warnings.add("Employee has no email - email notification/payslip sending will fail.");
        }
        // Add other checks: missing bank details (if present), negative deductions, tax anomalies, etc.
        if (p.getTaxAmount() != null && p.getTaxAmount().compareTo(BigDecimal.ZERO) < 0) {
            warnings.add("Tax amount is negative.");
        }
        return warnings;
    }

    // Recalculate using Payroll.recalcTotals() but also ensure rounding
    private void recalcAndRound(Payroll p) {
        p.recalcTotals();
        p.setBasicSalary(round2(p.getBasicSalary()));
        p.setAllowances(round2(p.getAllowances()));
        p.setDeductions(round2(p.getDeductions()));
        p.setTaxAmount(round2(p.getTaxAmount()));
        p.setNetSalary(round2(p.getNetSalary()));
    }

    // Create or update basic CRUD
    @Transactional
    public PayrollResponseDto createOrUpdate(PayrollRequestDto req) {
        Employee emp = employeeRepo.findById(req.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + req.getEmployeeId()));

        Payroll p;
        if (req.getPayrollId() != null) {
            p = payrollRepo.findById(req.getPayrollId())
                    .orElseThrow(() -> new IllegalArgumentException("Payroll not found"));
        } else {
            // ensure unique per employee/month/year
            var existing = payrollRepo.findByEmployeeEmpIdAndMonthAndYear(emp.getEmpId(), req.getMonth(), req.getYear());
            if (existing.isPresent()) throw new IllegalArgumentException("Payroll already exists for employee/period");
            p = new Payroll();
            p.setEmployee(emp);
            p.setMonth(req.getMonth());
            p.setYear(req.getYear());
            p.setStatus(PayrollStatus.DRAFT);
        }

        p.setBasicSalary(req.getBasicSalary() == null ? BigDecimal.ZERO : req.getBasicSalary());
        p.setAllowances(req.getAllowances() == null ? BigDecimal.ZERO : req.getAllowances());
        p.setDeductions(req.getDeductions() == null ? BigDecimal.ZERO : req.getDeductions());
        p.setTaxAmount(req.getTaxAmount() == null ? BigDecimal.ZERO : req.getTaxAmount());

        recalcAndRound(p);

        Payroll saved = payrollRepo.save(p);
        return toDto(saved);
    }

    public Optional<PayrollResponseDto> getById(Long id) {
        return payrollRepo.findById(id).map(this::toDto);
    }

    public List<PayrollResponseDto> listForPeriod(Integer month, Integer year) {
        return payrollRepo.findByMonthAndYear(month, year).stream().map(this::toDto).collect(Collectors.toList());
    }

    // Generate single payroll - dryRun = true => do not persist
    @Transactional
    public GenerateResultDto generateSingle(Long payrollId, boolean dryRun) {
        Payroll p = payrollRepo.findById(payrollId).orElseThrow(() -> new IllegalArgumentException("Payroll not found"));
        // recalc
        recalcAndRound(p);
        Employee e = p.getEmployee();
        List<String> warnings = validateForWarnings(e, p);
        if (!dryRun) {
            p.setStatus(PayrollStatus.GENERATED);
            payrollRepo.save(p);
        }
        return GenerateResultDto.builder()
                .payroll(toDto(p))
                .warnings(warnings)
                .persisted(!dryRun)
                .build();
    }

    // Generate bulk for a month/year for all employees; dryRun => do not persist changes
    @Transactional
    public List<GenerateResultDto> generateBulk(Integer month, Integer year, boolean dryRun) {
        List<Payroll> existing = payrollRepo.findByMonthAndYear(month, year);
        // if payrolls already exist for some employees, we update them; else create for all employees
        Map<Long, Payroll> byEmp = existing.stream()
                .collect(Collectors.toMap(p -> p.getEmployee().getEmpId(), p -> p));

        List<Employee> allEmployees = employeeRepo.findAll();
        List<GenerateResultDto> results = new ArrayList<>();

        for (Employee e : allEmployees) {
            Payroll p = byEmp.getOrDefault(e.getEmpId(), null);
            if (p == null) {
                p = new Payroll();
                p.setEmployee(e);
                p.setMonth(month);
                p.setYear(year);
                p.setBasicSalary(BigDecimal.ZERO);
                p.setAllowances(BigDecimal.ZERO);
                p.setDeductions(BigDecimal.ZERO);
                p.setTaxAmount(BigDecimal.ZERO);
                p.setStatus(PayrollStatus.DRAFT);
            }
            // Business: fill amounts - simple demo: keep basicSalary if present; otherwise you might fetch from employee profile
            // For demo, if basic = 0, skip or put a warning.
            recalcAndRound(p);
            List<String> warnings = validateForWarnings(e, p);

            if (!dryRun) {
                p.setStatus(PayrollStatus.GENERATED);
                payrollRepo.save(p);
            }
            results.add(GenerateResultDto.builder()
                    .payroll(toDto(p))
                    .warnings(warnings)
                    .persisted(!dryRun)
                    .build());
        }
        return results;
    }

    // Mark single payroll as PAID
    @Transactional
    public PayrollResponseDto markAsPaid(Long payrollId) {
        Payroll p = payrollRepo.findById(payrollId).orElseThrow(() -> new IllegalArgumentException("Payroll not found"));
        if (p.getStatus() == PayrollStatus.PAID) throw new IllegalStateException("Payroll already PAID");
        p.setStatus(PayrollStatus.PAID);
        payrollRepo.save(p);
        return toDto(p);
    }

    // CSV export for a period
    public String exportCsv(Integer month, Integer year) {
        List<Payroll> rows = payrollRepo.findByMonthAndYear(month, year);
        StringBuilder sb = new StringBuilder();
        sb.append("payrollId,employeeId,employeeName,month,year,basic,allowances,deductions,tax,net,status\n");
        for (Payroll p : rows) {
            Employee e = p.getEmployee();
            String name = e == null ? "" : e.getFirstName() + (e.getLastName() == null ? "" : " " + e.getLastName());
            sb.append(p.getPayrollId()).append(',')
                    .append(e == null ? "" : e.getEmpId()).append(',')
                    .append('"').append(name.replace("\"","'")).append('"').append(',')
                    .append(p.getMonth()).append(',')
                    .append(p.getYear()).append(',')
                    .append(p.getBasicSalary()).append(',')
                    .append(p.getAllowances()).append(',')
                    .append(p.getDeductions()).append(',')
                    .append(p.getTaxAmount()).append(',')
                    .append(p.getNetSalary()).append(',')
                    .append(p.getStatus()).append('\n');
        }
        return sb.toString();
    }
}
