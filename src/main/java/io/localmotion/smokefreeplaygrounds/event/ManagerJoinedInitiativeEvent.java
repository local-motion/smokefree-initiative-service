package io.localmotion.smokefreeplaygrounds.event;

import io.localmotion.initiative.domain.GeoLocation;
import io.localmotion.initiative.domain.Status;
import io.localmotion.initiative.domain.Type;
import lombok.*;

/**
 * Note that the manager may or may not be a volunteer for this workspace. However any manager is considered a volunteer
 * of the initiative and will be reflected as such in the projections. So there is no need to issue a JoinInitiativeCommand
 * prior to this command.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
public class ManagerJoinedInitiativeEvent {
    String initiativeId;
    String managerId;
}
