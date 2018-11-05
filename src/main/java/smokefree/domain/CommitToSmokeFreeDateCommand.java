package smokefree.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CommitToSmokeFreeDateCommand {
    @TargetAggregateIdentifier
    String initiativeId;

    @NotNull
    LocalDate smokeFreeDate;
}
