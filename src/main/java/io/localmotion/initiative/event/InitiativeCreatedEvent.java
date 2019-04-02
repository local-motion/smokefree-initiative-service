package io.localmotion.initiative.event;

import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class InitiativeCreatedEvent {
    String initiativeId;
    String name;
    GeoLocation geoLocation;
}
