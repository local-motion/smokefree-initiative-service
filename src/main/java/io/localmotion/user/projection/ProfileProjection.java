package io.localmotion.user.projection;

import com.google.gson.Gson;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.user.domain.NotificationLevel;
import io.localmotion.user.event.*;
import io.micronaut.context.annotation.Context;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import io.localmotion.personaldata.PersonalDataRecord;
import io.localmotion.personaldata.PersonalDataRepository;
import io.localmotion.user.domain.UserPII;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;

@Slf4j
@Context
public class ProfileProjection {

    public static final String PROPERTY_REMOVED = "removed";

//    private final Map<String, Profile> profilesById = newConcurrentMap();
//    private final Map<String, Profile> profilesByName = newConcurrentMap();
//
//    private final Map<String, Profile> deletedProfilesById = newConcurrentMap();

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
//            profile = new Profile(evt.getUserId(), null, null, null, null);
        }

//        profilesById.put(profile.getId(), profile);
//        profilesByName.put(profile.getUsername(), profile);
        activeProfiles.put(profile);
    }

    @EventHandler
    void on(UserRevivedEvent evt) {
        log.info("ON EVENT {}", evt);
//        Profile profile = deletedProfilesById.get(evt.getUserId());
        Profile profile = deletedProfiles.getById(evt.getUserId());
        if (profile.getUsername() != null) {
//            profilesById.put(profile.getId(), profile);
//            profilesByName.put(profile.getUsername(), profile);

            deletedProfiles.remove(profile);
            activeProfiles.put(evt.getUserName() == null ? profile : profile.withUsername(evt.getUserName()));
        }
        else
            log.warn("Trying to revive user with personal data removed, Ignoring...");
    }

    @EventHandler
    void on(UserDeletedEvent evt) {
        log.info("ON EVENT {}", evt);
//        Profile userProfile = profilesById.get(evt.getUserId());
//        profilesByName.remove(userProfile.getUsername());
//        profilesById.remove(evt.getUserId());
//        deletedProfilesById.put(userProfile.getId(), userProfile);

        Profile profile = activeProfiles.getById(evt.getUserId());
        activeProfiles.remove(profile);
        deletedProfiles.put(profile);
    }

    @EventHandler
    void on(PersonalDataDeletedEvent evt) {
        log.info("ON EVENT {}", evt);
//        deletedProfilesById.put(evt.getUserId(), new Profile(evt.getUserId(), null, null, null, null));
        deletedProfiles.put(new Profile(evt.getUserId(), null, null, null, null));
    }

    @EventHandler
    void on(NotificationSettingsUpdatedEvent evt) {
        log.info("ON EVENT {}", evt);
//        Profile userProfile = profilesById.get(evt.getUserId());
        Profile userProfile = activeProfiles.getById(evt.getUserId());
        if (userProfile == null)
            log.warn("Ignoring event because user profile not present: {}", evt);
        else {
            Profile newUserProfile = userProfile.withNotificationLevel(evt.getNotificationLevel());
//            profilesByName.put(newUserProfile.getUsername(), newUserProfile);
//            profilesById.put(newUserProfile.getId(), newUserProfile);
            activeProfiles.put(userProfile);
        }
    }

    @EventHandler
    void on(MemberJoinedInitiativeEvent evt) {
        log.info("ON EVENT {}", evt);
//        Profile userProfile = profilesById.get(evt.getMemberId());
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
//        return profilesById.get(id);
        return activeProfiles.getById(id);
    }

    public Profile getProfileByName(String username) {
//        return profilesByName.get(username);
        return activeProfiles.getByName(username);
    }

    public Profile getProfileByEmailAddress(String emailAddress) {
        return activeProfiles.getByEmailAddress(emailAddress);
    }

    public Profile getDeletedProfile(String id) {
//        return deletedProfilesById.get(id);
        return deletedProfiles.getById(id);
    }

    public Profile getDeletedProfileByEmailAddress(String emailAddress) {
        return deletedProfiles.getByEmailAddress(emailAddress);
    }

    public Collection<Profile> getAllProfiles() {
//        return profilesById.values();
        return activeProfiles.getAllProfiles();
    }

    public boolean emailExists(String emailAddress) {
//        return profilesById.values().stream().anyMatch(profile -> profile.getEmailAddress().equals(emailAddress));
        return activeProfiles.getByEmailAddress(emailAddress) != null;
    }
}