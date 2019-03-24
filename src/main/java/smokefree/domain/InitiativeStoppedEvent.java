package smokefree.domain;

import io.localmotion.initiative.domain.Status;
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
