package smokefree;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import smokefree.domain.CreateInitiativeCommand;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.Playground;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Collection;

@Controller("/playgrounds")
public class PlaygroundImportController {
    @Inject
    CommandGateway commandGateway;
    @Inject
    Configuration configuration;
    @Inject
    InitiativeProjection initiativeProjection;

    @Post(value="/", consumes= MediaType.APPLICATION_JSON)
    public String importPlayground(@Size(max=4096) @Body CreateInitiativeCommand cmd) {
        return commandGateway.sendAndWait(cmd);
    }

    @Get
    public Collection<Playground> playgrounds() {
        return initiativeProjection.playgrounds();
    }
}
