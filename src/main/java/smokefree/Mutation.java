package smokefree;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import smokefree.domain.*;
import smokefree.graphql.CreateInitiativeInput;
import smokefree.graphql.InputAcceptedResponse;
import smokefree.graphql.JoinInitiativeInput;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.google.common.collect.Maps.newHashMap;

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

    public InputAcceptedResponse claimManagerRole(ClaimManagerRoleCommand cmd) {
        // TODO: Currently hardcoded. Solved by AWS Cognito
        final HashMap<String, Object> metaData = newHashMap();
        metaData.put("user_id", "manager-1");

        gateway.sendAndWait(new GenericCommandMessage<>(cmd, metaData));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd) {
        // TODO: Currently hardcoded. Solved by AWS Cognito
        final HashMap<String, Object> metaData = newHashMap();
        metaData.put("user_id", "manager-1");

        gateway.sendAndWait(new GenericCommandMessage<>(cmd, metaData));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToNotBecomeSmokeFree(DecideToNotBecomeSmokeFreeCommand cmd) {
        // TODO: Currently hardcoded. Solved by AWS Cognito
        final HashMap<String, Object> metaData = newHashMap();
        metaData.put("user_id", "manager-1");

        gateway.sendAndWait(new GenericCommandMessage<>(cmd, metaData));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }
}