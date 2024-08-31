package com.example.review_study_app.scheduler;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;

import com.example.review_study_app.common.utils.MyDateUtils;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * ReviewStudyLocalScheduler 는 Local 용 스케줄링 작업을 수행 책임을 담당하는 클래스이다.
 *
 * < year과 weekNumber를 reviewStudySchedulerFacade 에서 관리할 수 있는데, 여기서 관리하는 이유 >
 * - @scheduled의 cron을 설정할 때, 이번주인지, 다음주인지, 지난주인지에 대해 고려해야 할 때가 있다.
 * - 이럴 때, cron 값에 따라 currentWeekNumber 에 +1, -1 등을 추가해 주어야 한다.
 * - 만약, year과 weekNumber를 reviewStudySchedulerFacade 에서 관리하면, ReviewStudyScheduler 와 reviewStudySchedulerFacade 2개의 파일을 신경써줘야 한다.
 * - 반면, 여기서 관리하면, 1곳에서만 수정 작업을 가능하게 되어, 유지보수성이 더 좋은 것 같다고 생각했다.
 *
 * < ReviewStudyScheduler를 local 용과 prod용으로 분리한 이유 >
 * - local 에서 작업할 때, @scheduled의 cron을 설정을 수정할 때가 있다.
 * - 이때, 수정한 걸 다시 원래 설정으로 바꾸는 걸 계속 까먹거나 번거롭다고 생각해서, 환경별로 파일을 분리했다.
 *
 * < application.yml 과 application-local.yml 파일의 값을 활용해서, 환경별로 분리할 수 있는데, 클래스를 분리한 이유 >
 * - local 작업을 하면서 weekNumber의 값과 @scheduled의 cron의 값을 같이 변경해 볼 때도 있는데, 이때 1곳에서만 변경하도록 하는게 좋을 것 같아서
 */

@Slf4j
@Component
@Profile("local")
public class ReviewStudySchedulerLocalConfig {

    private final ReviewStudySchedulerService reviewStudySchedulerService;

    @Autowired
    public ReviewStudySchedulerLocalConfig(
        ReviewStudySchedulerService reviewStudySchedulerService
    ) {
        this.reviewStudySchedulerService = reviewStudySchedulerService;
    }

//    @Scheduled(fixedRate = 60000)
    public void runCreateNewLabel() {
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);

        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);

        reviewStudySchedulerService.createNewWeekNumberLabel(currentYear, currentWeekNumber);
    }

//    @Scheduled(fixedRate = 60000)
    public void runCreateNewIssue() {
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);

        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);

        reviewStudySchedulerService.createNewWeeklyReviewIssues(currentYear, currentWeekNumber);
    }

//    @Scheduled(fixedRate = 60000)
    public void runCloseIssues() {
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);

        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);

        reviewStudySchedulerService.closeWeeklyReviewIssues(currentYear, currentWeekNumber);
    }
}
