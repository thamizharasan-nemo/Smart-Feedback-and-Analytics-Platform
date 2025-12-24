package com.example.FeedbackSystem.service.other_services;

import com.example.FeedbackSystem.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SchedulerService {
    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    ExcelExportService excelExportService;

    // Runs on Monday at midnight (00:00)
    // CRON Format - second minute hour day-of-month month day-of-week
    @Scheduled(cron = "0 0 0 * * MON")
    public void deleteExpiredRefreshTokens(){
        refreshTokenService.deleteExpiredTokens();
    }

    // Runs on 1st day of each month (09:00AM)
    @Scheduled(cron = "0 0 9 1 * *")
    public void exportExcelReportForStudents() {
        try {
            excelExportService.exportStudentsDetailsToExcel();
        } catch (IOException io){
            throw new RuntimeException();
        }
    }

    @Scheduled(cron = " 0 0 9 1 * *")
    public void exportExcelReportForCourses() {
        try {
            excelExportService.exportCourseDetailsToExcel();
        } catch (IOException io){
            throw new RuntimeException();
        }
    }
}
