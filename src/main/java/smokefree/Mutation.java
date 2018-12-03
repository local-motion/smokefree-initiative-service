package smokefree;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.utils.SecurityService;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonMap;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class Mutation implements GraphQLMutationResolver {
    @Inject
    CommandGateway gateway;

    @Inject
    SecurityService securityService;

    private String requireUserId() {
        return securityService.username().orElseThrow(() -> new AuthenticationException("Username required"));
    }

    public InputAcceptedResponse createInitiative(CreateInitiativeInput input) {
        final CreateInitiativeCommand command = new CreateInitiativeCommand(
                input.getInitiativeId(),
                input.getName(),
                input.getType(),
                input.getStatus(),
                new GeoLocation(input.getLat(), input.getLng()));
        final CompletableFuture<String> result = gateway.send(decorateWithUserId(command));
        final InputAcceptedResponse response = InputAcceptedResponse.fromFuture(result);

        return joinInitiative(new JoinInitiativeInput(response.getId()));
    }

    @SneakyThrows
    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input) {
        String citizenId = requireUserId();

        JoinInitiativeCommand cmd = new JoinInitiativeCommand(input.getInitiativeId(), citizenId);
        gateway.sendAndWait(cmd);
        return new InputAcceptedResponse(input.getInitiativeId());
    }

    /***********
     * Playground Manager related functionality
     ************/

    public InputAcceptedResponse claimManagerRole(ClaimManagerRoleCommand cmd) {
        gateway.sendAndWait(decorateWithUserId(cmd));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd) {
        gateway.sendAndWait(decorateWithUserId(cmd));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToNotBecomeSmokeFree(DecideToNotBecomeSmokeFreeCommand cmd) {
        gateway.sendAndWait(decorateWithUserId(cmd));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse commitToSmokeFreeDate(CommitToSmokeFreeDateCommand cmd) {
        gateway.sendAndWait(decorateWithUserId(cmd));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    private GenericCommandMessage<?> decorateWithUserId(Object cmd) {
        return new GenericCommandMessage<>(cmd, singletonMap("user_id", requireUserId()));
    }
}