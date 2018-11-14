package smokefree.projection;

import org.junit.jupiter.api.Test;
import smokefree.domain.GeoLocation;
import smokefree.domain.InitiativeCreatedEvent;
import smokefree.domain.Status;
import smokefree.domain.Type;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InitiativeProjectionTest {

    @Test
    void progress() {
        InitiativeProjection projection = new InitiativeProjection();
        projection.on(initiativeCreated(Status.in_progress));
        projection.on(initiativeCreated(Status.in_progress));
        projection.on(initiativeCreated(Status.in_progress));
        projection.on(initiativeCreated(Status.in_progress));
        projection.on(initiativeCreated(Status.not_started));
        projection.on(initiativeCreated(Status.not_started));
        projection.on(initiativeCreated(Status.finished));
        projection.on(initiativeCreated(Status.finished));

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

    InitiativeCreatedEvent initiativeCreated(Status status) {
        return new InitiativeCreatedEvent(UUID.randomUUID().toString(), Type.smokefree, status, "Not relevant", new GeoLocation());
    }
}