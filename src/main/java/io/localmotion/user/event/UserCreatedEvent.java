package io.localmotion.user.event;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class UserCreatedEvent {
    String userId;
    long piiRecordId;
}
