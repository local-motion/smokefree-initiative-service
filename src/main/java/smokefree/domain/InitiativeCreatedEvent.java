package smokefree.domain;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class InitiativeCreatedEvent {
    String initiativeId;
    Type type;
    Status status;
    String name;
    GeoLocation geoLocation;
}
