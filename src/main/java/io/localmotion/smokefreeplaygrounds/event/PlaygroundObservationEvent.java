package io.localmotion.smokefreeplaygrounds.event;

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
    String observerId;
    Boolean smokefree;
    String comment;
    LocalDate observationDate;
}
