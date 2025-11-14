package com.example.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll",
        uniqueConstraints = @UniqueConstraint(name = "uk_payroll_employee_period",
                columnNames = {"emp_id", "month", "year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payrollId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    // Period as simple month/year integers (1-12)
    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    // Monetary fields using BigDecimal
    @Column(name = "basic_salary", precision = 15, scale = 2, nullable = false)
    private BigDecimal basicSalary = BigDecimal.ZERO;

    @Column(name = "allowances", precision = 15, scale = 2, nullable = false)
    private BigDecimal allowances = BigDecimal.ZERO;

    @Column(name = "deductions", precision = 15, scale = 2, nullable = false)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "net_salary", precision = 15, scale = 2, nullable = false)
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Simple recalculation helper: gross = basic + allowances; net = gross - deductions - tax.
     * Use consistent BigDecimal rounding in your service when calling this.
     */
    public void recalcTotals() {
        BigDecimal gross = (basicSalary == null ? BigDecimal.ZERO : basicSalary)
                .add(allowances == null ? BigDecimal.ZERO : allowances);
        this.netSalary = gross
                .subtract(deductions == null ? BigDecimal.ZERO : deductions)
                .subtract(taxAmount == null ? BigDecimal.ZERO : taxAmount);
    }

    public enum PayrollStatus {
        DRAFT,
        GENERATED,
        PAID
    }
}
