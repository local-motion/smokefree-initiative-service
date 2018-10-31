package smokefree.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class InitiativeCreatedEvent {
    String initiativeId;
    Type type;
    Status status;
    String name;
    GeoLocation geoLocation;
}
