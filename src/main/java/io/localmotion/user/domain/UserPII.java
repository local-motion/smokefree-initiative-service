package io.localmotion.user.domain;

import lombok.*;

@Value
public class UserPII {
    String name;
    String emailAddress;
}
