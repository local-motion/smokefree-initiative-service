package io.localmotion.security.user;

import io.localmotion.eventsourcing.tracker.TrackerProjection;
import io.localmotion.user.domain.ProfileStatus;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationException;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class SecurityContextFactory {

    private final List<AuthenticationProvider> authenticationProviders = List.of(new CognitoAuthenticationProvider());

    @Inject
    ProfileProjection profileProjection;

    @Inject
    private TrackerProjection trackerProjection;

    public SecurityContext createSecurityContext(Authentication authentication) {

        // Return an empty security context for unauthenticated users
        if (authentication == null)
            return new SecurityContext();

        // Find the appropriate authentication provider
        Optional<AuthenticationProvider> authenticationProvider = authenticationProviders.stream().filter(provider -> provider.appliesTo(authentication)).findFirst();
        if (!authenticationProvider.isPresent())
            throw new AuthenticationException("Authentication context cannot be handled by any authentication provider");

        // Check whether the projections are up-to-date
        if (!trackerProjection.isUpToDate())
            return new SecurityContext(authentication, ProfileStatus.UNDETERMINED, null, null);

        // Try to retrieve the profile
        Profile profile = authenticationProvider.get().getProfile(authentication, profileProjection);
        if (profile != null) {
            // Active profile found, test whether any of the attributes has changed
            String providedUserName = authenticationProvider.get().getUserName(authentication);
            if (!profile.getUsername().equals(providedUserName)) {
                log.info("User name changed in active profile.");
                return new SecurityContext(authentication, ProfileStatus.ACTIVE_USER_NAME_CHANGED, profile, providedUserName);
            }
            else
                return new SecurityContext(authentication, ProfileStatus.ACTIVE, profile, null);
        }

        // Try to retrieve a deleted profile
        profile = authenticationProvider.get().getDeletedProfile(authentication, profileProjection);
        if (profile != null) {
            // Deleted profile found, test whether any of the attributes has changed
            String providedUserName = authenticationProvider.get().getUserName(authentication);
            if (!profile.getUsername().equals(providedUserName)) {
                log.info("User accessing deleted profile with new user name");
                return new SecurityContext(authentication, ProfileStatus.DELETED_USER_NAME_CHANGED, profile, providedUserName);
            }
            else {
                log.info("User accessing deleted profile");
                return new SecurityContext(authentication, ProfileStatus.DELETED, profile, null);
            }
        }

        // Create a new profile (to be used for create a user)
        profile = authenticationProvider.get().createProfile(authentication);
        return new SecurityContext(authentication, ProfileStatus.NEW, profile, null);
    }

}
