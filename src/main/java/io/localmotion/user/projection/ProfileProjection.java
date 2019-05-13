package io.localmotion.user.projection;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import io.localmotion.personaldata.PersonalDataRecord;
import io.localmotion.personaldata.PersonalDataRepository;
import io.localmotion.application.Application;
import io.localmotion.user.event.UserCreatedEvent;
import io.localmotion.user.event.UserDeletedEvent;
import io.localmotion.user.domain.UserPII;
import io.localmotion.user.event.UserRevivedEvent;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;

@Slf4j
@Singleton
public class ProfileProjection {

    public static final String PROPERTY_REMOVED = "removed";

    private final Map<String, Profile> profilesById = newConcurrentMap();
    private final Map<String, Profile> profilesByName = newConcurrentMap();

    private final Map<String, Profile> deletedProfilesById = newConcurrentMap();

    // 'Injecting' using the application context
    private PersonalDataRepository personalDataRepository = Application.getApplicationContext().getBean(PersonalDataRepository.class);


    /*
            Event handlers
     */

    @EventHandler
    public void on(UserCreatedEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);

        Profile profile;
        if (evt.getPiiRecordId() != 0) {
            PersonalDataRecord personalDataRecord = personalDataRepository.getRecord(evt.getPiiRecordId());
            Gson gson = new Gson();
            UserPII userPII = gson.fromJson(personalDataRecord.getData(), UserPII.class);
            profile = new Profile(evt.getUserId(), userPII.getName(), userPII.getEmailAddress());
            log.info("User profile retrieved pii record " + evt.getPiiRecordId() + " for " + evt.getUserId() + " with data " + userPII);
        }
        else {
            profile = new Profile(evt.getUserId(), "PROPERTY_REMOVED", "PROPERTY_REMOVED");
        }

        profilesById.put(profile.getId(), profile);
        profilesByName.put(profile.getUsername(), profile);
    }

    @EventSourcingHandler
    void on(UserRevivedEvent evt) {
        log.info("ON EVENT {}", evt);
        Profile profile = deletedProfilesById.get(evt.getUserId());
        profilesById.put(profile.getId(), profile);
        profilesByName.put(profile.getUsername(), profile);
    }

    @EventSourcingHandler
    void on(UserDeletedEvent evt) {
        log.info("ON EVENT {}", evt);
        Profile userProfile = profilesById.get(evt.getUserId());
        profilesByName.remove(userProfile.getUsername());
        profilesById.remove(evt.getUserId());
        deletedProfilesById.put(userProfile.getId(), userProfile);
    }

    /*
            Retrieval methods
     */

    public Profile profile(String id) {
        return profilesById.get(id);
    }

    public Profile getProfileByName(String username) {
        return profilesByName.get(username);
    }

    public Profile getDeletedProfile(String id) {
        return deletedProfilesById.get(id);
    }

    public Collection<Profile> getAllProfiles() {
        return profilesById.values();
    }

    public boolean emailExists(String emailAddress) {
        return profilesById.values().stream().anyMatch(profile -> profile.getEmailAddress().equals(emailAddress));
    }
}