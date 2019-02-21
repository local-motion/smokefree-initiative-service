package smokefree.projection;

import io.axoniq.axonserver.grpc.event.Event;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.messaging.MetaData;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;
import smokefree.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.*;
import static smokefree.domain.Status.*;

class InitiativeProjectionTest {

    @Test
    void should_return_created_initiatives() {
        InitiativeProjection projection = new InitiativeProjection();
        final InitiativeCreatedEvent initiative1 = initiativeCreated(in_progress);

        projection.on(initiative1);
        projection.on(initiativeCreated(in_progress));
        projection.on(initiativeCreated(not_started));
        projection.on(initiativeCreated(finished));
        projection.on(new CitizenJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-1"), null);
        projection.on(new CitizenJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-2"), null);
        assertEquals(4, projection.playgrounds(null).size());

        final Playground playground = projection.playground(initiative1.getInitiativeId(), null);
        assertNotNull(playground);
        assertEquals(in_progress, playground.getStatus());
        assertEquals(2, playground.getVolunteerCount());
    }

    @Test
    void should_calculate_percentage_and_absolute_numbers_based_on_status() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", in_progress));
        projection.on(initiativeCreated(in_progress));
        projection.on(initiativeCreated(in_progress));
        projection.on(initiativeCreated(in_progress));
        projection.on(initiativeCreated("initiative-2", not_started));
        projection.on(initiativeCreated(not_started));
        projection.on(initiativeCreated(finished));
        projection.on(initiativeCreated(finished));

        // Transition status and trigger progress recalculations
        projection.on(new InitiativeProgressedEvent("initiative-1", in_progress, finished));
        projection.on(new InitiativeProgressedEvent("initiative-2", not_started, in_progress));

        Progress progress = projection.progress();

        final Progress.Stat workingOnIt = progress.getWorkingOnIt();
        assertEquals(4, workingOnIt.getCount());
        assertEquals(50, workingOnIt.getPercentage());

        final Progress.Stat smokeFree = progress.getSmokeFree();
        assertEquals(3, smokeFree.getCount());
        assertEquals(37, smokeFree.getPercentage());

        final Progress.Stat smoking = progress.getSmoking();
        assertEquals(1, smoking.getCount());
        assertEquals(12, smoking.getPercentage());

        assertEquals(8, progress.getTotal());
        assertEquals(5, progress.getRemaining());
    }

    @Test
    void should_reflect_decisions_in_status_progression() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", not_started));
        projection.on(new InitiativeProgressedEvent("initiative-1", not_started, in_progress));
        assertEquals(in_progress, projection.playground("initiative-1", null).getStatus());

        projection.on(new InitiativeProgressedEvent("initiative-1", in_progress, finished));
        assertEquals(finished, projection.playground("initiative-1", null).getStatus());
    }

    @Test
    void should_expose_smokefree_date_when_committed() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", in_progress));
        assertEquals(in_progress, projection.playground("initiative-1", null).getStatus());

        projection.on(new InitiativeProgressedEvent("initiative-1", in_progress, finished));
        assertEquals(finished, projection.playground("initiative-1", null).getStatus());

        Playground playground = projection.playground("initiative-1", null);
        assertNotNull(playground);
        assertNull(playground.getSmokeFreeDate());

        LocalDate today = now();
        LocalDate tomorrow = now().plusDays(1);
//        projection.on(new SmokeFreeDateCommittedEvent("initiative-1", today, tomorrow), new DateTime());
        SmokeFreeDateCommittedEvent evt = new SmokeFreeDateCommittedEvent("initiative-1", today, tomorrow);
        projection.on(evt, new GenericEventMessage<>(evt));

        playground = projection.playground("initiative-1", null);
        assertEquals(finished, playground.getStatus());
        assertEquals(tomorrow, playground.getSmokeFreeDate());
    }
    @Test
    void should_store_managers_per_playground() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", in_progress));

        projection.on(new ManagerJoinedInitiativeEvent("initiative-1", "citizen-1"), MetaData
                .with("user_id", "manager-1")
                .and("user_name", "Jack Ma"));

        Playground playground = projection.playground("initiative-1", null);
        assertEquals(1, playground.getManagers().size());
        assertEquals(new Playground.Manager("manager-1", "Jack Ma"), playground.getManagers().get(0));
    }

    InitiativeCreatedEvent initiativeCreated(Status status) {
        return initiativeCreated(UUID.randomUUID().toString(), status);
    }

    private static final String PLAYGROUND_NAME_INITIATIVE_1 = "Happy Smokefree";
    private static final String PLAYGROUND_NAME_INITIATIVE_2 = "Happy Smokefree";
    InitiativeCreatedEvent initiativeCreated(String uuid, Status status) {
        return new InitiativeCreatedEvent(uuid, Type.smokefree, status, "Happy Smokefree", new GeoLocation(12.956314, 77.648635));
    }
    InitiativeCreatedEvent initiativeCreated(String uuid, Status status, GeoLocation location) {
        return new InitiativeCreatedEvent(uuid, Type.smokefree, status, "Happy Smokefree 2", location);
    }
    @Test
    void should_record_smokefreeplaygroundobservation() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", in_progress));
        projection.on(new PlaygroundObservationIndicatedEvent("initiative-1", "user_id", true, "I do not see anyone is smoking", LocalDate.now()), MetaData
                .with("user_id", "manager-1")
                .and("user_name", "Jack Ma"));
        Playground playground = projection.playground("initiative-1", null);
        assertEquals(1, playground.getPlaygroundObservations().size());
    }

    @Test
    void should_returnNearbyPlaygrounds_when_playgroundsAreLessThan100MetersClose() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", in_progress));
        projection.on(initiativeCreated("initiative-2", in_progress));
        List<Playground> playgrounds = projection.checkForNearByPlaygrounds(new GeoLocation(12.956314, 77.648635), SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE);
        assertEquals(2, playgrounds.size());
        assertEquals("Happy Smokefree", playgrounds.get(0).getName());
    }

    @Test
    void should_notReturnAnyPlaygrounds_when_playgroundsAreNotLessThan100MetersClose() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", in_progress));
        projection.on(initiativeCreated("initiative-2", in_progress, new GeoLocation(13.956314, 77.648635)));
        List<Playground> playgrounds = projection.checkForNearByPlaygrounds(new GeoLocation(14.956314, 77.648635), SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE);
        assertEquals(0, playgrounds.size());
    }

    @Test
    void should_notAllowToAddPlaygrounds_when_SystemHasAlready1000Playgrounds() {
        InitiativeProjection projection = new InitiativeProjection(2L);
        projection.on(initiativeCreated("initiative-1", in_progress));
        projection.on(initiativeCreated("initiative-2", in_progress, new GeoLocation(13.956314, 77.648635)));
        RuntimeException thrown =
                assertThrows(RuntimeException.class,
                        () -> projection.checkForMaximumPlaygrounds(),
                        "Expected checkForMaximumPlaygrounds() to throw, but it didn't");
        assertTrue(thrown.getMessage().contains("MAX_PLAYGROUNDS: System is already loaded with " + SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED + " playgrounds"));
    }

    @Test
    void should_throwException_when_playgroundNameAlreadyExist() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", in_progress));
        RuntimeException thrown =
                assertThrows(RuntimeException.class,
                        () -> projection.isPlaygroundAlreadyExist(PLAYGROUND_NAME_INITIATIVE_1),
                        "Expected isPlaygroundAlreadyExist() to throw, but it didn't");
        assertTrue(thrown.getMessage().contains("PLAYGROUND_ALREADY_EXIST: Playground name " + PLAYGROUND_NAME_INITIATIVE_1 + " is already exist"));

    }

    @Test
    void should_notAllowUserToJoinInitiative_when_playgroundReachedMaxVolunteersAllowed() {
        InitiativeProjection projection = new InitiativeProjection(2L, 2);
        projection.on(initiativeCreated("initiative-1", in_progress));
        projection.on(new CitizenJoinedInitiativeEvent("initiative-1", "citizen-1"), MetaData
                .with("user_id", "manager-1")
                .and("user_name", "Jack Ma"));
        projection.on(new CitizenJoinedInitiativeEvent("initiative-1", "citizen-2"), MetaData
                .with("user_id", "manager-1")
                .and("user_name", "Jack Ma"));
        assertFalse(projection.checkForMaximumVolunteers("initiative-1"));
    }

    @Test
    void should_notAllowUserToClaimManagerRole_when_playgroundHasAlreadyHadMaxManagers() {
        InitiativeProjection projection = new InitiativeProjection(2L, 2);
        projection.on(initiativeCreated("initiative-1", in_progress));
        projection.on(new ManagerJoinedInitiativeEvent("initiative-1", "citizen-1"), MetaData
                .with("user_id", "manager-1")
                .and("user_name", "Jack Ma"));
        projection.on(new ManagerJoinedInitiativeEvent("initiative-1", "citizen-2"), MetaData
                .with("user_id", "manager-2")
                .and("user_name", "Jack Ma"));
        projection.on(new ManagerJoinedInitiativeEvent("initiative-1", "citizen-3"), MetaData
                .with("user_id", "manager-3")
                .and("user_name", "Jack Ma"));
        assertFalse(projection.checkForMaximumManagers("initiative-1"));
    }
}