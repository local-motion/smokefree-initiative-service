package smokefree.domain;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CitizenJoinedInitiativeEvent {
    String initiativeId;
    String citizenId;
}
