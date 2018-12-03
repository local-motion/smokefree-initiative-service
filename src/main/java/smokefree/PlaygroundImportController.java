package smokefree;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.utils.SecurityService;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import smokefree.domain.CreateInitiativeCommand;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.Playground;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Collection;

import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;
import static java.util.Collections.singletonMap;

@Secured(IS_AUTHENTICATED)
@Controller("/playgrounds")
public class PlaygroundImportController {
    @Inject
    Configuration configuration; // TODO: Hack for triggering Axon bootstrap

    @Inject
    CommandGateway commandGateway;
    @Inject
    SecurityService securityService;
    @Inject
    InitiativeProjection initiativeProjection;

    @Post
    public String importPlayground(@Size(max=4096) @Body CreateInitiativeCommand cmd) {
        return commandGateway.sendAndWait(decorateWithUserId(cmd));
    }

    @Get
    public Collection<Playground> playgrounds() {
        return initiativeProjection.playgrounds();
    }

    private String requireUserId() {
        return securityService.username().orElseThrow(() -> new AuthenticationException("Username required"));
    }

    private GenericCommandMessage<?> decorateWithUserId(Object cmd) {
        return new GenericCommandMessage<>(cmd, singletonMap("user_id", requireUserId()));
    }
}
