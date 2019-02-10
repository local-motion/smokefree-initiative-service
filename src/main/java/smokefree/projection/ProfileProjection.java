package smokefree.projection;

import io.micronaut.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;
import smokefree.domain.CitizenJoinedInitiativeEvent;
import smokefree.domain.UserCreatedEvent;

import javax.inject.Singleton;
import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;

@Slf4j
@Singleton
public class ProfileProjection {


    /*
        TODO: Note the current implementation of this profile is no longer correct as it requires users to be volunteers (=joined a playground community)
        which does not have to be the case.
        At the moment this profile is not used (user info is fetched from the jwt token), but in the future this projection will be restored to use, when
        user created events can be picked up (instead of the current initiative joined events)
     */


    private final Map<String, Profile> profilesById = newConcurrentMap();
    private final Map<String, Profile> profilesByName = newConcurrentMap();

    @EventHandler
    public void on(UserCreatedEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
//        final String userId = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID);
//        final String userName = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME);
//
//        if (StringUtils.isEmpty(userId)) {
//            log.info("User ID not available, ignoring...");
//            return;
//        }
//        if (StringUtils.isEmpty(userName)) {
//            log.info("User name not available, ignoring...");
//            return;
//        }
        Profile profile = new Profile(evt.getUserId(), evt.getName(), evt.getEmailAddress());
        profilesById.put(evt.getUserId(), profile);
        profilesByName.put(evt.getName(), profile);
    }

    public Profile profile(String id) {
        return profilesById.get(id);
    }
}