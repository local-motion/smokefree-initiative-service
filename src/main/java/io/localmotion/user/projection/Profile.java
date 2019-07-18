package io.localmotion.user.projection;

import io.localmotion.user.domain.NotificationLevel;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Value
@Wither
public class Profile {

    public static final NotificationLevel DEFAULT_NOTIFICATION_LEVEL = NotificationLevel.NONE;

    String id;
    String username;
    String emailAddress;
    NotificationLevel notificationLevel;
    Set<String> initiativeMemberships;

//    public Profile(String id, String username, String emailAddress) {
//        this(id, username, emailAddress, NotificationLevel.NONE, new HashSet<>());
//    }
}
