package smokefree.domain;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class ManagerJoinedInitiativeEvent {
    String initiativeId;
    String managerId;
}
