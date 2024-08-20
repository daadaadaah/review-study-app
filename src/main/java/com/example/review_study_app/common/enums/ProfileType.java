package com.example.review_study_app.common.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ProfileType {
    LOCAL("local"),
    PROD("prod");

    private final String profileName;

    ProfileType(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }

    public static ProfileType fromString(String profileName) {
        for (ProfileType type : ProfileType.values()) {
            if (type.profileName.equalsIgnoreCase(profileName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown profile: " + profileName);
    }

    public static boolean isSupportedProfile(String profileName) {
        return Arrays.stream(ProfileType.values())
            .map(ProfileType::getProfileName)
            .collect(Collectors.toSet())
            .contains(profileName);
    }
}
