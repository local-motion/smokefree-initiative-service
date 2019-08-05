package io.localmotion.chatbox.sync;

import com.google.gson.Gson;
import io.localmotion.chatbox.ChatboxRepository;
import io.localmotion.chatbox.model.ChatBox;
import io.localmotion.chatbox.model.Participation;
import io.localmotion.chatbox.model.User;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.personaldata.PersonalDataRecord;
import io.localmotion.personaldata.PersonalDataRepository;
import io.localmotion.smokefreeplaygrounds.event.PlaygroundInitiativeCreatedEvent;
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

    /*
            User events
     */

    @EventHandler
    public void on(UserCreatedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);

        String userName = getUserNameFromPersonalDataRecord(evt.getPiiRecordId(), "onbekend");

        User user = chatboxRepository.getUserWithExternalId(evt.getUserId());
        if (user == null)
            chatboxRepository.createUser(userName, evt.getUserId(), eventMessage.getTimestamp());
    }

    @EventHandler
    void on(UserRevivedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);

        User user = chatboxRepository.getUserWithExternalId(evt.getUserId());
        if (user != null && user.getLastUpdateTime().isBefore(eventMessage.getTimestamp()) && evt.getPiiRecordId() != null) {
            String eventUserName = getUserNameFromPersonalDataRecord(evt.getPiiRecordId());
            if (eventUserName != null && !eventUserName.equals(user.getName()))
            chatboxRepository.changeUserName(user.getId(), eventUserName, eventMessage.getTimestamp());
        }
    }

    @EventHandler
    void on(UserRenamedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);

        User user = chatboxRepository.getUserWithExternalId(evt.getUserId());
        if (user != null && user.getLastUpdateTime().isBefore(eventMessage.getTimestamp())) {
            String eventUserName = getUserNameFromPersonalDataRecord(evt.getPiiRecordId());
            if (eventUserName != null && !eventUserName.equals(user.getName()))
                chatboxRepository.changeUserName(user.getId(), eventUserName, eventMessage.getTimestamp());
        }
    }

    @EventHandler
    void on(UserDeletedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);

        User user = chatboxRepository.getUserWithExternalId(evt.getUserId());
        if (user != null && user.getLastUpdateTime().isBefore(eventMessage.getTimestamp())) {
            chatboxRepository.deleteUser(user.getId(), eventMessage.getTimestamp());
        }
    }

    @EventHandler
    void on(PersonalDataDeletedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);

        User user = chatboxRepository.getUserWithExternalId(evt.getUserId());
        if (user != null && user.getLastUpdateTime().isBefore(eventMessage.getTimestamp())) {
            chatboxRepository.changeUserName(user.getId(), "onbekend", eventMessage.getTimestamp());
        }
    }



    /*
            ChatBox events
     */

    @EventHandler
    void on(PlaygroundInitiativeCreatedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        if (chatboxRepository.getChatBoxWithExternalId(evt.getInitiativeId()) == null)
            chatboxRepository.createChatBox(evt.getInitiativeId(), eventMessage.getTimestamp());
    }

     /*
            Participation events
     */

    @EventHandler
    void on(MemberJoinedInitiativeEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);

        ChatBox chatBox = chatboxRepository.getChatBoxWithExternalId(evt.getInitiativeId());
        User user = chatboxRepository.getUserWithExternalId(evt.getMemberId());
        if (chatBox != null && user != null) {
            Participation participation = chatboxRepository.getParticipation(chatBox.getId(), user.getId());
            if (participation == null)
                chatboxRepository.createParticipation(chatBox.getId(), user.getId(), eventMessage.getTimestamp());
        }
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