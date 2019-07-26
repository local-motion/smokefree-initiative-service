package io.localmotion.user.event;

import lombok.*;

import javax.annotation.Nullable;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class UserRevivedEvent {
    String userId;
    @Nullable Long piiRecordId;     // (optional) UserPII record type, specified properties valid
}
