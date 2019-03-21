package smokefree.projection;

import lombok.Data;
import lombok.Value;
import org.axonframework.eventhandling.EventMessage;
import smokefree.domain.Status;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

@Data
public class Playground {
    private final String id;
    private String name;
    private Double lat;
    private Double lng;
    private Status status;
    private LocalDate smokeFreeDate;
    private int votes;
    private Set<Volunteer> volunteers = new HashSet<>();
    private List<Manager> managers = newArrayList();
    private List<PlaygroundObservation> playgroundObservations = newArrayList();

    // Checklists are maintained both on user level and on the overall level
    // For both goes that the last update counts
    // Note that the checklist are actually sets containing those items that have been check. All other items are considered not to be checked.
    private Set<String> jointChecklistItems = new HashSet<>();
    private Map<String,Set<String>> individualChecklistItems = new HashMap<>();         // key = user
    private Set<String> ownChecklistItems = null;                                       // see getPlaygroundForUser


    // Internal fields
    private EventMessage<?> lastEventMessage = null;

    // Deduced properties
    public int getVolunteerCount() {
        return volunteers.size();
    }
    public Date getLastUpdateTimestamp() {
        return lastEventMessage != null ? new Date(lastEventMessage.getTimestamp().toEpochMilli()) : new Date();    // TODO this will be a required field, so remove the null check after DB is cleared
    }


    /*
        Value classes
     */

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
        String observerId;
        String observerName;
        Boolean smokefree;
        LocalDate observationDate;
        String comment;
    }


    /*
        Constructors
     */

    public Playground(String id, String name, Double lat, Double lng, Status status, LocalDate smokeFreeDate, int votes, EventMessage<?> lastEventMessage) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.smokeFreeDate = smokeFreeDate;
        this.votes = votes;
        this.lastEventMessage = lastEventMessage;
    }

    public Playground(String id, String name, Double lat, Double lng, Status status, LocalDate smokeFreeDate, int votes,
                      Set<Volunteer> volunteers, List<Manager> managers, List<PlaygroundObservation> playgroundObservations,
                      Set<String> jointChecklistItems, Set<String> ownChecklistItems, EventMessage<?> lastEventMessage) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.smokeFreeDate = smokeFreeDate;
        this.votes = votes;
        this.volunteers = volunteers;
        this.managers = managers;
        this.playgroundObservations = playgroundObservations;
        this.jointChecklistItems = jointChecklistItems;
        this.ownChecklistItems = ownChecklistItems;
        this.lastEventMessage = lastEventMessage;
    }


    /*
        Update methods
     */

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

    void setMetaData(EventMessage<?> lastEventMessage) {
        this.lastEventMessage = lastEventMessage;
    }


    /*
        Retrieval methods
     */

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
        Set<String> usersChecklistItems = userId != null && individualChecklistItems.containsKey(userId) ?
                                                    individualChecklistItems.get(userId) : Collections.emptySet();
        return new Playground(id, name, lat, lng, status, smokeFreeDate, votes, volunteers, managers,
                                playgroundObservations, jointChecklistItems, usersChecklistItems, lastEventMessage);
    }
}
