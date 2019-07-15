package io.localmotion.security.user;

import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.security.authentication.Authentication;

public class CognitoAuthenticationProvider implements AuthenticationProvider {
    @Override
    public String getProviderName() {
        return "Cognito";
    }

    @Override
    public boolean appliesTo(Authentication authentication) {
        String issuer =  (String) authentication.getAttributes().get("iss");
        return issuer != null && issuer.startsWith("https://cognito-idp.");
    }

    @Override
    public String getUserId(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }

    @Override
    public String getUserName(Authentication authentication) {
        return authentication != null ? (String) authentication.getAttributes().get(SmokefreeConstants.JWTClaimSet.COGNITO_USER_NAME) : null;
    }

    @Override
    public String getUserEmailAddress(Authentication authentication) {
        return authentication != null ? (String) authentication.getAttributes().get(SmokefreeConstants.JWTClaimSet.EMAIL_ADDRESS) : null;
    }

    @Override
    public Profile getProfile(Authentication authentication, ProfileProjection profileProjection) {
        return authentication != null ? profileProjection.getProfileByEmailAddress(getUserEmailAddress(authentication)) : null;
    }

    @Override
    public Profile getDeleteProfile(Authentication authentication, ProfileProjection profileProjection) {
        return authentication != null ? profileProjection.getDeletedProfileByEmailAddress(getUserEmailAddress(authentication)) : null;
    }
}
