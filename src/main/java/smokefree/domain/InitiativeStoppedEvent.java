package smokefree.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InitiativeStoppedEvent {
    String initiativeId;
    Status before;
    Status after;
    String reason;
}
