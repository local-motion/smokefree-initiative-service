package io.localmotion.initiative.projection;

import io.localmotion.initiative.domain.Status;
import io.localmotion.user.projection.ProfileProjection;
import lombok.Data;
import lombok.Value;
import org.axonframework.eventhandling.EventMessage;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Data
public class Initiative {

    private final ProfileProjection profileProjection;

    private final String id;
    private String name;
    private Double lat;
    private Double lng;
    private Status status;
    private int votes;
    private Set<String> volunteerIds = new HashSet<>();

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
        return volunteerIds.size();
    }

    public Date getLastUpdateTimestamp() {
        return lastEventMessage != null ? new Date(lastEventMessage.getTimestamp().toEpochMilli()) : new Date();    // TODO this will be a required field, so remove the null check after DB is cleared
    }

    public Set<Volunteer> getVolunteers() {
        return volunteerIds.stream().map(id -> new Volunteer(id, profileProjection.profile(id) != null ? profileProjection.profile(id).getUsername() : " ** onbekend **" )).collect(Collectors.toSet());
    }


    /*
        Value classes
     */

    @Value
    public static class Volunteer {
        String userId;
        String userName;
    }



    /*
        Constructors
     */

    public Initiative(ProfileProjection profileProjection, String id, String name, Double lat, Double lng, Status status, int votes, EventMessage<?> lastEventMessage) {
        this.profileProjection = profileProjection;
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.votes = votes;
        this.lastEventMessage = lastEventMessage;
    }

    public Initiative(ProfileProjection profileProjection,
                      String id, String name, Double lat, Double lng, Status status, int votes,
                      Set<String> volunteerIds,
                      Set<String> jointChecklistItems, Set<String> ownChecklistItems, EventMessage<?> lastEventMessage) {
        this.profileProjection = profileProjection;
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.votes = votes;
        this.volunteerIds = volunteerIds;
        this.jointChecklistItems = jointChecklistItems;
        this.ownChecklistItems = ownChecklistItems;
        this.lastEventMessage = lastEventMessage;
    }



    /*
        Update methods
     */

    Initiative setChecklistItem(String actor, String item, Boolean checked) {
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
        return volunteerIds.contains(userId);
    }


    /**
     * Some information in the playground should not be exposed to all users, such as the checkboxes of each individual user.
     * Therefore use this method to compute a playground object suitable to expose to a particular user
     *
     * @param userId id of the user to create the perspective for (leave null for unauthenticated users)
     * @return playground with a subset of the properties of this playground
     */
    public Initiative getPlaygroundForUser(@Nullable String userId) {
        Set<String> usersChecklistItems = userId != null && individualChecklistItems.containsKey(userId) ?
                                                    individualChecklistItems.get(userId) : Collections.emptySet();
        return new Initiative(  profileProjection, id, name, lat, lng, status, votes, volunteerIds,
                                jointChecklistItems, usersChecklistItems, lastEventMessage);
    }
}
