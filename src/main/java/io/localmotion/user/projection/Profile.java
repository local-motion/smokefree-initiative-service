package io.localmotion.user.projection;

import io.localmotion.user.domain.NotificationLevel;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;
import java.util.Set;

@Value
@Wither
public class Profile {
    String id;
    String username;
    String emailAddress;
    NotificationLevel notificationLevel;
    Set<String> initiativeMemberships;
//    List<String> initiativeMemberships;
}
