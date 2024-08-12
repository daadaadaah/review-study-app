package com.example.review_study_app.service.notification.vo;

import org.springframework.core.io.ByteArrayResource;

public record JsonFile(
    String fileName,
    ByteArrayResource byteArrayResource
) {

}
