package io.localmotion.smokefreeplaygrounds.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ClaimManagerRoleCommand {
    @TargetAggregateIdentifier
    String initiativeId;
}
