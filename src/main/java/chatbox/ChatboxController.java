package chatbox;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import lombok.extern.slf4j.Slf4j;
import smokefree.domain.CreateInitiativeCommand;
import smokefree.projection.Playground;

import javax.validation.constraints.Size;
import java.util.*;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Slf4j
//@Secured(IS_AUTHENTICATED)
@Secured(IS_ANONYMOUS)
@Controller("${micronaut.context.path:}/chatbox")
public class ChatboxController {

    private static final Map<String, List<ChatMessage>> chatboxMessages = new HashMap<>();

    @Post("/{chatboxId}")
//    public String postMessage(Authentication authentication, @Size(max=4096) @Body ChatMessage chatMessage) {
    public String postMessage(String chatboxId, @Size(max=4096) @Body ChatMessage chatMessage) {
        log.info("chat message for" + chatboxId + ": " + chatMessage);

        List<ChatMessage> messages = chatboxMessages.containsKey(chatboxId) ? chatboxMessages.get(chatboxId) : new ArrayList<>();
        messages.add(chatMessage);
        chatboxMessages.put(chatboxId, messages);
        return "Thank you";
    }

    @Get("/{chatboxId}")
    public Collection<ChatMessage> getMessages(String chatboxId) {
        log.info("fetching for: " + chatboxId);
//        List<String> messages = new ArrayList<>();
//        messages.add("Hallo, hoe heet jij?");
//        messages.add("Ik heet Dimitri");
//        messages.add("En ik ben leuk ;)");
        List<ChatMessage> messages = chatboxMessages.containsKey(chatboxId) ? chatboxMessages.get(chatboxId) : new ArrayList<>();
        return messages;
    }


}
