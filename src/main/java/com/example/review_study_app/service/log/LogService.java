package com.example.review_study_app.service.log;


public interface LogService {

    void saveJobLog(SaveJobLogDto saveJobLogDto);

    void saveStepLog(SaveStepLogDto saveStepLogDto);

    void saveTaskLog(SaveTaskLogDto saveTaskLogDto);
}