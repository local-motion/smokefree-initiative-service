package smokefree.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import smokefree.domain.Status;

import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Data
@AllArgsConstructor
public class Playground {
    String id;
    String name;
    Double lat;
    Double lng;
    Status status;
    LocalDate smokeFreeDate;
    int volunteerCount;
    int votes;
    final List<Manager> managers = newArrayList();
    final PlaygroundObservations playgroundObservations;


    Playground addManager(Manager manager) {
        managers.add(manager);
        return this;
    }

    Playground addPlaygroundObservation(Observation observation) {
        playgroundObservations.getVolunteersObservations().add(observation);
        return this;
    }

    @Value
    static class Manager {
        String id;
        String username;
    }

    @Data
    public static class PlaygroundObservations{
        int smokefreeObservationsCount;
        int smokeObservationCount;
        int smokefreeConsecutiveStreak;
        final List<Observation> volunteersObservations = newArrayList();
    }

    @Value
    static class Observation {
        String citizenId;
        String userName;
        Boolean isSmokeFree;
        LocalDate observationDate;
        String recordedObservation;
    }
}
