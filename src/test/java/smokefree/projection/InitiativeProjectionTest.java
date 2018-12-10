package smokefree.projection;

import org.junit.jupiter.api.Test;
import smokefree.domain.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        projection.on(new CitizenJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-1"));
        projection.on(new CitizenJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-2"));
        assertEquals(4, projection.playgrounds().size());

        final Playground playground = projection.playground(initiative1.getInitiativeId());
        assertNotNull(playground);
        assertEquals(in_progress, playground.getStatus());
        assertEquals(2, playground.getVolunteerCount());
    }

    @Test
    void should_calculate_percentage_and_absolute_numbers_based_on_status() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated(in_progress));
        projection.on(initiativeCreated(in_progress));
        projection.on(initiativeCreated(in_progress));
        projection.on(initiativeCreated(in_progress));
        projection.on(initiativeCreated(not_started));
        projection.on(initiativeCreated(not_started));
        projection.on(initiativeCreated(finished));
        projection.on(initiativeCreated(finished));

        Progress progress = projection.progress();

        final Progress.Stat workingOnIt = progress.getWorkingOnIt();
        assertEquals(4, workingOnIt.getCount());
        assertEquals(50d, workingOnIt.getPercentage());

        final Progress.Stat smokeFree = progress.getSmokeFree();
        assertEquals(2, smokeFree.getCount());
        assertEquals(25d, smokeFree.getPercentage());

        final Progress.Stat smoking = progress.getSmoking();
        assertEquals(2, smoking.getCount());
        assertEquals(25d, smoking.getPercentage());

        assertEquals(8, progress.getTotal());
        assertEquals(6, progress.getRemaining());
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

    InitiativeCreatedEvent initiativeCreated(Status status) {
        return initiativeCreated(UUID.randomUUID().toString(), status);
    }

    InitiativeCreatedEvent initiativeCreated(String uuid, Status status) {
        return new InitiativeCreatedEvent(uuid, Type.smokefree, status, "Not relevant", new GeoLocation());
    }
}