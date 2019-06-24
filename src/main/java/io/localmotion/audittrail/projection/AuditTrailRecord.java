package io.localmotion.audittrail.projection;

import io.localmotion.user.domain.NotificationLevel;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.Wither;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@Getter
public class AuditTrailRecord {
    String actorId;
    String actorName = null;
    Instant instant;
    EventType eventType;
    String details;

    private final ProfileProjection profileProjection;


    public AuditTrailRecord(String actorId, Instant instant, EventType eventType, String details, ProfileProjection profileProjection) {
        this.profileProjection = profileProjection;
        this.actorId= actorId;
        this.instant = instant;
        this.eventType = eventType;
        this.details = details;
    }

    public LocalDateTime getInstant() {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
    private ProfileProjection getProfileProjection() {
        return profileProjection;
    }

    public String getActorName() {
        if (actorName == null) {
            Profile profile = profileProjection.profile(actorId);
            if (profile != null) {
                actorName = profile.getUsername();
                return actorName;
            }
            else
                return "onbekend";
        }
        else
            return actorName;
    }
}
