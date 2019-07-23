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

//    // After the projection startup has been set, wait for an amount of time. If no event occur in this period
//    // we assume there are no events (empty system) and we consider this projection to be up-to-date.
//    private static final long WAIT_FOR_EVENT_STREAM_AFTER_STARTUP = 10*1000;         // 10 secs
//
//    // If no profile-related events are received after the last event for some time, the projection is considered to be
//    // up-to-date.
//    private static final long EVENT_UPDATE_STREAM_COMPLETE_THRESHOLD = 3*1000;         // 3 secs
//
//    private long projectionStartupTimestamp = 0;
//    private long lastEventReceivedTimestamp = 0;
//    private boolean isUpToDate = false;


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
            activeProfiles.put(evt.getNewUserName() == null ? profile : profile.withUsername(evt.getNewUserName()));
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
            activeProfiles.put(profile.withUsername(evt.getNewUserName()));
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
            activeProfiles.put(userProfile);
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

//    public void markProjectionStartup() {
//        projectionStartupTimestamp = projectionStartupTimestamp > 0 ? projectionStartupTimestamp : System.currentTimeMillis();
//    }
//    private void markEventReceived() {
//        markProjectionStartup();
//        lastEventReceivedTimestamp = System.currentTimeMillis();
//    }
//
//    /**
//     * Based on the handling of incoming events this projection tries to determine whether it is up-to-date, ie whether
//     * all events that were emitted prior to startup of this projection have been handled.
//     * This fact can be used to assess the accuracy of this projection for instance when user mutations need to be validated.
//     * Note that this projection is eventually consistent, so even when this method return true, some events may still
//     * not be processed.
//     * @return whether this projection has processed (nearly) all events
//     */
//    public boolean isUpToDate() {
//        return  projectionStartupTimestamp > 0 &&
//                (System.currentTimeMillis())
//        return lastEventReceivedTimestamp > 0 && (System.currentTimeMillis() - lastEventReceivedTimestamp) > EVENT_UPDATE_STREAM_COMPLETE_THRESHOLD;
//    }

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
}