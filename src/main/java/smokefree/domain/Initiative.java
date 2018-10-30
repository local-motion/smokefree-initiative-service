package smokefree.domain;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@AggregateRoot
public class Initiative {
    @AggregateIdentifier
    private String id;
    private Set<String> citizens = newHashSet();

    @CommandHandler
    public Initiative(CreateInitiativeCommand cmd) {
        apply(new InitiativeCreatedEvent(cmd.initiativeId, cmd.type, cmd.status, cmd.name, cmd.lat, cmd.lng));
    }

    @EventSourcingHandler
    void on(InitiativeCreatedEvent evt) {
        this.id = evt.getInitiativeId();
    }

    @CommandHandler
    public void joinInitiative(JoinInitiativeCommand cmd) {
        if (citizens.contains(cmd.getCitizenId())) {
            log.warn("{} already joined {}. Ignoring...", cmd.getCitizenId(), cmd.getInitiativeId());
        } else {
            apply(new InitiativeJoinedEvent(cmd.getInitiativeId(), cmd.getCitizenId()));
        }
    }

    @EventSourcingHandler
    void on(InitiativeJoinedEvent evt) {
        citizens.add(evt.getCitizenId());
    }






































    /*
    @CommandHandler
    public void joinInitiative(JoinInitiativeCommand cmd) {
        if (citizens.contains(cmd.citizenId)) {
            log.warn("{} tried joining initiative {} multiple times. Ignoring...", cmd.citizenId, cmd.initiativeId);
        } else {
            apply(new InitiativeJoinedEvent(cmd.initiativeId, cmd.citizenId));
        }
    }

    @EventSourcingHandler
    void on(InitiativeJoinedEvent evt) {
        citizens.add(evt.getCitizenId());
    }
    */
}

