package smokefree.projection;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.jupiter.api.Test;
import smokefree.domain.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.*;
import static smokefree.domain.Status.*;

class InitiativeProjectionTest {

    @Test
    void should_return_created_initiatives() {
        InitiativeProjection projection = new InitiativeProjection();

        final InitiativeCreatedEvent initiative1 = triggerInitiativeCreatedEvent(projection, in_progress);
        triggerInitiativeCreatedEvent(projection, in_progress);
        triggerInitiativeCreatedEvent(projection, not_started);
        triggerInitiativeCreatedEvent(projection, finished);

        CitizenJoinedInitiativeEvent joined1 = new CitizenJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-1");
        projection.on(joined1, null, getMessageForEvent(joined1));

        CitizenJoinedInitiativeEvent joined2 = new CitizenJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-2");
        projection.on(joined1, null, getMessageForEvent(joined2));

        final Playground playground = projection.playground(initiative1.getInitiativeId(), null);
        assertNotNull(playground);
        assertEquals(in_progress, playground.getStatus());
        assertEquals(2, playground.getVolunteerCount());
    }

    @Test
    void should_calculate_percentage_and_absolute_numbers_based_on_status() {
        InitiativeProjection projection = new InitiativeProjection();


        triggerInitiativeCreatedEvent(projection, "initiative-1", in_progress);
        triggerInitiativeCreatedEvent(projection, in_progress);
        triggerInitiativeCreatedEvent(projection, in_progress);
        triggerInitiativeCreatedEvent(projection, in_progress);

        triggerInitiativeCreatedEvent(projection, "initiative-2", not_started);
        triggerInitiativeCreatedEvent(projection, not_started);
        triggerInitiativeCreatedEvent(projection, finished);
        triggerInitiativeCreatedEvent(projection, finished);

        // Transition status and trigger progress recalculations
        InitiativeProgressedEvent progressedEvent1 = new InitiativeProgressedEvent("initiative-1", in_progress, finished);
        projection.on(progressedEvent1, getMessageForEvent(progressedEvent1));

        InitiativeProgressedEvent progressedEvent2 = new InitiativeProgressedEvent("initiative-2", not_started, in_progress);
        projection.on(progressedEvent2, getMessageForEvent(progressedEvent2));

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

        triggerInitiativeCreatedEvent(projection, "initiative-1", not_started);

        InitiativeProgressedEvent progressedEvent1 = new InitiativeProgressedEvent("initiative-1", not_started, in_progress);
        projection.on(progressedEvent1, getMessageForEvent(progressedEvent1));

        assertEquals(in_progress, projection.playground("initiative-1", null).getStatus());

        InitiativeProgressedEvent progressedEvent2 = new InitiativeProgressedEvent("initiative-1", in_progress, finished);
        projection.on(progressedEvent2, getMessageForEvent(progressedEvent2));

        assertEquals(finished, projection.playground("initiative-1", null).getStatus());
    }

    @Test
    void should_expose_smokefree_date_when_committed() {
        InitiativeProjection projection = new InitiativeProjection();

        triggerInitiativeCreatedEvent(projection, "initiative-1", in_progress);
        assertEquals(in_progress, projection.playground("initiative-1", null).getStatus());

        InitiativeProgressedEvent progressedEvent1 = new InitiativeProgressedEvent("initiative-1", in_progress, finished);
        projection.on(progressedEvent1, getMessageForEvent(progressedEvent1));
        assertEquals(finished, projection.playground("initiative-1", null).getStatus());

        Playground playground = projection.playground("initiative-1", null);
        assertNotNull(playground);
        assertNull(playground.getSmokeFreeDate());

        LocalDate today = now();
        LocalDate tomorrow = now().plusDays(1);
//        projection.on(new SmokeFreeDateCommittedEvent("initiative-1", today, tomorrow), new DateTime());

        SmokeFreeDateCommittedEvent committedEvent = new SmokeFreeDateCommittedEvent("initiative-1", today, tomorrow);
        projection.on(committedEvent, new GenericEventMessage<>(committedEvent));

        playground = projection.playground("initiative-1", null);
        assertEquals(finished, playground.getStatus());
        assertEquals(tomorrow, playground.getSmokeFreeDate());
    }

    @Test
    void should_store_managers_per_playground() {
        InitiativeProjection projection = new InitiativeProjection();
//        projection.on(initiativeCreated("initiative-1", in_progress));
        triggerInitiativeCreatedEvent(projection, "initiative-1", in_progress);


        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("user_id", "manager-1");
        metadataMap.put("user_name", "Jack Ma");

        ManagerJoinedInitiativeEvent managerjoinedEvent =
                new ManagerJoinedInitiativeEvent("initiative-1", "citizen-1");

        EventMessage<?> managerjoinedEventMessage = getMessageForEvent(managerjoinedEvent, metadataMap);
        projection.on(managerjoinedEvent, managerjoinedEventMessage.getMetaData(), managerjoinedEventMessage);


//        projection.on(new ManagerJoinedInitiativeEvent("initiative-1", "citizen-1"), MetaData
//                .with("user_id", "manager-1")
//                .and("user_name", "Jack Ma"));

        Playground playground = projection.playground("initiative-1", null);
        assertEquals(1, playground.getManagers().size());
        assertEquals(new Playground.Manager("manager-1", "Jack Ma"), playground.getManagers().get(0));
    }

    @Test
    void should_record_smokefreeplaygroundobservation() {
        InitiativeProjection projection = new InitiativeProjection();

        InitiativeCreatedEvent initiativeCreatedEvent = initiativeCreated("initiative-1", in_progress, new GeoLocation());
        projection.on(initiativeCreatedEvent, getMessageForEvent(initiativeCreatedEvent));
        projection.on(initiativeCreatedEvent, getMessageForEvent(initiativeCreatedEvent));

        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("user_id", "manager-1");
        metadataMap.put("cognito:username", "Jack Ma");

        PlaygroundObservationEvent playgroundObservationEvent =
                new PlaygroundObservationEvent("initiative-1", "user_id", true, "I do not see anyone is smoking", LocalDate.now());

        EventMessage<?> playgroundObservationEventMessage = getMessageForEvent(playgroundObservationEvent, metadataMap);
        projection.on(playgroundObservationEvent, playgroundObservationEventMessage.getMetaData(), playgroundObservationEventMessage);

//        projection.on(playgroundObservationEvent, MetaData
//                .with("user_id", "manager-1")
//                .and("user_name", "Jack Ma")
//                , getMessageForEvent(playgroundObservationEvent));

        Playground playground = projection.playground("initiative-1", null);
        assertEquals(1, playground.getPlaygroundObservations().size());
    }


    /*
        Helpers
     */

    private InitiativeCreatedEvent triggerInitiativeCreatedEvent(InitiativeProjection projection, Status status) {
        return triggerInitiativeCreatedEvent(projection, UUID.randomUUID().toString(), status, new GeoLocation());
    }
    private InitiativeCreatedEvent triggerInitiativeCreatedEvent(InitiativeProjection projection, String uuid, Status status) {
        InitiativeCreatedEvent event = initiativeCreated(uuid, status, new GeoLocation());
        projection.on(event, getMessageForEvent(event));
        return event;
    }
    private InitiativeCreatedEvent triggerInitiativeCreatedEvent(InitiativeProjection projection, String uuid, Status status, GeoLocation location) {
        InitiativeCreatedEvent event = initiativeCreated(uuid, status,location);
        projection.on(event, getMessageForEvent(event));
        return event;
    }


    private static final String PLAYGROUND_NAME_INITIATIVE_1 = "Happy Smokefree";
    private static final String PLAYGROUND_NAME_INITIATIVE_2 = "Happy Smokefree 2";

    InitiativeCreatedEvent initiativeCreated(Status status) {
        return initiativeCreated(UUID.randomUUID().toString(), status, new GeoLocation());
    }
    InitiativeCreatedEvent initiativeCreated(String uuid, Status status, GeoLocation location) {
        return new InitiativeCreatedEvent(uuid, Type.smokefree, status, PLAYGROUND_NAME_INITIATIVE_1, location);
    }


    /*
        Utilities
     */

    private <T> EventMessage<T> getMessageForEvent(T event) {
        return new GenericEventMessage<T>(event);
    }
    private <T> EventMessage<T> getMessageForEvent(T event, Map<String, ?> metadataMap) {
        return new GenericEventMessage<T>(event, metadataMap);
    }
}