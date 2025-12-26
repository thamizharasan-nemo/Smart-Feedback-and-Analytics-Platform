package com.feedbacks.FeedbackSystem.service.other_services;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.model.Course;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.service.CourseService;
import com.feedbacks.FeedbackSystem.service.UserService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExcelExportService {

    private final UserService userService;
    private final CourseService courseService;

    public ExcelExportService(UserService userService, CourseService courseService) {
        this.userService = userService;
        this.courseService = courseService;
    }

    public String exportStudentsDetailsToExcel() throws IOException {
        String path = System.getProperty("user.dir") + "\\exported_resources\\students.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("students");
            // Header row values
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Roll No");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Email");
            headerRow.createCell(3).setCellValue("Signed At");

            // Data and rows values
            List<UserResponseDTO> usersList = userService.getAllUsersByRole(User.Role.STUDENT);

            List<LocalDate> createdDate = usersList.stream()
                    .map(u -> {
                        User user = userService.getUserById(u.getUserId());
                        return user.getUserCreatedAt();
                    })
                    .toList();


            int rowIndex = 1; // first row after header row
            int dateIndex = 0;
            for (UserResponseDTO user : usersList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(user.getIdentityNo());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail());

                LocalDate created = createdDate.get(dateIndex++);
                row.createCell(3).setCellValue(created != null ? created.toString() : "");

            }

            addSizing(rowIndex, sheet);

            // Write to file
            try (FileOutputStream fileOutput = new FileOutputStream(path)) {
                workbook.write(fileOutput);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Error while creating excel sheet. " + e.getMessage());
        }
        return path;
    }

    public void addSizing(int index, Sheet sheet){
        // Adding auto sizing to columns
        for (int i = 0; i <= index; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    public Resource downloadStudentsExcelSheet() throws IOException {
        ByteArrayResource resource;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("students");
            // Header row values
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Roll No");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Email");
            headerRow.createCell(3).setCellValue("Signed At");

            // Data and rows values
            List<UserResponseDTO> usersList = userService.getAllUsersByRole(User.Role.STUDENT);

            int rowIndex = 1; // first row after header row
            for (UserResponseDTO user : usersList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(user.getIdentityNo());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail());
            }

            // Adding auto sizing to columns - 3 columns
            addSizing(rowIndex ,sheet);

            // Convert workbook to byte[]
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            workbook.write(byteOutputStream);

            resource = new ByteArrayResource(byteOutputStream.toByteArray());
        }
        catch (Exception e) {
            throw new RuntimeException("Error while exporting student excel sheet. " + e.getMessage());
        }
        return resource;
    }

    public String exportCourseDetailsToExcel() throws IOException {
        String path = Paths.get(System.getProperty("user.dir") , "exported_resources", "courses.xlsx").toString();

        try(Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("courses");

            Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("Course_id");
//            headerRow.createCell(1).setCellValue("Course_Name");
//            headerRow.createCell(2).setCellValue("Course_Description");
//            headerRow.createCell(3).setCellValue("Created_by");
//            headerRow.createCell(4).setCellValue("Modified_by");
//            headerRow.createCell(5).setCellValue("Deleted_by");
//            headerRow.createCell(6).setCellValue("is_deleted");
//            headerRow.createCell(7).setCellValue("Instructor_Name");
            String[] headerColValues = {"Course_id", "Course_Name", "Course_Description", "Created_by", "Modified_by", "Deleted_by", "is_deleted", "Instructor_Name"};
            createHeaderRow(headerRow, 7, headerColValues);

            List<Course> courseList = courseService.getAllCourses();
            int rowIndex = 1;
            for(Course course : courseList){
                Row bodyRow = sheet.createRow(rowIndex++);
                bodyRow.createCell(0).setCellValue(course.getCourseId());
                bodyRow.createCell(1).setCellValue(course.getCourseName());
                bodyRow.createCell(2).setCellValue(course.getCourseDescription());
                bodyRow.createCell(3).setCellValue(course.getCreatedBy());
                bodyRow.createCell(4).setCellValue(course.getModifiedBy());
                bodyRow.createCell(5).setCellValue(course.getDeletedBy());
                bodyRow.createCell(6).setCellValue(course.isDeleted());
                bodyRow.createCell(7).setCellValue(course.getInstructor() == null ? "not assigned" :course.getInstructor().getInstructorName());
            }

            addSizing(rowIndex + 1, sheet);

            try(FileOutputStream fileOutputStream = new FileOutputStream(path)){
                workbook.write(fileOutputStream);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Error while creating excel sheet. " + e.getMessage());
        }
        return path;
    }

    public void createHeaderRow(Row headerRow, int columnIndex, String[] colNames){
        for (int i = 0; i <= columnIndex; i++) {
            headerRow.createCell(i).setCellValue(colNames[i]);
        }
    }

    public void createBodyRow(Row bodyRow, int columnIndex, List<?> colValues) {
        for (Object object : colValues) {

            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    bodyRow.createCell(columnIndex++).setCellValue(value != null ? value.toString() : "null");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
