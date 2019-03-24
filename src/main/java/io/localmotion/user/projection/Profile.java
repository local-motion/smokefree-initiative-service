package io.localmotion.user.projection;

import lombok.Value;

@Value
public class Profile {
    String id;
    String username;
    String emailAddress;
}
