package com.example.review_study_app.service.log;


import com.example.review_study_app.service.log.dto.SaveTaskLogDto;

public interface NoAlertLogService {

    void saveDiscordLog(SaveTaskLogDto saveTaskLogDto);
}
