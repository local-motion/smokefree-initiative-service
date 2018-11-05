package smokefree.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmokeFreeDateCommittedEvent {
    String initiativeId;
    LocalDate previousDate;
    LocalDate smokeFreeDate;
}
