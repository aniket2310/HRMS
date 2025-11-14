package com.example.hrms.util;

import com.example.hrms.dto.PayrollResponseDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PdfGenerator {

    public byte[] generatePayslip(PayrollResponseDto dto) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);

            doc.add(new Paragraph("Payslip", h1));
            doc.add(new Paragraph("Employee: " + dto.getEmployeeName(), normal));
            doc.add(new Paragraph("Period: " + dto.getMonth() + "/" + dto.getYear(), normal));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.addCell(new Phrase("Basic Salary", normal));
            table.addCell(new Phrase(dto.getBasicSalary().toString(), normal));

            table.addCell(new Phrase("Allowances", normal));
            table.addCell(new Phrase(dto.getAllowances().toString(), normal));

            table.addCell(new Phrase("Deductions", normal));
            table.addCell(new Phrase(dto.getDeductions().toString(), normal));

            table.addCell(new Phrase("Tax", normal));
            table.addCell(new Phrase(dto.getTaxAmount().toString(), normal));

            table.addCell(new Phrase("Net Salary", h1));
            table.addCell(new Phrase(dto.getNetSalary().toString(), h1));

            doc.add(table);
            doc.close();

            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create PDF", ex);
        }
    }
}
