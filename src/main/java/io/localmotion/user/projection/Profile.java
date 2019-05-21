package io.localmotion.user.projection;

import lombok.Setter;
import lombok.Value;

@Value
@Setter
public class Profile {
    String id;
    String username;
    String emailAddress;
}
