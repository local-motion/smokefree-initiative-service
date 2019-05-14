package io.localmotion.chatbox;

import io.localmotion.chatbox.notification.ChatboxUser;
import io.localmotion.chatbox.notification.ChatboxUserId;
import io.localmotion.chatbox.notification.ChatboxUserRepositoryImplementation;
import io.localmotion.initiative.projection.Initiative;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.validation.Validated;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ValidationException;
import java.util.Collection;
import java.util.Optional;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Slf4j
@Secured(IS_AUTHENTICATED)
@Controller("${micronaut.context.path:}/chatbox")
@Validated
public class ChatboxController {

    @Inject
    private ChatboxRepositoryImplementation chatboxRepository;

    @Inject
    private InitiativeProjection initiativeProjection;

    @Inject
    private ChatboxUserRepositoryImplementation chatBoxUserRepositoryImplementation;


    @Post("/{chatboxId}")
    public ChatMessage postMessage(Authentication authentication, String chatboxId, @Body ChatMessage chatMessage) {
        final String userName = authentication.getAttributes().get("cognito:username").toString();
        log.info("chat message for"  + chatboxId + ": " + chatMessage + " from: " + userName);

        if (!isValidChatboxId(chatboxId))
            throw new ValidationException("error: invalid chatbox");

        if (!isUserAuthorisedForChatbox(authentication.getName(), chatboxId))
            throw new ValidationException("error: user not authorised for chatbox");

        chatMessage.setAuthor(userName);
        chatMessage.setChatboxId(chatboxId);

        chatboxRepository.storeMessage(chatboxId, chatMessage);

        return chatMessage;
    }

    @Post("/{chatboxId}/{lastMessageId}")
    public HttpStatus updateLastReadMessage(Authentication authentication, String lastMessageId, String chatboxId) {

        final String userid = authentication.getAttributes().get("user_id").toString();

        if (!isUserAuthorisedForChatbox(authentication.getName(), chatboxId))
            throw new ValidationException("error: user not authorised for chatbox");

        if (!isValidChatboxId(chatboxId))
            throw new ValidationException("error: invalid chatbox");

        if(!isValidMessageId(lastMessageId)) {
            throw new ValidationException("error: invalid message");
        }

        Optional<ChatboxUser>  oldChatBoxNotification = checkChatBoxNotificationExists(userid, chatboxId);

        if(oldChatBoxNotification.isPresent()) {
            oldChatBoxNotification.get().setReadMessageId(lastMessageId);
            chatBoxUserRepositoryImplementation.update(oldChatBoxNotification.get());
        } else {
            chatBoxUserRepositoryImplementation.create(new ChatboxUser(new ChatboxUserId(userid,chatboxId), lastMessageId, null));
        }

        return HttpStatus.OK;
    }

    private Optional<ChatboxUser> checkChatBoxNotificationExists(String userId, String chatboxId) {
        try {
            return Optional.of(chatBoxUserRepositoryImplementation.findReadMessageIdByPersonAndChatBox(userId, chatboxId));
        }catch(NoResultException ex) {
            return Optional.empty();
        }
    }

    @Secured(IS_ANONYMOUS)
    @Get("/{chatboxId}{?since}")
    public Collection<ChatMessage> getMessages(String chatboxId, @Nullable String since) {
        log.info("fetching for: " + chatboxId + ", since: " + since);

        if (since == null)
            return chatboxRepository.getMessages(chatboxId);
        else
            return chatboxRepository.getMessagesSince(chatboxId, since);
    }

    private boolean isValidChatboxId(String chatboxId) {
        return getPlayground(chatboxId) != null;
    }

    private Initiative getPlayground(String playgroundId) {
        for (Initiative i: initiativeProjection.getInitiatives(null))
            if (i.getId().equals(playgroundId))
                return i;
        return null;
    }

    private boolean isUserAuthorisedForChatbox(String userId, String chatboxId) {
        final Initiative initiative = getPlayground(chatboxId);
        if (initiative != null)
            return initiative.getMembers().stream().anyMatch(volunteer -> volunteer.getUserId().equals(userId));
        return false;
    }

    private boolean isValidMessageId(String messageId) {
        boolean messageExists = false;
        try {
            messageExists = chatboxRepository.getMessageById(messageId) != null;
        } catch (NoResultException ex) {
            messageExists = false;
        }
        return messageExists;
    }
}
