package io.localmotion.initiative.projection;

import io.localmotion.initiative.domain.GeoLocation;
import io.localmotion.initiative.domain.Status;
import io.localmotion.initiative.domain.Type;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.event.ManagerJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.event.PlaygroundInitiativeCreatedEvent;
import io.localmotion.smokefreeplaygrounds.event.PlaygroundObservationEvent;
import io.localmotion.smokefreeplaygrounds.event.SmokeFreeDateCommittedEvent;
import io.localmotion.smokefreeplaygrounds.projection.Playground;
import io.localmotion.smokefreeplaygrounds.projection.PlaygroundProjection;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.*;
import static io.localmotion.initiative.domain.Status.*;

class InitiativeProjectionTest {

    @Test
    void should_return_created_initiatives() {
        InitiativeProjection projection = new InitiativeProjection();

        final PlaygroundInitiativeCreatedEvent initiative1 = triggerInitiativeCreatedEvent(projection, in_progress);
        triggerInitiativeCreatedEvent(projection, in_progress);
        triggerInitiativeCreatedEvent(projection, not_started);
        triggerInitiativeCreatedEvent(projection, finished);

        MemberJoinedInitiativeEvent joined1 = new MemberJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-1");
        projection.on(joined1, null, getMessageForEvent(joined1));

        MemberJoinedInitiativeEvent joined2 = new MemberJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-2");
        projection.on(joined1, null, getMessageForEvent(joined2));

        final Initiative initiative = projection.playground(initiative1.getInitiativeId(), null);
        assertNotNull(initiative);
        assertEquals(in_progress, initiative.getStatus());
        assertEquals(2, initiative.getVolunteerCount());
    }

    @Test
    void should_expose_smokefree_date_when_committed() {
        InitiativeProjection projection = new InitiativeProjection();

        triggerInitiativeCreatedEvent(projection, "initiative-1", in_progress);
        assertEquals(in_progress, projection.playground("initiative-1", null).getStatus());

        Initiative initiative = projection.playground("initiative-1", null);
        assertNotNull(initiative);
        assertNull(initiative.getSmokeFreeDate());

        LocalDate today = now();
        LocalDate tomorrow = now().plusDays(1);

        SmokeFreeDateCommittedEvent committedEvent = new SmokeFreeDateCommittedEvent("initiative-1", tomorrow);
        projection.on(committedEvent, new GenericEventMessage<>(committedEvent));

        initiative = projection.playground("initiative-1", null);
        assertEquals(finished, initiative.getStatus());
        assertEquals(tomorrow, initiative.getSmokeFreeDate());
    }

    @Test
    void should_store_managers_per_playground() {
        InitiativeProjection projection = new InitiativeProjection();
        triggerInitiativeCreatedEvent(projection, "initiative-1", in_progress);


        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("user_id", "manager-1");
        metadataMap.put("user_name", "Jack Ma");

        ManagerJoinedInitiativeEvent managerjoinedEvent =
                new ManagerJoinedInitiativeEvent("initiative-1", "citizen-1");

        EventMessage<?> managerjoinedEventMessage = getMessageForEvent(managerjoinedEvent, metadataMap);
        projection.on(managerjoinedEvent, managerjoinedEventMessage.getMetaData(), managerjoinedEventMessage);


        Initiative initiative = projection.playground("initiative-1", null);
        assertEquals(1, initiative.getManagers().size());
        assertEquals(new Initiative.Manager("manager-1", "Jack Ma"), initiative.getManagers().get(0));
    }

    @Test
    void should_record_smokefreeplaygroundobservation() {
        PlaygroundProjection projection = new PlaygroundProjection();

        PlaygroundInitiativeCreatedEvent initiativeCreatedEvent = initiativeCreated("initiative-1", in_progress, new GeoLocation());
        projection.on(initiativeCreatedEvent, getMessageForEvent(initiativeCreatedEvent));
        projection.on(initiativeCreatedEvent, getMessageForEvent(initiativeCreatedEvent));

        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("user_id", "manager-1");
        metadataMap.put("cognito:username", "Jack Ma");

        PlaygroundObservationEvent playgroundObservationEvent =
                new PlaygroundObservationEvent("initiative-1", "user_id", true, "I do not see anyone is smoking", LocalDate.now());

        EventMessage<?> playgroundObservationEventMessage = getMessageForEvent(playgroundObservationEvent, metadataMap);
        projection.on(playgroundObservationEvent, playgroundObservationEventMessage.getMetaData(), playgroundObservationEventMessage);

        Playground initiative = projection.playground("initiative-1", null);
        assertEquals(1, initiative.getPlaygroundObservations().size());
    }

//    @Test
//    void should_record_smokefreeplaygroundobservation() {
//        InitiativeProjection projection = new InitiativeProjection();
//
//        PlaygroundInitiativeCreatedEvent initiativeCreatedEvent = initiativeCreated("initiative-1", in_progress, new GeoLocation());
//        projection.on(initiativeCreatedEvent, getMessageForEvent(initiativeCreatedEvent));
//        projection.on(initiativeCreatedEvent, getMessageForEvent(initiativeCreatedEvent));
//
//        Map<String, String> metadataMap = new HashMap<>();
//        metadataMap.put("user_id", "manager-1");
//        metadataMap.put("cognito:username", "Jack Ma");
//
//        PlaygroundObservationEvent playgroundObservationEvent =
//                new PlaygroundObservationEvent("initiative-1", "user_id", true, "I do not see anyone is smoking", LocalDate.now());
//
//        EventMessage<?> playgroundObservationEventMessage = getMessageForEvent(playgroundObservationEvent, metadataMap);
////        projection.on(playgroundObservationEvent, playgroundObservationEventMessage.getMetaData(), playgroundObservationEventMessage);
//        projection.on(playgroundObservationEvent, playgroundObservationEventMessage);
//
//        Initiative initiative = projection.playground("initiative-1", null);
//        assertEquals(1, initiative.getPlaygroundObservations().size());
//    }
//

    /*
        Helpers
     */

    private PlaygroundInitiativeCreatedEvent triggerInitiativeCreatedEvent(InitiativeProjection projection, Status status) {
        return triggerInitiativeCreatedEvent(projection, UUID.randomUUID().toString(), status, new GeoLocation());
    }
    private PlaygroundInitiativeCreatedEvent triggerInitiativeCreatedEvent(InitiativeProjection projection, String uuid, Status status) {
        PlaygroundInitiativeCreatedEvent event = initiativeCreated(uuid, status, new GeoLocation());
        projection.on(event, getMessageForEvent(event));
        return event;
    }
    private PlaygroundInitiativeCreatedEvent triggerInitiativeCreatedEvent(InitiativeProjection projection, String uuid, Status status, GeoLocation location) {
        PlaygroundInitiativeCreatedEvent event = initiativeCreated(uuid, status,location);
        projection.on(event, getMessageForEvent(event));
        return event;
    }


    private static final String PLAYGROUND_NAME_INITIATIVE_1 = "Happy Smokefree";
    private static final String PLAYGROUND_NAME_INITIATIVE_2 = "Happy Smokefree 2";

    PlaygroundInitiativeCreatedEvent initiativeCreated(Status status) {
        return initiativeCreated(UUID.randomUUID().toString(), status, new GeoLocation());
    }
    PlaygroundInitiativeCreatedEvent initiativeCreated(String uuid, Status status, GeoLocation location) {
        return new PlaygroundInitiativeCreatedEvent(uuid, Type.smokefree, status, PLAYGROUND_NAME_INITIATIVE_1, location);
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