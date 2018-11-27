package smokefree.domain;

import lombok.*;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

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
