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
import smokefree.aws.rds.secretmanager.SmokefreeConstants;
import smokefree.domain.*;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newConcurrentMap;
import static java.util.Collections.unmodifiableCollection;

@Slf4j
@Singleton
public class InitiativeProjection {

    /**
     * It holds a maximum value that System can allow user to add playgrounds
     */
    public final long MAXIMUM_PLAYGROUNDS_ALLOWED;

    public final long MAXIMUM_VOLUNTEERS_PER_PLAYGROUND;

    private final Map<String, Playground> playgrounds = newConcurrentMap();
    private final Progress progress = new Progress();

    /**
     * the default value for the maximum playgrounds
     */
    public InitiativeProjection() {
        MAXIMUM_PLAYGROUNDS_ALLOWED = SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED;
        MAXIMUM_VOLUNTEERS_PER_PLAYGROUND = SmokefreeConstants.MAXIMUM_VOLUNTEERS_PER_PLAYGROUND;
    }

    /**
     * the custom value for the maximum playgrounds to be allowed into the System
     * @param maximumPlaygrounds
     */
    public InitiativeProjection(Long maximumPlaygrounds) {
        MAXIMUM_PLAYGROUNDS_ALLOWED = maximumPlaygrounds;
        MAXIMUM_VOLUNTEERS_PER_PLAYGROUND = SmokefreeConstants.MAXIMUM_VOLUNTEERS_PER_PLAYGROUND;
    }

    /**
     * the custom value for the maximum playgrounds to be allowed into the System, and
     * the custom value for the maximum volunteers per playground
     * @param maximumPlaygrounds
     */
    public InitiativeProjection(Long maximumPlaygrounds, long maximumVolunteers) {
        MAXIMUM_PLAYGROUNDS_ALLOWED = maximumPlaygrounds;
        MAXIMUM_VOLUNTEERS_PER_PLAYGROUND = maximumVolunteers;
    }



    /*
            Event handlers
     */

    @EventHandler
    public void on(InitiativeCreatedEvent evt) {
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
                0));

        progress.increment(evt.getStatus());
    }

    @EventHandler
    public void on(CitizenJoinedInitiativeEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setVolunteerCount(playground.getVolunteerCount() + 1);
        log.info("user: " + metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME));
        playground.getVolunteers().add(new Playground.Volunteer(evt.getCitizenId(), metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME).toString()));
    }

    @EventHandler
    public void on(InitiativeProgressedEvent evt) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setStatus(evt.getAfter());

        progress.change(evt.getBefore(), evt.getAfter());
    }

    @EventHandler
    public void on(SmokeFreeDateCommittedEvent evt, EventMessage<SmokeFreeDateCommittedEvent> msg)  {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setSmokeFreeDate(evt.getSmokeFreeDate());
    }

    @EventHandler
    public void on(ManagerJoinedInitiativeEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
        final String userId = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID);                 // TODO should this data not be extracted from the event itself?
        final String userName = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME);             // TODO should this data not be extracted from the event itself?

        Playground playground = playgrounds.get(evt.getInitiativeId());
        Playground.Manager manager = new Playground.Manager(userId, userName);
        playground.addManager(manager);
    }

    @EventHandler
    public void on(PlaygroundObservationIndicatedEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);
        final String userId = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID);
        final String userName = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME);
        Playground.PlaygroundObservation playgroundObservation = new Playground.PlaygroundObservation(evt.getObserver(), metaData.get(SmokefreeConstants.JWTClaimSet.COGNITO_USER_NAME).toString(), evt.getSmokefree(), evt.getObservationDate(), evt.getComment());
        playgrounds.get(evt.getInitiativeId()).addPlaygroundObservation(playgroundObservation);
    }

    @EventSourcingHandler
    void on(CheckListUpdateEvent evt) {
        log.info("ON EVENT {}", evt);
        Playground playground = playgrounds.get(evt.getInitiativeId());
        playground.setChecklistItem(evt.getActor(), evt.getChecklistItem(), evt.isChecked());
        Playground exPlayground = playground.getPlaygroundForUser(null);
    }


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
     * It finds the playgrounds list which are nearby to the given GeoLocation.
     * So it will useful to display in front-end, asking user for confirmation
     * @param newPlaygroundLocation
     * @param distance
     * @return
     */
    public List<Playground> checkForNearByPlaygrounds(GeoLocation newPlaygroundLocation, long distance) {
        GeodeticCalculator geoCalc = new GeodeticCalculator();
        Ellipsoid reference = Ellipsoid.WGS84;
        GlobalPosition newPlaygroundLocationPosition = new GlobalPosition(newPlaygroundLocation.getLat(), newPlaygroundLocation.getLng(), 0.0); // Point A
        return playgrounds.entrySet().stream()
                .filter( playgroundEntry -> {
                    Playground playground = playgroundEntry.getValue();
                    GlobalPosition playgroundPosition = new GlobalPosition(playground.getLat() , playground.getLng(), 0.0);
                    double playgroundsDistance = geoCalc.calculateGeodeticCurve(reference, playgroundPosition, newPlaygroundLocationPosition).getEllipsoidalDistance();
                    return  playgroundsDistance < distance ? true:false;
                }).map(playgroundEntry -> playgroundEntry.getValue())
                .collect(Collectors.toList());
    }

    /**
     * It cross-check whether System has already holding the maximum allowed playgrounds
     * @return true: if system does not reach maximum playgrounds allowed
     * @throws RuntimeException : if System has already reached maximum playground allowed
     */
    public boolean checkForMaximumPlaygrounds() {
        if(playgrounds.size() < MAXIMUM_PLAYGROUNDS_ALLOWED ){
            return true;
        } else {
            throw new RuntimeException("MAX_PLAYGROUNDS: System is already loaded with " + SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED + " playgrounds");
        }
    }

    /**
     * It check for a playground name existance against playgrounds  before create a initiative
     * @param playgroundName
     * @throws  RuntimeException: if playground name is already exist
     */
    public void isPlaygroundAlreadyExist(String playgroundName) {
         playgrounds.entrySet()
                .stream()
                .filter( playgroundEntry -> playgroundEntry.getValue().getName().equals(playgroundName))
                .map(playgroundEntry -> playgroundEntry.getValue())
                .findFirst()
                .ifPresent( p -> {
                    throw new RuntimeException("PLAYGROUND_ALREADY_EXIST: Playground name " + playgroundName + " is already exist");
                });

    }

    /**
     * It cross-check the total volunters of a playgrond against the max allowed volunteers per playgrouns
     * @param id
     * @return
     */
    public boolean checkForMaximumVolunteers(String id) {
        return playgrounds.entrySet()
                .stream()
                .filter(playgroundEntry -> playgroundEntry.getValue().getId().equals(id))
                .flatMap(playgroundEntry -> playgroundEntry.getValue().getVolunteers().stream()).count() < MAXIMUM_VOLUNTEERS_PER_PLAYGROUND ? true :false;
    }

}
