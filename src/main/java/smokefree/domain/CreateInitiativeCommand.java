package smokefree.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInitiativeCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    Type type;
    Status status;
    String name;
    Double lat;
    Double lng;
}
