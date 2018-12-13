package smokefree.projection;

import io.micronaut.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import smokefree.domain.CitizenJoinedInitiativeEvent;

import javax.inject.Singleton;
import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;

@Slf4j
@Singleton
public class ProfileProjection {
    private final Map<String, Profile> profiles = newConcurrentMap();

    @EventHandler
    public void on(CitizenJoinedInitiativeEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
        final String userId = (String) metaData.get("user_id");
        final String userName = (String) metaData.get("user_name");

        if (StringUtils.isEmpty(userId)) {
            log.info("User ID not available, ignoring...");
            return;
        }
        if (StringUtils.isEmpty(userName)) {
            log.info("User name not available, ignoring...");
            return;
        }

        profiles.put(userId, new Profile(userId, userName));
    }

    public Profile profile(String id) {
        return profiles.get(id);
    }
}