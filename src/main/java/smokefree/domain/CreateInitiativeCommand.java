package smokefree.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateInitiativeCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    String name;
    Type type;
    Status status;
    GeoLocation geoLocation;
}
