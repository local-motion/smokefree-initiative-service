package io.localmotion.eventsourcing.tracker;

import com.google.gson.Gson;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.personaldata.PersonalDataRecord;
import io.localmotion.personaldata.PersonalDataRepository;
import io.localmotion.user.domain.NotificationLevel;
import io.localmotion.user.domain.UserPII;
import io.localmotion.user.event.*;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileStore;
import io.micronaut.context.annotation.Context;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.MetaData;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;

/**
 * This projection can be used to estimate whether the replaying of events on system startup has been completed and
 * therefore of whether the projections can be considered to be up-to-date. Note that as the projections are eventually
 * consistent they are never guaranteed to be fully up to date.
 *
 * The replay is considered to be finished when any of these are true:
 * - projection startup is marked and no event have been received after the WAIT_FOR_EVENT_STREAM_AFTER_STARTUP period
 *   (clean system startup)
 * - at least one event received and no events received for a period of time EVENT_UPDATE_STREAM_COMPLETE_MARGIN
 *   (system up with no active users)
 * - timestamp of an event is very close (EVENT_IS_CURRENT_MARGIN) to the current time
 *
 * The replay-is-finished status is evaluated on request and on receipt of each event (clean startup rule is ignored in this case).
 * When replay is determined to be complete, its status will remain complete.
 */
@Slf4j
@Context
public class TrackerProjection {

    private static final long WAIT_FOR_EVENT_STREAM_AFTER_STARTUP = 20*1000;            // 20 secs
    private static final long EVENT_UPDATE_STREAM_COMPLETE_MARGIN = 3*1000;             // 3 secs
    private static final long EVENT_IS_CURRENT_MARGIN = 10;                             // 10 milli-secs

    private long projectionStartupTimestamp = 0;
    private long lastEventReceivedTimestamp = 0;
    private boolean isUpToDate = false;


    @EventHandler
    void on(EventMessage<?> eventMessage) {
        markStartup();
        long currentTime = System.currentTimeMillis();
        long eventTime = eventMessage.getTimestamp().toEpochMilli();

        isUpToDate |=   ( currentTime - eventTime < EVENT_IS_CURRENT_MARGIN ) ||
                        ( lastEventReceivedTimestamp > 0 && currentTime - lastEventReceivedTimestamp > EVENT_UPDATE_STREAM_COMPLETE_MARGIN );

        lastEventReceivedTimestamp = currentTime;
    }


    public void markStartup() {
        projectionStartupTimestamp = projectionStartupTimestamp > 0 ? projectionStartupTimestamp : System.currentTimeMillis();
    }

    /**
     * @return whether this projection (and by assumption all projections) has processed (nearly) all events
     */
    public boolean isUpToDate() {
        long currentTime = System.currentTimeMillis();
        isUpToDate |=   ( projectionStartupTimestamp > 0 && lastEventReceivedTimestamp == 0 && currentTime - projectionStartupTimestamp > WAIT_FOR_EVENT_STREAM_AFTER_STARTUP ) ||
                        ( lastEventReceivedTimestamp > 0 && currentTime - lastEventReceivedTimestamp > EVENT_UPDATE_STREAM_COMPLETE_MARGIN );
        return isUpToDate;
    }

}