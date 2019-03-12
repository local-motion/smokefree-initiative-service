package smokefree.projection;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;
import org.joda.time.DateTime;
import smokefree.DomainException;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;
import smokefree.domain.*;

import javax.inject.Singleton;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newConcurrentMap;


// TODO refactor playground updates to the wither pattern so playground can be immutable and updates atomic (including metadata)

@Slf4j
@Singleton
public class InitiativeProjection {

    /**
     * It holds a maximum value that System can allow user to add playgrounds
     */
    public final long MAXIMUM_PLAYGROUNDS_ALLOWED;

    private final Map<String, Playground> playgrounds = newConcurrentMap();
    private final Progress progress = new Progress();

    /**
     * the default value for the maximum playgrounds
     */
    public InitiativeProjection() {
        MAXIMUM_PLAYGROUNDS_ALLOWED = SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED;
    }

    /**
     * the custom value for the maximum playgrounds to be allowed into the System
     * @param maximumPlaygrounds maximum number of playgrounds allowed
     */
    public InitiativeProjection(Long maximumPlaygrounds) {
        MAXIMUM_PLAYGROUNDS_ALLOWED = maximumPlaygrounds;
    }


    /*
            Event handlers
     */

    @EventHandler
    public void on(InitiativeCreatedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        final GeoLocation geoLocation = evt.getGeoLocation();
        if (playgrounds.containsKey(evt.getInitiativeId())) {
            log.warn("Received initiative creation for {} {} multiple times", evt.getInitiativeId(), evt.getName());
            return;
        }
        playgrounds.put(evt.getInitiativeId(), new Playground(
                evt.getInitiativeId(),
                evt.getName(),
                geoLocation.getLat(),
                geoLocation.getLng(),
                evt.getStatus(),
                null,
                0,
                eventMessage
                ));

        progress.increment(evt.getStatus());
    }

    @EventHandler
    public void on(CitizenJoinedInitiativeEvent evt, MetaData metaData, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.getVolunteers().add(new Playground.Volunteer(evt.getCitizenId(), metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME).toString()));
        playground.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(InitiativeProgressedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setStatus(evt.getAfter());
        playground.setLastEventMessage(eventMessage);

        progress.change(evt.getBefore(), evt.getAfter());
    }

    @EventHandler
    public void on(SmokeFreeDateCommittedEvent evt, EventMessage<SmokeFreeDateCommittedEvent> eventMessage)  {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setSmokeFreeDate(evt.getSmokeFreeDate());
        playground.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(ManagerJoinedInitiativeEvent evt, MetaData metaData, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        final String userId = evt.getManagerId();
        final String userName = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME);             // TODO should this data not be extracted from the event itself?

        Playground playground = playgrounds.get(evt.getInitiativeId());
        Playground.Manager manager = new Playground.Manager(userId, userName);
        playground.addManager(manager);

        // Also register the manager as a volunteer
        playground.getVolunteers().add(new Playground.Volunteer(evt.getManagerId(), metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME).toString()));

        playground.setLastEventMessage(eventMessage);
    }

    @EventHandler
    public void on(PlaygroundObservationEvent evt, MetaData metaData, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        final String userId = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID);
        final String userName = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME);
        Playground.PlaygroundObservation playgroundObservation = new Playground.PlaygroundObservation(evt.getObserver(), metaData.get(SmokefreeConstants.JWTClaimSet.COGNITO_USER_NAME).toString(), evt.getSmokefree(), evt.getObservationDate(), evt.getComment());
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.addPlaygroundObservation(playgroundObservation);
        playground.setLastEventMessage(eventMessage);

    }

    @EventHandler
    void on(CheckListUpdateEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {} AT {}", evt, eventMessage.getTimestamp());
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setChecklistItem(evt.getActor(), evt.getChecklistItem(), evt.isChecked());
        playground.setLastEventMessage(eventMessage);
    }


    /*
            Generic event message handling
     */

//    @EventHandler
//    void on(EventMessage<?> eventMessage, @Timestamp DateTime timestamp) {
//        log.info("ON EVENTMESSAGE {} at {}", eventMessage, timestamp);
//    }

    /*
            Serving the projections
     */

    public Collection<Playground> playgrounds(String userId) {
        return playgrounds.values().stream().map(playground -> playground.getPlaygroundForUser(userId)).collect(Collectors.toList());
    }

    public Playground playground(String id, String userId) {
        Playground playground = playgrounds.containsKey(id) ? playgrounds.get(id).getPlaygroundForUser(userId) : null;
        return playground;
    }

    public Progress progress() {
        return progress;
    }

    /**
     * Throws a exception if the given playground name already exist.
     * @param playgroundName     name of the playground to be checked whether it's already exist or not
     * @return                   <code>void</code> if the given playground name is not already exist
     * @throws RuntimeException  if the given playground name is already exist
     */
    public void isPlaygroundAlreadyExist(String playgroundName) {
        playgrounds.entrySet()
                .stream()
                .filter( playgroundEntry -> playgroundEntry.getValue().getName().equals(playgroundName))
                .map(playgroundEntry -> playgroundEntry.getValue())
                .findFirst()
                .ifPresent( p -> {
                    throw new DomainException("PLAYGROUND_NAME_ALREADY_EXIST", "Two playgrounds can not have the same name", "Playground " + playgroundName + " is already exist, please choose a different name");
                });

    }
    /**
     * It finds the playgrounds list which are nearby to the given GeoLocation.
     * So it will useful to display in front-end, asking user for confirmation
     * @param newPlaygroundLocation  <code>GeoLocation</code> of the new playground
     * @param distance               minimum distance between playgrounds in meters
     * @return void                  if there are no playgrounds exist within 100 Meters
     */
    public void checkPlaygroundsWithinRadius(GeoLocation newPlaygroundLocation, long distance) {
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        Ellipsoid ellipsoidsReference = Ellipsoid.WGS84;
        GlobalPosition newPlaygroundLocationPosition = new GlobalPosition(newPlaygroundLocation.getLat(), newPlaygroundLocation.getLng(), 0.0); // Point A
        playgrounds.entrySet().stream()
                .filter( playgroundEntry -> {
                    Playground playground = playgroundEntry.getValue();
                    GlobalPosition playgroundPosition = new GlobalPosition(playground.getLat() , playground.getLng(), 0.0);
                    double playgroundsDistance = geodeticCalculator.calculateGeodeticCurve(ellipsoidsReference, playgroundPosition, newPlaygroundLocationPosition).getEllipsoidalDistance();
                    return  playgroundsDistance < distance ? true:false;
                }).map(playgroundEntry -> playgroundEntry.getValue())
                .findFirst()
                .ifPresent(p -> {
                    throw new DomainException("PLAYGROUNS_LOCATED_CLOSELY", "Two playgrounds can not exists within 100 Meters","There is a playground already exists within 100 Meters");
                });
    }

    /**
     * Checks whether new playground can be added or not , based on the maximum playgrounds allowed in the system.
     * @return void             if system does have capacity to hold new playground
     * @throws RuntimeException if System does not have capacity to hold new playground
     */
    public void checkForMaximumPlaygrounds() {
        if(playgrounds.size() < MAXIMUM_PLAYGROUNDS_ALLOWED ){
            return;
        } else {
            throw new DomainException("MAXIMUM_PLAYGROUNDS","Can not add more than " + MAXIMUM_PLAYGROUNDS_ALLOWED  + " playgrounds","System is already reached the maximum of playgrounds allowed, please contact helpline");
        }
    }

}
