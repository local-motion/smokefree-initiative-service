package io.localmotion.user.command;

import io.localmotion.user.domain.NotificationLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SetNotificationPreferencesCommand {
    @TargetAggregateIdentifier
    String userId;
    @NotNull
    NotificationLevel notificationLevel;
}
