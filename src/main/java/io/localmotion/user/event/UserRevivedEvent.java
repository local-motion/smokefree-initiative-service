package io.localmotion.user.event;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class UserRevivedEvent {
    String userId;
    String userName;                // If not null the userName has changed.
}
