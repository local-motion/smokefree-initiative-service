package io.localmotion.user.projection;

import com.google.gson.Gson;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.user.domain.NotificationLevel;
import io.localmotion.user.event.*;
import io.micronaut.context.annotation.Context;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import io.localmotion.personaldata.PersonalDataRecord;
import io.localmotion.personaldata.PersonalDataRepository;
import io.localmotion.user.domain.UserPII;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;

@Slf4j
@Context
public class ProfileProjection {

    public static final String PROPERTY_REMOVED = "removed";

    private final ProfileStore activeProfiles = new ProfileStore();
    private final ProfileStore deletedProfiles = new ProfileStore();


    @Inject
    PersonalDataRepository personalDataRepository;


    /*
            Event handlers
     */

    @EventHandler
    public void on(UserCreatedEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);

        Profile profile;
        PersonalDataRecord personalDataRecord = personalDataRepository.getRecord(evt.getPiiRecordId());
        if (personalDataRecord != null) {
            UserPII userPII = new Gson().fromJson(personalDataRecord.getData(), UserPII.class);
            profile = new Profile(evt.getUserId(), userPII.getName(), userPII.getEmailAddress(), NotificationLevel.NONE, new HashSet<>());
            log.info("User profile retrieved pii record " + evt.getPiiRecordId() + " for " + evt.getUserId() + " with data " + userPII);
        }
        else {
            profile = new Profile(evt.getUserId(), "*PROPERTY_REMOVED*", "*PROPERTY_REMOVED*", NotificationLevel.NONE, new HashSet<>());
        }

        activeProfiles.put(profile);
    }

    @EventHandler
    void on(UserRevivedEvent evt) {
        log.info("ON EVENT {}", evt);
        Profile profile = deletedProfiles.getById(evt.getUserId());
        if (profile.getUsername() != null) {

            deletedProfiles.remove(profile.getId());
            activeProfiles.put(updateProfileFromPersonalData(profile, evt.getPiiRecordId()));
        }
        else
            log.warn("Trying to revive user with personal data removed, Ignoring...");
    }

    @EventHandler
    void on(UserDeletedEvent evt) {
        log.info("ON EVENT {}", evt);

        Profile profile = activeProfiles.getById(evt.getUserId());
        activeProfiles.remove(profile.getId());
        deletedProfiles.put(profile);
    }

    @EventHandler
    void on(PersonalDataDeletedEvent evt) {
        log.info("ON EVENT {}", evt);
        deletedProfiles.put(new Profile(evt.getUserId(), null, null, null, null));
    }

    @EventHandler
    void on(UserRenamedEvent evt) {
        log.info("ON EVENT {}", evt);
        Profile profile = activeProfiles.getById(evt.getUserId());
        if (profile.getUsername() != null) {
            activeProfiles.put(updateProfileFromPersonalData(profile, evt.getPiiRecordId()));
        }
        else
            log.warn("Trying to rename user with personal data removed, Ignoring...");
    }

    @EventHandler
    void on(NotificationSettingsUpdatedEvent evt) {
        log.info("ON EVENT {}", evt);
        Profile userProfile = activeProfiles.getById(evt.getUserId());
        if (userProfile == null)
            log.warn("Ignoring event because user profile not present: {}", evt);
        else {
            Profile newUserProfile = userProfile.withNotificationLevel(evt.getNotificationLevel());
            activeProfiles.put(newUserProfile);
        }
    }

    @EventHandler
    void on(MemberJoinedInitiativeEvent evt) {
        log.info("ON EVENT {}", evt);
        Profile userProfile = activeProfiles.getById(evt.getMemberId());
        if (userProfile == null)
            log.warn("Ignoring event because user profile not present: {}", evt);
        else {
            userProfile.getInitiativeMemberships().add(evt.getInitiativeId());
        }
    }


    /*
            Retrieval methods
     */

    public Profile profile(String id) {
        return activeProfiles.getById(id);
    }

    public Profile getProfileByName(String username) {
        return activeProfiles.getByName(username);
    }

    public Profile getProfileByEmailAddress(String emailAddress) {
        return activeProfiles.getByEmailAddress(emailAddress);
    }

    public Profile getDeletedProfile(String id) {
        return deletedProfiles.getById(id);
    }

    public Profile getDeletedProfileByEmailAddress(String emailAddress) {
        return deletedProfiles.getByEmailAddress(emailAddress);
    }

    public Collection<Profile> getAllProfiles() {
        return activeProfiles.getAllProfiles();
    }

    public boolean emailExists(String emailAddress) {
        return activeProfiles.getByEmailAddress(emailAddress) != null;
    }


    /*
            PII support methods
     */

    private Profile updateProfileFromPersonalData(Profile profile, Long recordId) {
        Profile newProfile = profile;
        if (recordId != null) {
            PersonalDataRecord personalDataRecord = personalDataRepository.getRecord(recordId);
            if (personalDataRecord != null) {
                UserPII userPII = new Gson().fromJson(personalDataRecord.getData(), UserPII.class);
                if (userPII.getName() != null)
                    newProfile = newProfile.withUsername(userPII.getName());
                if (userPII.getEmailAddress() != null)
                    newProfile = newProfile.withEmailAddress(userPII.getEmailAddress());
            }
        }
        return newProfile;
    }

}