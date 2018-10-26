package smokefree.domain;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class InitiativeJoinedEvent {
    String initiativeId;
    String citizenId;
}
