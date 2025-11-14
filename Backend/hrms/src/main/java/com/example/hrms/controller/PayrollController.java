package com.example.hrms.controller;

import com.example.hrms.dto.*;
import com.example.hrms.service.PayrollService;
import com.example.hrms.util.PdfGenerator;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/payrolls")
@CrossOrigin
public class PayrollController {

    private final PayrollService payrollService;
    private final PdfGenerator pdfGenerator;

    public PayrollController(PayrollService payrollService, PdfGenerator pdfGenerator) {
        this.payrollService = payrollService;
        this.pdfGenerator = pdfGenerator;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<PayrollResponseDto> createOrUpdate(@RequestBody PayrollRequestDto dto) {
        return ResponseEntity.ok(payrollService.createOrUpdate(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<PayrollResponseDto> get(@PathVariable Long id) {
        return payrollService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<List<PayrollResponseDto>> listForPeriod(
            @RequestParam Integer month, @RequestParam Integer year) {
        return ResponseEntity.ok(payrollService.listForPeriod(month, year));
    }

    /**
     * Generate single payroll (dryRun flag): if dryRun=true, do not persist status change.
     */
    @PostMapping("/{id}/generate")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<GenerateResultDto> generateSingle(
            @PathVariable Long id, @RequestParam(defaultValue = "true") boolean dryRun) {
        return ResponseEntity.ok(payrollService.generateSingle(id, dryRun));
    }

    /**
     * Generate bulk for month/year. dryRun=true => no persistence.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<List<GenerateResultDto>> generateBulk(
            @RequestParam Integer month, @RequestParam Integer year,
            @RequestParam(defaultValue = "true") boolean dryRun) {
        return ResponseEntity.ok(payrollService.generateBulk(month, year, dryRun));
    }

    /**
     * Mark payroll as PAID (admin only)
     */
    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PayrollResponseDto> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.markAsPaid(id));
    }

    /**
     * CSV export for period
     */
    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<ByteArrayResource> exportCsv(@RequestParam Integer month, @RequestParam Integer year) {
        String csv = payrollService.exportCsv(month, year);
        byte[] bytes = csv.getBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes);
        String filename = "payroll_" + month + "_" + year + ".csv";
        return ResponseEntity.ok()
                .contentLength(bytes.length)
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    /**
     * Single payslip PDF
     */
    @GetMapping("/{id}/payslip")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<ByteArrayResource> singlePayslip(@PathVariable Long id) {
        PayrollResponseDto dto = payrollService.getById(id).orElseThrow(() -> new IllegalArgumentException("Not found"));
        byte[] pdf = pdfGenerator.generatePayslip(dto);
        ByteArrayResource resource = new ByteArrayResource(pdf);
        String filename = "payslip_" + dto.getEmployeeId() + "_" + dto.getMonth() + "_" + dto.getYear() + ".pdf";
        return ResponseEntity.ok()
                .contentLength(pdf.length)
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    /**
     * Bulk payslip (ZIP of PDFs) for a period
     */
    @GetMapping("/payslips/zip")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<ByteArrayResource> bulkPayslipsZip(@RequestParam Integer month, @RequestParam Integer year) {
        List<PayrollResponseDto> rows = payrollService.listForPeriod(month, year);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (PayrollResponseDto dto : rows) {
                byte[] pdf = pdfGenerator.generatePayslip(dto);
                String entryName = "payslip_" + dto.getEmployeeId() + "_" + dto.getMonth() + "_" + dto.getYear() + ".pdf";
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.write(pdf);
                zos.closeEntry();
            }
            zos.finish();
            byte[] zipBytes = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(zipBytes);
            String filename = "payslips_" + month + "_" + year + ".zip";
            return ResponseEntity.ok()
                    .contentLength(zipBytes.length)
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create ZIP", ex);
        }
    }
}
