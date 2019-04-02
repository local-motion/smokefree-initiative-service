package io.localmotion.smokefreeplaygrounds.command;

import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
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
public class CreatePlaygroundInitiativeCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @NotBlank
    String name;
    @NotNull
    CreationStatus creationStatus;
    GeoLocation geoLocation;
}
