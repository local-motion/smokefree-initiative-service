package smokefree.domain;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.Assert;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;
import smokefree.DomainException;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;
import smokefree.graphql.error.ErrorCode;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.axonframework.common.Assert.assertNonNull;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static smokefree.domain.Status.*;

@Slf4j
@NoArgsConstructor
@AggregateRoot
public class Initiative {

    // Constants

    private static final Set<String> CHECKLIST_ITEMS = Set.of(new String[] {
            "invite_manager", "order_flyers", "distribute_flyers",
            "press_announcement", "newsletter_announcement", "website_announcement",
            "adjust_regulations", "publish_regulations",
            "order_sign", "place_sign", "press_announcement_smokefree"
    });


    // InitiativeProjection initiativeProjection = Application.getApplicationContext().getBean(InitiativeProjection.class);

    // Instance properties

    @AggregateIdentifier
    private String id;
    // TODO: Specs mention Phase; Should we rename to Phase and use prepare, execute and sustain as values?
    private Status status;
    private Set<String> managers = newHashSet();
    private Set<String> citizens = newHashSet();

    private LocalDate smokeFreeDate;
    private LocalDate lastObservationDate;
    private int totalObservationsCount;

    @CommandHandler
    public Initiative(CreateInitiativeCommand cmd, MetaData metaData) {
        /*validateMaximumPlaygroundCapacity();
        validateDuplicatePlaygroundNames(cmd.getName());
        validatePlaygroundsRange(cmd.getGeoLocation(), SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE);*/
        apply(new InitiativeCreatedEvent(cmd.initiativeId, cmd.type, cmd.status, cmd.name, cmd.geoLocation), metaData);
    }

    @CommandHandler
    public void joinInitiative(JoinInitiativeCommand cmd, MetaData metaData) {
        if (citizens.contains(cmd.getCitizenId())) {
            log.warn("{} already joined {}. Ignoring...", cmd.citizenId, cmd.initiativeId);
        } else {
            validateMaximumAllowedVolunteers();
            apply(new CitizenJoinedInitiativeEvent(cmd.initiativeId, cmd.citizenId), metaData);
        }
    }

    @CommandHandler
    public void claimManagerRole(ClaimManagerRoleCommand cmd, MetaData metaData) {
        String managerId = requireUserId(metaData);

        if (managers.contains(managerId)) {
            log.warn("{} is already managing {}. Ignoring...", managerId, cmd.initiativeId);
        } else {
            validateMaximumAllowedManagers();
            apply(new ManagerJoinedInitiativeEvent(cmd.initiativeId, managerId), metaData);
        }
    }

    @CommandHandler
    public void decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd, MetaData metaData) {
        assertCurrentUserIsManager(metaData);

        if (status != not_started && status != stopped) {
            log.warn("Status is already {}, cannot change to {}. Ignoring...", status, in_progress);
        } else {
            apply(new InitiativeProgressedEvent(cmd.initiativeId, status, in_progress), metaData);
        }
    }

    @CommandHandler
    public void decideToNotBecomeSmokeFree(DecideToNotBecomeSmokeFreeCommand cmd, MetaData metaData) {
        assertCurrentUserIsManager(metaData);
        apply(new InitiativeStoppedEvent(cmd.initiativeId, status, stopped, cmd.reason), metaData);
    }

    @CommandHandler
    public void commitToSmokeFreeDate(CommitToSmokeFreeDateCommand cmd, MetaData metaData) {
//        assertEarlierCommittedDateNotInPast();
        assertCurrentUserIsManager(metaData);
        if (smokeFreeDate == null || !cmd.smokeFreeDate.isEqual(smokeFreeDate)) {
            apply(new SmokeFreeDateCommittedEvent(cmd.initiativeId, smokeFreeDate, cmd.smokeFreeDate), metaData);
        }
        if (status != finished) {
            apply(new InitiativeProgressedEvent(cmd.initiativeId, status, finished), metaData);
        }
    }

    @CommandHandler
    public void recordPlaygroundObservation(RecordPlaygroundObservationCommand cmd, MetaData metaData) {
        LocalDate today = LocalDate.now();
        if (lastObservationDate != null && lastObservationDate.isEqual(today))
            throw new ValidationException("Only one playground observation can be registered per day");
        validateMaximumAllowedObservations();
        apply(new PlaygroundObservationEvent(cmd.getInitiativeId(), cmd.getObserver() , cmd.getSmokefree(), cmd.getComment(), today), metaData);
    }

    @CommandHandler
    public void checklistUpdate(UpdateChecklistCommand cmd, MetaData metaData) {
        assertUserIsInitiativeParticipant(metaData);
        if (!CHECKLIST_ITEMS.contains(cmd.getChecklistItem()))
            throw new DomainException("UNKNOWNITEM", "Unknown checklist item: " + cmd.getChecklistItem(), "Technical error: Unknown checklist item");

        // TODO check for superfluous updates the prevent issuing events for those

        apply(new CheckListUpdateEvent(cmd.getInitiativeId(), cmd.getActor() , cmd.getChecklistItem(), cmd.isChecked()), metaData);
    }

//    private void assertEarlierCommittedDateNotInPast() {
//        if (this.smokeFreeDate == null) {
//            return;
//        }
//        if (this.smokeFreeDate.isBefore(now())) {
//            throw new ValidationException("Cannot commit to a new smoke-free date once an earlier committed date has passed");
//        }
//    }

    /*
   Retrieval functions (use for outputting state consistent with the update, otherwise consider using the projections)
    */
    @CommandHandler
    public Initiative getInitiative(GetInitiativeCommand cmd, MetaData metaData) {
        return this;
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

    @EventSourcingHandler
    void on(SmokeFreeDateCommittedEvent evt) {
        smokeFreeDate = evt.smokeFreeDate;
    }

    @EventSourcingHandler
    void on(PlaygroundObservationEvent evt) {
        lastObservationDate = evt.getObservationDate();
        totalObservationsCount++;
    }

    @EventSourcingHandler
    void on(CheckListUpdateEvent evt) { }

    private String requireUserId(MetaData metaData) {
        final String userId = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID);
        assertNonNull(userId, () -> new DomainException(
                ErrorCode.UNAUTHENTICATED.toString(),
                "User ID must be set",
                "You are not logged in"));
        return userId;
    }


    /*
               Validations
     */

    private void assertUserIsInitiativeParticipant(MetaData metaData) {
        String userId = requireUserId(metaData);
        if (!citizens.contains(userId) && !managers.contains(userId))
            throw new ValidationException("User is not participating in this initiative");
    }

    private void assertCurrentUserIsManager(MetaData metaData) {
        String userId = requireUserId(metaData);
        Assert.assertThat(userId, id -> managers.contains(id), () -> new DomainException(
                ErrorCode.UNAUTHORIZED.toString(),
                userId + " is not a manager",
                "You are not a manager of this playground"));
    }




    private void validateMaximumAllowedVolunteers() {
        if(citizens.size() >= SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_VOLUNTEERS_ALLOWED) {
            throw new DomainException("MAXIMUM_VOLUNTEERS",
                    "No more than " + SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_VOLUNTEERS_ALLOWED + " members can join the initiative" ,
                    "No more than " + SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_VOLUNTEERS_ALLOWED + " members can join the initiative");
        }
    }

    private void validateMaximumAllowedManagers() {
        if(managers.size() >= SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_MANAGERS_ALLOWED) {
            throw new DomainException("MAXIMUM_MANAGERS",
                    "No more than " + SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_MANAGERS_ALLOWED + " volunteers can claim for manager role",
                    "No more than " + SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_MANAGERS_ALLOWED + " volunteers can claim for manager role");
        }
    }

    private void validateMaximumAllowedObservations() {
       if(totalObservationsCount > SmokefreeConstants.PlaygroundObservation.MAXIMUM_NR_OF_OBSERVATIONS) {
           throw new DomainException("MAXIMUM_OBSERVATIONS",
                   "No more than " + SmokefreeConstants.PlaygroundObservation.MAXIMUM_NR_OF_OBSERVATIONS + " observations can be recorded",
                   "No more than " + SmokefreeConstants.PlaygroundObservation.MAXIMUM_NR_OF_OBSERVATIONS + " observations can be recorded");
       }
    }

}

