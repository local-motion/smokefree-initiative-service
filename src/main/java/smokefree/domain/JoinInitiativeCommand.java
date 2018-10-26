package smokefree.domain;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class JoinInitiativeCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    String citizenId;
}
