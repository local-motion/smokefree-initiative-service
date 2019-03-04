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
public class PlaygroundObservationEvent {
    String initiativeId;
    String observer;
    Boolean smokefree;
    String comment;
    LocalDate observationDate;
}
