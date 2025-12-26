package com.feedbacks.FeedbackSystem.service.other_services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    // Async helps methods run asynchronously
    public void simpleMailSender(String to, String subject, String htmlBody){
        try {
            Thread.sleep(5000);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // to do multipart mail (img, aud, vid)

            // getting file which is to be attached, using url path
            String url = System.getProperty("user.dir") + "\\sukuna.jpeg";
            FileSystemResource file = new FileSystemResource(new File(url));

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);  // the true tells that "this is HTML file or code". if not it takes the plain string as normal
            helper.addAttachment(file.getFilename(), file); // file(img, aud, vid) is attached in the mail
            mailSender.send(message);

        } catch (MessagingException | InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Use thymeleaf template dependency for beautiful emails


    @Async
    public void newCourseAddedEmail(String receivers, String subject, String body) {
        try {
            Thread.sleep(5000);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

            messageHelper.setSubject(subject);
            messageHelper.setText(body, true);
            messageHelper.setBcc(receivers);

            mailSender.send(mimeMessage);
        } catch (MessagingException | InterruptedException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
