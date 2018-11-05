package smokefree.domain;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.Assert;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static smokefree.domain.Status.in_progress;
import static smokefree.domain.Status.not_started;
import static smokefree.domain.Status.stopped;

@Slf4j
@NoArgsConstructor
@AggregateRoot
public class Initiative {
    @AggregateIdentifier
    private String id;
    private Status status;
    private Set<String> managers = newHashSet();
    private Set<String> citizens = newHashSet();

    @CommandHandler
    public Initiative(CreateInitiativeCommand cmd) {
        apply(new InitiativeCreatedEvent(cmd.initiativeId, cmd.type, cmd.status, cmd.name, cmd.geoLocation));
    }

    @CommandHandler
    public void joinInitiative(JoinInitiativeCommand cmd) {
        if (citizens.contains(cmd.getCitizenId())) {
            log.warn("{} already joined {}. Ignoring...", cmd.citizenId, cmd.initiativeId);
        } else {
            apply(new CitizenJoinedInitiativeEvent(cmd.initiativeId, cmd.citizenId));
        }
    }

    @CommandHandler
    public void claimManagerRole(ClaimManagerRoleCommand cmd, MetaData metaData) {
        String managerId = (String) metaData.get("user_id");
        if (managers.contains(managerId)) {
            log.warn("{} is already managing {}. Ignoring...", managerId, cmd.initiativeId);
        } else {
            apply(new ManagerJoinedInitiativeEvent(cmd.initiativeId, managerId));
        }
    }

    @CommandHandler
    public void decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd, MetaData metaData) {
        String userId = (String) metaData.get("user_id");
        Assert.isTrue(managers.contains(userId), () -> userId + " is not a manager");

        if (status != not_started && status != stopped) {
            log.warn("Status is already {}, cannot change to {}. Ignoring...", status, in_progress);
        } else {
            apply(new InitiativeProgressedEvent(cmd.initiativeId, status, in_progress));
        }
    }

    @CommandHandler
    public void decideToNotBecomeSmokeFree(DecideToNotBecomeSmokeFreeCommand cmd, MetaData metaData) {
        String userId = (String) metaData.get("user_id");
        Assert.isTrue(managers.contains(userId), () -> userId + " is not a manager");
        apply(new InitiativeStoppedEvent(cmd.initiativeId, status, stopped, cmd.reason));
    }

    @EventSourcingHandler
    void on(InitiativeCreatedEvent evt) {
        this.id = evt.initiativeId;
        this.status = evt.status;
    }

    @EventSourcingHandler
    void on(CitizenJoinedInitiativeEvent evt) {
        citizens.add(evt.citizenId);
    }

    @EventSourcingHandler
    void on(ManagerJoinedInitiativeEvent evt) {
        managers.add(evt.managerId);
    }

    @EventSourcingHandler
    void on(InitiativeProgressedEvent evt) {
        status = evt.after;
    }

    @EventSourcingHandler
    void on(InitiativeStoppedEvent evt) {
        status = evt.after;
    }
}

