package io.localmotion.initiative.event;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class MemberJoinedInitiativeEvent {
    String initiativeId;
    String memberId;
}
