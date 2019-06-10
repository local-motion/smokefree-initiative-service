package io.localmotion.audittrail.projection;

import io.localmotion.eventsourcing.axon.MetaDataManager;
import io.localmotion.initiative.event.ChecklistUpdateEvent;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.event.*;
import io.localmotion.user.event.NotificationSettingsUpdatedEvent;
import io.localmotion.user.event.UserCreatedEvent;
import io.localmotion.user.event.UserDeletedEvent;
import io.localmotion.user.event.UserRevivedEvent;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.context.annotation.Context;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static com.google.common.collect.Maps.newConcurrentMap;

@Slf4j
@Context
public class AuditTrailProjection {

    private static final int MAX_RECORDS_TO_RETAIN = 50000;
    private static final int MAX_ACTOR_RECORDS_TO_RETAIN = 1000;
    private static final int MAX_INITIATIVE_RECORDS_TO_RETAIN = 5000;

    private final AuditTrail auditTrail = new AuditTrail(MAX_RECORDS_TO_RETAIN);
    private final Map<String, AuditTrail> auditTrailsByActor = newConcurrentMap();
    private final Map<String, AuditTrail> auditTrailsByInitiative = newConcurrentMap();


    @Inject
    private ProfileProjection profileProjection;

    /*
            Serving the projections
     */

//    public AuditTrail getAuditTrail(String initiativeId, String actorId, Instant fromTime, Instant toTime, Integer maxRecords) {
    public AuditTrail getAuditTrail(String initiativeId, String actorId, LocalDateTime fromTime, LocalDateTime toTime, Integer maxRecords) {
        AuditTrail auditTrail = actorId != null ? auditTrailsByActor.get(actorId) : initiativeId != null ? auditTrailsByInitiative.get(initiativeId) : this.auditTrail;
        if (auditTrail == null || (maxRecords != null && maxRecords == 0))
            return new AuditTrail(0);

        List<AuditTrailRecord> records = auditTrail.getRecords();
        if (fromTime == null && toTime == null) {
            if (maxRecords == null)
                return auditTrail;
            else {
                return new AuditTrail(records.subList(maxRecords > records.size() ? 0 : records.size() - maxRecords, records.size()));
            }
        }
        else {
            int fromIndex = 0;
            while (fromIndex < records.size() && records.get(fromIndex).getInstant().isBefore(fromTime)) fromIndex++;

            int toIndex = fromIndex;
            while (toIndex < records.size() && records.get(toIndex).getInstant().isBefore(toTime)) toIndex++;

            return new AuditTrail(records.subList(fromIndex, toIndex));
        }
    }

    /*
            User events
     */

    @EventHandler
    public void on(UserCreatedEvent event, EventMessage<?> eventMessage) {
//        log.info("ON EVENT {}", event);
        String actor = getUserId(eventMessage);
        String details = DetailsBuilder.instance()
                .add("userId", event.getUserId())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.USER_CREATED, details);
        storeRecord(record, actor);
    }

    @EventHandler
    public void on(UserRevivedEvent event, EventMessage<?> eventMessage) {
//        log.info("ON EVENT {}", event);
        String actor = getUserId(eventMessage);
        String details = DetailsBuilder.instance()
                .add("userId", event.getUserId())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.USER_REVIVED, details);
        storeRecord(record, actor);
    }

    @EventHandler
    public void on(UserDeletedEvent event, EventMessage<?> eventMessage) {
//        log.info("ON EVENT {}", event);
        String actor = getUserId(eventMessage);
        String details = DetailsBuilder.instance()
                .add("userId", event.getUserId())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.USER_DELETED, details);
        storeRecord(record, actor);
    }

    @EventHandler
    void on(NotificationSettingsUpdatedEvent event, EventMessage<?> eventMessage) {
//        log.info("ON EVENT {}", event);
        String actor = getUserId(eventMessage);
        String details = DetailsBuilder.instance()
                .add("userId", event.getUserId())
                .add("notificationLevel", event.getNotificationLevel())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.NOTIFICATION_SETTINGS_UPDATED, details);
        storeRecord(record, actor);
    }


    /*
            Initiative events
     */

    @EventHandler
    void on(MemberJoinedInitiativeEvent event, EventMessage<?> eventMessage) {
        String actor = getUserId(eventMessage);
        String initiativeId = event.getInitiativeId();
        String details = DetailsBuilder.instance()
                .add("memberId", event.getMemberId())
                .add("initiativeId", initiativeId)
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.INITIATIVE_JOINED, details);
        storeRecord(record, actor, initiativeId);
    }

    @EventHandler
    void on(ChecklistUpdateEvent event, EventMessage<?> eventMessage) {
        String actor = getUserId(eventMessage);
        String initiativeId = event.getInitiativeId();
        String details = DetailsBuilder.instance()
                .add("initiativeId", initiativeId)
                .add("checklistItem", event.getChecklistItem())
                .add("checked", event.isChecked())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.CHECKBOX_UPDATE, details);
        storeRecord(record, actor, initiativeId);
    }


    /*
            Playground events
     */

    @EventHandler
    void on(PlaygroundInitiativeCreatedEvent event, EventMessage<?> eventMessage) {
        String actor = getUserId(eventMessage);
        String initiativeId = event.getInitiativeId();
        String details = DetailsBuilder.instance()
                .add("initiativeId", initiativeId)
                .add("name", event.getName())
                .add("longitude", event.getGeoLocation().getLng())
                .add("lattitude", event.getGeoLocation().getLat())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.PLAYGROUND_CREATED, details);
        storeRecord(record, actor, initiativeId);
    }

    @EventHandler
    void on(SmokeFreeDecisionEvent event, EventMessage<?> eventMessage) {
        String actor = getUserId(eventMessage);
        String initiativeId = event.getInitiativeId();
        String details = DetailsBuilder.instance()
                .add("initiativeId", initiativeId)
                .add("willBecomeSmokefree", event.getWillBecomeSmokefree())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.SMOKEFREE_DECISION, details);
        storeRecord(record, actor, initiativeId);
    }

    @EventHandler
    void on(SmokeFreeDateCommittedEvent event, EventMessage<?> eventMessage) {
        String actor = getUserId(eventMessage);
        String initiativeId = event.getInitiativeId();
        String details = DetailsBuilder.instance()
                .add("initiativeId", initiativeId)
                .add("smokeFreeDate", event.getSmokeFreeDate())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.SMOKEFREE_DATE_COMMITTED, details);
        storeRecord(record, actor, initiativeId);
    }

    @EventHandler
    void on(ManagerJoinedInitiativeEvent event, EventMessage<?> eventMessage) {
        String actor = getUserId(eventMessage);
        String initiativeId = event.getInitiativeId();
        String details = DetailsBuilder.instance()
                .add("initiativeId", initiativeId)
                .add("managerId", event.getManagerId())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.MANAGER_JOINED_INITIATIVE, details);
        storeRecord(record, actor, initiativeId);
    }

    @EventHandler
    void on(PlaygroundObservationEvent event, EventMessage<?> eventMessage) {
        String actor = getUserId(eventMessage);
        String initiativeId = event.getInitiativeId();
        String details = DetailsBuilder.instance()
                .add("initiativeId", initiativeId)
                .add("observerId", event.getObserverId())
                .add("smokefree", event.getSmokefree())
                .add("observationDate", event.getObservationDate())
                .add("comment", event.getComment())
                .build();
        AuditTrailRecord record = createAuditTrailRecord(actor, eventMessage.getTimestamp(), EventType.PLAYGROUND_OBSERVATION, details);
        storeRecord(record, actor, initiativeId);
    }


    /*
            Other events
     */

    @EventHandler
    void on(EventMessage<?> eventMessage) {
        // All events should be captured for completeness (even if they are not captured in the audit trail, so log a warning for all uncaptured events
        log.warn("EVENT IGNORED FOR AUDIT TRAIL {}", eventMessage.getPayload());
    }


    /*
            Utility methods
     */

    private AuditTrailRecord createAuditTrailRecord(String actorId, Instant eventTimestamp, EventType eventType, String details) {
        Profile profile = profileProjection.profile(actorId);
        String actorName = profile != null ? profile.getUsername() : "onbekend";
        return new AuditTrailRecord(actorName, eventTimestamp, eventType, details);
    }


    private void storeRecord(AuditTrailRecord record, String actor) {
        storeRecord(record, actor, null);
    }
    private void storeRecord(AuditTrailRecord record, String actor, String initiativeId) {
        auditTrail.addRecord(record);
        if (actor != null) {
            AuditTrail actorAuditTrail = auditTrailsByActor.containsKey(actor) ? auditTrailsByActor.get(actor) : new AuditTrail(MAX_ACTOR_RECORDS_TO_RETAIN);
            actorAuditTrail.addRecord(record);
            auditTrailsByActor.put(actor, actorAuditTrail);
        }
        if (initiativeId != null) {
            AuditTrail initiativeAuditTrail = auditTrailsByInitiative.containsKey(initiativeId) ? auditTrailsByInitiative.get(initiativeId) : new AuditTrail(MAX_INITIATIVE_RECORDS_TO_RETAIN);
            initiativeAuditTrail.addRecord(record);
            auditTrailsByInitiative.put(initiativeId, initiativeAuditTrail);
        }
    }

    private String getUserId(EventMessage<?> eventMessage) {
        return new MetaDataManager(eventMessage.getMetaData()).getUserId();
    }

}