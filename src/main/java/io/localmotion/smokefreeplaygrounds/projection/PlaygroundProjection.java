package io.localmotion.smokefreeplaygrounds.projection;

import io.localmotion.eventsourcing.axon.MetaDataManager;
import io.localmotion.initiative.domain.GeoLocation;
import io.localmotion.initiative.domain.Status;
import io.localmotion.initiative.event.ChecklistUpdateEvent;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.event.*;
import io.localmotion.user.projection.ProfileProjection;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.MetaData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newConcurrentMap;


// TODO refactor playground updates to the wither pattern so playground can be immutable and updates atomic (including metadata)

@Slf4j
@Singleton
public class PlaygroundProjection {

    @Inject
    private ProfileProjection profileProjection;

    private final Map<String, Playground> playgrounds = newConcurrentMap();


    /*
            Event handlers
     */

    @EventHandler
    public void on(PlaygroundInitiativeCreatedEvent evt, EventMessage<?> eventMessage) {
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
                eventMessage
                ));
    }

    @EventHandler
    public void on(MemberJoinedInitiativeEvent evt, MetaData metaData, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.getVolunteers().add(new Playground.Volunteer(evt.getMemberId(), profileProjection.profile(evt.getMemberId()).getUsername()));
        playground.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(SmokeFreeDecisionEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        Status oldStatus = playground.getStatus();
        Status newStatus = oldStatus == Status.not_started ? Status.in_progress : oldStatus;
        playground.setStatus(newStatus);
        playground.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(SmokeFreeDateCommittedEvent evt, EventMessage<SmokeFreeDateCommittedEvent> eventMessage)  {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setSmokeFreeDate(evt.getSmokeFreeDate());
        playground.setStatus(Status.finished);
        playground.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(ManagerJoinedInitiativeEvent evt, MetaData metaData, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        final String userId = evt.getManagerId();
        final String userName = profileProjection.profile(userId).getUsername();

        Playground playground = playgrounds.get(evt.getInitiativeId());
        Playground.Manager manager = new Playground.Manager(userId, userName);
        playground.addManager(manager);

        // Also register the manager as a volunteer
        playground.getVolunteers().add(new Playground.Volunteer(userId, userName));

        playground.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(PlaygroundObservationEvent evt, MetaData metaData, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        final String observerId = evt.getObserver();
        final String observerName = profileProjection.profile(observerId).getUsername();
        Playground.PlaygroundObservation playgroundObservation = new Playground.PlaygroundObservation(observerId, observerName, evt.getSmokefree(), evt.getObservationDate(), evt.getComment());
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.addPlaygroundObservation(playgroundObservation);
        playground.setLastEventMessage(eventMessage);

    }

    @EventHandler
    void on(ChecklistUpdateEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {} AT {}", evt, eventMessage.getTimestamp());
        final String userId = new MetaDataManager(eventMessage.getMetaData()).getUserId();
        log.info("CHECKLIST ACTOR: {}", userId);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setChecklistItem(evt.getActor(), evt.getChecklistItem(), evt.isChecked());
        playground.setLastEventMessage(eventMessage);
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

    public Collection<Playground> getAllPlaygrounds() {
        return playgrounds.values();
    }

}
