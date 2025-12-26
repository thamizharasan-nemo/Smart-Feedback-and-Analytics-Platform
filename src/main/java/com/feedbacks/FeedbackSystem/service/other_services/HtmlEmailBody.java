package com.feedbacks.FeedbackSystem.service.other_services;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HtmlEmailBody {

    private final UserService userService;
    private final EmailService emailService;

    public HtmlEmailBody(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    public void newCourseAddedHtmlBody(String courseName){
        List<UserResponseDTO> studentList = userService.getAllUsersByRole(User.Role.STUDENT);
        List<String> receiversEmails = studentList.stream()
                .map(UserResponseDTO::getEmail)
                .toList();

        for(UserResponseDTO student : studentList) {
            String htmlContent = "<h1 style=\"color: #4CAF50;\">Hello, " + student.getUsername() + "</h1>"
                    + "<p>A new course is added in our feedback system</b> 🎉</p>"
                    + "<p>Course name is " + courseName + "</p>"
                    + "<p>Enroll to the course faster \uD83D\uDE80</p>"
                    + "<hr>"
                    + "<small>This is an automated email, please do not reply.</small>";
            emailService.newCourseAddedEmail(   // Async call
                    student.getEmail(),
                    "New Course is Added \uD83D\uDCE2",
                    htmlContent
            );
        }
    }

    public void registrationEmail(UserResponseDTO user){
            //sends an email to the registered user from the email in the application.properties
            String htmlContent = "<h1 style=\"color: #4CAF50;\">Welcome, " + user.getUsername() + "</h1>"
                    + "<p>Your registration was <b>successful</b> 🎉</p>"
                    + "<p>Thanks for joining <i>Feedback System</i>.</p>"
                    + "<hr>"
                    + "<small>This is an automated email, please do not reply.</small>";
            emailService.simpleMailSender(   // Async call
                    user.getEmail(),
                    "Registered Successfully!",
                    htmlContent
            );
    }
}
