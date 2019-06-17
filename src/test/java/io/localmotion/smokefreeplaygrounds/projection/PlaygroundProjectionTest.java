package io.localmotion.smokefreeplaygrounds.projection;

import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.domain.Status;
import io.localmotion.smokefreeplaygrounds.event.*;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.localmotion.smokefreeplaygrounds.domain.CreationStatus.*;
import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
class PlaygroundProjectionTest {

    @Inject
    PlaygroundProjection playgroundProjection;


    @Test
    void should_return_created_initiatives() {
        PlaygroundProjection projection = new PlaygroundProjection();

        final PlaygroundInitiativeCreatedEvent initiative1 = triggerInitiativeCreatedEvent(projection, "initiative-1",ONLINE_NOT_STARTED);
        triggerInitiativeCreatedEvent(projection, "initiative-2",ONLINE_NOT_STARTED);
        triggerInitiativeCreatedEvent(projection, "initiative-3", IMPORT_NOT_STARTED);
        triggerInitiativeCreatedEvent(projection, "initiative-2", IMPORT_FINISHED);

        MemberJoinedInitiativeEvent joined1 = new MemberJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-1");
        projection.on(joined1, null, getMessageForEvent(joined1));

        MemberJoinedInitiativeEvent joined2 = new MemberJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-2");
        projection.on(joined2, null, getMessageForEvent(joined2));

        final Playground initiative = projection.playground(initiative1.getInitiativeId(), null);
        assertNotNull(initiative);
        // TO-DO expected after java refacor
        //assertEquals(ONLINE_NOT_STARTED, initiative.getStatus());
        assertEquals(Status.NOT_STARTED, initiative.getStatus());
        assertEquals(2, initiative.getVolunteerCount());
    }

    @Test
    void should_expose_smokefree_date_when_committed() {
        PlaygroundProjection projection = new PlaygroundProjection();

        triggerInitiativeCreatedEvent(projection, "initiative-1", ONLINE_NOT_STARTED);

        SmokeFreeDecisionEvent smokeFreeDecisionEvent = new SmokeFreeDecisionEvent("initiative-1", true);
        assertEquals(Status.NOT_STARTED, projection.playground("initiative-1", null).getStatus());

        Playground initiative = projection.playground("initiative-1", null);
        assertNotNull(initiative);
        assertNull(initiative.getSmokeFreeDate());

        LocalDate today = now();
        LocalDate tomorrow = now().plusDays(1);

        SmokeFreeDateCommittedEvent committedEvent = new SmokeFreeDateCommittedEvent("initiative-1", tomorrow);
        projection.on(committedEvent, new GenericEventMessage<>(committedEvent));

        initiative = projection.playground("initiative-1", null);
        assertEquals(Status.FINISHED, initiative.getStatus());
        assertEquals(tomorrow, initiative.getSmokeFreeDate());
    }

    @Test
    void should_store_managers_per_playground() {
        PlaygroundProjection projection = this.playgroundProjection; //new PlaygroundProjection();
        triggerInitiativeCreatedEvent(projection, "initiative-1", IMPORT_NOT_STARTED);
        // when(profileProjection.profile("manager-1")).thenReturn(new AdminJobCommandRecord("manager-1", "Jack Ma", null));

        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("user_id", "manager-1");
        metadataMap.put("user_name", "Jack Ma");

        ManagerJoinedInitiativeEvent managerjoinedEvent =
                new ManagerJoinedInitiativeEvent("initiative-1", "manager-1");

        EventMessage<?> managerjoinedEventMessage = getMessageForEvent(managerjoinedEvent, metadataMap);
        projection.on(managerjoinedEvent, managerjoinedEventMessage.getMetaData(), managerjoinedEventMessage);


        Playground initiative = projection.playground("initiative-1", null);
        // assertEquals(1, initiative.getManagers().size());
        assertEquals(1, initiative.getManagerIds().size());
        // TO-DO Mock ProfileProjection and then enable below line
        //assertEquals(new Playground.Manager("manager-1", "Jack Ma"), initiative.getManagers().get(0));
    }

    @Test
    void should_record_smokefreeplaygroundobservation() {
        PlaygroundProjection projection = new PlaygroundProjection();


        PlaygroundInitiativeCreatedEvent initiativeCreatedEvent = initiativeCreated("initiative-1", ONLINE_NOT_STARTED, new GeoLocation());
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
        //assertEquals(1, initiative.getPlaygroundObservations().size());
        assertEquals(1, initiative.getPlaygroundObservationsInternal().size());
    }


    /*
        Helpers
     */

    private PlaygroundInitiativeCreatedEvent triggerInitiativeCreatedEvent(PlaygroundProjection projection, CreationStatus creationStatus) {
        return triggerInitiativeCreatedEvent(projection, UUID.randomUUID().toString(), creationStatus, new GeoLocation());
    }
    private PlaygroundInitiativeCreatedEvent triggerInitiativeCreatedEvent(PlaygroundProjection projection, String uuid, CreationStatus creationStatus) {
        PlaygroundInitiativeCreatedEvent event = initiativeCreated(uuid, creationStatus, new GeoLocation());
        projection.on(event, getMessageForEvent(event));
        return event;
    }
    private PlaygroundInitiativeCreatedEvent triggerInitiativeCreatedEvent(PlaygroundProjection projection, String uuid, CreationStatus creationStatus, GeoLocation location) {
        PlaygroundInitiativeCreatedEvent event = initiativeCreated(uuid, creationStatus, location);
        projection.on(event, getMessageForEvent(event));
        return event;
    }


    private static final String PLAYGROUND_NAME_INITIATIVE_1 = "Happy Smokefree";

    PlaygroundInitiativeCreatedEvent initiativeCreated(CreationStatus creationStatus) {
        return initiativeCreated(UUID.randomUUID().toString(), creationStatus, new GeoLocation());
    }
    PlaygroundInitiativeCreatedEvent initiativeCreated(String uuid, CreationStatus creationStatus, GeoLocation location) {
        return new PlaygroundInitiativeCreatedEvent(uuid, PLAYGROUND_NAME_INITIATIVE_1, creationStatus, location);
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

    @MockBean(ProfileProjection.class)
    ProfileProjection profileProjection() {
        return mock(ProfileProjection.class);
    }


}