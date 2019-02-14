package smokefree.projection;

import lombok.*;
import smokefree.domain.Status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    final List<PlaygroundObservation> playgroundObservations = newArrayList();
    final Set<Volunteer> volunteers = new HashSet<>();


    Playground addManager(Manager manager) {
        managers.add(manager);
        return this;
    }

    Playground addPlaygroundObservation(PlaygroundObservation playgroundObservation) {
        playgroundObservations.add(playgroundObservation);
        return this;
    }

    @Value
    static class Manager {
        String id;
        String username;
    }

    @Value
    public static class Volunteer {
        String userId;
        String userName;
    }

    @Value
    public static class PlaygroundObservation{
        String userId;
        String userName;
        Boolean smokefree;
        LocalDate observationDate;
        String comment;
    }
}
