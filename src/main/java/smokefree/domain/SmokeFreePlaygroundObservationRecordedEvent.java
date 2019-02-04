package smokefree.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SmokeFreePlaygroundObservationRecordedEvent {
    String initiativeId;
    String citizenId;
    Boolean isSmokeFree;
    String recordObservation;
    LocalDate observationDate;
}
