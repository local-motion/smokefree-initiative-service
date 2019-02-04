package smokefree;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import smokefree.domain.*;
import smokefree.graphql.CreateInitiativeInput;
import smokefree.graphql.InputAcceptedResponse;
import smokefree.graphql.JoinInitiativeInput;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.Playground;

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

    @Inject
    InitiativeProjection initiativeProjection;

    private SecurityContext toContext(DataFetchingEnvironment environment) {
        return environment.getContext();
    }

    @SneakyThrows
    public Playground createInitiative(CreateInitiativeInput input, DataFetchingEnvironment env) {
        final CreateInitiativeCommand command = new CreateInitiativeCommand(
                input.getInitiativeId(),
                input.getName(),
                input.getType(),
                input.getStatus(),
                new GeoLocation(input.getLat(), input.getLng()));
        final CompletableFuture<String> result = gateway.send(decorateWithMetaData(command, env));
        final InputAcceptedResponse response = InputAcceptedResponse.fromFuture(result);
        final String playgroundId = response.getId();
//        return joinInitiative(new JoinInitiativeInput(response.getId()), env);
        joinInitiative(new JoinInitiativeInput(playgroundId), env);
        Playground playground = initiativeProjection.playground(playgroundId);
        return playground;
    }

    @SneakyThrows
    public Playground joinInitiative(JoinInitiativeInput input, DataFetchingEnvironment env) {
        String citizenId = toContext(env).requireUserId();

        JoinInitiativeCommand cmd = new JoinInitiativeCommand(input.getInitiativeId(), citizenId);
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
//        return new InputAcceptedResponse(input.getInitiativeId());
        return initiativeProjection.playground(input.getInitiativeId());
    }

    //noticeSmokeFreePlayground(input: RecordSmokeFreePlaygroundObservationCommand!): Playground!
    //recordSmokeFreePlaygroundObservation(input: RecordSmokeFreePlaygroundObservationCommand!): Playground!
    @SneakyThrows
    public Playground recordSmokeFreePlaygroundObservation(RecordSmokeFreePlaygroundObservationCommand input, DataFetchingEnvironment env) {
        String citizenId = toContext(env).requireUserId();

        RecordSmokeFreePlaygroundObservationCommand cmd = new RecordSmokeFreePlaygroundObservationCommand(input.getInitiativeId(), citizenId, input.getIsSmokeFree(), input.getRecordObservation());
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
//        return new InputAcceptedResponse(input.getInitiativeId());
        return initiativeProjection.playground(input.getInitiativeId());
    }
    /***********
     * Playground Manager related functionality
     ************/

    public Playground claimManagerRole(ClaimManagerRoleCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
//        return initiativeProjection.playground(cmd.getInitiativeId());
        Playground playground = initiativeProjection.playground(cmd.getInitiativeId());
        log.info("playground mamagers: " + playground.getManagers());
        return playground;
    }

    public InputAcceptedResponse decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToNotBecomeSmokeFree(DecideToNotBecomeSmokeFreeCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse commitToSmokeFreeDate(CommitToSmokeFreeDateCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    private GenericCommandMessage<?> decorateWithMetaData(Object cmd, DataFetchingEnvironment env) {
        MetaData metaData = MetaData
                .with("user_id", toContext(env).requireUserId())
                .and("user_name", toContext(env).requireUserName())
                .and("email", toContext(env).emailId());
        return new GenericCommandMessage<>(cmd, metaData);
    }
}