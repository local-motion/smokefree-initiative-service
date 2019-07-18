package io.localmotion.user.controller;

import io.localmotion.user.domain.ProfileStatus;
import io.localmotion.user.projection.Profile;
import lombok.Value;

@Value
public class ProfileResponse {
    private ProfileStatus profileStatus;
    private Profile profile;
    private String newUserName;
}
