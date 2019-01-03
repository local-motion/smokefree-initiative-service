package chatbox;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import lombok.extern.slf4j.Slf4j;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.Playground;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.validation.constraints.Size;
import java.util.*;

import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Slf4j
@Secured(IS_AUTHENTICATED)
@Controller("${micronaut.context.path:}/chatbox")
public class ChatboxController {
    private static final String MESSAGE_STORE_NAME = "chatbox";

    private static final Map<String, List<ChatMessage>> chatboxMessages = new HashMap<>();

    private final EntityManagerFactory entityManagerFactory;
    private final EntityManager entityManager;

    @Inject
    InitiativeProjection initiativeProjection;

    public ChatboxController() {
        entityManagerFactory = Persistence.createEntityManagerFactory(MESSAGE_STORE_NAME);
        entityManager = entityManagerFactory.createEntityManager();
    }

    @Post("/{chatboxId}")
    public String postMessage(Authentication authentication, String chatboxId, @Size(max=4096) @Body ChatMessage chatMessage) {
        log.info("chat message for"  + chatboxId + ": " + chatMessage + " from: " + authentication.getAttributes().get("cognito:username"));

        if (!isValidChatboxId(chatboxId))
            return "error: invalid chatbox";

        if (!isUserAuthorisedForChatbox(authentication.getName(), chatboxId))
            return "error: user not authorised for chatbox";

        chatMessage.setAuthor((String) authentication.getAttributes().get("cognito:username"));
        chatMessage.setChatboxId(chatboxId);

        List<ChatMessage> messages = chatboxMessages.containsKey(chatboxId) ? chatboxMessages.get(chatboxId) : new ArrayList<>();
        messages.add(chatMessage);
        chatboxMessages.put(chatboxId, messages);

        entityManager.getTransaction().begin();
        entityManager.persist(chatMessage);
        entityManager.getTransaction().commit();


        return "Thank you";
    }

    @Get("/{chatboxId}")
    public Collection<ChatMessage> getMessages(String chatboxId) {
        log.info("fetching for: " + chatboxId);
        List<ChatMessage> messages = chatboxMessages.containsKey(chatboxId) ? chatboxMessages.get(chatboxId) : new ArrayList<>();

        Query query = entityManager.createQuery("SELECT m from ChatMessage m WHERE m.chatboxId = :chatboxId");
        query.setParameter("chatboxId", chatboxId);
        List<ChatMessage> messages2 = query.getResultList();

        return messages2;
    }

    private boolean isValidChatboxId(String chatboxId) {
        return getPlayground(chatboxId) != null;
    }

    private Playground getPlayground(String playgroundId) {
        for (Playground i: initiativeProjection.playgrounds())
            if (i.getId().equals(playgroundId))
                return i;
        return null;
    }

    private boolean isUserAuthorisedForChatbox(String userId, String chatboxId) {
        final Playground playground = getPlayground(chatboxId);
        if (playground != null)
            return true;            // TODO for now always return true, because we do not yet maintain a list of volunteers
        return false;
    }
}
