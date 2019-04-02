package smokefree.domain;

import lombok.*;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;

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

    @NotBlank(message = "Message must have at least " + SmokefreeConstants.PlaygroundObservation.MINIMUM_COMMENT_LENGTH + " character")
    @Pattern(regexp = "^[A-Za-z0-9\\n!@&(),.?\": ]+$", message = "Please enter only allowed special charaxters: @&(),.?\": ")
    @Size(max = SmokefreeConstants.PlaygroundObservation.MAXIMUM_COMMENT_LENGTH, message = "Message length must not exceed {max} characters")
    String comment;

}
