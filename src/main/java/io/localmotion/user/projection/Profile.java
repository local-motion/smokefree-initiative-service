package io.localmotion.user.projection;

import io.localmotion.user.domain.NotificationLevel;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Wither
public class Profile {
    String id;
    String username;
    String emailAddress;
    NotificationLevel notificationLevel;
}
