package smokefree.projection;

import org.axonframework.messaging.MetaData;
import org.junit.jupiter.api.Test;
import smokefree.domain.*;

import java.time.LocalDate;
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
        assertEquals(4, projection.playgrounds().size());

        final Playground playground = projection.playground(initiative1.getInitiativeId());
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
        assertEquals(in_progress, projection.playground("initiative-1").getStatus());

        projection.on(new InitiativeProgressedEvent("initiative-1", in_progress, finished));
        assertEquals(finished, projection.playground("initiative-1").getStatus());
    }

    @Test
    void should_expose_smokefree_date_when_committed() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", in_progress));
        assertEquals(in_progress, projection.playground("initiative-1").getStatus());

        projection.on(new InitiativeProgressedEvent("initiative-1", in_progress, finished));
        assertEquals(finished, projection.playground("initiative-1").getStatus());

        Playground playground = projection.playground("initiative-1");
        assertNotNull(playground);
        assertNull(playground.getSmokeFreeDate());

        LocalDate today = now();
        LocalDate tomorrow = now().plusDays(1);
        projection.on(new SmokeFreeDateCommittedEvent("initiative-1", today, tomorrow));

        playground = projection.playground("initiative-1");
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

        Playground playground = projection.playground("initiative-1");
        assertEquals(1, playground.getManagers().size());
        assertEquals(new Playground.Manager("manager-1", "Jack Ma"), playground.getManagers().get(0));
    }

    InitiativeCreatedEvent initiativeCreated(Status status) {
        return initiativeCreated(UUID.randomUUID().toString(), status);
    }

    InitiativeCreatedEvent initiativeCreated(String uuid, Status status) {
        return new InitiativeCreatedEvent(uuid, Type.smokefree, status, "Not relevant", new GeoLocation());
    }
    @Test
    void should_record_smokefreeplaygroundobservation() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated("initiative-1", in_progress));
        projection.on(new ManagerJoinedInitiativeEvent("initiative-1", "citizen-1"), MetaData
                .with("user_id", "manager-1")
                .and("user_name", "Jack Ma"));
        Playground playground = projection.playground("initiative-1");
        assertEquals(1, playground.getManagers().size());
        //assertEquals(new Playground.Manager("manager-1", "Jack Ma"), playground.getManagers().get(0));
    }
}