package io.localmotion.smokefreeplaygrounds.command;

import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePlaygroundInitiativeCommand {
    @TargetAggregateIdentifier
    String initiativeId;

    @Length(min = 3, message = "The name must be at least 3 characters")
    @Length(max = 40, message = "The name must be less than 40 characters")
    @Pattern(regexp="^([a-zA-Z0-9&'!\\-] ?)*[a-zA-Z0-9&'!\\-]$") // only the middle characters may be a space and no consecutive spaces
    @NotBlank
    String name;
    @NotNull
    CreationStatus creationStatus;
    GeoLocation geoLocation;
}
