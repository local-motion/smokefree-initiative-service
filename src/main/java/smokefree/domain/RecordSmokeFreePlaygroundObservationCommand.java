package smokefree.domain;

import lombok.*;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class RecordSmokeFreePlaygroundObservationCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    String citizenId;
    Boolean isSmokeFree;
    String recordObservation;

}
