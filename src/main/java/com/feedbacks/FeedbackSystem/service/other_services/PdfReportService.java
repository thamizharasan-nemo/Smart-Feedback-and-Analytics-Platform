package com.feedbacks.FeedbackSystem.service.other_services;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.service.serviceImple.UserServiceImpl;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class PdfReportService {

    private final UserServiceImpl userService;

    private final Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
    private final Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.DARK_GRAY);
    private final Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

    public PdfReportService(UserServiceImpl userService) {
        this.userService = userService;
    }

    public String generateUserReport() {
        String path = System.getProperty("user.dir") + "\\user-report.pdf";
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(new File(path));
            Document document = new Document(PageSize.A4);
            // 1. Create writer FIRST
            PdfWriter.getInstance(document, fileOutputStream);
            // 2. Open the Document
            document.open(); // doc file should be opened first to write the contents

            // 3. Then add Contents
            Paragraph reportTitle = new Paragraph("Student Report", titleFont);
            reportTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(reportTitle);
            document.add(Chunk.NEWLINE);

            Paragraph dateTag = new Paragraph("Date: " + LocalDate.now(), subTitleFont);
            dateTag.setAlignment(Element.ALIGN_LEFT);
            document.add(dateTag);

            Paragraph dayTag = new Paragraph("Day: " + DayOfWeek.from(LocalDate.now()), subTitleFont);
            dayTag.setAlignment(Element.ALIGN_LEFT);
            document.add(dayTag);

            createTable(document, 3);
            // 4. Finally CLOSE it
            document.close(); // and it should be closed

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return path;
    }

    public void leaveEmptyLines(Paragraph paragraph, int lines){
        for (int i = 0; i < lines; i++){
            paragraph.add(new Paragraph(" "));
        }
    }

    public void createTable(Document document, int noOfColumn) throws DocumentException {
        Paragraph paragraph = new Paragraph();
        leaveEmptyLines(paragraph, 2);
        document.add(paragraph);
        PdfPTable table = new PdfPTable(noOfColumn);

        table.setWidthPercentage(100);
        float[] widths = {2f, 3f, 3f};
        table.setWidths(widths);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

        PdfPCell rollNoCell = new PdfPCell(new Phrase("Roll No", headerFont));
        rollNoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rollNoCell.setPadding(10);
        PdfPCell nameCell = new PdfPCell(new Phrase("Username", headerFont));
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nameCell.setPadding(10);
        PdfPCell emailCell = new PdfPCell(new Phrase("Email", headerFont));
        emailCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        emailCell.setPadding(10);

        table.addCell(rollNoCell);
        table.addCell(nameCell);
        table.addCell(emailCell);

        table.setHeaderRows(1);
        getDBData(table);
        document.add(table);
    }

    public void getDBData(PdfPTable table){
        List<UserResponseDTO> users = userService.getAllUsersByRole(User.Role.STUDENT);
        for(UserResponseDTO user : users){

            table.getDefaultCell().setPadding(10);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.addCell(user.getIdentityNo());
            table.addCell(user.getUsername());
            table.addCell(user.getEmail());
        }

    }
}