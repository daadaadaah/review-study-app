package com.example.review_study_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ReviewStudyAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewStudyAppApplication.class, args);
	}

}
