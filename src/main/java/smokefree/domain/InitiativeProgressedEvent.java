package smokefree.domain;

import lombok.*;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InitiativeProgressedEvent {
    String initiativeId;
    Status before;
    Status after;
}
