package smokefree.projection;

import lombok.*;
import smokefree.domain.Status;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.*;

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
    Set<Volunteer> volunteers = new HashSet<>();
    List<Manager> managers = newArrayList();
    List<PlaygroundObservation> playgroundObservations = newArrayList();

    // Checklists are maintained both on user level and on the overall level
    // For both goes that the last update counts
    // Note that the checklist are actually sets containing those items that have been check. All other items are considered not to be checked.
    Set<String> jointChecklistItems = new HashSet<>();
    Map<String,Set<String>> individualChecklistItems = new HashMap<>();         // key = user
    Set<String> ownChecklistItems = null;                                       // see getPlaygroundForUser


    public Playground(String id, String name, Double lat, Double lng, Status status, LocalDate smokeFreeDate, int volunteerCount, int votes) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.smokeFreeDate = smokeFreeDate;
        this.volunteerCount = volunteerCount;
        this.votes = votes;
    }

    Playground addManager(Manager manager) {
        managers.add(manager);
        return this;
    }

    Playground addPlaygroundObservation(PlaygroundObservation playgroundObservation) {
        playgroundObservations.add(playgroundObservation);
        return this;
    }

    Playground setChecklistItem(String actor, String item, Boolean checked) {
        Set<String> actorChecklistItems = individualChecklistItems.containsKey(actor) ? individualChecklistItems.get(actor) : new HashSet<>();
        if (checked) {
            jointChecklistItems.add(item);
            actorChecklistItems.add(item);
        }
        else {
            jointChecklistItems.remove(item);
            actorChecklistItems.remove(item);
        }
        individualChecklistItems.put(actor, actorChecklistItems);
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
    public static class PlaygroundObservation {
        String userId;
        String userName;
        Boolean smokefree;
        LocalDate observationDate;
        String comment;
    }


    public boolean isVolunteer(String userId) {
        return volunteers.stream().anyMatch(volunteer -> volunteer.userId.equals(userId));
    }

    public boolean isManager(String userId) {
        return managers.stream().anyMatch(manager -> manager.id.equals(userId));
    }

    public boolean isParticipant(String userId) {
        return isVolunteer(userId) || isManager(userId);
    }

    /**
     * Some information in the playground should not be exposed to all users, such as the checkboxes of each individual user.
     * Therefore use this method to compute a playground object suitable to expose to a particular user
     *
     * @param userId id of the user to create the perspective for (leave null for unauthenticated users)
     * @return playground with a subset of the properties of this playground
     */
    public Playground getPlaygroundForUser(@Nullable String userId) {
        Set<String> usersChecklistItems = userId != null ? individualChecklistItems.get(userId) : Collections.emptySet();
        return new Playground(id, name, lat, lng, status, smokeFreeDate, volunteerCount, votes, volunteers, managers,
                                playgroundObservations, jointChecklistItems, null, usersChecklistItems);
    }
}
