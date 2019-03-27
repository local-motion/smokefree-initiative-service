package io.localmotion.smokefreeplaygrounds.aggregate;

import io.localmotion.application.Application;
import io.localmotion.application.DomainException;
import io.localmotion.initiative.aggregate.Initiative;
import io.localmotion.initiative.command.CreateInitiativeCommand;
import io.localmotion.initiative.command.JoinInitiativeCommand;
import io.localmotion.initiative.command.UpdateChecklistCommand;
import io.localmotion.initiative.domain.GeoLocation;
import io.localmotion.initiative.domain.Status;
import io.localmotion.initiative.event.CheckListUpdateEvent;
import io.localmotion.initiative.event.CitizenJoinedInitiativeEvent;
import io.localmotion.initiative.event.InitiativeCreatedEvent;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.interfacing.graphql.error.ErrorCode;
import io.localmotion.smokefreeplaygrounds.command.ClaimManagerRoleCommand;
import io.localmotion.smokefreeplaygrounds.command.CommitToSmokeFreeDateCommand;
import io.localmotion.smokefreeplaygrounds.command.DecideToBecomeSmokeFreeCommand;
import io.localmotion.smokefreeplaygrounds.command.RecordPlaygroundObservationCommand;
import io.localmotion.smokefreeplaygrounds.event.ManagerJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.event.PlaygroundObservationEvent;
import io.localmotion.smokefreeplaygrounds.event.SmokeFreeDateCommittedEvent;
import io.localmotion.smokefreeplaygrounds.event.SmokeFreeDecisionEvent;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.Assert;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static io.localmotion.initiative.domain.Status.*;
import static org.axonframework.common.Assert.assertNonNull;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@AggregateRoot
public class PlaygroundInitiative extends Initiative {

    // Constants

    private static final Set<String> CHECKLIST_ITEMS = Set.of(new String[] {
            "invite_manager", "order_flyers", "distribute_flyers",
            "press_announcement", "newsletter_announcement", "website_announcement",
            "adjust_regulations", "publish_regulations",
            "order_sign", "place_sign", "press_announcement_smokefree"
    });


    InitiativeProjection initiativeProjection = Application.getApplicationContext().getBean(InitiativeProjection.class);

    // Instance properties

//    @AggregateIdentifier
//    private String id;


    // TODO: Specs mention Phase; Should we rename to Phase and use prepare, execute and sustain as values?
    private Status status;
    private Set<String> managers = newHashSet();
//    private Set<String> citizens = newHashSet();

    private LocalDate smokeFreeDate;
    private LocalDate lastObservationDate;


//    @Override
//    public void checklistUpdate(UpdateChecklistCommand cmd, MetaData metaData) {
//        log.info("I am here, not doing anything");
//    }


    @CommandHandler
    public PlaygroundInitiative(CreateInitiativeCommand cmd, MetaData metaData) {
        validateMaximumPlaygroundCapacity();
        validateDuplicatePlaygroundNames(cmd.getName());
        validatePlaygroundsRange(cmd.getGeoLocation(), SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE);
        apply(new InitiativeCreatedEvent(cmd.getInitiativeId(), cmd.getType(), cmd.getStatus(), cmd.getName(), cmd.getGeoLocation()), metaData);
    }

    @Override
    protected Set<String> getChecklistItems() {
        return CHECKLIST_ITEMS;
    }

    @CommandHandler
    public void claimManagerRole(ClaimManagerRoleCommand cmd, MetaData metaData) {
        String managerId = requireUserId(metaData);

        if (managers.contains(managerId)) {
            log.warn("{} is already managing {}. Ignoring...", managerId, cmd.getInitiativeId());
        } else {
            validateMaximumAllowedManagers();
            apply(new ManagerJoinedInitiativeEvent(cmd.getInitiativeId(), managerId), metaData);
        }
    }

    @CommandHandler
    public void decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd, MetaData metaData) {
        assertCurrentUserIsManager(metaData);

        if (status != not_started && status != stopped) {
            log.warn("Status is already {}, cannot change to {}. Ignoring...", status, in_progress);
        } else {
            apply(new SmokeFreeDecisionEvent(cmd.getInitiativeId(), true), metaData);
        }
    }

    @CommandHandler
    public void commitToSmokeFreeDate(CommitToSmokeFreeDateCommand cmd, MetaData metaData) {
        assertCurrentUserIsManager(metaData);
        if (smokeFreeDate == null || !cmd.getSmokeFreeDate().isEqual(smokeFreeDate)) {
            apply(new SmokeFreeDateCommittedEvent(cmd.getInitiativeId(), smokeFreeDate, cmd.getSmokeFreeDate()), metaData);
        }
    }

    @CommandHandler
    public void recordPlaygroundObservation(RecordPlaygroundObservationCommand cmd, MetaData metaData) {
        LocalDate today = LocalDate.now();
        if (lastObservationDate != null && lastObservationDate.isEqual(today))
            throw new ValidationException("Only one playground observation can be registered per day");
        apply(new PlaygroundObservationEvent(cmd.getInitiativeId(), cmd.getObserver() , cmd.getSmokefree(), cmd.getComment(), today), metaData);
    }



    /*
            Event handlers
     */

    @EventSourcingHandler
    void on(InitiativeCreatedEvent evt) {
        this.id = evt.getInitiativeId();
        this.status = evt.getStatus();
    }

    @EventSourcingHandler
    void on(ManagerJoinedInitiativeEvent evt) {
        managers.add(evt.getManagerId());
        citizens.add(evt.getManagerId());
    }

    @EventSourcingHandler
    void on(SmokeFreeDecisionEvent evt) {
        status = status == not_started ? in_progress : status;
    }

    @EventSourcingHandler
    void on(SmokeFreeDateCommittedEvent evt) {
        smokeFreeDate = evt.getSmokeFreeDate();
    }

    @EventSourcingHandler
    void on(PlaygroundObservationEvent evt) {
        lastObservationDate = evt.getObservationDate();
    }

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

//    private void assertUserIsInitiativeParticipant(MetaData metaData) {
//        String userId = requireUserId(metaData);
//        if (!citizens.contains(userId) && !managers.contains(userId))
//            throw new ValidationException("User is not participating in this initiative");
//    }

    private void assertCurrentUserIsManager(MetaData metaData) {
        String userId = requireUserId(metaData);
        Assert.assertThat(userId, id -> managers.contains(id), () -> new DomainException(
                ErrorCode.UNAUTHORIZED.toString(),
                userId + " is not a manager",
                "You are not a manager of this playground"));
    }

    private void validateMaximumPlaygroundCapacity() {
        if(initiativeProjection.getAllPlaygrounds().size() >= SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED) {
            throw new DomainException(ErrorCode.MAXIMUM_PLAYGROUNDS_CAPACITY_REACHED.toString(),
                    "Can not add more than " + SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED + " playgrounds",
                    "Sorry, Maximum playgrounds capacity is reached, please contact helpline");
        }
    }

    private void validateDuplicatePlaygroundNames(String playgroundName) {
        initiativeProjection.getAllPlaygrounds().stream()
                .filter( playground -> playground.getName().equals(playgroundName))
                .findFirst()
                .ifPresent( p -> {
                    throw new DomainException(ErrorCode.DUPLICATE_PLAYGROUND_NAME.toString(),
                            "Initiative " + playgroundName + " does already exist, please choose a different name",
                            "Initiative name does already exist");
                });

    }

    private void validatePlaygroundsRange(GeoLocation newPlaygroundLocation, long distance) {
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        final Ellipsoid ellipsoidsReference = Ellipsoid.WGS84;
        final GlobalPosition newPlaygroundPosition = new GlobalPosition(newPlaygroundLocation.getLat(), newPlaygroundLocation.getLng(), 0.0);
        initiativeProjection.getAllPlaygrounds().stream()
                .filter( playground -> {
                    GlobalPosition currentPlaygroundPosition = new GlobalPosition(playground.getLat() , playground.getLng(), 0.0);
                    double playgroundsDistance = geodeticCalculator.calculateGeodeticCurve(ellipsoidsReference, currentPlaygroundPosition, newPlaygroundPosition).getEllipsoidalDistance();
                    return  playgroundsDistance < distance;
                })
                .findFirst()
                .ifPresent(p -> {
                    throw new DomainException(ErrorCode.PLAYGROUNS_LOCATED_CLOSELY.toString(),
                            "Two playgrounds can not exist within " + SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE+ " Meters",
                            "playground does already exists within "+ SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE+ " Meters");
                });
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

}

