package io.localmotion.smokefreeplaygrounds.event;

import io.localmotion.initiative.domain.GeoLocation;
import io.localmotion.initiative.domain.Status;
import io.localmotion.initiative.domain.Type;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class PlaygroundInitiativeCreatedEvent {
    String initiativeId;
    Type type;
    Status status;
    String name;
    GeoLocation geoLocation;
}
