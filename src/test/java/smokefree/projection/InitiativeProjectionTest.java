package smokefree.projection;

import org.junit.jupiter.api.Test;
import smokefree.domain.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

class InitiativeProjectionTest {

    @Test
    void playgrounds() {
        InitiativeProjection projection = new InitiativeProjection();
        final InitiativeCreatedEvent initiative1 = initiativeCreated(Status.in_progress);

        projection.on(initiative1);
        projection.on(initiativeCreated(Status.in_progress));
        projection.on(initiativeCreated(Status.not_started));
        projection.on(initiativeCreated(Status.finished));
        projection.on(new CitizenJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-1"));
        projection.on(new CitizenJoinedInitiativeEvent(initiative1.getInitiativeId(), "citizen-2"));


        final Playground playground = projection
                .playgrounds()
                .stream()
                .filter(p -> p.getId().equals(initiative1.getInitiativeId()))
                .findFirst()
                .orElseThrow();
        assertEquals(2, playground.getVolunteerCount());
    }

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