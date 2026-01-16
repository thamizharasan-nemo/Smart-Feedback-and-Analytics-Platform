package com.feedbacks.FeedbackSystem.service.other_services;

import com.feedbacks.FeedbackSystem.service.serviceImple.RefreshTokenServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SchedulerService {

    private final RefreshTokenServiceImpl refreshTokenService;
    private final ExcelExportService excelExportService;

    public SchedulerService(RefreshTokenServiceImpl refreshTokenService, ExcelExportService excelExportService) {
        this.refreshTokenService = refreshTokenService;
        this.excelExportService = excelExportService;
    }

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
