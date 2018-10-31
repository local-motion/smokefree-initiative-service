package smokefree.projection;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.annotation.MessageHandler;
import smokefree.domain.GeoLocation;
import smokefree.domain.InitiativeCreatedEvent;
import smokefree.domain.InitiativeJoinedEvent;
import smokefree.domain.Status;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;

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
                evt.getStatus()));

        if (Status.finished == evt.getStatus()) {
            progress.incrementSmokeFree();
        } else if (Status.in_progress == evt.getStatus()) {
            progress.incrementWorkingOnIt();
        } else {
            progress.incrementSmoking();
        }
    }

    @EventHandler
    public void on(InitiativeJoinedEvent evt) {
        log.info("ON EVENT {}", evt);
    }

    public Collection<Playground> playgrounds() {
        return unmodifiableCollection(playgrounds.values());
    }

    public Progress progress() {
        return progress;
    }
}
