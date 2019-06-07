package io.localmotion.user.event;

import io.localmotion.user.domain.NotificationLevel;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class NotificationSettingsUpdatedEvent {
    String userId;
    NotificationLevel newNotificationLevel;
}
