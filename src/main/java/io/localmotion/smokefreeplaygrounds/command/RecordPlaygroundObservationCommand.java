package io.localmotion.smokefreeplaygrounds.command;

import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.*;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class RecordPlaygroundObservationCommand {
    @TargetAggregateIdentifier
    String initiativeId;
    String observer;
    Boolean smokefree;

    //@NotBlank(message = "Message must have at least " + SmokefreeConstants.PlaygroundObservation.MINIMUM_COMMENT_LENGTH + " character")
    // TO-DO yet to decide on a allowed characters
    //@Pattern(regexp = "^[A-Za-z0-9\\n!@&(),.?\": ]+$", message = "Please enter only allowed special charaxters: @&(),.?\": ")
    @Size(max = SmokefreeConstants.PlaygroundObservation.MAXIMUM_COMMENT_LENGTH, message = "Message length must not exceed {max} characters")
    String comment;

}
