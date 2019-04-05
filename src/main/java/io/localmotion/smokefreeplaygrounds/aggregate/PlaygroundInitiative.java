package io.localmotion.smokefreeplaygrounds.aggregate;

import io.localmotion.application.Application;
import io.localmotion.application.DomainException;
import io.localmotion.initiative.aggregate.Initiative;
import io.localmotion.smokefreeplaygrounds.command.CreatePlaygroundInitiativeCommand;
import io.localmotion.interfacing.graphql.error.ErrorCode;
import io.localmotion.smokefreeplaygrounds.command.ClaimManagerRoleCommand;
import io.localmotion.smokefreeplaygrounds.command.CommitToSmokeFreeDateCommand;
import io.localmotion.smokefreeplaygrounds.command.DecideToBecomeSmokeFreeCommand;
import io.localmotion.smokefreeplaygrounds.command.RecordPlaygroundObservationCommand;
import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
import io.localmotion.smokefreeplaygrounds.domain.Status;
import io.localmotion.smokefreeplaygrounds.event.*;
import io.localmotion.smokefreeplaygrounds.projection.Playground;
import io.localmotion.smokefreeplaygrounds.projection.PlaygroundProjection;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.Assert;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateRoot;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
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


    private PlaygroundProjection playgroundProjection = Application.getApplicationContext().getBean(PlaygroundProjection.class);

    // Instance properties

    private Status status;
    private Set<String> managers = newHashSet();

    private LocalDate smokeFreeDate;
    private LocalDate lastObservationDate;


    @CommandHandler
    public PlaygroundInitiative(CreatePlaygroundInitiativeCommand cmd, MetaData metaData) {
        validateMaximumPlaygroundCapacity();
        validateDuplicatePlaygroundNames(cmd.getName());
        validatePlaygroundsRange(cmd.getGeoLocation(), SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE);
        apply(new PlaygroundInitiativeCreatedEvent(cmd.getInitiativeId(), cmd.getName(), cmd.getCreationStatus(), cmd.getGeoLocation()), metaData);
    }

    @Override
    protected Set<String> getChecklistItems() {
        return CHECKLIST_ITEMS;
    }

    @Override
    protected int getMaximumNumberOfMembers() {
        return SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_NR_OF_VOLUNTEERS;
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

        if (status != Status.NOT_STARTED) {
            log.warn("Status is already {}, Ignoring DecideToBecomeSmokeFreeCommand ...", status);
        } else {
            apply(new SmokeFreeDecisionEvent(cmd.getInitiativeId(), true), metaData);
        }
    }

    @CommandHandler
    public void commitToSmokeFreeDate(CommitToSmokeFreeDateCommand cmd, MetaData metaData) {
        assertCurrentUserIsManager(metaData);
        if (smokeFreeDate == null || !cmd.getSmokeFreeDate().isEqual(smokeFreeDate)) {
            apply(new SmokeFreeDateCommittedEvent(cmd.getInitiativeId(), cmd.getSmokeFreeDate()), metaData);
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



    /*
            Event handlers
     */

    @EventSourcingHandler
    void on(PlaygroundInitiativeCreatedEvent evt) {
        this.id = evt.getInitiativeId();
        this.status = evt.getCreationStatus() == CreationStatus.IMPORT_FINISHED ? Status.FINISHED : Status.NOT_STARTED;
    }

    @EventSourcingHandler
    void on(ManagerJoinedInitiativeEvent evt) {
        managers.add(evt.getManagerId());
        members.add(evt.getManagerId());
    }

    @EventSourcingHandler
    void on(SmokeFreeDecisionEvent evt) {
        status = status == Status.NOT_STARTED ? Status.IN_PROGRESS : status;
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

    private void assertCurrentUserIsManager(MetaData metaData) {
        String userId = requireUserId(metaData);
        Assert.assertThat(userId, id -> managers.contains(id), () -> new DomainException(
                ErrorCode.UNAUTHORIZED.toString(),
                userId + " is not a manager",
                "You are not a manager of this playground"));
    }

    private void validateMaximumPlaygroundCapacity() {
        if(playgroundProjection.getAllPlaygrounds().size() >= SmokefreeConstants.MAXIMUM_NR_OF_PLAYGROUNDS) {
            throw new DomainException(ErrorCode.MAXIMUM_PLAYGROUNDS_CAPACITY_REACHED.toString(),
                    "Can not add more than " + SmokefreeConstants.MAXIMUM_NR_OF_PLAYGROUNDS + " playgrounds",
                    "Sorry, Maximum playgrounds capacity is reached, please contact helpline");
        }
    }

    private void validateDuplicatePlaygroundNames(String playgroundName) {
        playgroundProjection.getAllPlaygrounds().stream()
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
        playgroundProjection.getAllPlaygrounds().stream()
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

    private void validateMaximumAllowedManagers() {
        if(managers.size() >= SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_NR_OF_MANAGERS) {
            throw new DomainException("MAXIMUM_MANAGERS",
                    "No more than " + SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_NR_OF_MANAGERS + " volunteers can claim for manager role",
                    "No more than " + SmokefreeConstants.PlaygroundWorkspace.MAXIMUM_NR_OF_MANAGERS + " volunteers can claim for manager role");
        }
    }

    private void validateMaximumAllowedObservations() {
        if(getPlaygroundObservations(id).size() > SmokefreeConstants.PlaygroundObservation.MAXIMUM_NR_OF_OBSERVATIONS) {
            throw new DomainException("MAXIMUM_OBSERVATIONS",
                    "No more than " + SmokefreeConstants.PlaygroundObservation.MAXIMUM_NR_OF_OBSERVATIONS + " observations can be recorded",
                    "No more than " + SmokefreeConstants.PlaygroundObservation.MAXIMUM_NR_OF_OBSERVATIONS + " observations can be recorded");
        }
    }

    private List<Playground.PlaygroundObservation> getPlaygroundObservations(String id) {
        return playgroundProjection.getAllPlaygrounds()
                .stream()
                .filter(playground -> playground.getId().equals(id))
                .flatMap(playground -> playground.getPlaygroundObservations().stream())
                .collect(Collectors.toList());
    }

}

