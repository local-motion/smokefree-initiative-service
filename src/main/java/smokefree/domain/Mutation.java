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
                input.getType(),
                input.getStatus(),
                input.getName(),
                input.getLat(),
                input.getLng());
        final CompletableFuture<String> result = gateway.send(command);
        return InputAcceptedResponse.fromFuture(result);
    }

    @SneakyThrows
    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
























/*
    @SneakyThrows
    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input) {
        gateway.sendAndWait(new JoinInitiativeCommand(input.getInitiativeId(), input.getCitizenId()));
        return new InputAcceptedResponse(input.getInitiativeId());
    }

*/

}