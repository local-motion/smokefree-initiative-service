package smokefree.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateInitiativeCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    String name;
    @NotNull
    Type type;
    @NotNull
    Status status;
    GeoLocation geoLocation;
}
