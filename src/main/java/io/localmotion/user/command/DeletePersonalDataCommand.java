package io.localmotion.user.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeletePersonalDataCommand {
    @TargetAggregateIdentifier
    String userId;
}
