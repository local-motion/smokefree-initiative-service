package smokefree.projection;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import smokefree.domain.GeoLocation;
import smokefree.domain.InitiativeCreatedEvent;
import smokefree.domain.CitizenJoinedInitiativeEvent;
import smokefree.domain.Status;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableCollection;

@Slf4j
@Singleton
public class InitiativeProjection {
    private final Map<String, Playground> playgrounds = newHashMap();
    private final Progress progress = new Progress();

    @EventHandler
    public void on(InitiativeCreatedEvent evt) {
        log.info("ON EVENT {}", evt);
        final GeoLocation geoLocation = evt.getGeoLocation();
        if (playgrounds.containsKey(evt.getInitiativeId())) {
            log.warn("Received initiative creation for {} {} multiple times", evt.getInitiativeId(), evt.getName());
            return;
        }
        playgrounds.put(evt.getInitiativeId(), new Playground(
                evt.getInitiativeId(),
                evt.getName(),
                geoLocation.getLat(),
                geoLocation.getLng(),
                evt.getStatus(),
                0,
                0));

        if (Status.finished == evt.getStatus()) {
            progress.incrementSmokeFree();
        } else if (Status.in_progress == evt.getStatus()) {
            progress.incrementWorkingOnIt();
        } else {
            progress.incrementSmoking();
        }
    }

    @EventHandler
    public void on(CitizenJoinedInitiativeEvent evt) {
        log.info("ON EVENT {}", evt);
        final Playground playground = playgrounds.get(evt.getInitiativeId());
        final Playground updatedPlayground = playground.withVolunteerCount(playground.getVolunteerCount() + 1);
        playgrounds.put(evt.getInitiativeId(), updatedPlayground);
    }

    public Collection<Playground> playgrounds() {
        return unmodifiableCollection(playgrounds.values());
    }

    public Playground playground(String id) {
        return playgrounds.get(id);
    }

    public Progress progress() {
        return progress;
    }
}
