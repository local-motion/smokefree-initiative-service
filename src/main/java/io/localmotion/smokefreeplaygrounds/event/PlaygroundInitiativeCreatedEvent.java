package io.localmotion.smokefreeplaygrounds.event;

import io.localmotion.initiative.event.InitiativeCreatedEvent;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class PlaygroundInitiativeCreatedEvent {
    String initiativeId;
    String name;
    CreationStatus creationStatus;
    GeoLocation geoLocation;
}
