package io.localmotion.user.event;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class PersonalDataDeletedEvent {
    String userId;
}
