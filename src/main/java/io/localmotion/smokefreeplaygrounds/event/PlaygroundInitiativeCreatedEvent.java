package io.localmotion.smokefreeplaygrounds.event;

import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
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
