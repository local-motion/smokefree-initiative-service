package io.localmotion.user.event;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class UserRevivedEvent {
    String userId;
    String newUserName;                // If null the userName remains unchanged.
}
