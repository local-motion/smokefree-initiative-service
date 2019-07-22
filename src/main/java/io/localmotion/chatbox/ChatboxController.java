package io.localmotion.chatbox;

import io.localmotion.initiative.projection.Initiative;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.security.user.SecurityContextFactory;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.validation.Validated;
import lombok.extern.slf4j.Slf4j;
import io.localmotion.initiative.projection.InitiativeProjection;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Size;
import java.util.Collection;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Slf4j
@Secured(IS_AUTHENTICATED)
@Controller("${micronaut.context.path:}/chatbox")
@Validated
public class ChatboxController {

    @Inject
    private ChatboxRepository chatboxRepository;

    @Inject
    private InitiativeProjection initiativeProjection;

    @Inject
    SecurityContextFactory securityContextFactory;


    @Post("/{chatboxId}")
    public ChatMessage postMessage(Authentication authentication, String chatboxId, @Body ChatMessage chatMessage) {

        // Establish the security context
        SecurityContext securityContext = securityContextFactory.createSecurityContext(authentication);

        if (!securityContext.isAuthenticated())
            throw new ValidationException("User must be authenticated");

        final String userName = securityContext.requireUserName();
//        final String userName = authentication.getAttributes().get("cognito:username").toString();

        log.info("chat message for"  + chatboxId + ": " + chatMessage + " from: " + userName);

        if (!isValidChatboxId(chatboxId))
            throw new ValidationException("Invalid chatbox");

//        if (!isUserAuthorisedForChatbox(authentication.getName(), chatboxId))
        if (!isUserAuthorisedForChatbox(securityContext.requireUserId(), chatboxId))
            throw new ValidationException("User not authorised for chatbox");

        chatMessage.setAuthor(userName);
        chatMessage.setChatboxId(chatboxId);

        chatboxRepository.storeMessage(chatboxId, chatMessage);

        return chatMessage;
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
}
