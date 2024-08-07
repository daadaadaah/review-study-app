package com.example.review_study_app.repository.log.aop;


import com.example.review_study_app.repository.log.LogGoogleSheetsRepository;
import com.example.review_study_app.repository.log.exception.GoogleSheetsRollbackFailureException;
import com.example.review_study_app.repository.log.exception.GoogleSheetsTransactionException;
import java.io.IOException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * GoogleSheetsTransactionAspect 는 Google Sheets 와의 데이터 연동시 트랜잭션 관리를 담당하는 AOP 클래스입니다.
 */

@Aspect
@Component
public class GoogleSheetsTransactionAspect {

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    @Autowired
    public GoogleSheetsTransactionAspect(
        LogGoogleSheetsRepository logGoogleSheetsRepository
    ) {
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
    }

    @Around("@annotation(com.example.review_study_app.repository.log.aop.GoogleSheetsTransactional)")
    public Object handleGoogleSheetsTransaction(ProceedingJoinPoint joinPoint) throws Throwable {

        try {
            return joinPoint.proceed();
        } catch (GoogleSheetsTransactionException googleSheetsTransactionException) {

            String googleSheetsRollbackRange = googleSheetsTransactionException.getGoogleSheetsRollbackRange();

            try {
                rollback(googleSheetsRollbackRange);

                throw googleSheetsTransactionException;
            } catch (Exception rollbackException) {
                throw new GoogleSheetsRollbackFailureException(rollbackException);
            }
        }
    }

    private void rollback(String rollbackRange) throws IOException {
        logGoogleSheetsRepository.remove(rollbackRange);
    }
}
