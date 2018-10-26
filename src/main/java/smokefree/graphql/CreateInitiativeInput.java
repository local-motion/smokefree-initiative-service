package smokefree.graphql;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import smokefree.domain.Status;
import smokefree.domain.Type;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public
class CreateInitiativeInput {
    String initiativeId;
    Type type;
    Status status;
    String name;
    Double lat;
    Double lng;
}
