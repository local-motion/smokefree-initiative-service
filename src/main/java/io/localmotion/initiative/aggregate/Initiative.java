package io.localmotion.initiative.aggregate;

import io.localmotion.application.Application;
import io.localmotion.application.DomainException;
import io.localmotion.eventsourcing.axon.MetaDataManager;
import io.localmotion.initiative.command.JoinInitiativeCommand;
import io.localmotion.initiative.command.UpdateChecklistCommand;
import io.localmotion.initiative.event.ChecklistUpdateEvent;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.interfacing.graphql.error.ErrorCode;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.axonframework.common.Assert.assertNonNull;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@AggregateRoot
public class Initiative {

    // Constants

    private  InitiativeProjection initiativeProjection = Application.getApplicationContext().getBean(InitiativeProjection.class);

    // Instance properties

    @Setter
    @AggregateIdentifier
    protected String id;
    // TODO: Specs mention Phase; Should we rename to Phase and use prepare, execute and sustain as values?
//    private Status status;  // TODO we need to revive the status in some form
//    protected Set<String> managers = newHashSet();
    protected Set<String> members = newHashSet();


    @CommandHandler
    public void joinInitiative(JoinInitiativeCommand cmd, MetaData metaData) {
        if (members.contains(cmd.getMemberId())) {
            log.warn("{} already joined {}. Ignoring...", cmd.getMemberId(), cmd.getInitiativeId());
        } else {
            validateMaximumAllowedVolunteers();
            apply(new MemberJoinedInitiativeEvent(cmd.getInitiativeId(), cmd.getMemberId()), metaData);
        }
    }

    @CommandHandler
    public void checklistUpdate(UpdateChecklistCommand cmd, MetaData metaData) {
        assertUserIsInitiativeParticipant(metaData);
        if (!getChecklistItems().contains(cmd.getChecklistItem()))
            throw new DomainException("UNKNOWNITEM", "Unknown checklist item: " + cmd.getChecklistItem(), "Technical error: Unknown checklist item");

        // TODO check for superfluous updates the prevent issuing events for those

        apply(new ChecklistUpdateEvent(cmd.getInitiativeId(), cmd.getChecklistItem(), cmd.isChecked()), metaData);
    }

    protected Set<String> getChecklistItems() {
        // Override to implement
        return Collections.emptySet();
    }



    /*
            Event handlers
     */

    @EventSourcingHandler
    void on(MemberJoinedInitiativeEvent evt) {
        members.add(evt.getMemberId());
    }

    @EventSourcingHandler
    void on(ChecklistUpdateEvent evt) { }



    /*
               Validations
     */

    protected void assertUserIsInitiativeParticipant(MetaData metaData) {
        String userId = new MetaDataManager(metaData).getUserId();
        if (!members.contains(userId))
            throw new ValidationException("User is not participating in this initiative");
    }

    private void validateMaximumPlaygroundCapacity() {
        if(initiativeProjection.getAllPlaygrounds().size() >= SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED) {
            throw new DomainException(ErrorCode.MAXIMUM_PLAYGROUNDS_CAPACITY_REACHED.toString(),
                    "Can not add more than " + SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED + " playgrounds",
                    "Sorry, Maximum playgrounds capacity is reached, please contact helpline");
        }
    }

    private void validateMaximumAllowedVolunteers() {
        if(members.size() >= SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_VOLUNTEERS_ALLOWED) {
            throw new DomainException("MAXIMUM_VOLUNTEERS",
                    "No more than " + SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_VOLUNTEERS_ALLOWED + " members can join the initiative" ,
                    "No more than " + SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_VOLUNTEERS_ALLOWED + " members can join the initiative");
        }
    }

}

