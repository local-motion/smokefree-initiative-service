package smokefree.projection;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import personaldata.PersonalDataRecord;
import personaldata.PersonalDataRepository;
import smokefree.domain.UserCreatedEvent;
import smokefree.domain.UserDeletedEvent;
import smokefree.domain.UserPII;
import smokefree.domain.UserRevivedEvent;

import javax.inject.Singleton;
import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;

@Slf4j
@Singleton
public class ProfileProjection {

    private final Map<String, Profile> profilesById = newConcurrentMap();
    private final Map<String, Profile> profilesByName = newConcurrentMap();

    private final Map<String, Profile> deletedProfilesById = newConcurrentMap();


    /*
            Event handlers
     */

    @EventHandler
    public void on(UserCreatedEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);

        Profile profile;
        if (evt.getPiiRecordId() != 0) {
            PersonalDataRecord personalDataRecord = PersonalDataRepository.getInstance().getRecord(evt.getPiiRecordId());
            Gson gson = new Gson();
            UserPII userPII = gson.fromJson(personalDataRecord.getData(), UserPII.class);
            profile = new Profile(evt.getUserId(), userPII.getName(), userPII.getEmailAddress());
            log.info("User profile retrieved pii record " + evt.getPiiRecordId() + " for " + evt.getUserId() + " with data " + userPII);
        }
        else {
            profile = new Profile(evt.getUserId(), evt.getName(), evt.getEmailAddress());
        }

        profilesById.put(profile.getId(), profile);
        profilesByName.put(profile.getUsername(), profile);
    }

    @EventSourcingHandler
    void on(UserRevivedEvent evt) {
        Profile profile = deletedProfilesById.get(evt.getUserId());
        profilesById.put(profile.getId(), profile);
        profilesByName.put(profile.getUsername(), profile);
    }

    @EventSourcingHandler
    void on(UserDeletedEvent evt) {
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

    public Profile getDeletedProfile(String id) {
        return deletedProfilesById.get(id);
    }
}