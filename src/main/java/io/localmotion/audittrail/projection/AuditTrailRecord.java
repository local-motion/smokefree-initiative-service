package io.localmotion.audittrail.projection;

import io.localmotion.user.domain.NotificationLevel;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Wither;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

//@Value
@Data
public class AuditTrailRecord {
    String actorName;
    Instant instant;
//    LocalDateTime instant;
    EventType eventType;
    String details;

    public AuditTrailRecord(String actorName, Instant instant, EventType eventType, String details) {
        this.actorName = actorName;
        this.instant = instant;
        this.eventType = eventType;
        this.details = details;
    }

    public LocalDateTime getInstant() {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
