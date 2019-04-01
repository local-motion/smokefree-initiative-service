package io.localmotion.initiative.projection;

import io.localmotion.initiative.domain.GeoLocation;
import io.localmotion.initiative.domain.Status;
import io.localmotion.initiative.event.ChecklistUpdateEvent;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.event.*;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
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
public class InitiativeProjection {

    @Inject
    private ProfileProjection profileProjection;

    private final Map<String, Initiative> playgrounds = newConcurrentMap();


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
        playgrounds.put(evt.getInitiativeId(), new Initiative(
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
        Initiative initiative = playgrounds.get(evt.getInitiativeId());
        initiative.getVolunteers().add(new Initiative.Volunteer(evt.getMemberId(), profileProjection.profile(evt.getMemberId()).getUsername()));
        initiative.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(SmokeFreeDecisionEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        Initiative initiative = playgrounds.get(evt.getInitiativeId());
        Status oldStatus = initiative.getStatus();
        Status newStatus = oldStatus == Status.not_started ? Status.in_progress : oldStatus;
        initiative.setStatus(newStatus);
        initiative.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(SmokeFreeDateCommittedEvent evt, EventMessage<SmokeFreeDateCommittedEvent> eventMessage)  {
        log.info("ON EVENT {}", evt);
        Initiative initiative = playgrounds.get(evt.getInitiativeId());
        initiative.setSmokeFreeDate(evt.getSmokeFreeDate());
        initiative.setStatus(Status.finished);
        initiative.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(ManagerJoinedInitiativeEvent evt, MetaData metaData, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        final String userId = evt.getManagerId();
        final String userName = profileProjection.profile(userId).getUsername();

        Initiative initiative = playgrounds.get(evt.getInitiativeId());
        Initiative.Manager manager = new Initiative.Manager(userId, userName);
        initiative.addManager(manager);

        // Also register the manager as a volunteer
        initiative.getVolunteers().add(new Initiative.Volunteer(userId, userName));

        initiative.setLastEventMessage(eventMessage);
    }

    @EventHandler
    void on(ChecklistUpdateEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {} AT {}", evt, eventMessage.getTimestamp());
        Initiative initiative = playgrounds.get(evt.getInitiativeId());
        initiative.setChecklistItem(evt.getActor(), evt.getChecklistItem(), evt.isChecked());
        initiative.setLastEventMessage(eventMessage);
    }


    /*
            Serving the projections
     */

    public Collection<Initiative> playgrounds(String userId) {
        return playgrounds.values().stream().map(playground -> playground.getPlaygroundForUser(userId)).collect(Collectors.toList());
    }

    public Initiative playground(String id, String userId) {
        Initiative initiative = playgrounds.containsKey(id) ? playgrounds.get(id).getPlaygroundForUser(userId) : null;
        return initiative;
    }

    public Collection<Initiative> getAllPlaygrounds() {
        return playgrounds.values();
    }

}
