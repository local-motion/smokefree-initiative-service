package io.localmotion.smokefreeplaygrounds.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecideToBecomeSmokeFreeCommand {
    @TargetAggregateIdentifier
    String initiativeId;
}
