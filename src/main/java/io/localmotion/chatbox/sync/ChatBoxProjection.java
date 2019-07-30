package io.localmotion.chatbox.sync;

import com.google.gson.Gson;
import io.localmotion.chatbox.ChatboxRepository;
import io.localmotion.chatbox.model.User;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.personaldata.PersonalDataRecord;
import io.localmotion.personaldata.PersonalDataRepository;
import io.localmotion.user.domain.UserPII;
import io.localmotion.user.event.*;
import io.micronaut.context.annotation.Context;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;

import javax.inject.Inject;

@Slf4j
@Context
public class ChatBoxProjection {

    @Inject
    private PersonalDataRepository personalDataRepository;

    @Inject
    private ChatboxRepository chatboxRepository;


    /*
            Event handlers
     */

    @EventHandler
    public void on(UserCreatedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);

        String userName = getUserNameFromPersonalDataRecord(evt.getPiiRecordId(), "onbekend");

        User user = chatboxRepository.getUserWithExternalId(evt.getUserId());
        if (user == null)
            chatboxRepository.createUser(userName, evt.getUserId());
    }

    @EventHandler
    void on(UserRevivedEvent evt) {
        log.info("ON EVENT {}", evt);
    }

    @EventHandler
    void on(UserDeletedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);

        User user = chatboxRepository.getUserWithExternalId(evt.getUserId());
        if (user != null && user.getLastUpdateTime().isBefore(eventMessage.getTimestamp())) {
            chatboxRepository.deleteUser(user, eventMessage.getTimestamp());
        }
    }

    @EventHandler
    void on(PersonalDataDeletedEvent evt) {
        log.info("ON EVENT {}", evt);
    }

    @EventHandler
    void on(UserRenamedEvent evt) {
        log.info("ON EVENT {}", evt);
    }


    @EventHandler
    void on(MemberJoinedInitiativeEvent evt) {
        log.info("ON EVENT {}", evt);
    }



    /*
            PII support methods
     */

    private String getUserNameFromPersonalDataRecord(long recordId, String fallbackUserName) {
        String userName = getUserNameFromPersonalDataRecord(recordId);
        return userName != null ? userName : fallbackUserName;
    }
    private String getUserNameFromPersonalDataRecord(long recordId) {
        PersonalDataRecord personalDataRecord = personalDataRepository.getRecord(recordId);
        return personalDataRecord != null ?
                new Gson().fromJson(personalDataRecord.getData(), UserPII.class).getName()
                :
                null;
    }

}