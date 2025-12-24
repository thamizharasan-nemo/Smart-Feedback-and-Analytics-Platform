package com.example.FeedbackSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableScheduling
@EnableAsync
public class FeedbackSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbackSystemApplication.class, args);
	}

}
