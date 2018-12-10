package smokefree.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SmokeFreeDateCommittedEvent {
    String initiativeId;
    LocalDate previousDate;
    LocalDate smokeFreeDate;
}
