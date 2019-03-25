package io.localmotion.smokefreeplaygrounds.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SmokeFreeDecisionEvent {
    String initiativeId;
    Boolean willBecomeSmokefree;
}
