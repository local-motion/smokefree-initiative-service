package io.localmotion.security.user;

import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.security.authentication.Authentication;

public interface AuthenticationProvider {

    public String getProviderName();

    public boolean appliesTo(Authentication authentication);

    public String getUserId(Authentication authentication);
    public String getUserName(Authentication authentication);
    public String getUserEmailAddress(Authentication authentication);

    public Profile getProfile(Authentication authentication, ProfileProjection profileProjection);
    public Profile getDeletedProfile(Authentication authentication, ProfileProjection profileProjection);

    public Profile createProfile(Authentication authentication);

}
