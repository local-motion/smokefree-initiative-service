package smokefree.domain;

import io.localmotion.initiative.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InitiativeProgressedEvent {
    String initiativeId;
    Status before;
    Status after;
}
