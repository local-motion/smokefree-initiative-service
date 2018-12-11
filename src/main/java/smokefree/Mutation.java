package smokefree;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import io.micronaut.core.util.StringUtils;
import io.micronaut.security.authentication.Authentication;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.google.common.collect.Maps.newHashMap;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class Mutation implements GraphQLMutationResolver {
    @Inject
    CommandGateway gateway;

    @Inject
    SecurityService securityService;

    /**
     * In JWT / Cognito, the 'sub' ID is considered the unique 'username'.
     */
    private String requireUserId() {
        return securityService.username().orElseThrow(() -> new AuthenticationException("Not logged in"));
    }

    private String requireUserName() {
        Authentication authentication = securityService.getAuthentication().orElseThrow(() -> new AuthenticationException("Not logged in"));
        String userName = (String) authentication.getAttributes().get("cognito:username");
        if (StringUtils.isEmpty(userName)) {
            throw new AuthenticationException("No username");
        }
        return userName;
    }

    public InputAcceptedResponse createInitiative(CreateInitiativeInput input) {
        final CreateInitiativeCommand command = new CreateInitiativeCommand(
                input.getInitiativeId(),
                input.getName(),
                input.getType(),
                input.getStatus(),
                new GeoLocation(input.getLat(), input.getLng()));
        final CompletableFuture<String> result = gateway.send(decorateWithMetaData(command));
        final InputAcceptedResponse response = InputAcceptedResponse.fromFuture(result);

        return joinInitiative(new JoinInitiativeInput(response.getId()));
    }

    @SneakyThrows
    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input) {
        String citizenId = requireUserId();

        JoinInitiativeCommand cmd = new JoinInitiativeCommand(input.getInitiativeId(), citizenId);
        gateway.sendAndWait(decorateWithMetaData(cmd));
        return new InputAcceptedResponse(input.getInitiativeId());
    }

    /***********
     * Playground Manager related functionality
     ************/

    public InputAcceptedResponse claimManagerRole(ClaimManagerRoleCommand cmd) {
        gateway.sendAndWait(decorateWithMetaData(cmd));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd) {
        gateway.sendAndWait(decorateWithMetaData(cmd));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToNotBecomeSmokeFree(DecideToNotBecomeSmokeFreeCommand cmd) {
        gateway.sendAndWait(decorateWithMetaData(cmd));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse commitToSmokeFreeDate(CommitToSmokeFreeDateCommand cmd) {
        gateway.sendAndWait(decorateWithMetaData(cmd));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    private GenericCommandMessage<?> decorateWithMetaData(Object cmd) {
        final Map<String, String> metaData = newHashMap();
        metaData.put("user_id", requireUserId());
        metaData.put("user_name", requireUserName());

        return new GenericCommandMessage<>(cmd, metaData);
    }
}