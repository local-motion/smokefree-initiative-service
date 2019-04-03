package io.localmotion.initiative.projection;

import io.localmotion.eventsourcing.axon.EventHandlerPlugin;
import io.localmotion.eventsourcing.axon.MetaDataManager;
import io.localmotion.initiative.event.ChecklistUpdateEvent;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
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

    // for now just register the handler here. In the future there should be a mechanism for the handlers to register themselves
    {
        eventHandlerPlugins.add(new PlaygroundEventHandlerPlugin());
    }

    private final Map<String, Initiative> initiatives = newConcurrentMap();


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
        initiatives.put(initiative.getId(), initiative);
    }

    @EventHandler
    public void on(MemberJoinedInitiativeEvent evt, MetaData metaData, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        Initiative initiative = initiatives.get(evt.getInitiativeId());
        initiative.getMemberIds().add(evt.getMemberId());
        initiative.setLastEventMessage(eventMessage);
    }

    @EventHandler
    void on(ChecklistUpdateEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {} AT {}", evt, eventMessage.getTimestamp());
        String actorId = new MetaDataManager(eventMessage.getMetaData()).getUserId();
        Initiative initiative = initiatives.get(evt.getInitiativeId());
        initiative.setChecklistItem(actorId, evt.getChecklistItem(), evt.isChecked());
        initiative.setLastEventMessage(eventMessage);
    }


    /*
            Serving the projections
     */

    public Collection<Initiative> getInitiatives(String userId) {
        return initiatives.values().stream().map(playground -> playground.getInitiativeForUser(userId)).collect(Collectors.toList());
    }

    public Initiative getInitiative(String id, String userId) {
        Initiative initiative = initiatives.containsKey(id) ? initiatives.get(id).getInitiativeForUser(userId) : null;
        return initiative;
    }

    public Collection<Initiative> getAllInitiatives() {
        return initiatives.values();
    }

}
