package com.example.review_study_app.service.notification.factory.message;

import static com.example.review_study_app.domain.ReviewStudyInfo.createLabelUrl;
import static com.example.review_study_app.service.notification.DiscordNotificationService.EMOJI_CONGRATS;
import static com.example.review_study_app.service.notification.DiscordNotificationService.EMOJI_WARING;


public class LabelCreationMessageFactory {

    public static String createNewLabelCreationSuccessMessage(String weekNumberLabelName) {
        return EMOJI_CONGRATS+" 새로운 라벨(["+weekNumberLabelName+"]("+createLabelUrl()+")) 생성이 성공했습니다. "+ EMOJI_CONGRATS;
    }

    public static String createNewLabelCreationFailureMessage(String weekNumberLabelName,
        Exception exception) {
        return EMOJI_WARING+" 새로운 라벨(["+weekNumberLabelName+"]("+createLabelUrl()+")) 생성에 실패했습니다. "+ EMOJI_WARING+" \n"
            + " 에러 메시지 : "+exception.getMessage();
    }
}
