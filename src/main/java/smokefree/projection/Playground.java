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
    final PlaygroundObservations playgroundObservations;
    final Set<Volunteer> volunteers = new HashSet<>();


    Playground addManager(Manager manager) {
        managers.add(manager);
        return this;
    }

    Playground addPlaygroundObservation(Observation observation) {
        playgroundObservations.getVolunteersObservations().add(observation);
        return this;
    }

    @AllArgsConstructor
    static class User {
        @Getter @Setter String userId;
        @Getter @Setter String userName;
    }

    /*
      later Manager class will be extending User class,
      and we need to make changes from front-end to back-end , because its being already used.
     */
    @Value
    static class Manager {
        String id;
        String username;
    }

    @Value
    public static class Volunteer extends User {
        public Volunteer(String userId, String userName) {
            super(userId, userName);
        }

        @Override
        public String toString() {
            return "[" + this.getUserId() + "," + this.getUserName() +"]";
        }

        @Override
        public boolean equals(Object volunteer) {
            return this.getUserName().compareTo(((Volunteer)volunteer).getUserName()) == 0;

        }

        @Override
        public int hashCode() {
            return this.getUserName().hashCode();
        }
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
