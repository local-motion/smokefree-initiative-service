package smokefree.domain;

import lombok.*;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class IndicatePlaygroundObservationCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    Boolean smokefree;
    String comment;

}
