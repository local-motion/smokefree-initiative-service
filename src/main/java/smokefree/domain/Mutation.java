package smokefree.domain;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import smokefree.graphql.CreateInitiativeInput;
import smokefree.graphql.InputAcceptedResponse;
import smokefree.graphql.JoinInitiativeInput;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class Mutation implements GraphQLMutationResolver {
    @Inject
    CommandGateway gateway;

    public InputAcceptedResponse createInitiative(CreateInitiativeInput input) {
        final CreateInitiativeCommand command = new CreateInitiativeCommand(
                input.getInitiativeId(),
                input.getName(),
                input.getType(),
                input.getStatus(),
                new GeoLocation(input.getLat(), input.getLng()));
        final CompletableFuture<String> result = gateway.send(command);
        return InputAcceptedResponse.fromFuture(result);
    }

    @SneakyThrows
    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input) {
        // TODO: Currently hardcoded. Solved by local-motion/product#48
        String citizenId = UUID.randomUUID().toString();

        JoinInitiativeCommand cmd = new JoinInitiativeCommand(input.getInitiativeId(), citizenId);
        gateway.sendAndWait(cmd);
        return new InputAcceptedResponse(input.getInitiativeId());
    }
























/*
    @SneakyThrows
    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input) {
        gateway.sendAndWait(new JoinInitiativeCommand(input.getInitiativeId(), input.getCitizenId()));
        return new InputAcceptedResponse(input.getInitiativeId());
    }

*/

}