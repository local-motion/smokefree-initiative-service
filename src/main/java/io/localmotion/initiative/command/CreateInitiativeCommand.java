package io.localmotion.initiative.command;

import io.localmotion.initiative.domain.Status;
import io.localmotion.initiative.domain.Type;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.hibernate.validator.constraints.SafeHtml;
import io.localmotion.initiative.domain.GeoLocation;

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
