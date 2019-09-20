package io.localmotion.chatbox;

import io.localmotion.chatbox.model.ChatBox;
import io.localmotion.chatbox.model.User;
import io.localmotion.eventsourcing.tracker.TrackerProjection;
import io.localmotion.initiative.projection.Initiative;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.security.user.SecurityContextFactory;
import io.micronaut.http.HttpResponse;
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
import java.util.Collections;
import java.util.List;

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
    private SecurityContextFactory securityContextFactory;

    @Inject
    private TrackerProjection trackerProjection;


    @Post("/{chatboxId}")
    public HttpResponse<SimpleResponse> postMessage(Authentication authentication, String chatboxId, @Body ChatMessageDTO chatMessage /* note that we only use the text attribute */ ) {

        // Validate that the projections are up-to-date
        if (!trackerProjection.isUpToDate())
            return HttpResponse.status(HttpStatus.SERVICE_UNAVAILABLE, "System is starting up");

        // Establish the security context
        SecurityContext securityContext = securityContextFactory.createSecurityContext(authentication);

        if (!securityContext.isAuthenticated())
            return HttpResponse.status(HttpStatus.UNAUTHORIZED, "User must be authenticated");


        ChatBox chatBox = chatboxRepository.getChatBoxWithExternalId(chatboxId);
        if (chatBox == null)
            return HttpResponse.status(HttpStatus.NOT_FOUND, "Invalid chatbox");

        User user = chatboxRepository.getUserWithExternalId(securityContext.requireUserId());
        if (user == null)
            return HttpResponse.status(HttpStatus.NOT_FOUND, "Invalid chatbox user");

        if (!isUserAuthorisedForChatbox(user.getId(), chatBox.getId()))
            return HttpResponse.status(HttpStatus.UNAUTHORIZED, "User not authorised for chatbox");


        log.info("chat message for"  + chatboxId + ": " + chatMessage + " from: " + user.getName());
        chatboxRepository.storeMessage(chatBox.getId(), user.getId(), chatMessage.getText());

        return HttpResponse.ok(new SimpleResponse("ok"));
    }

    @Secured(IS_ANONYMOUS)
    @Get("/{chatboxId}{?since}")
    public List<ChatMessageDTO> getMessages(String chatboxId, @Nullable String since) {
        log.info("fetching for: " + chatboxId + ", since: " + since);

        ChatBox chatBox = chatboxRepository.getChatBoxWithExternalId(chatboxId);
        if (chatBox == null)
            return Collections.emptyList();

        if (since == null)
            return chatboxRepository.getMessages(chatBox.getId());
        else
            return chatboxRepository.getMessagesSince(chatBox.getId(), since);
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

    private boolean isUserAuthorisedForChatboxOld(String userId, String chatboxId) {
        final Initiative initiative = getPlayground(chatboxId);
        if (initiative != null)
            return initiative.getMembers().stream().anyMatch(volunteer -> volunteer.getUserId().equals(userId));
        return false;
    }

    private boolean isUserAuthorisedForChatbox(int userId, int chatboxId) {
        return chatboxRepository.getParticipation(chatboxId, userId) != null;
    }
}
