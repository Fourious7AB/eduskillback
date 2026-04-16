package com.example.eduskill.service.impl;

import com.example.eduskill.entity.Course;
import com.example.eduskill.entity.Payment;
import com.example.eduskill.entity.Student;
import com.example.eduskill.service.InvoiceService;

import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Override
    public byte[] generateInvoice(Student student,
                                  Course course,
                                  Payment payment) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // ================= TITLE =================
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            // ================= COMPANY =================
            PdfPTable companyTable = new PdfPTable(1);
            companyTable.setWidthPercentage(100);

            PdfPCell companyCell = new PdfPCell();
            companyCell.setPadding(8);

            companyCell.addElement(new Paragraph("Eduskilliq Futuretech Private Limited", boldFont));
            companyCell.addElement(new Paragraph(
                    "Bidesh Mitra P1, Raghunathsayer More,\nBishnupur, Bankura, West Bengal - 722122",
                    normalFont));

            companyCell.addElement(new Paragraph("GSTIN: 19AAJCE3696E1ZA", normalFont));
            companyCell.addElement(new Paragraph("CIN: U85499WB2025PTC285358", normalFont));
            companyCell.addElement(new Paragraph("Invoice No: " + payment.getInvoiceNumber(), normalFont));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            companyCell.addElement(new Paragraph(
                    "Date: " + payment.getPaymentDate().format(formatter),
                    normalFont));

            companyTable.addCell(companyCell);
            document.add(companyTable);

            document.add(new Paragraph(" "));

            // ================= STUDENT =================
            PdfPTable studentTable = new PdfPTable(2);
            studentTable.setWidthPercentage(100);

            studentTable.addCell(getCell("Student Name", boldFont));
            studentTable.addCell(getCell(student.getName(), normalFont));

            studentTable.addCell(getCell("Email", boldFont));
            studentTable.addCell(getCell(student.getEmail(), normalFont));

            studentTable.addCell(getCell("Course", boldFont));
            studentTable.addCell(getCell(course.getCourseName(), normalFont));

            document.add(studentTable);

            document.add(new Paragraph(" "));

            // ================= TABLE =================
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 1, 2, 2});

            addHeader(table, "Sl No", boldFont);
            addHeader(table, "Service", boldFont);
            addHeader(table, "Qty", boldFont);
            addHeader(table, "Rate", boldFont);
            addHeader(table, "Amount", boldFont);


            double amount = payment.getAmount();
            double total = amount;

            table.addCell("1");
            table.addCell(course.getCourseName());
            table.addCell("1");
            table.addCell("₹" + amount);
            table.addCell("₹" + amount);

            addEmptyRow(table, 3);
            table.addCell("Total");
            table.addCell("₹" + total);



            document.add(table);

            document.add(new Paragraph(" "));

            // ================= DECLARATION =================
            document.add(new Paragraph(
                    "Declaration:\nWe declare that this invoice shows the actual price and GST applied as per law.",
                    normalFont));

            document.add(new Paragraph(" "));

            // ================= STAMP ABOVE SIGN =================
            try {
                Image stamp = Image.getInstance("src/main/resources/static/stamp.png");
                stamp.scaleToFit(120, 120);
                stamp.setAlignment(Element.ALIGN_RIGHT);
                document.add(stamp);
            } catch (Exception e) {
                System.out.println("Stamp not found");
            }

            Paragraph sign = new Paragraph("Authorized Signatory", boldFont);
            sign.setAlignment(Element.ALIGN_RIGHT);
            document.add(sign);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Invoice generation failed", e);
        }
    }

    // ================= HELPERS =================
    private PdfPCell getCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        return cell;
    }

    private void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell header = new PdfPCell(new Phrase(text, font));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setPadding(5);
        table.addCell(header);
    }

    private void addEmptyRow(PdfPTable table, int colspan) {
        PdfPCell empty = new PdfPCell(new Phrase(""));
        empty.setColspan(colspan);
        empty.setBorder(Rectangle.NO_BORDER);
        table.addCell(empty);
    }
}