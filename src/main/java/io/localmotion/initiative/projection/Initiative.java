package io.localmotion.initiative.projection;

import io.localmotion.user.projection.ProfileProjection;
import io.netty.util.internal.ConcurrentSet;
import lombok.Data;
import lombok.Value;
import org.axonframework.eventhandling.EventMessage;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


@Data
public class Initiative {

    private final ProfileProjection profileProjection;

    private final String id;
    private String name;
    private Double lat;
    private Double lng;
    private String status;
    private int votes;
    private Set<String> memberIds = new HashSet<>();

    // Checklists are maintained both on user level and on the overall level
    // For both goes that the last update counts
    // Note that the checklist are actually sets containing those items that have been check. All other items are considered not to be checked.
    private ConcurrentSet<String> jointChecklistItems = new ConcurrentSet<>();
    private ConcurrentMap<String,ConcurrentSet<String>> individualChecklistItems = new ConcurrentHashMap<>();         // key = user
    private ConcurrentSet<String> ownChecklistItems = null;                                       // see getInitiativeForUser


    // Internal fields
    private EventMessage<?> lastEventMessage = null;

    // Deduced properties
    public int getVolunteerCount() {
        return memberIds.size();
    }

    public Date getLastUpdateTimestamp() {
        return lastEventMessage != null ? new Date(lastEventMessage.getTimestamp().toEpochMilli()) : new Date();    // TODO this will be a required field, so remove the null check after DB is cleared
    }

    public Set<Member> getMembers() {
        return memberIds.stream().map(id -> new Member(id, profileProjection.profile(id) != null ? profileProjection.profile(id).getUsername() : " ** onbekend **" )).collect(Collectors.toSet());
    }


    /*
        Value classes
     */

    @Value
    public static class Member {
        String userId;
        String userName;
    }



    /*
        Constructors
     */

    public Initiative(ProfileProjection profileProjection, String id, String name, Double lat, Double lng, String status, int votes, EventMessage<?> lastEventMessage) {
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
                      String id, String name, Double lat, Double lng, String status, int votes,
                      Set<String> memberIds,
                      ConcurrentSet<String> jointChecklistItems, ConcurrentSet<String> ownChecklistItems, EventMessage<?> lastEventMessage) {
        this.profileProjection = profileProjection;
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.votes = votes;
        this.memberIds = memberIds;
        this.jointChecklistItems = jointChecklistItems;
        this.ownChecklistItems = ownChecklistItems;
        this.lastEventMessage = lastEventMessage;
    }



    /*
        Update methods
     */

    Initiative setChecklistItem(String actor, String item, Boolean checked) {
        ConcurrentSet<String> actorChecklistItems = individualChecklistItems.containsKey(actor) ? individualChecklistItems.get(actor) : new ConcurrentSet<>();
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

    public boolean isMember(String userId) {
        return memberIds.contains(userId);
    }


    /**
     * Some information in the initiative should not be exposed to all users, such as the checkboxes of each individual user.
     * Therefore use this method to compute a initiative object suitable to expose to a particular user
     *
     * @param userId id of the user to create the perspective for (leave null for unauthenticated users)
     * @return initiative with a subset of the properties of this initiative
     */
    public Initiative getInitiativeForUser(@Nullable String userId) {
        ConcurrentSet<String> usersChecklistItems = userId != null && individualChecklistItems.containsKey(userId) ?
                                                    individualChecklistItems.get(userId) : new ConcurrentSet<>();
        return new Initiative(  profileProjection, id, name, lat, lng, status, votes, memberIds,
                                jointChecklistItems, usersChecklistItems, lastEventMessage);
    }
}
