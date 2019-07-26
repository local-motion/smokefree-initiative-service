package io.localmotion.security.user;

import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.user.domain.ProfileStatus;
import io.localmotion.user.projection.Profile;
import io.micronaut.core.util.StringUtils;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationException;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class SecurityContext {
    private final Authentication authentication;
    private final ProfileStatus profileStatus;
    private final Profile profile;
    private final String newUserName;

    private AdminJobCommandRecord adminCommand = null;

    public SecurityContext() {
        this(null, ProfileStatus.NONE, null, null);
    }

    public SecurityContext(Authentication authentication) {
        this(authentication, ProfileStatus.NONE, null, null);
    }

    public SecurityContext(Authentication authentication, ProfileStatus profileStatus, Profile profile, String newUserName) {
        this.authentication = authentication;
        this.profileStatus = profileStatus;
        this.profile = profile;
        this.newUserName = newUserName;
    }

    public Authentication authentication() {
        return authentication;
    }


    public void setAdminCommand(AdminJobCommandRecord adminJobCommandRecord) {
        adminCommand = adminJobCommandRecord;
    }

    public boolean isAuthenticated() {
        return profileStatus == ProfileStatus.ACTIVE;
    }

    public @Nullable
    String userId() {
        return profile != null ? profile.getId() : null;
    }

    public String requireUserId() {
        if (!isAuthenticated())
            throw new AuthenticationException("Not logged in");
        return profile.getId();
    }

    public String requireUserName() {
        if (!isAuthenticated())
            throw new AuthenticationException("Not logged in");
        return profile.getUsername();
    }

    public String emailId() {
        if (!isAuthenticated())
            throw new AuthenticationException("Not logged in");
        return profile.getEmailAddress();
    }

}
