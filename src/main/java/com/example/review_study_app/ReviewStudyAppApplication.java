package com.example.review_study_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReviewStudyAppApplication {

	public static void main(String[] args) {
		System.out.println("---------- 배포 성공? ---------");
		SpringApplication.run(ReviewStudyAppApplication.class, args);
	}

}
