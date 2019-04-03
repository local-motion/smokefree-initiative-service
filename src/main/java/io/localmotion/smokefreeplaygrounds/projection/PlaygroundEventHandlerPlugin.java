package io.localmotion.smokefreeplaygrounds.projection;

import io.localmotion.eventsourcing.axon.EventHandlerPlugin;
import io.localmotion.initiative.projection.Initiative;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.event.PlaygroundInitiativeCreatedEvent;
import org.axonframework.eventhandling.EventMessage;

public class PlaygroundEventHandlerPlugin implements EventHandlerPlugin<InitiativeProjection> {

    @Override
    public boolean handleEventMessage(InitiativeProjection target, EventMessage eventMessage) {
        if (eventMessage.getPayload() instanceof PlaygroundInitiativeCreatedEvent) {
            PlaygroundInitiativeCreatedEvent evt = (PlaygroundInitiativeCreatedEvent) eventMessage.getPayload();
            String status = evt.getCreationStatus() == CreationStatus.IMPORT_FINISHED ? "complete" : "initial";
            Initiative initiative = new Initiative(
                    target.getProfileProjection(),
                    evt.getInitiativeId(),
                    evt.getName(),
                    evt.getGeoLocation().getLat(),
                    evt.getGeoLocation().getLng(),
                    status,
                    0,
                    eventMessage);
            target.onNewInitiative(initiative);
            return true;
        }

        return  false;
    }
}
