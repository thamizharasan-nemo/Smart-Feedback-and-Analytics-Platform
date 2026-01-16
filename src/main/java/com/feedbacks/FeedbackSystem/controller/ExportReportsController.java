package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.service.other_services.ExcelExportService;
import com.feedbacks.FeedbackSystem.service.other_services.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequestMapping("/export")
public class ExportReportsController {

    private final PdfReportService pdfService;
    private final ExcelExportService excelExportService;

    public ExportReportsController(PdfReportService pdfService, ExcelExportService excelExportService) {
        this.pdfService = pdfService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/pdf/user")
    public String generateUserReport() {
        String filePath = pdfService.generateUserReport();
        return "Pdf generated in: " + filePath;
    }

    @GetMapping("/user/excel")
    public ResponseEntity<String> excelExportStudents() throws IOException {
        return ResponseEntity.ok().body("Excel sheet exported at "+ excelExportService.exportStudentsDetailsToExcel());
    }

    @GetMapping("/user/excel/download")
    public ResponseEntity<Resource> downloadStudentsExcelSheet() throws IOException {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename:students.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelExportService.downloadStudentsExcelSheet());
    }

    @GetMapping("/course/excel")
    public ResponseEntity<String> excelExportCourses() throws IOException {
        return ResponseEntity.ok().body("Excel sheet exported at "+ excelExportService.exportCourseDetailsToExcel());
    }
}
