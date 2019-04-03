package io.localmotion.initiative.projection;

import io.localmotion.eventsourcing.axon.EventHandlerPlugin;
import io.localmotion.eventsourcing.axon.MetaDataManager;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
import io.localmotion.initiative.event.ChecklistUpdateEvent;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.event.PlaygroundInitiativeCreatedEvent;
import io.localmotion.smokefreeplaygrounds.projection.PlaygroundEventHandlerPlugin;
import io.localmotion.user.projection.ProfileProjection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.MetaData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newConcurrentMap;



@Slf4j
@Singleton
public class InitiativeProjection {

    @Inject @Getter
    private ProfileProjection profileProjection;


    private final List<EventHandlerPlugin<InitiativeProjection>> eventHandlerPlugins = new ArrayList<>();

    // for now just register the handler here. In the future their should be a mechanism for the handlers to register themselves
    {
        eventHandlerPlugins.add(new PlaygroundEventHandlerPlugin());
    }

    private final Map<String, Initiative> playgrounds = newConcurrentMap();


    /*
            Event handlers
     */

    @EventHandler
    public void on(EventMessage<?> eventMessage) {
        log.info("ON EVENTMESSAGE {}", eventMessage);
        for (EventHandlerPlugin<InitiativeProjection> i : eventHandlerPlugins)
            if (i.handleEventMessage(this, eventMessage))
                return;
    }

    public void onNewInitiative(Initiative initiative) {
        playgrounds.put(initiative.getId(), initiative);
    }

    @EventHandler
    public void on(MemberJoinedInitiativeEvent evt, MetaData metaData, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        Initiative initiative = playgrounds.get(evt.getInitiativeId());
        initiative.getVolunteerIds().add(evt.getMemberId());
        initiative.setLastEventMessage(eventMessage);
    }

    @EventHandler
    void on(ChecklistUpdateEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {} AT {}", evt, eventMessage.getTimestamp());
        String actorId = new MetaDataManager(eventMessage.getMetaData()).getUserId();
        Initiative initiative = playgrounds.get(evt.getInitiativeId());
        initiative.setChecklistItem(actorId, evt.getChecklistItem(), evt.isChecked());
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
