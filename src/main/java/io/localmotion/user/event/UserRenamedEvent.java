package io.localmotion.user.event;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class UserRenamedEvent {
    String userId;
    Long piiRecordId;       // UserPII record type, only userName property is valid
}
