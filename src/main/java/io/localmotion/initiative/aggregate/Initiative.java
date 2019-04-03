package io.localmotion.initiative.aggregate;

import io.localmotion.application.DomainException;
import io.localmotion.eventsourcing.axon.MetaDataManager;
import io.localmotion.initiative.command.JoinInitiativeCommand;
import io.localmotion.initiative.command.UpdateChecklistCommand;
import io.localmotion.initiative.event.ChecklistUpdateEvent;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
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
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@AggregateRoot
public class Initiative {

    // Constants

    public static  final int DEFAULT_MAXIMUM_NUMBER_OF_MEMBERS = 100;

    // Instance properties

    @Setter
    @AggregateIdentifier
    protected String id;
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


    /*
            Methods for subclasses
     */

    protected Set<String> getChecklistItems() {
        // Override to implement
        return Collections.emptySet();
    }

    protected int getMaximumNumberOfMembers() {
        return DEFAULT_MAXIMUM_NUMBER_OF_MEMBERS;
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

    private void validateMaximumAllowedVolunteers() {
        if(members.size() >= getMaximumNumberOfMembers()) {
            throw new DomainException("MAXIMUM_MEMBERS",
                    "No more than " + getMaximumNumberOfMembers() + " members can join the initiative" ,
                    "No more than " + getMaximumNumberOfMembers() + " members can join the initiative");
        }
    }

}

