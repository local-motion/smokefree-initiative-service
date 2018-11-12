package smokefree;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.queryhandling.QueryGateway;
import smokefree.domain.CreateInitiativeCommand;

import javax.inject.Inject;
import javax.validation.constraints.Size;

@Controller("/playgrounds")
public class PlaygroundImportController {
    @Inject
    CommandGateway commandGateway;
    @Inject
    Configuration configuration;

    @Post(value="/", consumes= MediaType.APPLICATION_JSON)
    public String importPlayground(@Size(max=4096) @Body CreateInitiativeCommand cmd) {
        return commandGateway.sendAndWait(cmd);
    }
}
