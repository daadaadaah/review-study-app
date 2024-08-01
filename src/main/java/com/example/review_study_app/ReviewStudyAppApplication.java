package com.example.review_study_app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableRetry
@EnableScheduling
@SpringBootApplication
public class ReviewStudyAppApplication {

	public static void main(String[] args) {
		log.info("review study scheduler가 시작되었습니다.");
		SpringApplication.run(ReviewStudyAppApplication.class, args);
	}

}
