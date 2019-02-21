package smokefree.projection;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.joda.time.DateTime;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;
import smokefree.domain.*;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newConcurrentMap;
import static java.util.Collections.unmodifiableCollection;

@Slf4j
@Singleton
public class InitiativeProjection {
    private final Map<String, Playground> playgrounds = newConcurrentMap();
    private final Progress progress = new Progress();


    /*
            Event handlers
     */

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
                null,
                0,
                0));

        progress.increment(evt.getStatus());
    }

    @EventHandler
    public void on(CitizenJoinedInitiativeEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setVolunteerCount(playground.getVolunteerCount() + 1);
        log.info("user: " + metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME));
        playground.getVolunteers().add(new Playground.Volunteer(evt.getCitizenId(), metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME).toString()));
    }

    @EventHandler
    public void on(InitiativeProgressedEvent evt) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setStatus(evt.getAfter());

        progress.change(evt.getBefore(), evt.getAfter());
    }

    @EventHandler
    public void on(SmokeFreeDateCommittedEvent evt, EventMessage<SmokeFreeDateCommittedEvent> msg)  {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setSmokeFreeDate(evt.getSmokeFreeDate());
    }

    @EventHandler
    public void on(ManagerJoinedInitiativeEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
        final String userId = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID);                 // TODO should this data not be extracted from the event itself?
        final String userName = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME);             // TODO should this data not be extracted from the event itself?

        Playground playground = playgrounds.get(evt.getInitiativeId());
        Playground.Manager manager = new Playground.Manager(userId, userName);
        playground.addManager(manager);
    }

    @EventHandler
    public void on(PlaygroundObservationIndicatedEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
        final String userId = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID);
        final String userName = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME);
        Playground.PlaygroundObservation playgroundObservation = new Playground.PlaygroundObservation(evt.getObserver(), metaData.get(SmokefreeConstants.JWTClaimSet.COGNITO_USER_NAME).toString(), evt.getSmokefree(), evt.getObservationDate(), evt.getComment());
        playgrounds.get(evt.getInitiativeId()).addPlaygroundObservation(playgroundObservation);
    }

    @EventSourcingHandler
    void on(CheckListUpdateEvent evt) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setChecklistItem(evt.getActor(), evt.getChecklistItem(), evt.isChecked());
        Playground exPlayground = playground.getPlaygroundForUser(null);
    }


    /*
            Serving the projections
     */

    public Collection<Playground> playgrounds(String userId) {
        return playgrounds.values().stream().map(playground -> playground.getPlaygroundForUser(userId)).collect(Collectors.toList());
    }

    public Playground playground(String id, String userId) {
        Playground playground = playgrounds.containsKey(id) ? playgrounds.get(id).getPlaygroundForUser(userId) : null;
        return playground;
    }

    public Progress progress() {
        return progress;
    }

}
