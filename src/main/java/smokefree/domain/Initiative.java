package smokefree.domain;

import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateRoot;
import org.axonframework.eventsourcing.EventSourcingHandler;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@NoArgsConstructor
@AggregateRoot
public class Initiative {

    @AggregateIdentifier
    private String id;

    @CommandHandler
    public Initiative(JoinInitiativeCommand cmd) {
        apply(new InitiativeJoinedEvent(cmd.getInitiativeId()));
    }

    @EventSourcingHandler
    void on(InitiativeJoinedEvent evt) {
        this.id = evt.getInitiativeId();
    }
}

