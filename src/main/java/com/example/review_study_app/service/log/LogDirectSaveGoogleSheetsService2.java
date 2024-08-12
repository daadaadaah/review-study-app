package com.example.review_study_app.service.log;

import static com.example.review_study_app.service.notification.DiscordNotificationService.MAX_DISCORD_MESSAGE_LENGTH;
import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveDetailLogFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveRollbackFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveRollbackSuccessMessage;
import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveSuccessMessage;
import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveUnKnownFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.JobLogsSaveMessageFactory.createJobLogsSaveDetailLogFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.JobLogsSaveMessageFactory.createJobLogsSaveRollbackFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.JobLogsSaveMessageFactory.createJobLogsSaveRollbackSuccessMessage;
import static com.example.review_study_app.service.notification.factory.message.JobLogsSaveMessageFactory.createJobLogsSaveSuccessMessage;
import static com.example.review_study_app.service.notification.factory.message.JobLogsSaveMessageFactory.createJobLogsSaveUnknownFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.StepLogsSaveMessageFactory.createStepLogsSaveDetailLogFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.StepLogsSaveMessageFactory.createStepLogsSaveRollbackFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.StepLogsSaveMessageFactory.createStepLogsSaveRollbackSuccessMessage;
import static com.example.review_study_app.service.notification.factory.message.StepLogsSaveMessageFactory.createStepLogsSaveSuccessMessage;
import static com.example.review_study_app.service.notification.factory.message.StepLogsSaveMessageFactory.createStepLogsSaveUnKnownFailureMessage;

import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.repository.log.LogGoogleSheetsRepository;
import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;
import com.example.review_study_app.repository.log.exception.GoogleSheetsRollbackFailureException;
import com.example.review_study_app.repository.log.exception.GoogleSheetsTransactionException;
import com.example.review_study_app.repository.log.exception.SaveDetailLogException;
import com.example.review_study_app.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.service.log.dto.SaveStepLogDto;
import com.example.review_study_app.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.service.log.helper.LogHelper;
import com.example.review_study_app.service.notification.NotificationService;
import com.example.review_study_app.service.notification.enums.LogSaveResultType;
import com.example.review_study_app.service.notification.vo.JsonFile;
import com.example.review_study_app.service.notification.vo.LogSaveResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LogDirectSaveGoogleSheetsService2 {

//    private final Stack<String> batchProcessResultStack = new Stack<>();

    private final Stack<LogSaveResult> logSaveResultStack = new Stack<>();


    // 성공 - 성공 메시지
    // 실패 - 실패 메시지, 관련 데이터 1, 관련 데이터 2


    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    private final NotificationService notificationService;

    private final LogHelper logHelper;

    private final ObjectMapper objectMapper;

    @Autowired
    public LogDirectSaveGoogleSheetsService2(
        LogGoogleSheetsRepository logGoogleSheetsRepository,
        NotificationService notificationService,
        LogHelper logHelper,
        ObjectMapper objectMapper
    ) {
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
        this.notificationService = notificationService;
        this.logHelper = logHelper;
        this.objectMapper = objectMapper;
    }

    private <T> ByteArrayResource createJsonResourceFromObject(String fileName, T fileData) {

        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(fileData);

            ByteArrayResource jsonResource = new ByteArrayResource(jsonBytes) {
                @Override
                public String getFilename() { // TODO : 파일 형식 다양하게 만들 수 있게. json, xlsx 등
                    return fileName+".json"; // 파일 이름 지정
                }
            };

            return jsonResource;
        } catch (JsonProcessingException jsonProcessingException) {

            throw new RuntimeException("jsonProcessingException 발생="+jsonProcessingException.getMessage()); // TODO : 에외처리 다시 수정해야 함.
        }
    }

    /**
     * saveJobLog는 Job 관련된 로그를 저장하는 메서드이다.
     *
     * < @Async("logSaveTaskExecutor") 추가한 이유 >
     * - 비즈니스 로직인 Github 작업과 로그 작업을 디커플링 시키기 위해
     * -
     *
     *
     */
    // TODO : 디스코드로! Job, Step, Task 모두 보내면, 디스코드 시끄러울 것 같은데, 이거 어떻게 할지 고민해보기
    @Async("logSaveTaskExecutor") // TODO : 로그 저장 실패시, 비동기 예외 처리 어떻게 할 것인지 + 트랜잭션 처리
    public void saveJobLog(SaveJobLogDto saveJobLogDto) {

//        String newJobDetailLogRange = null;

        UUID jobId = saveJobLogDto.jobId();

        long jobDetailLogId = saveJobLogDto.endTime();

        JobDetailLog jobDetailLog = JobDetailLog.of(
            jobDetailLogId,
            logHelper.getEnvironment(),
            saveJobLogDto,
            logHelper.getCreatedAt(saveJobLogDto.startTime())
        );

        ExecutionTimeLog executionTimeLog = ExecutionTimeLog.of(
            jobId,
            null,
            logHelper.getEnvironment(),
            BatchProcessType.JOB,
            saveJobLogDto.methodName(),
            saveJobLogDto.status(),
            saveJobLogDto.statusReason(),
            jobDetailLogId,
            saveJobLogDto.endTime() - saveJobLogDto.startTime(),
            logHelper.getCreatedAt(saveJobLogDto.startTime())
        );

        // TODO : 예외가 발생할 때만, 해야되긴하는데....
        String jobDetailLogFileName = "jobDetailLog_"+ jobDetailLog.id();

        String jobExecutionTimeLogFileName = "jobExecutionTimeLog_"+ executionTimeLog.id();

        List<JsonFile> jsonFileList = createJobJsonFiles(jobDetailLog, executionTimeLog);


        try {
            logGoogleSheetsRepository.saveJobLogsWithTx(jobDetailLog, executionTimeLog);

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.SUCCESS,
                createJobLogsSaveSuccessMessage(jobId),
                null
            ));


//            batchProcessResultStack.add(createJobLogsSaveSuccessMessage(jobId));

        } catch (SaveDetailLogException exception) { // 롤백 필요 없음

//            List<JsonFile> jsonFileList = createJsonFiles(jobDetailLog, executionTimeLog);
//
//            String jobDetailLogFileName = "jobDetailLog_"+ jobDetailLog.id();
////
////            JsonFile jobDetailLogJsonFile = new JsonFile(jobDetailLogFileName, createJsonResourceFromObject(jobDetailLogFileName, jobDetailLog);
////
////            jsonFileList.add(jobDetailLogJsonFile);
////
////
//            String jobExecutionTimeLogFileName = "jobExecutionTimeLog_"+ executionTimeLog.id();
////
////            ByteArrayResource jobExecutionTimeLogJsonResource = createJsonResourceFromObject(jobExecutionTimeLogFileName, executionTimeLog);
////
////            JsonFile jobExecutionTimeLogJsonFile = new JsonFile(jobExecutionTimeLogFileName, jobExecutionTimeLogJsonResource);
////
////            jsonFileList.add(jobExecutionTimeLogJsonFile);

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createJobLogsSaveDetailLogFailureMessage(
                    exception,
                    jobDetailLogFileName,
                    jobExecutionTimeLogFileName
                ),
                jsonFileList
            ));


//            notificationService.sendMessageWithJsonFile(currentMessage, "dd", jobDetailLog);

//            batchProcessResultStack.add(createJobLogsSaveDetailLogFailureMessage(
//                exception,
//                jobDetailLog,
//                executionTimeLog
//            ));

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우
            log.error("--------exception={}", exception.getMessage());
//
//            String jobDetailLogFileName = "jobDetailLog_"+ jobDetailLog.id();
//
//            String jobExecutionTimeLogFileName = "jobExecutionTimeLog_"+ executionTimeLog.id();
//
//            List<JsonFile> jsonFileList = createJsonFiles(jobDetailLog, executionTimeLog);
//

            logSaveResultStack.add(new LogSaveResult(
                    LogSaveResultType.FAILURE,
                    createJobLogsSaveRollbackSuccessMessage(
                        exception,
                        jobDetailLogFileName,
                        jobExecutionTimeLogFileName,
                        exception.getGoogleSheetsRollbackRange()
                    ),
                    jsonFileList
            ));


//            batchProcessResultStack.add(createJobLogsSaveRollbackSuccessMessage(
//                exception,
//                jobDetailLog,
//                executionTimeLog,
//                exception.getGoogleSheetsRollbackRange()
//            ));

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우
            log.error("--------exception={}", exception.getMessage());

//            String jobDetailLogFileName = "jobDetailLog_"+ jobDetailLog.id();
//
//            String jobExecutionTimeLogFileName = "jobExecutionTimeLog_"+ executionTimeLog.id();
//
//            List<JsonFile> jsonFileList = createJsonFiles(jobDetailLog, executionTimeLog);

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createJobLogsSaveRollbackFailureMessage(
                    exception,
                    jobDetailLogFileName,
                    jobExecutionTimeLogFileName,
                    exception.getGoogleSheetsRollbackRange()
                ),
                jsonFileList
            ));


//            batchProcessResultStack.add(createJobLogsSaveRollbackFailureMessage(
//                exception,
//                jobDetailLog,
//                executionTimeLog,
//                exception.getGoogleSheetsRollbackRange()
//            ));

        } catch (Exception exception) {
            log.error("--------exceptionClass={}", exception.getClass().getSimpleName());

            log.error("--------exception={}", exception.getMessage());

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createJobLogsSaveUnknownFailureMessage(
                    exception,
                    jobDetailLogFileName,
                    jobExecutionTimeLogFileName
                ),
                jsonFileList
            ));



//            batchProcessResultStack.add(createJobLogsSaveUnknownFailureMessage(
//                exception,
//                jobDetailLog,
//                executionTimeLog
//            ));
        }


        notifyBatchProcessTotalResult();




//        if(batchProcessSuccessStack.size() > 0) {
//            notifyBatchProcessTotalResult();
//        }
    }


    private List<JsonFile> createJobJsonFiles(JobDetailLog jobDetailLog, ExecutionTimeLog executionTimeLog) {
        List<JsonFile> jsonFileList = new ArrayList<>();

        String jobDetailLogFileName = "jobDetailLog_"+ jobDetailLog.id();

        JsonFile jobDetailLogJsonFile = new JsonFile(jobDetailLogFileName, createJsonResourceFromObject(jobDetailLogFileName, jobDetailLog));

        jsonFileList.add(jobDetailLogJsonFile);

        String jobExecutionTimeLogFileName = "jobExecutionTimeLog_"+ executionTimeLog.id();

        ByteArrayResource jobExecutionTimeLogJsonResource = createJsonResourceFromObject(jobExecutionTimeLogFileName, executionTimeLog);

        JsonFile jobExecutionTimeLogJsonFile = new JsonFile(jobExecutionTimeLogFileName, jobExecutionTimeLogJsonResource);


        jsonFileList.add(jobExecutionTimeLogJsonFile);

        return jsonFileList;
    }

    private void notifyBatchProcessTotalResult() {
        StringBuilder currentMessage = new StringBuilder("");

        List<ByteArrayResource> byteArrayResources = new ArrayList<>();

        while (!logSaveResultStack.isEmpty()) {
            LogSaveResult logSaveResult = logSaveResultStack.pop();
            // 1. 메시지 글자수 체크
            String newMessage = logSaveResult.message();

            if(logSaveResult.unsavedLogJsonResources() != null) {
                List<JsonFile> jsonFileList = logSaveResult.unsavedLogJsonResources();

                for(JsonFile jsonFile: jsonFileList) {
                    byteArrayResources.add(jsonFile.byteArrayResource()); // TODO : 추가 되기 전에 용량 체크
                }


                if(currentMessage.length() + newMessage.length() < MAX_DISCORD_MESSAGE_LENGTH) {
                    // 현재 메시지에 새로운 내용을 추가합니다.
                    currentMessage.append("\n").append(newMessage);
                } else { // MAX_DISCORD_MESSAGE_LENGTH 초과한 경우

                    // 현재 메시지가 길이를 초과하면 메시지를 전송하고, 현재 메시지를 비워야 합니다.
                    notificationService.sendMessageWithFile(currentMessage.toString(), byteArrayResources);

                    currentMessage = new StringBuilder(newMessage);

                    byteArrayResources = new ArrayList<>();
                }
            } else {
                if(currentMessage.length() + newMessage.length() < MAX_DISCORD_MESSAGE_LENGTH) {
                    // 현재 메시지에 새로운 내용을 추가합니다.
                    currentMessage.append("\n").append(newMessage);
                } else { // MAX_DISCORD_MESSAGE_LENGTH 초과한 경우

                    // 현재 메시지가 길이를 초과하면 메시지를 전송하고, 현재 메시지를 비워야 합니다.
                    notificationService.sendMessage(currentMessage.toString());

                    currentMessage = new StringBuilder(newMessage);
                }
            }




            // 2. josn 파일 용량 체크
        }

        if(byteArrayResources != null) {
            notificationService.sendMessageWithFile(currentMessage.toString(), byteArrayResources);
        } else {
            notificationService.sendMessage(currentMessage.toString());
        }


    }


//    private void notifyBatchProcessTotalResult() {
//        StringBuilder currentMessage = new StringBuilder("");
//
//        while (!batchProcessResultStack.isEmpty()) {
//
//            String value = batchProcessResultStack.pop();
//
//            if(value.length() < MAX_DISCORD_MESSAGE_LENGTH) {
//                // TODO : githubApiLog 자체가 2000자 넘음.
//            }
//
//            if(currentMessage.length() + value.length() < MAX_DISCORD_MESSAGE_LENGTH) {
//                // 현재 메시지에 새로운 내용을 추가합니다.
//                currentMessage.append("\n").append(value);
//            } else { // MAX_DISCORD_MESSAGE_LENGTH 초과한 경우
//                log.info("currentMessage = {}", currentMessage.toString());
//
//                log.info("currentMessage.length() + value.length() = {}", currentMessage.length() + value.length());
//
//                // 현재 메시지가 길이를 초과하면 메시지를 전송하고, 현재 메시지를 비워야 합니다.
//                notificationService.sendMessage(currentMessage.toString());
//
//
//
//                currentMessage = new StringBuilder(value);
//            }
//        }
//
//        // 마지막으로 남은 메시지를 전송합니다.
//        if (currentMessage.length() > 0) {
//            notificationService.sendMessage(currentMessage.toString());
//        }
//    }

    @Async("logSaveTaskExecutor")
    public void saveStepLog(SaveStepLogDto saveStepLogDto) {
        UUID stepId = saveStepLogDto.stepId();

        long stepDetailLogId = saveStepLogDto.endTime();

        StepDetailLog stepDetailLog = StepDetailLog.of(
            stepDetailLogId,
            logHelper.getEnvironment(),
            saveStepLogDto,
            logHelper.getCreatedAt(saveStepLogDto.endTime())
        );

        long timeTaken = saveStepLogDto.endTime() - saveStepLogDto.startTime();

        ExecutionTimeLog executionTimeLog = ExecutionTimeLog.of(
            saveStepLogDto.stepId(),
            saveStepLogDto.jobId(),
            logHelper.getEnvironment(),
            BatchProcessType.STEP,
            saveStepLogDto.methodName(),
            saveStepLogDto.status(),
            saveStepLogDto.statusReason(),
            stepDetailLogId,
            timeTaken,
            logHelper.getCreatedAt(saveStepLogDto.endTime())
        );

        // TODO : 예외가 발생할 때만, 해야되긴하는데....
        String stepDetailLogFileName = "stepDetailLog_"+ stepDetailLog.id();

        String stepExecutionTimeLogFileName = "stepExecutionTimeLog_"+ executionTimeLog.id();

        List<JsonFile> jsonFileList = createStepJsonFiles(stepDetailLog, executionTimeLog);





        try {
            logGoogleSheetsRepository.saveStepLogsWithTx(stepDetailLog, executionTimeLog);

//            batchProcessResultStack.add(createStepLogsSaveSuccessMessage(stepId));

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.SUCCESS,
                createStepLogsSaveSuccessMessage(stepId),
                null
            ));


        } catch (SaveDetailLogException exception) { // 롤백 필요 없음

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createStepLogsSaveDetailLogFailureMessage(
                    exception,
                    stepDetailLogFileName,
                    stepExecutionTimeLogFileName
                ),
                jsonFileList
            ));

//            batchProcessResultStack.add(createStepLogsSaveDetailLogFailureMessage(
//                exception,
//                stepDetailLog,
//                executionTimeLog
//            ));

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우


            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createStepLogsSaveRollbackSuccessMessage(
                    exception,
                    stepDetailLogFileName,
                    stepExecutionTimeLogFileName,
                    exception.getGoogleSheetsRollbackRange()
                ),
                jsonFileList
            ));

//            batchProcessResultStack.add(createStepLogsSaveRollbackSuccessMessage(
//                exception,
//                stepDetailLog,
//                executionTimeLog,
//                exception.getGoogleSheetsRollbackRange()
//            ));

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createStepLogsSaveRollbackFailureMessage(
                    exception,
                    stepDetailLogFileName,
                    stepExecutionTimeLogFileName,
                    exception.getGoogleSheetsRollbackRange()
                ),
                jsonFileList
            ));


//            batchProcessResultStack.add(createStepLogsSaveRollbackFailureMessage(
//                exception,
//                stepDetailLog,
//                executionTimeLog,
//                exception.getGoogleSheetsRollbackRange()
//            ));

        } catch (Exception exception) {

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createStepLogsSaveUnKnownFailureMessage(
                    exception,
                    stepDetailLogFileName,
                    stepExecutionTimeLogFileName
                ),
                jsonFileList
            ));

//            batchProcessResultStack.add(createStepLogsSaveUnKnownFailureMessage(
//                exception,
//                stepDetailLog,
//                executionTimeLog
//            ));
        }
    }


    private List<JsonFile> createStepJsonFiles(StepDetailLog stepDetailLog, ExecutionTimeLog executionTimeLog) {
        List<JsonFile> jsonFileList = new ArrayList<>();

        String stepDetailLogFileName = "stepDetailLog_"+ stepDetailLog.id();

        JsonFile stepDetailLogJsonFile = new JsonFile(stepDetailLogFileName, createJsonResourceFromObject(stepDetailLogFileName, stepDetailLog));

        jsonFileList.add(stepDetailLogJsonFile);

        String stepExecutionTimeLogFileName = "stepExecutionTimeLog_"+ executionTimeLog.id();

        ByteArrayResource stepExecutionTimeLogJsonResource = createJsonResourceFromObject(stepExecutionTimeLogFileName, executionTimeLog);

        JsonFile stepExecutionTimeLogJsonFile = new JsonFile(stepExecutionTimeLogFileName, stepExecutionTimeLogJsonResource);

        jsonFileList.add(stepExecutionTimeLogJsonFile);

        return jsonFileList;
    }




    @Async("logSaveTaskExecutor") // TODO : resttemplate 통신 로그 저장 방법 AOP -> 인터셉터로 바꾼 후에 수정하기
    public void saveTaskLog(SaveTaskLogDto saveTaskLogDto) {

        UUID taskId = saveTaskLogDto.taskId();

        long taskDetailLogId = saveTaskLogDto.endTime();

        long timeTaken = saveTaskLogDto.endTime() - saveTaskLogDto.startTime();

        GithubApiLog githubApiLog = new GithubApiLog(
            taskDetailLogId,
            logHelper.getEnvironment(),
            saveTaskLogDto.batchProcessName(),
            saveTaskLogDto.httpMethod(),
            saveTaskLogDto.url(),
            saveTaskLogDto.requestHeaders(),
            saveTaskLogDto.requestBody(),
            saveTaskLogDto.responseStatusCode(),
            saveTaskLogDto.responseHeaders(),
            saveTaskLogDto.responseBody(),
            timeTaken,
            logHelper.getCreatedAt(saveTaskLogDto.endTime())
        );

        ExecutionTimeLog executionTimeLog = ExecutionTimeLog.of(
            taskId,
            saveTaskLogDto.stepId(),
            logHelper.getEnvironment(),
            BatchProcessType.TASK,
            saveTaskLogDto.batchProcessName(),
            saveTaskLogDto.status(),
            saveTaskLogDto.statusReason(),
            taskDetailLogId,
            timeTaken,
            logHelper.getCreatedAt(saveTaskLogDto.endTime())
        );

        // TODO : 예외가 발생할 때만, 해야되긴하는데....
        String githubApiDetailLogFileName = "githubApiDetailLog_"+ githubApiLog.id();

        String githubApiExecutionTimeLogFileName = "githubApiExecutionTimeLog_"+ executionTimeLog.id();

        List<JsonFile> jsonFileList = createGithubApiJsonFiles(githubApiLog, executionTimeLog);





        try {

            logGoogleSheetsRepository.saveGithubApiLogsWithTx(githubApiLog, executionTimeLog);



            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.SUCCESS,
                createGithubApiLogsSaveSuccessMessage(taskId),
                null
            ));

//            batchProcessResultStack.add(createGithubApiLogsSaveSuccessMessage(taskId));


        } catch (SaveDetailLogException exception) { // 롤백 필요 없음


            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createGithubApiLogsSaveDetailLogFailureMessage(
                    exception,
                    githubApiDetailLogFileName,
                    githubApiExecutionTimeLogFileName
                ),
                jsonFileList
            ));

//            batchProcessResultStack.add(createGithubApiLogsSaveDetailLogFailureMessage(
//                exception,
//                githubApiLog,
//                executionTimeLog
//            ));

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우


            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createGithubApiLogsSaveRollbackSuccessMessage(
                    exception,
                    githubApiDetailLogFileName,
                    githubApiExecutionTimeLogFileName,
                    exception.getGoogleSheetsRollbackRange()
                ),
                jsonFileList
            ));

//            batchProcessResultStack.add(createGithubApiLogsSaveRollbackSuccessMessage(
//                exception,
//                githubApiLog,
//                executionTimeLog,
//                exception.getGoogleSheetsRollbackRange()
//            ));

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우


            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createGithubApiLogsSaveRollbackFailureMessage(
                    exception,
                    githubApiDetailLogFileName,
                    githubApiExecutionTimeLogFileName,
                    exception.getGoogleSheetsRollbackRange()
                ),
                jsonFileList
            ));

//            batchProcessResultStack.add(createGithubApiLogsSaveRollbackFailureMessage(
//                exception,
//                githubApiLog,
//                executionTimeLog,
//                exception.getGoogleSheetsRollbackRange()
//            ));

        } catch (Exception exception) {

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createGithubApiLogsSaveUnKnownFailureMessage(
                    exception,
                    githubApiDetailLogFileName,
                    githubApiExecutionTimeLogFileName
                ),
                jsonFileList
            ));



//            batchProcessResultStack.add(createGithubApiLogsSaveUnKnownFailureMessage(
//                exception,
//                githubApiLog,
//                executionTimeLog
//            ));
        }
    }


    private List<JsonFile> createGithubApiJsonFiles(GithubApiLog githubApiLog, ExecutionTimeLog executionTimeLog) {
        List<JsonFile> jsonFileList = new ArrayList<>();

        String stepDetailLogFileName = "githubApiDetailLog_"+ githubApiLog.id();

        JsonFile stepDetailLogJsonFile = new JsonFile(stepDetailLogFileName, createJsonResourceFromObject(stepDetailLogFileName, githubApiLog));

        jsonFileList.add(stepDetailLogJsonFile);

        String stepExecutionTimeLogFileName = "githubApiExecutionTimeLog_"+ executionTimeLog.id();

        ByteArrayResource stepExecutionTimeLogJsonResource = createJsonResourceFromObject(stepExecutionTimeLogFileName, executionTimeLog);

        JsonFile stepExecutionTimeLogJsonFile = new JsonFile(stepExecutionTimeLogFileName, stepExecutionTimeLogJsonResource);

        jsonFileList.add(stepExecutionTimeLogJsonFile);

        return jsonFileList;
    }
}
