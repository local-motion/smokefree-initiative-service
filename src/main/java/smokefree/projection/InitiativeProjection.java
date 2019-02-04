package smokefree.projection;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import smokefree.domain.*;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;
import static java.util.Collections.unmodifiableCollection;

@Slf4j
@Singleton
public class InitiativeProjection {
    private final Map<String, Playground> playgrounds = newConcurrentMap();
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
                null,
                0,
                0,
                new Playground.PlaygroundObservations()));

        progress.increment(evt.getStatus());
    }

    @EventHandler
    public void on(CitizenJoinedInitiativeEvent evt) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setVolunteerCount(playground.getVolunteerCount() + 1);
    }

    @EventHandler
    public void on(InitiativeProgressedEvent evt) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setStatus(evt.getAfter());

        progress.change(evt.getBefore(), evt.getAfter());
    }

    @EventHandler
    public void on(SmokeFreeDateCommittedEvent evt) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setSmokeFreeDate(evt.getSmokeFreeDate());
    }

    @EventHandler
    public void on(ManagerJoinedInitiativeEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
        final String userId = (String) metaData.get("user_id");                 // TODO should this data not be extracted from the event itself?
        final String userName = (String) metaData.get("user_name");             // TODO should this data not be extracted from the event itself?

        Playground playground = playgrounds.get(evt.getInitiativeId());
        Playground.Manager manager = new Playground.Manager(userId, userName);
        playground.addManager(manager);
    }

    @EventHandler
    public void on(SmokeFreePlaygroundObservationRecordedEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
        final String userId = (String) metaData.get("user_id");                 // TODO should this data not be extracted from the event itself?
        final String userName = (String) metaData.get("user_name");
        Playground.PlaygroundObservations playgroundObservations = playgrounds.get(evt.getInitiativeId()).getPlaygroundObservations();
        if(evt.getIsSmokeFree()) {
            playgroundObservations.smokefreeObservationsCount += 1;
            playgroundObservations.smokefreeConsecutiveStreak += 1;
        } else {
            playgroundObservations.smokefreeConsecutiveStreak = 0;
            playgroundObservations.smokeObservationCount += 1;
        }
        Playground.Observation observation = new Playground.Observation(evt.getCitizenId(), metaData.get("user_name").toString(), evt.getIsSmokeFree(), evt.getObservationDate(), evt.getRecordObservation() );
        playgroundObservations.getVolunteersObservations().add(observation);
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

    public Playground.PlaygroundObservations validation(String id) {
        return playgrounds.get(id).getPlaygroundObservations();
    }
}
