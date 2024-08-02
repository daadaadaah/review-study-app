package com.example.review_study_app.common.service.log;

import com.example.review_study_app.common.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.common.service.log.dto.SaveStepLogDto;
import com.example.review_study_app.common.service.log.dto.SaveTaskLogDto;


public interface LogService {

    void saveJobLog(SaveJobLogDto saveJobLogDto);

    void saveStepLog(SaveStepLogDto saveStepLogDto);

    void saveTaskLog(SaveTaskLogDto saveTaskLogDto);
}