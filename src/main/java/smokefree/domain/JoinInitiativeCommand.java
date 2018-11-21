package smokefree.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinInitiativeCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    @NotBlank
    String citizenId;
}
