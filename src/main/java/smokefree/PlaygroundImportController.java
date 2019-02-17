package smokefree;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.messaging.MetaData;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;
import smokefree.domain.CreateInitiativeCommand;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.Playground;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Collection;

import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Secured(IS_AUTHENTICATED)
@Controller("${micronaut.context.path:}/playgrounds")
public class PlaygroundImportController {
    @Inject
    Configuration configuration; // TODO: Hack for triggering Axon bootstrap

    @Inject
    CommandGateway commandGateway;
    @Inject
    InitiativeProjection initiativeProjection;

    @Post
    public String importPlayground(Authentication authentication, @Size(max=4096) @Body CreateInitiativeCommand cmd) {
        return commandGateway.sendAndWait(decorateWithUserId(cmd, authentication));
    }

    @Get
    public Collection<Playground> playgrounds() {
        return initiativeProjection.playgrounds(null);
    }

    private GenericCommandMessage<?> decorateWithUserId(Object cmd, Authentication authentication) {
        SecurityContext context = new SecurityContext(authentication);

        return new GenericCommandMessage<>(cmd, MetaData
                .with(SmokefreeConstants.JWTClaimSet.USER_ID, context.requireUserId())
                .and(SmokefreeConstants.JWTClaimSet.USER_NAME, context.requireUserName()));
    }
}
