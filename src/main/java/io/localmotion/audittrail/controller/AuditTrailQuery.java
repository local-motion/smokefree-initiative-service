package io.localmotion.audittrail.controller;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import io.localmotion.audittrail.projection.AuditTrail;
import io.localmotion.audittrail.projection.AuditTrailProjection;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class AuditTrailQuery implements GraphQLQueryResolver {

    @Inject AuditTrailProjection auditTrailProjection;

    public AuditTrail auditTrail(String initiativeId, String actorId, LocalDateTime fromTime, LocalDateTime toTime, Integer maxRecords) {
        return auditTrailProjection.getAuditTrail(initiativeId, actorId, fromTime, toTime, maxRecords);
    }
}